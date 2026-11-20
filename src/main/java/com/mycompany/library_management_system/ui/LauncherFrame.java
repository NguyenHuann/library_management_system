package com.mycompany.library_management_system.ui;

import java.nio.file.Path;

/** NetBeans JFrame Form for selecting the server or client role. */
public class LauncherFrame extends javax.swing.JFrame {
    private final Path database;
    private final String host;
    private final int port;

    public LauncherFrame() {
        this(Path.of("data", "library.db"), "127.0.0.1", 9999);
    }

    public LauncherFrame(Path database, String host, int port) {
        this.database = database;
        this.host = host;
        this.port = port;
        initComponents();
        setLocationRelativeTo(null);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        content = new javax.swing.JPanel();
        introduction = new javax.swing.JPanel();
        heading = new javax.swing.JLabel();
        subtitle = new javax.swing.JLabel();
        choices = new javax.swing.JPanel();
        serverButton = new javax.swing.JButton();
        clientButton = new javax.swing.JButton();
        hint = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Thư viện • Chọn chế độ làm việc");
        setMinimumSize(new java.awt.Dimension(660, 350));

        content.setBackground(new java.awt.Color(245, 247, 248));
        content.setBorder(javax.swing.BorderFactory.createEmptyBorder(32, 36, 28, 36));
        content.setPreferredSize(new java.awt.Dimension(700, 350));
        content.setLayout(new java.awt.BorderLayout(0, 26));

        introduction.setOpaque(false);
        introduction.setLayout(new java.awt.GridLayout(2, 1, 0, 8));

        heading.setFont(new java.awt.Font("Segoe UI", 1, 26)); // NOI18N
        heading.setForeground(new java.awt.Color(23, 43, 57));
        heading.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        heading.setText("Hệ thống quản lý thư viện");
        introduction.add(heading);

        subtitle.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        subtitle.setForeground(new java.awt.Color(106, 122, 132));
        subtitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        subtitle.setText("Chọn chế độ để bắt đầu phiên làm việc");
        introduction.add(subtitle);

        content.add(introduction, java.awt.BorderLayout.NORTH);

        choices.setOpaque(false);
        choices.setLayout(new java.awt.GridLayout(1, 2, 20, 0));

        serverButton.setBackground(new java.awt.Color(23, 43, 57));
        serverButton.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        serverButton.setForeground(new java.awt.Color(255, 255, 255));
        serverButton.setText("<html><center><b>MÁY CHỦ</b><br><br>Quản lý kết nối và dữ liệu</center></html>");
        serverButton.setFocusPainted(false);
        serverButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverButtonActionPerformed(evt);
            }
        });
        choices.add(serverButton);

        clientButton.setBackground(new java.awt.Color(13, 119, 110));
        clientButton.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        clientButton.setForeground(new java.awt.Color(255, 255, 255));
        clientButton.setText("<html><center><b>ỨNG DỤNG THƯ VIỆN</b><br><br>Tra cứu và quản lý sách</center></html>");
        clientButton.setFocusPainted(false);
        clientButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clientButtonActionPerformed(evt);
            }
        });
        choices.add(clientButton);

        content.add(choices, java.awt.BorderLayout.CENTER);

        hint.setForeground(new java.awt.Color(106, 122, 132));
        hint.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        hint.setText("Khởi động máy chủ trước, sau đó mở một hoặc nhiều Client.");
        content.add(hint, java.awt.BorderLayout.SOUTH);

        getContentPane().add(content, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void serverButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverButtonActionPerformed
        new ServerFrame(database, port).setVisible(true);
    }//GEN-LAST:event_serverButtonActionPerformed

    private void clientButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clientButtonActionPerformed
        new ClientFrame(host, port).setVisible(true);
    }//GEN-LAST:event_clientButtonActionPerformed

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            Theme.install();
            new LauncherFrame().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel choices;
    private javax.swing.JButton clientButton;
    private javax.swing.JPanel content;
    private javax.swing.JLabel heading;
    private javax.swing.JLabel hint;
    private javax.swing.JPanel introduction;
    private javax.swing.JButton serverButton;
    private javax.swing.JLabel subtitle;
    // End of variables declaration//GEN-END:variables
}
