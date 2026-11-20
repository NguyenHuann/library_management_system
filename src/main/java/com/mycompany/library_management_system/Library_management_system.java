package com.mycompany.library_management_system;

import com.mycompany.library_management_system.server.LibraryServer;
import com.mycompany.library_management_system.ui.ClientFrame;
import com.mycompany.library_management_system.ui.ServerFrame;
import com.mycompany.library_management_system.ui.Theme;
import com.mycompany.library_management_system.ui.LauncherFrame;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/** Entry point for NetBeans, executable JAR, desktop server and remote clients. */
public final class Library_management_system {
    private Library_management_system() { }

    public static void main(String[] args) {
        try {
            Options options = Options.parse(args);
            if (options.help) {
                System.out.println("Java Swing / SQLite Library Management\n"
                        + "java -jar library-management.jar [--server|--client] [options]\n"
                        + "  --host <IP>      Client: default 127.0.0.1\n"
                        + "  --port <port>    TCP: default 9999\n"
                        + "  --db <file>      Server: default data/library.db\n"
                        + "  --headless       Console server (requires --server)\n"
                        + "No arguments: open the role selector.");
                return;
            }
            if (options.headless) {
                LibraryServer server = new LibraryServer(options.database, options.port,
                        System.out::println, count -> { });
                server.start();
                Runtime.getRuntime().addShutdownHook(new Thread(server::close, "server-shutdown"));
                new CountDownLatch(1).await();
                return;
            }
            SwingUtilities.invokeLater(() -> {
                Theme.install();
                if (options.server) new ServerFrame(options.database, options.port).setVisible(true);
                else if (options.client) new ClientFrame(options.host, options.port).setVisible(true);
                else new LauncherFrame(options.database, options.host, options.port).setVisible(true);
            });
        } catch (Exception ex) {
            System.err.println("Không thể khởi động: " + ex.getMessage());
            if (!java.awt.GraphicsEnvironment.isHeadless() && !Arrays.asList(args).contains("--headless")) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Lỗi khởi động", JOptionPane.ERROR_MESSAGE);
            }
            System.exit(1);
        }
    }

    private static final class Options {
        boolean server, client, headless, help;
        String host = "127.0.0.1";
        int port = 9999;
        Path database = Path.of("data", "library.db");

        static Options parse(String[] args) {
            Options o = new Options();
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                switch (arg) {
                    case "--server" -> o.server = true;
                    case "--client" -> o.client = true;
                    case "--headless" -> o.headless = true;
                    case "--help", "-h" -> o.help = true;
                    case "--host", "--port", "--db" -> {
                        if (++i == args.length) throw new IllegalArgumentException("Thiếu giá trị cho " + arg);
                        switch (arg) {
                            case "--host" -> o.host = args[i];
                            case "--port" -> o.port = Integer.parseInt(args[i]);
                            case "--db" -> o.database = Path.of(args[i]);
                            default -> throw new AssertionError();
                        }
                    }
                    default -> throw new IllegalArgumentException("Tham số không hợp lệ: " + arg);
                }
            }
            if (o.server && o.client) throw new IllegalArgumentException("Chọn --server hoặc --client.");
            if (o.headless && !o.server) throw new IllegalArgumentException("--headless cần đi cùng --server.");
            if (o.port < 1 || o.port > 65535) throw new IllegalArgumentException("Cổng phải từ 1 đến 65535.");
            return o;
        }
    }
}
