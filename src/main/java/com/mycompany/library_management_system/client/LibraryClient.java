package com.mycompany.library_management_system.client;

import com.mycompany.library_management_system.model.Book;
import com.mycompany.library_management_system.protocol.LibraryNotice;
import com.mycompany.library_management_system.protocol.NoticeDatagram;
import com.mycompany.library_management_system.protocol.Request;
import com.mycompany.library_management_system.protocol.Response;
import com.mycompany.library_management_system.protocol.Wire;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.function.Consumer;

/** A sequential TCP session. Only the server ever accesses SQLite. */
public final class LibraryClient implements AutoCloseable {
    private static final SecureRandom RANDOM = new SecureRandom();
    private volatile Socket socket;
    private final Object lifecycle = new Object();
    private DatagramSocket noticeSocket;
    private InputStream input;
    private OutputStream output;

    public synchronized void connect(String host, int port) throws IOException {
        if (host == null || host.isBlank()) throw new IOException("Vui lòng nhập địa chỉ máy chủ.");
        if (port < 1 || port > 65535) throw new IOException("Cổng phải từ 1 đến 65535.");
        close();
        Socket candidate = new Socket();
        synchronized (lifecycle) { socket = candidate; }
        try {
            candidate.connect(new InetSocketAddress(host.trim(), port), 4000);
            candidate.setSoTimeout(8000);
            candidate.setTcpNoDelay(true);
            input = new BufferedInputStream(candidate.getInputStream());
            output = new BufferedOutputStream(candidate.getOutputStream());
            if (socket != candidate) throw new IOException("Kết nối đã bị hủy.");
            Response hello = request(new Request("PING", null, null, null, null));
            if (!hello.success()) throw new IOException(hello.message());
        } catch (IOException | RuntimeException ex) {
            try { candidate.close(); } catch (IOException ignored) { }
            close();
            if (ex instanceof IOException io) throw io;
            throw new IOException("Không thể kết nối máy chủ: " + ex.getMessage(), ex);
        }
    }

    public boolean isConnected() {
        Socket current = socket;
        return current != null && current.isConnected() && !current.isClosed();
    }

    public synchronized Response request(Request request) throws IOException {
        if (!isConnected()) throw new IOException("Chưa kết nối đến máy chủ.");
        try {
            Wire.write(output, request);
            Response response = Wire.read(input, Response.class);
            if (response == null || response.message() == null || response.books() == null) {
                throw new IOException("Phản hồi từ máy chủ không hợp lệ.");
            }
            return response;
        } catch (IOException | RuntimeException ex) {
            close();
            if (ex instanceof IOException io) throw io;
            throw new IOException("Lỗi giao thức từ máy chủ.", ex);
        }
    }

    public Response listBooks() throws IOException { return request(new Request("LIST", null, null, null, null)); }
    public Response searchBooks(String query) throws IOException { return request(new Request("SEARCH", null, query, null, null)); }
    public Response addBook(Book book) throws IOException { return request(new Request("ADD", book, null, null, null)); }
    public Response updateBook(Book book) throws IOException { return request(new Request("UPDATE", book, null, null, null)); }
    public Response deleteBook(Book book) throws IOException { return request(new Request("DELETE", null, null, book.code(), book.version())); }

    public synchronized Response login(String username, String password, Consumer<LibraryNotice> notifications)
            throws IOException {
        Socket tcp = socket;
        if (tcp == null || !isConnected()) throw new IOException("Chưa kết nối đến máy chủ.");
        if (notifications == null) return request(Request.login(username, password, 0, null));

        DatagramSocket udp = new DatagramSocket(new InetSocketAddress(tcp.getLocalAddress(), 0));
        try {
            byte[] random = new byte[24];
            RANDOM.nextBytes(random);
            String token = HexFormat.of().formatHex(random);
            Response response = request(Request.login(username, password, udp.getLocalPort(), token));
            if (!response.success()) return response;

            synchronized (lifecycle) {
                if (socket != tcp) throw new IOException("Kết nối đã bị hủy.");
                noticeSocket = udp;
            }
            Thread listener = new Thread(() -> receiveNotices(tcp, udp, token, notifications), "library-notices");
            listener.setDaemon(true);
            listener.start();
            return response;
        } finally {
            synchronized (lifecycle) {
                if (noticeSocket != udp) udp.close();
            }
        }
    }

    public Response borrowBook(String code) throws IOException { return request(Request.borrow(code)); }
    public Response returnBook(long loanId) throws IOException { return request(Request.returnLoan(loanId)); }
    public Response listLoans() throws IOException { return request(Request.loans()); }

    private void receiveNotices(Socket tcp, DatagramSocket udp, String token, Consumer<LibraryNotice> notifications) {
        byte[] buffer = new byte[NoticeDatagram.MAX_BYTES + 1];
        while (!udp.isClosed()) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                udp.receive(packet);
            } catch (IOException ex) {
                udp.close();
                return;
            }
            if (socket != tcp || !tcp.getInetAddress().equals(packet.getAddress())) continue;
            try {
                NoticeDatagram envelope = NoticeDatagram.decode(packet);
                LibraryNotice notice = envelope.notice();
                if (token.equals(envelope.token()) && notice != null && notice.message() != null
                        && notice.code() != null && notice.type() != null) {
                    notifications.accept(notice);
                }
            } catch (IOException | RuntimeException ignored) {
                // Invalid packets and notification callbacks cannot interrupt the TCP session.
            }
        }
    }

    /** May be called by the EDT to interrupt a pending network read. */
    @Override public void close() {
        Socket current;
        synchronized (lifecycle) {
            current = socket;
            socket = null;
            if (noticeSocket != null) {
                noticeSocket.close();
                noticeSocket = null;
            }
        }
        if (current != null) {
            try { current.close(); } catch (IOException ignored) { }
        }
    }
}
