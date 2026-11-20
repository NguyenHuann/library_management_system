package com.mycompany.library_management_system.server;

import com.mycompany.library_management_system.protocol.Request;
import com.mycompany.library_management_system.protocol.Response;
import com.mycompany.library_management_system.protocol.LibraryNotice;
import com.mycompany.library_management_system.protocol.NoticeDatagram;
import com.mycompany.library_management_system.protocol.Wire;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/** One TCP accept loop, one worker per connected client; clients never open SQLite. */
public final class LibraryServer implements AutoCloseable {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final Path database;
    private final int requestedPort;
    private final Consumer<String> logger;
    private final IntConsumer clientCountListener;
    private final Path activityLogPath;
    private final Set<Socket> clients = ConcurrentHashMap.newKeySet();
    private final Map<Socket, Session> sessions = new ConcurrentHashMap<>();
    private final Object logLock = new Object();
    private volatile boolean running;
    private volatile ServerSocket listener;
    private ExecutorService workers;
    private BufferedWriter logFile;
    private BufferedWriter activityFile;
    private DatagramSocket notificationSocket;

    public LibraryServer(Path database, int port, Consumer<String> logger, IntConsumer clientCountListener) {
        this(database, port, logger, clientCountListener, Path.of("logs", "client-activity.log"));
    }

    public LibraryServer(Path database, int port, Consumer<String> logger, IntConsumer clientCountListener,
            Path activityLogPath) {
        if (database == null) { throw new IllegalArgumentException("Đường dẫn CSDL không được trống."); }
        if (port < 0 || port > 65535) { throw new IllegalArgumentException("Cổng phải từ 0 đến 65535."); }
        this.database = database;
        this.requestedPort = port;
        this.logger = logger == null ? line -> { } : logger;
        this.clientCountListener = clientCountListener == null ? count -> { } : clientCountListener;
        this.activityLogPath = activityLogPath == null ? Path.of("logs", "client-activity.log") : activityLogPath;
    }

    public synchronized void start() throws IOException, SQLException {
        if (running) { throw new IllegalStateException("Server đang chạy."); }
        BookRepository repository = new BookRepository(database);
        ServerSocket socket = new ServerSocket();
        try {
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(requestedPort));
            Files.createDirectories(Path.of("logs"));
            synchronized (logLock) {
                logFile = Files.newBufferedWriter(Path.of("logs", "server.log"), StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                Path parent = this.activityLogPath.toAbsolutePath().normalize().getParent();
                if (parent != null) Files.createDirectories(parent);
                activityFile = Files.newBufferedWriter(this.activityLogPath, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
            notificationSocket = new DatagramSocket();
        } catch (IOException ex) {
            socket.close();
            synchronized (logLock) {
                if (activityFile != null) try { activityFile.close(); } catch (IOException ignored) { }
                if (logFile != null) try { logFile.close(); } catch (IOException ignored) { }
                activityFile = null; logFile = null;
            }
            if (notificationSocket != null) { notificationSocket.close(); notificationSocket = null; }
            throw ex;
        }
        AtomicInteger workerId = new AtomicInteger();
        ExecutorService executor = Executors.newCachedThreadPool(task -> {
            Thread thread = new Thread(task, "library-client-" + workerId.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
        listener = socket;
        workers = executor;
        running = true;
        log("SERVER START | TCP 0.0.0.0:" + socket.getLocalPort() + " | SQLite " + database.toAbsolutePath());
        notifyCount();
        Thread acceptThread = new Thread(() -> acceptClients(socket, executor, repository), "library-accept");
        acceptThread.start();
    }

    private void acceptClients(ServerSocket source, ExecutorService executor, BookRepository repository) {
        while (running && listener == source) {
            Socket client = null;
            try {
                client = source.accept();
                client.setTcpNoDelay(true);
                client.setKeepAlive(true);
                synchronized (this) {
                    if (!running || listener != source) {
                        client.close();
                        continue;
                    }
                    clients.add(client);
                    log("CLIENT CONNECT | " + address(client));
                    notifyCount();
                }
                Socket accepted = client;
                executor.execute(() -> handleClient(accepted, repository));
            } catch (IOException | RejectedExecutionException ex) {
                if (client != null) {
                    closeQuietly(client);
                    finishClient(client);
                }
                if (running && listener == source) { log("ACCEPT ERROR | " + ex.getMessage()); }
            }
        }
    }

    private void handleClient(Socket socket, BookRepository repository) {
        Session session = new Session();
        try (socket;
                BufferedInputStream input = new BufferedInputStream(socket.getInputStream());
                BufferedOutputStream output = new BufferedOutputStream(socket.getOutputStream())) {
            while (running && !socket.isClosed()) {
                Request request;
                try {
                    request = Wire.read(input, Request.class);
                } catch (EOFException ex) {
                    break;
                } catch (IOException ex) {
                    log("PROTOCOL ERROR | " + address(socket) + " | " + ex.getMessage());
                    if (!socket.isClosed()) {
                        Wire.write(output, Response.error("PROTOCOL", "Thông điệp mạng không hợp lệ. Hãy kết nối lại."));
                    }
                    break;
                }
                Response response = process(request, repository, session, socket);
                log("COMMAND | " + address(socket) + " | " + safe(request.command())
                        + (request.book() == null ? "" : " | " + safe(request.book().code()))
                        + (request.code() == null ? "" : " | " + safe(request.code()))
                        + " | " + (response.success() ? "OK" : response.errorCode()));
                activityLog(session, request, response);
                Wire.write(output, response);
                if (request.command() != null && "QUIT".equalsIgnoreCase(request.command().strip())) { break; }
            }
        } catch (IOException ex) {
            if (running) { log("CLIENT ERROR | " + address(socket) + " | " + ex.getMessage()); }
        } finally {
            sessions.remove(socket);
            finishClient(socket);
        }
    }

    private Response process(Request request, BookRepository repository, Session session, Socket socket) {
        if (request.command() == null || request.command().isBlank()) {
            return Response.error("VALIDATION", "Yêu cầu phải có mã lệnh.");
        }
        String command = request.command().strip().toUpperCase(Locale.ROOT);
        try {
            if ("LOGIN".equals(command)) {
                if (session.username != null) return Response.error("ALREADY_AUTHENTICATED", "Kết nối này đã đăng nhập.");
                Integer udpPort = request.udpPort();
                String token = request.udpToken();
                if (udpPort != null && (udpPort < 0 || udpPort > 65535)) {
                    return Response.error("VALIDATION", "Cổng UDP không hợp lệ.");
                }
                if ((udpPort == null && token != null)
                        || (udpPort != null && udpPort == 0 && token != null)
                        || (udpPort != null && udpPort > 0
                            && (token == null || !token.matches("[0-9a-f]{48}")))) {
                    return Response.error("VALIDATION", "Đăng ký UDP không hợp lệ.");
                }
                AccountRepository.AuthenticatedUser user = new AccountRepository(repository.databasePath())
                        .authenticate(request.username(), request.password());
                if (user == null) return Response.error("INVALID_CREDENTIALS", "Tên đăng nhập hoặc mật khẩu không đúng.");
                session.username = user.username();
                session.role = user.role();
                session.token = token;
                if (udpPort != null && udpPort > 0 && token != null) {
                    session.udpAddress = socket.getInetAddress();
                    session.udpPort = udpPort;
                }
                sessions.put(socket, session);
                return Response.signedIn(session.username, session.role);
            }
            if ("PING".equals(command) || "QUIT".equals(command)) {
                return "PING".equals(command) ? Response.ok("PONG", List.of()) : Response.ok("Đã ngắt kết nối.", List.of());
            }
            if (session.username == null) return Response.error("AUTH_REQUIRED", "Vui lòng đăng nhập trước khi thực hiện thao tác.");
            if (!"ADMIN".equals(session.role)
                    && !(command.equals("LIST") || command.equals("SEARCH") || command.equals("BORROW")
                        || command.equals("RETURN") || command.equals("LOANS"))) {
                return Response.error("FORBIDDEN", "Tài khoản bạn đọc không có quyền quản lý sách.");
            }
            return switch (command) {
                case "LIST" -> Response.ok("Đã tải danh sách sách.", repository.list());
                case "SEARCH" -> Response.ok("Đã tìm kiếm sách.", repository.search(request.query()));
                case "ADD" -> {
                    var book = repository.add(request.book());
                    broadcast(new LibraryNotice("BOOK_ADDED", book.code(), "Sách mới đã được thêm vào thư viện."));
                    yield Response.ok("Thêm sách thành công.", List.of(book));
                }
                case "UPDATE" -> {
                    var book = repository.update(request.book());
                    broadcast(new LibraryNotice("BOOK_UPDATED", book.code(), "Thông tin sách đã được cập nhật."));
                    yield Response.ok("Cập nhật sách thành công.", List.of(book));
                }
                case "DELETE" -> {
                    repository.delete(request.code(), request.version());
                    broadcast(new LibraryNotice("BOOK_DELETED", request.code(), "Một sách đã được xóa khỏi thư viện."));
                    yield Response.ok("Xóa sách thành công.", List.of());
                }
                case "BORROW" -> {
                    long id = new LoanRepository(repository.databasePath()).borrow(session.username, request.code());
                    broadcast(new LibraryNotice("BOOK_BORROWED", request.code(), "Sách đã được mượn bởi một bạn đọc."));
                    yield new Response(true, "Mượn sách thành công. Mã phiếu: " + id,
                            List.of(), null, session.username, List.of(), session.role);
                }
                case "RETURN" -> {
                    String code = new LoanRepository(repository.databasePath()).returnBook(session.username, request.loanId() == null ? 0 : request.loanId());
                    broadcast(new LibraryNotice("BOOK_RETURNED", code, "Sách đã được trả về thư viện."));
                    yield Response.ok("Trả sách thành công.", List.of());
                }
                case "LOANS" -> Response.loanHistory(new LoanRepository(repository.databasePath()).list(session.username));
                default -> Response.error("UNKNOWN_COMMAND", "Mã lệnh không được hỗ trợ: " + safe(command));
            };
        } catch (LibraryException ex) {
            return Response.error(ex.code(), ex.getMessage());
        } catch (SQLException ex) {
            log("DATABASE ERROR | " + ex.getMessage());
            return Response.error("DATABASE", "Không thể xử lý dữ liệu. Vui lòng thử lại sau.");
        } catch (RuntimeException ex) {
            log("REQUEST ERROR | " + ex.getMessage());
            return Response.error("VALIDATION", "Dữ liệu yêu cầu không hợp lệ.");
        }
    }

    private synchronized void finishClient(Socket socket) {
        if (clients.remove(socket)) {
            log("CLIENT DISCONNECT | " + address(socket));
            notifyCount();
        }
    }

    public int getPort() {
        ServerSocket current = listener;
        return current == null ? requestedPort : current.getLocalPort();
    }

    public int getConnectedClientCount() { return clients.size(); }
    public boolean isRunning() { return running; }

    @Override
    public synchronized void close() {
        if (!running) { return; }
        running = false;
        if (notificationSocket != null) { notificationSocket.close(); notificationSocket = null; }
        if (listener != null) {
            try { listener.close(); } catch (IOException ignored) { }
        }
        for (Socket socket : clients.toArray(Socket[]::new)) {
            closeQuietly(socket);
            finishClient(socket);
        }
        if (workers != null) { workers.shutdownNow(); }
        log("SERVER STOP");
        synchronized (logLock) {
            if (logFile != null) {
                try { logFile.close(); } catch (IOException ignored) { }
                logFile = null;
            }
            if (activityFile != null) {
                try { activityFile.close(); } catch (IOException ignored) { }
                activityFile = null;
            }
        }
        notifyCount();
    }

    private void log(String message) {
        String line = "[" + LocalDateTime.now().format(TIME) + "] " + safe(message);
        synchronized (logLock) {
            if (logFile != null) {
                try {
                    logFile.write(line);
                    logFile.newLine();
                    logFile.flush();
                } catch (IOException ex) {
                    System.err.println("Không thể ghi nhật ký server: " + ex.getMessage());
                }
            }
        }
        try { logger.accept(line); } catch (RuntimeException ignored) { }
    }

    private void activityLog(Session session, Request request, Response response) {
        String command = safe(request.command()).toUpperCase(Locale.ROOT);
        String user = session.username == null ? "-" : safe(session.username);
        String detail = switch (command) {
            case "LOGIN" -> response.success() ? "user=" + user : "LOGIN_FAILURE";
            case "BORROW" -> "code=" + safe(request.code());
            case "RETURN" -> "loan=" + String.valueOf(request.loanId());
            case "ADD", "UPDATE" -> "code=" + (request.book() == null ? "-" : safe(request.book().code()));
            case "DELETE" -> "code=" + safe(request.code());
            default -> "";
        };
        writeActivity("CLIENT | user=" + user + " | " + command + (detail.isBlank() ? "" : " | " + detail)
                + " | " + (response.success() ? "OK" : safe(response.errorCode())));
    }

    private void writeActivity(String message) {
        String line = "[" + LocalDateTime.now().format(TIME) + "] " + safe(message);
        synchronized (logLock) {
            if (activityFile != null) {
                try { activityFile.write(line); activityFile.newLine(); activityFile.flush(); }
                catch (IOException ex) { log("ACTIVITY LOG ERROR | " + ex.getMessage()); }
            }
        }
    }

    private void broadcast(LibraryNotice notice) {
        DatagramSocket socket = notificationSocket;
        if (socket == null || notice == null) return;
        for (Session session : sessions.values()) {
            if (session.udpAddress == null || session.udpPort <= 0 || session.token == null) continue;
            try {
                byte[] payload = new NoticeDatagram(session.token, notice).encode();
                socket.send(new DatagramPacket(payload, payload.length, session.udpAddress, session.udpPort));
            } catch (IOException ignored) { }
        }
    }

    private void notifyCount() {
        try { clientCountListener.accept(clients.size()); } catch (RuntimeException ignored) { }
    }

    private static String safe(String text) {
        if (text == null) { return "(null)"; }
        return text.replace('\n', ' ').replace('\r', ' ');
    }

    private static String address(Socket socket) { return String.valueOf(socket.getRemoteSocketAddress()); }

    private static void closeQuietly(Socket socket) {
        try { socket.close(); } catch (IOException ignored) { }
    }

    private static final class Session {
        String username;
        String role;
        String token;
        java.net.InetAddress udpAddress;
        int udpPort;
    }
}
