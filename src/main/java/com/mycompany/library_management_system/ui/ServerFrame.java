package com.mycompany.library_management_system.ui;

import com.mycompany.library_management_system.server.LibraryServer;
import java.awt.Cursor;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

/** NetBeans JFrame Form for server configuration, sessions and activity. */
public class ServerFrame extends JFrame {
    private volatile LibraryServer server;
    private volatile boolean disposed;
    private boolean working;

    public ServerFrame() { this(Path.of("data", "library.db"), 9999); }

    public ServerFrame(Path databaseFile, int initialPort) {
        initComponents();
        port.setText(Integer.toString(initialPort));
        database.setText(databaseFile.toString());
        setLocationByPlatform(true);
        getRootPane().setDefaultButton(start);
        appendLog("Sẵn sàng. Khởi động Server trước khi mở kết nối từ Client.");
        updateControls();
    }

    /** This method is maintained by NetBeans GUI Builder; edit the .form in Design. */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        bannerPanel = new javax.swing.JPanel();
        brandPanel = new javax.swing.JPanel();
        brandLabel = new javax.swing.JLabel();
        headingLabel = new javax.swing.JLabel();
        modeLabel = new javax.swing.JLabel();
        contentPanel = new javax.swing.JPanel();
        topPanel = new javax.swing.JPanel();
        metricsPanel = new javax.swing.JPanel();
        stateCard = new javax.swing.JPanel();
        stateCardCaption = new javax.swing.JLabel();
        state = new javax.swing.JLabel();
        stateCardHint = new javax.swing.JLabel();
        connectionCard = new javax.swing.JPanel();
        connectionCardCaption = new javax.swing.JLabel();
        connections = new javax.swing.JLabel();
        connectionCardHint = new javax.swing.JLabel();
        portCard = new javax.swing.JPanel();
        portCardCaption = new javax.swing.JLabel();
        address = new javax.swing.JLabel();
        portCardHint = new javax.swing.JLabel();
        configPanel = new javax.swing.JPanel();
        configTitleLabel = new javax.swing.JLabel();
        configFieldsPanel = new javax.swing.JPanel();
        portLabel = new javax.swing.JLabel();
        databaseLabel = new javax.swing.JLabel();
        port = new javax.swing.JTextField();
        database = new javax.swing.JTextField();
        browse = new javax.swing.JButton();
        start = new javax.swing.JButton();
        stop = new javax.swing.JButton();
        configHintLabel = new javax.swing.JLabel();
        activityPanel = new javax.swing.JPanel();
        logHeaderPanel = new javax.swing.JPanel();
        activityTitleLabel = new javax.swing.JLabel();
        logHint = new javax.swing.JLabel();
        logScrollPane = new javax.swing.JScrollPane();
        log = new javax.swing.JTextArea();
        footerPanel = new javax.swing.JPanel();
        foot = new javax.swing.JLabel();
        technologyLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Thư viện • Server");
        setMinimumSize(new java.awt.Dimension(900, 660));
        setPreferredSize(new java.awt.Dimension(1100, 780));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        bannerPanel.setBackground(new java.awt.Color(23, 43, 57));
        bannerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 30, 24, 30));
        bannerPanel.setLayout(new java.awt.BorderLayout(20, 0));

        brandPanel.setBackground(new java.awt.Color(23, 43, 57));
        brandPanel.setLayout(new java.awt.GridLayout(2, 1, 0, 7));

        brandLabel.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        brandLabel.setForeground(new java.awt.Color(145, 180, 186));
        brandLabel.setText("THƯ VIỆN / MÁY CHỦ");
        brandPanel.add(brandLabel);

        headingLabel.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        headingLabel.setForeground(new java.awt.Color(255, 255, 255));
        headingLabel.setText("Trung tâm kết nối");
        brandPanel.add(headingLabel);

        bannerPanel.add(brandPanel, java.awt.BorderLayout.CENTER);

        modeLabel.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        modeLabel.setForeground(new java.awt.Color(116, 214, 188));
        modeLabel.setText("TCP  /  CLIENT–SERVER");
        bannerPanel.add(modeLabel, java.awt.BorderLayout.EAST);

        getContentPane().add(bannerPanel, java.awt.BorderLayout.NORTH);

        contentPanel.setBackground(new java.awt.Color(245, 247, 248));
        contentPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 28, 16, 28));
        contentPanel.setLayout(new java.awt.BorderLayout(0, 18));

        topPanel.setBackground(new java.awt.Color(245, 247, 248));
        topPanel.setLayout(new java.awt.BorderLayout(0, 18));

        metricsPanel.setBackground(new java.awt.Color(245, 247, 248));
        metricsPanel.setLayout(new java.awt.GridLayout(1, 3, 15, 0));

        stateCard.setBackground(new java.awt.Color(255, 255, 255));
        stateCard.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 18, 16, 18));
        stateCard.setLayout(new java.awt.BorderLayout(0, 6));

        stateCardCaption.setFont(new java.awt.Font("Segoe UI", 1, 10)); // NOI18N
        stateCardCaption.setForeground(new java.awt.Color(106, 122, 132));
        stateCardCaption.setText("TRẠNG THÁI MÁY CHỦ");
        stateCard.add(stateCardCaption, java.awt.BorderLayout.NORTH);

        state.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        state.setForeground(new java.awt.Color(106, 122, 132));
        state.setText("Đã dừng");
        stateCard.add(state, java.awt.BorderLayout.CENTER);

        stateCardHint.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        stateCardHint.setForeground(new java.awt.Color(106, 122, 132));
        stateCardHint.setText("Tiếp nhận kết nối từ các Client");
        stateCard.add(stateCardHint, java.awt.BorderLayout.SOUTH);

        metricsPanel.add(stateCard);

        connectionCard.setBackground(new java.awt.Color(255, 255, 255));
        connectionCard.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 18, 16, 18));
        connectionCard.setLayout(new java.awt.BorderLayout(0, 6));

        connectionCardCaption.setFont(new java.awt.Font("Segoe UI", 1, 10)); // NOI18N
        connectionCardCaption.setForeground(new java.awt.Color(106, 122, 132));
        connectionCardCaption.setText("CLIENT ĐANG KẾT NỐI");
        connectionCard.add(connectionCardCaption, java.awt.BorderLayout.NORTH);

        connections.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        connections.setForeground(new java.awt.Color(23, 43, 57));
        connections.setText("0");
        connectionCard.add(connections, java.awt.BorderLayout.CENTER);

        connectionCardHint.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        connectionCardHint.setForeground(new java.awt.Color(106, 122, 132));
        connectionCardHint.setText("Số phiên làm việc hiện tại");
        connectionCard.add(connectionCardHint, java.awt.BorderLayout.SOUTH);

        metricsPanel.add(connectionCard);

        portCard.setBackground(new java.awt.Color(255, 255, 255));
        portCard.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 18, 16, 18));
        portCard.setLayout(new java.awt.BorderLayout(0, 6));

        portCardCaption.setFont(new java.awt.Font("Segoe UI", 1, 10)); // NOI18N
        portCardCaption.setForeground(new java.awt.Color(106, 122, 132));
        portCardCaption.setText("CỔNG TCP");
        portCard.add(portCardCaption, java.awt.BorderLayout.NORTH);

        address.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        address.setForeground(new java.awt.Color(23, 43, 57));
        address.setText("—");
        portCard.add(address, java.awt.BorderLayout.CENTER);

        portCardHint.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        portCardHint.setForeground(new java.awt.Color(106, 122, 132));
        portCardHint.setText("Địa chỉ lắng nghe: 0.0.0.0");
        portCard.add(portCardHint, java.awt.BorderLayout.SOUTH);

        metricsPanel.add(portCard);

        topPanel.add(metricsPanel, java.awt.BorderLayout.NORTH);

        configPanel.setBackground(new java.awt.Color(255, 255, 255));
        configPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(17, 20, 17, 20));
        configPanel.setLayout(new java.awt.BorderLayout(0, 15));

        configTitleLabel.setFont(new java.awt.Font("Segoe UI", 1, 17)); // NOI18N
        configTitleLabel.setForeground(new java.awt.Color(23, 43, 57));
        configTitleLabel.setText("Cấu hình máy chủ");
        configPanel.add(configTitleLabel, java.awt.BorderLayout.NORTH);

        configFieldsPanel.setBackground(new java.awt.Color(255, 255, 255));
        configFieldsPanel.setLayout(new java.awt.GridBagLayout());

        portLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        portLabel.setForeground(new java.awt.Color(23, 43, 57));
        portLabel.setText("Cổng TCP");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 7, 12);
        configFieldsPanel.add(portLabel, gridBagConstraints);

        databaseLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        databaseLabel.setForeground(new java.awt.Color(23, 43, 57));
        databaseLabel.setText("Tệp cơ sở dữ liệu SQLite");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 7, 12);
        configFieldsPanel.add(databaseLabel, gridBagConstraints);

        port.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        port.setForeground(new java.awt.Color(23, 43, 57));
        port.setCaretColor(new java.awt.Color(13, 119, 110));
        port.setText("9999");
        port.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 10, 8, 10));
        port.setPreferredSize(new java.awt.Dimension(90, 38));
        port.setName("serverPortField"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        configFieldsPanel.add(port, gridBagConstraints);

        database.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        database.setForeground(new java.awt.Color(23, 43, 57));
        database.setCaretColor(new java.awt.Color(13, 119, 110));
        database.setText("data/library.db");
        database.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 10, 8, 10));
        database.setPreferredSize(new java.awt.Dimension(350, 38));
        database.setName("databasePathField"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        configFieldsPanel.add(database, gridBagConstraints);

        browse.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        browse.setForeground(new java.awt.Color(23, 43, 57));
        browse.setText("Chọn tệp…");
        browse.setFocusPainted(false);
        browse.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 14, 10, 14));
        browse.setName("browseDatabaseButton"); // NOI18N
        browse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
        configFieldsPanel.add(browse, gridBagConstraints);

        start.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        start.setBackground(new java.awt.Color(13, 119, 110));
        start.setForeground(new java.awt.Color(255, 255, 255));
        start.setText("Khởi động Server");
        start.setFocusPainted(false);
        start.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 14, 10, 14));
        start.setName("startServerButton"); // NOI18N
        start.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
        configFieldsPanel.add(start, gridBagConstraints);

        stop.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        stop.setForeground(new java.awt.Color(23, 43, 57));
        stop.setText("Dừng Server");
        stop.setFocusPainted(false);
        stop.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 14, 10, 14));
        stop.setName("stopServerButton"); // NOI18N
        stop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        configFieldsPanel.add(stop, gridBagConstraints);

        configPanel.add(configFieldsPanel, java.awt.BorderLayout.CENTER);

        configHintLabel.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        configHintLabel.setForeground(new java.awt.Color(106, 122, 132));
        configHintLabel.setText("Client cùng máy dùng 127.0.0.1. Client trong mạng LAN dùng địa chỉ IP của máy chủ.");
        configPanel.add(configHintLabel, java.awt.BorderLayout.SOUTH);

        topPanel.add(configPanel, java.awt.BorderLayout.CENTER);

        contentPanel.add(topPanel, java.awt.BorderLayout.NORTH);

        activityPanel.setBackground(new java.awt.Color(255, 255, 255));
        activityPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(17, 20, 17, 20));
        activityPanel.setLayout(new java.awt.BorderLayout(0, 13));

        logHeaderPanel.setBackground(new java.awt.Color(255, 255, 255));
        logHeaderPanel.setLayout(new java.awt.BorderLayout(12, 0));

        activityTitleLabel.setFont(new java.awt.Font("Segoe UI", 1, 17)); // NOI18N
        activityTitleLabel.setForeground(new java.awt.Color(23, 43, 57));
        activityTitleLabel.setText("Nhật ký hoạt động");
        logHeaderPanel.add(activityTitleLabel, java.awt.BorderLayout.WEST);

        logHint.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        logHint.setForeground(new java.awt.Color(106, 122, 132));
        logHint.setText("Nhật ký được lưu tại logs/server.log");
        logHeaderPanel.add(logHint, java.awt.BorderLayout.EAST);

        activityPanel.add(logHeaderPanel, java.awt.BorderLayout.NORTH);

        logScrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        log.setEditable(false);
        log.setColumns(20);
        log.setRows(8);
        log.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        log.setBackground(new java.awt.Color(23, 43, 57));
        log.setForeground(new java.awt.Color(190, 219, 212));
        log.setCaretColor(new java.awt.Color(255, 255, 255));
        log.setLineWrap(true);
        log.setWrapStyleWord(true);
        log.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        log.setName("serverLog"); // NOI18N
        logScrollPane.setViewportView(log);

        activityPanel.add(logScrollPane, java.awt.BorderLayout.CENTER);

        contentPanel.add(activityPanel, java.awt.BorderLayout.CENTER);

        footerPanel.setBackground(new java.awt.Color(245, 247, 248));
        footerPanel.setLayout(new java.awt.BorderLayout(12, 0));

        foot.setForeground(new java.awt.Color(106, 122, 132));
        foot.setText("Sẵn sàng khởi động máy chủ.");
        footerPanel.add(foot, java.awt.BorderLayout.WEST);

        technologyLabel.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        technologyLabel.setForeground(new java.awt.Color(106, 122, 132));
        technologyLabel.setText("Java Swing · SQLite");
        footerPanel.add(technologyLabel, java.awt.BorderLayout.EAST);

        contentPanel.add(footerPanel, java.awt.BorderLayout.SOUTH);

        getContentPane().add(contentPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void startActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startActionPerformed
        startServer();
    }//GEN-LAST:event_startActionPerformed

    private void stopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopActionPerformed
        stopServer();
    }//GEN-LAST:event_stopActionPerformed

    private void browseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseActionPerformed
        chooseDatabase();
    }//GEN-LAST:event_browseActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeWindow();
    }//GEN-LAST:event_formWindowClosing

    private void startServer() {
        if (working || server != null && server.isRunning()) return;
        int number;
        Path path;
        try {
            number = Integer.parseInt(port.getText().strip());
            if (number < 1 || number > 65535) throw new IllegalArgumentException("Cổng phải từ 1 đến 65535.");
            if (database.getText().isBlank()) throw new IllegalArgumentException("Vui lòng chọn tệp cơ sở dữ liệu.");
            path = Path.of(database.getText().strip());
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Cấu hình chưa hợp lệ.\n" + e.getMessage(), "Kiểm tra cấu hình", JOptionPane.WARNING_MESSAGE);
            return;
        }
        working = true; updateControls(); foot.setText("Đang khởi động máy chủ…");
        new SwingWorker<LibraryServer, Void>() {
            @Override protected LibraryServer doInBackground() throws Exception {
                LibraryServer candidate = new LibraryServer(path, number, ServerFrame.this::appendLog, n -> SwingUtilities.invokeLater(() -> connections.setText(Integer.toString(n))));
                server = candidate;
                try { candidate.start(); }
                catch (Exception error) { candidate.close(); throw error; }
                if (disposed) candidate.close();
                return candidate;
            }
            @Override protected void done() {
                if (disposed) return;
                working = false;
                try {
                    LibraryServer running = get();
                    state.setText("Đang chạy"); state.setForeground(Theme.TEAL);
                    address.setText(Integer.toString(running.getPort()));
                    foot.setText("Máy chủ đang lắng nghe · Cổng " + running.getPort());
                    database.setToolTipText(path.toAbsolutePath().toString());
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                catch (ExecutionException e) {
                    server = null;
                    String message = e.getCause() == null ? e.getMessage() : e.getCause().getMessage();
                    appendLog("KHÔNG THỂ KHỞI ĐỘNG: " + message);
                    foot.setText("Khởi động thất bại. Kiểm tra cổng và quyền truy cập tệp.");
                    JOptionPane.showMessageDialog(ServerFrame.this, "Không thể khởi động Server.\n" + message, "Lỗi máy chủ", JOptionPane.ERROR_MESSAGE);
                }
                updateControls();
            }
        }.execute();
    }

    private void stopServer() {
        if (working || server == null) return;
        LibraryServer current = server;
        working = true; updateControls(); foot.setText("Đang đóng các kết nối…");
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() { current.close(); return null; }
            @Override protected void done() {
                if (disposed) return;
                working = false;
                try { get(); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                catch (ExecutionException e) { appendLog("Lỗi khi dừng: " + e.getCause().getMessage()); }
                server = null; connections.setText("0"); state.setText("Đã dừng"); state.setForeground(Theme.MUTED); address.setText("—");
                foot.setText("Máy chủ đã dừng. Các Client có thể kết nối lại sau khi khởi động."); updateControls();
            }
        }.execute();
    }

    private void updateControls() {
        boolean running = server != null && server.isRunning();
        port.setEnabled(!working && !running); database.setEnabled(!working && !running); browse.setEnabled(!working && !running);
        start.setEnabled(!working && !running); stop.setEnabled(!working && running);
        start.setText(working && !running ? "Đang xử lý…" : "Khởi động Server");
        setCursor(working ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }

    private void chooseDatabase() {
        JFileChooser chooser = new JFileChooser(); chooser.setDialogTitle("Chọn hoặc tạo tệp SQLite");
        chooser.setSelectedFile(Path.of(database.getText().isBlank() ? "library.db" : database.getText()).toFile());
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) database.setText(chooser.getSelectedFile().getAbsolutePath());
    }
    private void appendLog(String line) {
        if (!SwingUtilities.isEventDispatchThread()) { SwingUtilities.invokeLater(() -> appendLog(line)); return; }
        if (disposed) return;
        if (log.getDocument().getLength() > 200000) log.setText(log.getText().substring(100000));
        String prefix = line.startsWith("[") ? "" : "[" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] ";
        log.append(prefix + line + "\n"); log.setCaretPosition(log.getDocument().getLength());
    }
    private void closeWindow() {
        dispose();
    }
    @Override public void dispose() {
        if (!disposed) {
            disposed = true;
            LibraryServer current = server;
            if (current != null) {
                Thread cleanup = new Thread(current::close, "server-window-close"); cleanup.setDaemon(true); cleanup.start();
            }
        }
        super.dispose();
    }

    /** Run this JFrame Form directly using NetBeans Run File. */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Theme.install();
            new ServerFrame().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel activityPanel;
    private javax.swing.JLabel activityTitleLabel;
    private javax.swing.JLabel address;
    private javax.swing.JPanel bannerPanel;
    private javax.swing.JLabel brandLabel;
    private javax.swing.JPanel brandPanel;
    private javax.swing.JButton browse;
    private javax.swing.JPanel configFieldsPanel;
    private javax.swing.JLabel configHintLabel;
    private javax.swing.JPanel configPanel;
    private javax.swing.JLabel configTitleLabel;
    private javax.swing.JPanel connectionCard;
    private javax.swing.JLabel connectionCardCaption;
    private javax.swing.JLabel connectionCardHint;
    private javax.swing.JLabel connections;
    private javax.swing.JPanel contentPanel;
    private javax.swing.JTextField database;
    private javax.swing.JLabel databaseLabel;
    private javax.swing.JLabel foot;
    private javax.swing.JPanel footerPanel;
    private javax.swing.JLabel headingLabel;
    private javax.swing.JTextArea log;
    private javax.swing.JPanel logHeaderPanel;
    private javax.swing.JLabel logHint;
    private javax.swing.JScrollPane logScrollPane;
    private javax.swing.JPanel metricsPanel;
    private javax.swing.JLabel modeLabel;
    private javax.swing.JTextField port;
    private javax.swing.JPanel portCard;
    private javax.swing.JLabel portCardCaption;
    private javax.swing.JLabel portCardHint;
    private javax.swing.JLabel portLabel;
    private javax.swing.JButton start;
    private javax.swing.JLabel state;
    private javax.swing.JPanel stateCard;
    private javax.swing.JLabel stateCardCaption;
    private javax.swing.JLabel stateCardHint;
    private javax.swing.JButton stop;
    private javax.swing.JLabel technologyLabel;
    private javax.swing.JPanel topPanel;
    // End of variables declaration//GEN-END:variables
}
