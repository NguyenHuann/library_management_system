package com.mycompany.library_management_system.ui;

import com.mycompany.library_management_system.client.LibraryClient;
import com.mycompany.library_management_system.model.Book;
import com.mycompany.library_management_system.model.Loan;
import com.mycompany.library_management_system.protocol.LibraryNotice;
import com.mycompany.library_management_system.protocol.Response;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

/** NetBeans JFrame Form client. Its .form owns the entire editable component tree. */
public class ClientFrame extends JFrame {
    private final LibraryClient client = new LibraryClient();
    private final BookTableModel model = new BookTableModel();
    private final Timer heartbeat;
    private CardLayout screens;
    private Book selected;
    private String activeQuery = "";
    private boolean busy;
    private boolean passiveRefresh;
    private boolean connected;
    private boolean adjustingSelection;
    private boolean disposed;
    private boolean isAdmin;
    private long loginAttempt;
    private String signedInUsername;
    private LoansDialog loansDialog;

    public ClientFrame() { this("127.0.0.1", 9999); }

    public ClientFrame(String initialHost, int initialPort) {
        initComponents();
        screens = (CardLayout) root.getLayout();
        host.setText(initialHost);
        port.setText(Integer.toString(initialPort));
        year.setText(Integer.toString(LocalDate.now().getYear()));
        configureTable();
        editorScroll.getVerticalScrollBar().setUnitIncrement(18);
        screens.show(root, "connect");
        setSize(1240, 820);
        setLocationByPlatform(true);
        getRootPane().setDefaultButton(connect);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !adjustingSelection && table.getSelectedRow() >= 0) {
                showBook(model.books.get(table.convertRowIndexToModel(table.getSelectedRow())));
            }
        });
        heartbeat = new Timer(8000, e -> {
            if (connected && !busy && loansDialog == null) reload(activeQuery, true);
        });
        heartbeat.start();
        registerShortcuts();
        updateButtons();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Theme.install();
            new ClientFrame().setVisible(true);
        });
    }

    /** Layout and beans are owned by the NetBeans GUI Builder (.form). */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        root = new javax.swing.JPanel();
        connectionPage = new javax.swing.JPanel();
        introPanel = new javax.swing.JPanel();
        brandLabel = new javax.swing.JLabel();
        introStory = new javax.swing.JPanel();
        introHeadline = new javax.swing.JLabel();
        introDescription = new javax.swing.JLabel();
        connectionRight = new javax.swing.JPanel();
        connectionForm = new javax.swing.JPanel();
        connectionKicker = new javax.swing.JLabel();
        connectionTitle = new javax.swing.JLabel();
        connectionDescription = new javax.swing.JLabel();
        hostLabel = new javax.swing.JLabel();
        host = new javax.swing.JTextField();
        portLabel = new javax.swing.JLabel();
        port = new javax.swing.JTextField();
        usernameLabel = new javax.swing.JLabel();
        username = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        password = new javax.swing.JPasswordField();
        connect = new javax.swing.JButton();
        connectMessage = new javax.swing.JLabel();
        connectionHint = new javax.swing.JLabel();
        managementPage = new javax.swing.JPanel();
        sidebar = new javax.swing.JPanel();
        sidebarTop = new javax.swing.JPanel();
        sidebarBrand = new javax.swing.JLabel();
        navigationPanel = new javax.swing.JPanel();
        workspaceLabel = new javax.swing.JLabel();
        navigationLabel = new javax.swing.JLabel();
        navigationHint = new javax.swing.JLabel();
        sidebarBottom = new javax.swing.JPanel();
        connectedLabel = new javax.swing.JLabel();
        endpoint = new javax.swing.JLabel();
        loansButton = new javax.swing.JButton();
        disconnect = new javax.swing.JButton();
        signedInLabel = new javax.swing.JLabel();
        dashboard = new javax.swing.JPanel();
        dashboardTop = new javax.swing.JPanel();
        heading = new javax.swing.JPanel();
        headingWords = new javax.swing.JPanel();
        catalogBreadcrumb = new javax.swing.JLabel();
        catalogHeading = new javax.swing.JLabel();
        catalogSubtitle = new javax.swing.JLabel();
        headingActions = new javax.swing.JPanel();
        borrow = new javax.swing.JButton();
        refresh = new javax.swing.JButton();
        clear = new javax.swing.JButton();
        metrics = new javax.swing.JPanel();
        titlesMetric = new javax.swing.JPanel();
        titlesMetricCaption = new javax.swing.JLabel();
        count = new javax.swing.JLabel();
        titlesMetricHint = new javax.swing.JLabel();
        copiesMetric = new javax.swing.JPanel();
        copiesMetricCaption = new javax.swing.JLabel();
        copies = new javax.swing.JLabel();
        copiesMetricHint = new javax.swing.JLabel();
        lowStockMetric = new javax.swing.JPanel();
        lowStockMetricCaption = new javax.swing.JLabel();
        lowStock = new javax.swing.JLabel();
        lowStockMetricHint = new javax.swing.JLabel();
        dashboardBody = new javax.swing.JPanel();
        catalogPanel = new javax.swing.JPanel();
        catalogNorth = new javax.swing.JPanel();
        catalogTitleRow = new javax.swing.JPanel();
        catalogTitle = new javax.swing.JLabel();
        resultCount = new javax.swing.JLabel();
        searchRow = new javax.swing.JPanel();
        search = new javax.swing.JTextField();
        find = new javax.swing.JButton();
        searchHint = new javax.swing.JLabel();
        tableScroll = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        editorScroll = new javax.swing.JScrollPane();
        editorPanel = new javax.swing.JPanel();
        editorFields = new javax.swing.JPanel();
        editorTitle = new javax.swing.JLabel();
        formState = new javax.swing.JLabel();
        codeGroup = new javax.swing.JPanel();
        codeGroupLabel = new javax.swing.JLabel();
        code = new javax.swing.JTextField();
        titleGroup = new javax.swing.JPanel();
        titleGroupLabel = new javax.swing.JLabel();
        title = new javax.swing.JTextField();
        authorGroup = new javax.swing.JPanel();
        authorGroupLabel = new javax.swing.JLabel();
        author = new javax.swing.JTextField();
        numberFields = new javax.swing.JPanel();
        yearGroup = new javax.swing.JPanel();
        yearGroupLabel = new javax.swing.JLabel();
        year = new javax.swing.JTextField();
        quantityGroup = new javax.swing.JPanel();
        quantityGroupLabel = new javax.swing.JLabel();
        quantity = new javax.swing.JTextField();
        editButtons = new javax.swing.JPanel();
        add = new javax.swing.JButton();
        update = new javax.swing.JButton();
        delete = new javax.swing.JButton();
        footer = new javax.swing.JPanel();
        noticeLabel = new javax.swing.JLabel();
        status = new javax.swing.JLabel();
        syncHint = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Thư viện • Client");
        setMinimumSize(new java.awt.Dimension(1060, 700));
        setPreferredSize(new java.awt.Dimension(1240, 820));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        root.setBackground(new java.awt.Color(245, 247, 248));
        root.setLayout(new java.awt.CardLayout());

        connectionPage.setBackground(new java.awt.Color(255, 255, 255));
        connectionPage.setLayout(new java.awt.GridLayout(1, 2));

        introPanel.setBackground(new java.awt.Color(23, 43, 57));
        introPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(42, 48, 42, 48));
        introPanel.setLayout(new java.awt.BorderLayout(0, 18));

        brandLabel.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        brandLabel.setForeground(new java.awt.Color(255, 255, 255));
        brandLabel.setText("MÁY KHÁCH");
        introPanel.add(brandLabel, java.awt.BorderLayout.NORTH);

        introStory.setBackground(new java.awt.Color(23, 43, 57));
        introStory.setLayout(new java.awt.GridBagLayout());

        introHeadline.setFont(new java.awt.Font("Segoe UI", 1, 34)); // NOI18N
        introHeadline.setForeground(new java.awt.Color(255, 255, 255));
        introHeadline.setText("<html>Hệ thống quản lý thư viện qua mạng.</html>");
        introHeadline.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 26, 0);
        introStory.add(introHeadline, gridBagConstraints);

        introDescription.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        introDescription.setForeground(new java.awt.Color(183, 207, 211));
        introDescription.setText("<html>Quản lý danh mục sách tập trung.<br>Tra cứu, cập nhật và làm việc cùng nhau.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 32, 0);
        introStory.add(introDescription, gridBagConstraints);

        introPanel.add(introStory, java.awt.BorderLayout.CENTER);

        connectionPage.add(introPanel);

        connectionRight.setBackground(new java.awt.Color(255, 255, 255));
        connectionRight.setBorder(javax.swing.BorderFactory.createEmptyBorder(36, 48, 36, 48));
        connectionRight.setLayout(new java.awt.GridBagLayout());

        connectionForm.setBackground(new java.awt.Color(255, 255, 255));
        connectionForm.setPreferredSize(new java.awt.Dimension(390, 540));
        connectionForm.setLayout(new java.awt.GridBagLayout());

        connectionKicker.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        connectionKicker.setForeground(new java.awt.Color(13, 119, 110));
        connectionKicker.setText("BẮT ĐẦU PHIÊN LÀM VIỆC");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        connectionForm.add(connectionKicker, gridBagConstraints);

        connectionTitle.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        connectionTitle.setForeground(new java.awt.Color(23, 43, 57));
        connectionTitle.setText("Kết nối máy chủ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        connectionForm.add(connectionTitle, gridBagConstraints);

        connectionDescription.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        connectionDescription.setForeground(new java.awt.Color(106, 122, 132));
        connectionDescription.setText("Nhập địa chỉ máy chủ thư viện để tiếp tục.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 18, 0);
        connectionForm.add(connectionDescription, gridBagConstraints);

        hostLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        hostLabel.setForeground(new java.awt.Color(23, 43, 57));
        hostLabel.setText("Địa chỉ IP / tên máy chủ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 7, 0);
        connectionForm.add(hostLabel, gridBagConstraints);

        host.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        host.setForeground(new java.awt.Color(23, 43, 57));
        host.setText("127.0.0.1");
        host.setPreferredSize(new java.awt.Dimension(150, 34));
        host.setName("hostField"); // NOI18N
        host.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hostActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        connectionForm.add(host, gridBagConstraints);

        portLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        portLabel.setForeground(new java.awt.Color(23, 43, 57));
        portLabel.setText("Cổng TCP");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 7, 0);
        connectionForm.add(portLabel, gridBagConstraints);

        port.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        port.setForeground(new java.awt.Color(23, 43, 57));
        port.setText("9999");
        port.setPreferredSize(new java.awt.Dimension(150, 34));
        port.setName("portField"); // NOI18N
        port.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                portActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        connectionForm.add(port, gridBagConstraints);

        usernameLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        usernameLabel.setForeground(new java.awt.Color(23, 43, 57));
        usernameLabel.setText("Tên tài khoản");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        connectionForm.add(usernameLabel, gridBagConstraints);

        username.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        username.setForeground(new java.awt.Color(23, 43, 57));
        username.setPreferredSize(new java.awt.Dimension(150, 34));
        username.setName("usernameField"); // NOI18N
        username.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usernameActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        connectionForm.add(username, gridBagConstraints);

        passwordLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        passwordLabel.setForeground(new java.awt.Color(23, 43, 57));
        passwordLabel.setText("Mật khẩu");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        connectionForm.add(passwordLabel, gridBagConstraints);

        password.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        password.setForeground(new java.awt.Color(23, 43, 57));
        password.setPreferredSize(new java.awt.Dimension(150, 34));
        password.setName("passwordField"); // NOI18N
        password.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passwordActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 18, 0);
        connectionForm.add(password, gridBagConstraints);

        connect.setBackground(new java.awt.Color(13, 119, 110));
        connect.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        connect.setForeground(new java.awt.Color(255, 255, 255));
        connect.setText("Kết nối máy chủ  →");
        connect.setFocusPainted(false);
        connect.setName("connectButton"); // NOI18N
        connect.setPreferredSize(new java.awt.Dimension(130, 36));
        connect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        connectionForm.add(connect, gridBagConstraints);

        connectMessage.setForeground(new java.awt.Color(174, 62, 67));
        connectMessage.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        connectionForm.add(connectMessage, gridBagConstraints);

        connectionHint.setForeground(new java.awt.Color(106, 122, 132));
        connectionHint.setText("<html>Chạy Server trước khi kết nối.<br>Cùng máy: 127.0.0.1 · Cổng mặc định: 9999<br>Tài khoản mẫu: admin / admin123<br>Bạn đọc mẫu: bandoc / bandoc123</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        connectionForm.add(connectionHint, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        connectionRight.add(connectionForm, gridBagConstraints);

        connectionPage.add(connectionRight);

        root.add(connectionPage, "connect");

        managementPage.setBackground(new java.awt.Color(245, 247, 248));
        managementPage.setLayout(new java.awt.BorderLayout());

        sidebar.setBackground(new java.awt.Color(23, 43, 57));
        sidebar.setBorder(javax.swing.BorderFactory.createEmptyBorder(28, 18, 22, 18));
        sidebar.setPreferredSize(new java.awt.Dimension(200, 500));
        sidebar.setRequestFocusEnabled(false);
        sidebar.setLayout(new java.awt.BorderLayout());

        sidebarTop.setBackground(new java.awt.Color(23, 43, 57));
        sidebarTop.setLayout(new java.awt.BorderLayout(0, 32));

        sidebarBrand.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        sidebarBrand.setForeground(new java.awt.Color(255, 255, 255));
        sidebarBrand.setText("Thư viện");
        sidebarTop.add(sidebarBrand, java.awt.BorderLayout.NORTH);

        navigationPanel.setBackground(new java.awt.Color(23, 43, 57));
        navigationPanel.setLayout(new java.awt.GridBagLayout());

        workspaceLabel.setFont(new java.awt.Font("Segoe UI", 1, 10)); // NOI18N
        workspaceLabel.setForeground(new java.awt.Color(144, 168, 180));
        workspaceLabel.setText("KHÔNG GIAN LÀM VIỆC");
        workspaceLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 18, 0);
        navigationPanel.add(workspaceLabel, gridBagConstraints);

        navigationLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        navigationLabel.setForeground(new java.awt.Color(116, 214, 188));
        navigationLabel.setText("Danh mục sách");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 20, 0);
        navigationPanel.add(navigationLabel, gridBagConstraints);

        navigationHint.setForeground(new java.awt.Color(155, 179, 190));
        navigationHint.setText("<html>Thêm, tra cứu và cập nhật<br>dữ liệu sách trên máy chủ.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        navigationPanel.add(navigationHint, gridBagConstraints);

        sidebarTop.add(navigationPanel, java.awt.BorderLayout.CENTER);

        sidebar.add(sidebarTop, java.awt.BorderLayout.NORTH);

        sidebarBottom.setBackground(new java.awt.Color(23, 43, 57));
        sidebarBottom.setLayout(new java.awt.GridLayout(3, 1, 0, 10));

        connectedLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        connectedLabel.setForeground(new java.awt.Color(116, 214, 188));
        connectedLabel.setText("●  Đang kết nối");
        sidebarBottom.add(connectedLabel);

        endpoint.setForeground(new java.awt.Color(183, 207, 211));
        endpoint.setText("127.0.0.1:9999");
        sidebarBottom.add(endpoint);

        loansButton.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        loansButton.setForeground(new java.awt.Color(23, 43, 57));
        loansButton.setText("Phiếu mượn của tôi");
        loansButton.setFocusPainted(false);
        loansButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        loansButton.setName("loansButton"); // NOI18N
        loansButton.setPreferredSize(new java.awt.Dimension(130, 36));
        loansButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loansButtonActionPerformed(evt);
            }
        });
        sidebarBottom.add(loansButton);

        disconnect.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        disconnect.setForeground(new java.awt.Color(23, 43, 57));
        disconnect.setText("Ngắt kết nối");
        disconnect.setFocusPainted(false);
        disconnect.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        disconnect.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        disconnect.setPreferredSize(new java.awt.Dimension(130, 36));
        disconnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disconnectActionPerformed(evt);
            }
        });
        sidebarBottom.add(disconnect);

        sidebar.add(sidebarBottom, java.awt.BorderLayout.SOUTH);

        signedInLabel.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        signedInLabel.setForeground(new java.awt.Color(183, 207, 211));
        signedInLabel.setText("Tài khoản");
        signedInLabel.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        sidebar.add(signedInLabel, java.awt.BorderLayout.CENTER);

        managementPage.add(sidebar, java.awt.BorderLayout.WEST);

        dashboard.setBackground(new java.awt.Color(245, 247, 248));
        dashboard.setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 24, 16, 24));
        dashboard.setLayout(new java.awt.BorderLayout(0, 18));

        dashboardTop.setBackground(new java.awt.Color(245, 247, 248));
        dashboardTop.setLayout(new java.awt.BorderLayout(0, 18));

        heading.setBackground(new java.awt.Color(245, 247, 248));
        heading.setLayout(new java.awt.BorderLayout(8, 0));

        headingWords.setBackground(new java.awt.Color(245, 247, 248));
        headingWords.setLayout(new java.awt.GridLayout(3, 1, 0, 4));

        catalogBreadcrumb.setFont(new java.awt.Font("Segoe UI", 1, 10)); // NOI18N
        catalogBreadcrumb.setForeground(new java.awt.Color(106, 122, 132));
        catalogBreadcrumb.setText("THƯ VIỆN / DANH MỤC");
        headingWords.add(catalogBreadcrumb);

        catalogHeading.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        catalogHeading.setForeground(new java.awt.Color(23, 43, 57));
        catalogHeading.setText("Quản lý sách");
        headingWords.add(catalogHeading);

        catalogSubtitle.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        catalogSubtitle.setForeground(new java.awt.Color(106, 122, 132));
        catalogSubtitle.setText("Mỗi cuốn sách, một thế giới để khám phá.");
        headingWords.add(catalogSubtitle);

        heading.add(headingWords, java.awt.BorderLayout.CENTER);

        headingActions.setBackground(new java.awt.Color(245, 247, 248));
        headingActions.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 14));

        borrow.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        borrow.setForeground(new java.awt.Color(255, 255, 255));
        borrow.setBackground(new java.awt.Color(13, 119, 110));
        borrow.setText("Mượn sách");
        borrow.setName("borrowBookButton"); // NOI18N
        borrow.setFocusPainted(false);
        borrow.setPreferredSize(new java.awt.Dimension(124, 36));
        borrow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                borrowActionPerformed(evt);
            }
        });
        headingActions.add(borrow);

        refresh.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        refresh.setForeground(new java.awt.Color(23, 43, 57));
        refresh.setText("Làm mới");
        refresh.setFocusPainted(false);
        refresh.setPreferredSize(new java.awt.Dimension(130, 36));
        refresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshActionPerformed(evt);
            }
        });
        headingActions.add(refresh);

        clear.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        clear.setForeground(new java.awt.Color(23, 43, 57));
        clear.setText("Thêm sách mới");
        clear.setFocusPainted(false);
        clear.setPreferredSize(new java.awt.Dimension(130, 36));
        clear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearActionPerformed(evt);
            }
        });
        headingActions.add(clear);

        heading.add(headingActions, java.awt.BorderLayout.EAST);

        dashboardTop.add(heading, java.awt.BorderLayout.NORTH);

        metrics.setBackground(new java.awt.Color(245, 247, 248));
        metrics.setLayout(new java.awt.GridLayout(1, 3, 14, 0));

        titlesMetric.setBackground(new java.awt.Color(255, 255, 255));
        titlesMetric.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 18, 15, 18));
        titlesMetric.setPreferredSize(new java.awt.Dimension(180, 103));
        titlesMetric.setLayout(new java.awt.BorderLayout(0, 3));

        titlesMetricCaption.setFont(new java.awt.Font("Segoe UI", 1, 10)); // NOI18N
        titlesMetricCaption.setForeground(new java.awt.Color(106, 122, 132));
        titlesMetricCaption.setText("ĐẦU SÁCH");
        titlesMetric.add(titlesMetricCaption, java.awt.BorderLayout.NORTH);

        count.setFont(new java.awt.Font("Segoe UI", 1, 29)); // NOI18N
        count.setForeground(new java.awt.Color(23, 43, 57));
        count.setText("0");
        titlesMetric.add(count, java.awt.BorderLayout.CENTER);

        titlesMetricHint.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        titlesMetricHint.setForeground(new java.awt.Color(106, 122, 132));
        titlesMetricHint.setText("Trong danh sách hiện tại");
        titlesMetric.add(titlesMetricHint, java.awt.BorderLayout.SOUTH);

        metrics.add(titlesMetric);

        copiesMetric.setBackground(new java.awt.Color(255, 255, 255));
        copiesMetric.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 18, 15, 18));
        copiesMetric.setPreferredSize(new java.awt.Dimension(180, 103));
        copiesMetric.setLayout(new java.awt.BorderLayout(0, 3));

        copiesMetricCaption.setFont(new java.awt.Font("Segoe UI", 1, 10)); // NOI18N
        copiesMetricCaption.setForeground(new java.awt.Color(106, 122, 132));
        copiesMetricCaption.setText("TỔNG SỐ CUỐN");
        copiesMetric.add(copiesMetricCaption, java.awt.BorderLayout.NORTH);

        copies.setFont(new java.awt.Font("Segoe UI", 1, 29)); // NOI18N
        copies.setForeground(new java.awt.Color(23, 43, 57));
        copies.setText("0");
        copiesMetric.add(copies, java.awt.BorderLayout.CENTER);

        copiesMetricHint.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        copiesMetricHint.setForeground(new java.awt.Color(106, 122, 132));
        copiesMetricHint.setText("Số lượng trong danh sách");
        copiesMetric.add(copiesMetricHint, java.awt.BorderLayout.SOUTH);

        metrics.add(copiesMetric);

        lowStockMetric.setBackground(new java.awt.Color(255, 255, 255));
        lowStockMetric.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 18, 15, 18));
        lowStockMetric.setPreferredSize(new java.awt.Dimension(180, 103));
        lowStockMetric.setLayout(new java.awt.BorderLayout(0, 3));

        lowStockMetricCaption.setFont(new java.awt.Font("Segoe UI", 1, 10)); // NOI18N
        lowStockMetricCaption.setForeground(new java.awt.Color(106, 122, 132));
        lowStockMetricCaption.setText("SẮP HẾT SÁCH");
        lowStockMetric.add(lowStockMetricCaption, java.awt.BorderLayout.NORTH);

        lowStock.setFont(new java.awt.Font("Segoe UI", 1, 29)); // NOI18N
        lowStock.setForeground(new java.awt.Color(23, 43, 57));
        lowStock.setText("0");
        lowStockMetric.add(lowStock, java.awt.BorderLayout.CENTER);

        lowStockMetricHint.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lowStockMetricHint.setForeground(new java.awt.Color(106, 122, 132));
        lowStockMetricHint.setText("Đầu sách có số lượng ≤ 3");
        lowStockMetric.add(lowStockMetricHint, java.awt.BorderLayout.SOUTH);

        metrics.add(lowStockMetric);

        dashboardTop.add(metrics, java.awt.BorderLayout.CENTER);

        dashboard.add(dashboardTop, java.awt.BorderLayout.NORTH);

        dashboardBody.setBackground(new java.awt.Color(245, 247, 248));
        dashboardBody.setLayout(new java.awt.BorderLayout(18, 0));

        catalogPanel.setBackground(new java.awt.Color(255, 255, 255));
        catalogPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 10, 16));
        catalogPanel.setLayout(new java.awt.BorderLayout(0, 14));

        catalogNorth.setBackground(new java.awt.Color(255, 255, 255));
        catalogNorth.setLayout(new java.awt.BorderLayout(0, 12));

        catalogTitleRow.setBackground(new java.awt.Color(255, 255, 255));
        catalogTitleRow.setLayout(new java.awt.BorderLayout());

        catalogTitle.setFont(new java.awt.Font("Segoe UI", 1, 17)); // NOI18N
        catalogTitle.setForeground(new java.awt.Color(23, 43, 57));
        catalogTitle.setText("Danh mục sách");
        catalogTitleRow.add(catalogTitle, java.awt.BorderLayout.WEST);

        resultCount.setForeground(new java.awt.Color(106, 122, 132));
        resultCount.setText("0 đầu sách");
        catalogTitleRow.add(resultCount, java.awt.BorderLayout.EAST);

        catalogNorth.add(catalogTitleRow, java.awt.BorderLayout.NORTH);

        searchRow.setBackground(new java.awt.Color(255, 255, 255));
        searchRow.setLayout(new java.awt.BorderLayout(8, 0));

        search.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        search.setForeground(new java.awt.Color(23, 43, 57));
        search.setPreferredSize(new java.awt.Dimension(150, 34));
        search.setToolTipText("Nhập mã hoặc tên sách; để trống để hiển thị tất cả");
        search.setName("searchField"); // NOI18N
        search.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchActionPerformed(evt);
            }
        });
        searchRow.add(search, java.awt.BorderLayout.CENTER);

        find.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        find.setForeground(new java.awt.Color(255, 255, 255));
        find.setBackground(new java.awt.Color(13, 119, 110));
        find.setText("Tìm kiếm");
        find.setFocusPainted(false);
        find.setPreferredSize(new java.awt.Dimension(100, 36));
        find.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findActionPerformed(evt);
            }
        });
        searchRow.add(find, java.awt.BorderLayout.EAST);

        catalogNorth.add(searchRow, java.awt.BorderLayout.CENTER);

        searchHint.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        searchHint.setForeground(new java.awt.Color(106, 122, 132));
        searchHint.setText("Tìm theo mã hoặc tên sách · Chọn một dòng để chỉnh sửa");
        catalogNorth.add(searchHint, java.awt.BorderLayout.SOUTH);

        catalogPanel.add(catalogNorth, java.awt.BorderLayout.NORTH);

        tableScroll.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        table.setName("booksTable"); // NOI18N
        table.setForeground(new java.awt.Color(23, 43, 57));
        table.setRowHeight(44);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Mã sách", "Tên sách", "Tác giả", "Năm XB", "Số lượng"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableMouseClicked(evt);
            }
        });
        tableScroll.setViewportView(table);

        catalogPanel.add(tableScroll, java.awt.BorderLayout.CENTER);

        dashboardBody.add(catalogPanel, java.awt.BorderLayout.CENTER);

        editorScroll.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        editorScroll.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        editorScroll.setPreferredSize(new java.awt.Dimension(340, 420));

        editorPanel.setBackground(new java.awt.Color(255, 255, 255));
        editorPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 16, 14, 16));
        editorPanel.setLayout(new java.awt.BorderLayout());

        editorFields.setBackground(new java.awt.Color(255, 255, 255));
        editorFields.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        editorFields.setLayout(new java.awt.GridBagLayout());

        editorTitle.setFont(new java.awt.Font("Segoe UI", 1, 17)); // NOI18N
        editorTitle.setForeground(new java.awt.Color(23, 43, 57));
        editorTitle.setText("Thông tin sách");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        editorFields.add(editorTitle, gridBagConstraints);

        formState.setForeground(new java.awt.Color(106, 122, 132));
        formState.setText("Tạo bản ghi mới");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        editorFields.add(formState, gridBagConstraints);

        codeGroup.setBackground(new java.awt.Color(255, 255, 255));
        codeGroup.setLayout(new java.awt.BorderLayout(0, 4));

        codeGroupLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        codeGroupLabel.setForeground(new java.awt.Color(23, 43, 57));
        codeGroupLabel.setText("Mã sách *");
        codeGroup.add(codeGroupLabel, java.awt.BorderLayout.NORTH);

        code.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        code.setForeground(new java.awt.Color(23, 43, 57));
        code.setToolTipText("Thông tin bắt buộc. Mã sách tối đa 10 ký tự.");
        code.setName("bookCodeField"); // NOI18N
        code.setPreferredSize(new java.awt.Dimension(150, 34));
        code.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                codeActionPerformed(evt);
            }
        });
        codeGroup.add(code, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        editorFields.add(codeGroup, gridBagConstraints);

        titleGroup.setBackground(new java.awt.Color(255, 255, 255));
        titleGroup.setLayout(new java.awt.BorderLayout(0, 4));

        titleGroupLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        titleGroupLabel.setForeground(new java.awt.Color(23, 43, 57));
        titleGroupLabel.setText("Tên sách *");
        titleGroup.add(titleGroupLabel, java.awt.BorderLayout.NORTH);

        title.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        title.setForeground(new java.awt.Color(23, 43, 57));
        title.setName("bookTitleField"); // NOI18N
        title.setPreferredSize(new java.awt.Dimension(150, 34));
        titleGroup.add(title, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        editorFields.add(titleGroup, gridBagConstraints);

        authorGroup.setBackground(new java.awt.Color(255, 255, 255));
        authorGroup.setLayout(new java.awt.BorderLayout(0, 4));

        authorGroupLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        authorGroupLabel.setForeground(new java.awt.Color(23, 43, 57));
        authorGroupLabel.setText("Tác giả *");
        authorGroup.add(authorGroupLabel, java.awt.BorderLayout.NORTH);

        author.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        author.setForeground(new java.awt.Color(23, 43, 57));
        author.setPreferredSize(new java.awt.Dimension(150, 34));
        author.setName("bookAuthorField"); // NOI18N
        authorGroup.add(author, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        editorFields.add(authorGroup, gridBagConstraints);

        numberFields.setBackground(new java.awt.Color(255, 255, 255));
        numberFields.setLayout(new java.awt.GridLayout(1, 2, 10, 0));

        yearGroup.setBackground(new java.awt.Color(255, 255, 255));
        yearGroup.setLayout(new java.awt.BorderLayout(0, 4));

        yearGroupLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        yearGroupLabel.setForeground(new java.awt.Color(23, 43, 57));
        yearGroupLabel.setText("Năm xuất bản *");
        yearGroup.add(yearGroupLabel, java.awt.BorderLayout.NORTH);

        year.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        year.setForeground(new java.awt.Color(23, 43, 57));
        year.setText("2026");
        year.setPreferredSize(new java.awt.Dimension(120, 34));
        year.setName("bookYearField"); // NOI18N
        yearGroup.add(year, java.awt.BorderLayout.CENTER);

        numberFields.add(yearGroup);

        quantityGroup.setBackground(new java.awt.Color(255, 255, 255));
        quantityGroup.setLayout(new java.awt.BorderLayout(0, 4));

        quantityGroupLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        quantityGroupLabel.setForeground(new java.awt.Color(23, 43, 57));
        quantityGroupLabel.setText("Số lượng *");
        quantityGroup.add(quantityGroupLabel, java.awt.BorderLayout.NORTH);

        quantity.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        quantity.setForeground(new java.awt.Color(23, 43, 57));
        quantity.setText("0");
        quantity.setPreferredSize(new java.awt.Dimension(120, 34));
        quantity.setName("bookQuantityField"); // NOI18N
        quantityGroup.add(quantity, java.awt.BorderLayout.CENTER);

        numberFields.add(quantityGroup);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        editorFields.add(numberFields, gridBagConstraints);

        editButtons.setBackground(new java.awt.Color(255, 255, 255));
        editButtons.setLayout(new java.awt.GridLayout(1, 2, 8, 0));

        add.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        add.setForeground(new java.awt.Color(255, 255, 255));
        add.setBackground(new java.awt.Color(13, 119, 110));
        add.setText("Thêm sách");
        add.setFocusPainted(false);
        add.setPreferredSize(new java.awt.Dimension(130, 36));
        add.setName("addBookButton"); // NOI18N
        add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addActionPerformed(evt);
            }
        });
        editButtons.add(add);

        update.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        update.setForeground(new java.awt.Color(23, 43, 57));
        update.setText("Lưu thay đổi");
        update.setFocusPainted(false);
        update.setPreferredSize(new java.awt.Dimension(130, 36));
        update.setName("updateBookButton"); // NOI18N
        update.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateActionPerformed(evt);
            }
        });
        editButtons.add(update);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 7, 0);
        editorFields.add(editButtons, gridBagConstraints);

        delete.setBackground(new java.awt.Color(252, 237, 237));
        delete.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        delete.setForeground(new java.awt.Color(174, 62, 67));
        delete.setText("Xóa sách đã chọn");
        delete.setFocusPainted(false);
        delete.setName("deleteBookButton"); // NOI18N
        delete.setPreferredSize(new java.awt.Dimension(130, 36));
        delete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        editorFields.add(delete, gridBagConstraints);

        editorPanel.add(editorFields, java.awt.BorderLayout.NORTH);

        editorScroll.setViewportView(editorPanel);

        dashboardBody.add(editorScroll, java.awt.BorderLayout.EAST);

        dashboard.add(dashboardBody, java.awt.BorderLayout.CENTER);

        footer.setBackground(new java.awt.Color(245, 247, 248));
        footer.setLayout(new java.awt.BorderLayout());

        noticeLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        noticeLabel.setForeground(new java.awt.Color(13, 119, 110));
        noticeLabel.setText("Thông báo UDP · Chưa có thông báo");
        noticeLabel.setName("udpNoticeLabel"); // NOI18N
        footer.add(noticeLabel);

        status.setForeground(new java.awt.Color(106, 122, 132));
        status.setText("Sẵn sàng");
        footer.add(status, java.awt.BorderLayout.WEST);

        syncHint.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        syncHint.setForeground(new java.awt.Color(106, 122, 132));
        syncHint.setText("Đồng bộ tự động mỗi 8 giây");
        footer.add(syncHint, java.awt.BorderLayout.EAST);

        dashboard.add(footer, java.awt.BorderLayout.SOUTH);

        managementPage.add(dashboard, java.awt.BorderLayout.CENTER);

        root.add(managementPage, "manage");

        getContentPane().add(root, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void hostActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hostActionPerformed
        connect();
    }//GEN-LAST:event_hostActionPerformed

    private void portActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_portActionPerformed
        connect();
    }//GEN-LAST:event_portActionPerformed

    private void connectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectActionPerformed
        connect();
    }//GEN-LAST:event_connectActionPerformed

    private void usernameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usernameActionPerformed
        password.requestFocusInWindow();
    }//GEN-LAST:event_usernameActionPerformed

    private void passwordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passwordActionPerformed
        connect();
    }//GEN-LAST:event_passwordActionPerformed

    private void borrowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_borrowActionPerformed
        borrowSelected();
    }//GEN-LAST:event_borrowActionPerformed

    private void loansButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loansButtonActionPerformed
        showLoans();
    }//GEN-LAST:event_loansButtonActionPerformed

    private void disconnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disconnectActionPerformed
        disconnect();
    }//GEN-LAST:event_disconnectActionPerformed

    private void refreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshActionPerformed
        reload(activeQuery, false);
    }//GEN-LAST:event_refreshActionPerformed

    private void clearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearActionPerformed
        clearForm();
    }//GEN-LAST:event_clearActionPerformed

    private void findActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findActionPerformed
        reload(search.getText().strip(), false);
    }//GEN-LAST:event_findActionPerformed

    private void searchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchActionPerformed
        reload(search.getText().strip(), false);
    }//GEN-LAST:event_searchActionPerformed

    private void addActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addActionPerformed
        save(false);
    }//GEN-LAST:event_addActionPerformed

    private void updateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateActionPerformed
        save(true);
    }//GEN-LAST:event_updateActionPerformed

    private void deleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteActionPerformed
        delete();
    }//GEN-LAST:event_deleteActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeWindow();
    }//GEN-LAST:event_formWindowClosing

    private void tableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMouseClicked
        if (!busy && SwingUtilities.isLeftMouseButton(evt)) {
            int row = table.rowAtPoint(evt.getPoint());
            if (row >= 0) showBook(model.books.get(table.convertRowIndexToModel(row)));
        }
    }//GEN-LAST:event_tableMouseClicked

    private void codeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_codeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_codeActionPerformed

    private void configureTable() {
        JTable result = table;
        result.setModel(model);
        result.setRowHeight(49);
        result.setShowVerticalLines(false); result.setShowHorizontalLines(true);
        result.setIntercellSpacing(new Dimension(0, 1));
        result.setFont(Theme.font(12, Font.PLAIN)); result.setForeground(Theme.NAVY);
        result.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        result.setFillsViewportHeight(true);
        result.setAutoCreateRowSorter(true);
        result.getTableHeader().setReorderingAllowed(false);
        result.getTableHeader().setPreferredSize(new Dimension(0, 35));
        result.getTableHeader().setFont(Theme.font(11, Font.BOLD));
        int[] widths = {70, 210, 135, 55, 64};
        for (int i = 0; i < widths.length; i++) result.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        result.getColumnModel().getColumn(0).setMinWidth(63);
        result.getColumnModel().getColumn(3).setMaxWidth(65);
        result.getColumnModel().getColumn(4).setMaxWidth(75);
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
                super.getTableCellRendererComponent(table, value, selected, false, row, column);
                setBorder(new EmptyBorder(0, 8, 0, 6));
                setFont(Theme.font(12, column == 0 || column == 4 ? Font.BOLD : Font.PLAIN));
                setHorizontalAlignment(column >= 3 ? SwingConstants.CENTER : SwingConstants.LEFT);
                if (!selected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 252, 252));
                    setForeground(column == 4 && value instanceof Integer n && n <= 3 ? new Color(170, 113, 35) : column == 0 ? Theme.TEAL : Theme.NAVY);
                }
                setToolTipText(value == null ? "" : value.toString());
                return this;
            }
        };
        result.setDefaultRenderer(Object.class, renderer);
        result.setDefaultRenderer(Integer.class, renderer);

    }

    private void connect() {
        if (busy || connected) return;
        String address = host.getText().strip();
        int number;
        try {
            number = Integer.parseInt(port.getText().strip());
            if (number < 1 || number > 65535 || address.isBlank()) throw new IllegalArgumentException();
        } catch (IllegalArgumentException e) {
            connectMessage.setForeground(Theme.DANGER);
            connectMessage.setText("Nhập địa chỉ và cổng hợp lệ (1–65535).");
            return;
        }
        String account = username.getText().strip();
        char[] secret = password.getPassword();
        if (account.isEmpty() || secret.length == 0) {
            Arrays.fill(secret, '\0');
            connectMessage.setForeground(Theme.DANGER);
            connectMessage.setText("Nhập tên tài khoản và mật khẩu.");
            (account.isEmpty() ? username : password).requestFocusInWindow();
            return;
        }
        password.setText("");
        long attempt = ++loginAttempt;
        connectMessage.setForeground(Theme.MUTED);
        connectMessage.setText("Đang xác thực tài khoản…");
        execute(() -> {
            try {
                client.connect(address, number);
                Response login = client.login(account, new String(secret), notice -> receiveNotice(attempt, notice));
                if (!login.success()) client.close();
                return login;
            } finally {
                Arrays.fill(secret, '\0');
            }
        }, response -> {
            connected = true;
            applyRole(response.role());
            signedInUsername = response.username();
            signedInLabel.setText((isAdmin ? "THỦ THƯ · " : "BẠN ĐỌC · ") + signedInUsername);
            signedInLabel.setToolTipText(signedInUsername);
            endpoint.setText(address + ":" + number);
            endpoint.setToolTipText(endpoint.getText());
            setTitle("Thư viện • " + signedInUsername + " • " + address + ":" + number);
            activeQuery = ""; search.setText(""); clearForm();
            showBooks(List.of());
            resetNotice();
            screens.show(root, "manage");
            getRootPane().setDefaultButton(find);
            reload("", false);
        }, false);
    }

    private void disconnect() {
        if (busy) return;
        client.close(); connected = false; selected = null;
        isAdmin = false;
        editorScroll.setVisible(false);
        clear.setVisible(false);
        ++loginAttempt;
        signedInUsername = null;
        resetNotice();
        screens.show(root, "connect"); getRootPane().setDefaultButton(connect);
        connectMessage.setForeground(Theme.MUTED);
        connectMessage.setText("Đã đăng xuất. Bạn có thể đăng nhập lại.");
        password.requestFocusInWindow();
        setTitle("Thư viện • Client"); updateButtons();
        dashboardBody.revalidate(); dashboardBody.repaint();
    }

    private void receiveNotice(long attempt, LibraryNotice notice) {
        SwingUtilities.invokeLater(() -> {
            if (!disposed && connected && loginAttempt == attempt) {
                String text = "UDP · " + now() + " · " + notice.message();
                noticeLabel.setText(text);
                noticeLabel.setToolTipText(text);
            }
        });
    }

    private void resetNotice() {
        noticeLabel.setText("Thông báo UDP · Chưa có thông báo");
        noticeLabel.setToolTipText(null);
    }

    private void applyRole(String role) {
        // Unknown or missing roles never gain access to administration controls.
        isAdmin = role != null && "ADMIN".equalsIgnoreCase(role.strip());
        editorScroll.setVisible(isAdmin);
        clear.setVisible(isAdmin);
        navigationLabel.setText(isAdmin ? "Quản lý sách" : "Danh mục sách");
        dashboardBody.revalidate();
        dashboardBody.repaint();
        headingActions.revalidate();
        headingActions.repaint();
    }

    private void borrowSelected() {
        if (!connected || busy || table.getSelectedRow() < 0) return;
        Book book = model.books.get(table.convertRowIndexToModel(table.getSelectedRow()));
        execute(() -> client.borrowBook(book.code()), response -> {
            message(response.message(), "Mượn sách", JOptionPane.INFORMATION_MESSAGE);
            reload(activeQuery, true);
        }, false);
    }

    private void showLoans() {
        if (!connected || busy || loansDialog != null) return;
        LoansDialog dialog = new LoansDialog(this, signedInUsername, this::reloadLoans, this::returnLoan);
        loansDialog = dialog;
        reloadLoans();
        try {
            dialog.setVisible(true);
        } finally {
            dialog.dispose();
            loansDialog = null;
            reload(activeQuery, true);
        }
    }

    private void reloadLoans() {
        LoansDialog dialog = loansDialog;
        if (!connected || busy || dialog == null) return;
        execute(client::listLoans, response -> dialog.showLoans(response.loans(), null), false);
    }

    private void returnLoan(Loan loan) {
        LoansDialog dialog = loansDialog;
        if (!connected || busy || dialog == null) return;
        execute(() -> client.returnBook(loan.id()), response -> {
            dialog.setMessage(response.message());
            execute(client::listLoans, updated -> dialog.showLoans(updated.loans(), response.message()), false);
        }, false);
    }

    private void reload(String query, boolean silent) {
        if (!connected || busy) return;
        execute(() -> query.isBlank() ? client.listBooks() : client.searchBooks(query), response -> {
            activeQuery = query;
            showBooks(response.books());
            setStatus((query.isBlank() ? "Đã tải danh mục" : "Tìm thấy " + model.getRowCount() + " đầu sách") + " · " + now(), false);
        }, silent);
    }

    private void showBooks(List<Book> books) {
        adjustingSelection = true;
        try {
            model.setBooks(books == null ? List.of() : books);
            if (selected != null) {
                for (int i = 0; i < model.books.size(); i++) if (model.books.get(i).code().equals(selected.code())) {
                    int view = table.convertRowIndexToView(i);
                    if (view >= 0) table.setRowSelectionInterval(view, view);
                    break;
                }
            }
        } finally { adjustingSelection = false; }
        count.setText(Integer.toString(model.getRowCount()));
        copies.setText(Long.toString(model.books.stream().mapToLong(Book::quantity).sum()));
        lowStock.setText(Long.toString(model.books.stream().filter(book -> book.quantity() <= 3).count()));
        resultCount.setText(model.getRowCount() + " đầu sách");
    }

    private void showBook(Book book) {
        selected = book;
        code.setText(book.code()); title.setText(book.title()); author.setText(book.author());
        year.setText(Integer.toString(book.year())); quantity.setText(Integer.toString(book.quantity()));
        code.setEditable(false);
        formState.setText("Đang chỉnh sửa · " + book.code()); formState.setForeground(Theme.TEAL);
        updateButtons();
    }

    private void clearForm() {
        if (busy) return;
        selected = null; table.clearSelection();
        code.setText(""); title.setText(""); author.setText("");
        year.setText(Integer.toString(LocalDate.now().getYear())); quantity.setText("0"); code.setEditable(true);
        formState.setText("Tạo bản ghi mới"); formState.setForeground(Theme.MUTED);
        updateButtons(); code.requestFocusInWindow();
    }

    private Book readForm() {
        String id = code.getText().strip(), name = title.getText().strip(), writer = author.getText().strip();
        if (id.isEmpty() || id.length() > 10) throw new IllegalArgumentException("Mã sách phải có từ 1 đến 10 ký tự.");
        if (name.isEmpty() || name.length() > 100) throw new IllegalArgumentException("Tên sách phải có từ 1 đến 100 ký tự.");
        if (writer.isEmpty() || writer.length() > 100) throw new IllegalArgumentException("Tác giả phải có từ 1 đến 100 ký tự.");
        int published, stock;
        try { published = Integer.parseInt(year.getText().strip()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Năm xuất bản phải là số nguyên."); }
        if (published < 1 || published > LocalDate.now().getYear() + 1) throw new IllegalArgumentException("Năm xuất bản phải từ 1 đến " + (LocalDate.now().getYear() + 1) + ".");
        try { stock = Integer.parseInt(quantity.getText().strip()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Số lượng phải là số nguyên không âm."); }
        if (stock < 0) throw new IllegalArgumentException("Số lượng không được âm.");
        return new Book(id, name, writer, published, stock, selected == null ? 0 : selected.version());
    }

    private void save(boolean editing) {
        if (!connected || busy || (editing && selected == null)) return;
        Book book;
        try { book = readForm(); }
        catch (IllegalArgumentException e) { message(e.getMessage(), "Kiểm tra thông tin", JOptionPane.WARNING_MESSAGE); return; }
        execute(() -> editing ? client.updateBook(book) : client.addBook(book), response -> {
            clearForm();
            setStatus(response.message(), false);
            reload(activeQuery, false);
        }, false);
    }

    private void delete() {
        if (!connected || busy || selected == null) return;
        Book book = selected;
        int answer = JOptionPane.showConfirmDialog(this, "Xóa sách “" + book.title() + "” (" + book.code() + ")?\nThao tác này sẽ xóa sách khỏi danh mục.", "Xác nhận xóa sách", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) return;
        execute(() -> client.deleteBook(book), response -> {
            clearForm(); setStatus(response.message(), false); reload(activeQuery, false);
        }, false);
    }

    private void execute(Callable<Response> task, Consumer<Response> success, boolean silent) {
        if (busy || disposed) return;
        busy = true; passiveRefresh = silent; updateButtons();
        if (!silent && connected) setStatus("Đang trao đổi dữ liệu với máy chủ…", false);
        new SwingWorker<Response, Void>() {
            @Override protected Response doInBackground() throws Exception { return task.call(); }
            @Override protected void done() {
                if (disposed) { client.close(); return; }
                busy = false; passiveRefresh = false; updateButtons();
                try {
                    Response response = get();
                    if (response == null) throw new IOException("Máy chủ không trả về dữ liệu.");
                    if (!response.success()) {
                        setStatus(response.message(), true);
                        if (!connected) {
                            connectMessage.setForeground(Theme.DANGER);
                            connectMessage.setText(response.message());
                            password.requestFocusInWindow();
                        } else if (!silent) {
                            if (loansDialog != null) loansDialog.setMessage(response.message());
                            message(response.message(), "Không thể thực hiện", JOptionPane.WARNING_MESSAGE);
                        }
                        if (loansDialog == null && ("CONFLICT".equals(response.errorCode()) || "NOT_FOUND".equals(response.errorCode()))) {
                            // Preserve the user's edits until they explicitly choose a newly refreshed row.
                            formState.setText("Chọn lại dòng để tải dữ liệu mới");
                            formState.setForeground(Theme.DANGER);
                            reload(activeQuery, true);
                        }
                    } else success.accept(response);
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                catch (ExecutionException e) { connectionFailed(e.getCause()); }
                catch (Exception e) { connectionFailed(e); }
                updateButtons();
            }
        }.execute();
    }

    private void connectionFailed(Throwable error) {
        boolean wasConnected = connected;
        connected = false; client.close();
        isAdmin = false;
        editorScroll.setVisible(false);
        clear.setVisible(false);
        ++loginAttempt;
        signedInUsername = null;
        resetNotice();
        if (loansDialog != null) loansDialog.dispose();
        String detail = error.getMessage();
        String text = wasConnected ? "Mất kết nối đến máy chủ. Vui lòng kiểm tra và kết nối lại." : "Không thể kết nối. Kiểm tra IP, cổng và trạng thái Server.";
        connectMessage.setForeground(Theme.DANGER);
        connectMessage.setText("<html>" + text + "</html>");
        connectMessage.setToolTipText(detail);
        screens.show(root, "connect"); getRootPane().setDefaultButton(connect);
        setTitle("Thư viện • Client"); setStatus(text, true);
        if (wasConnected) message(text, "Mất kết nối", JOptionPane.WARNING_MESSAGE);
        updateButtons();
    }

    private void updateButtons() {
        connect.setEnabled(!busy); connect.setText(busy && !connected ? "Đang đăng nhập…" : "Đăng nhập");
        host.setEnabled(!busy); port.setEnabled(!busy);
        username.setEnabled(!busy); password.setEnabled(!busy);
        boolean ready = connected && !busy;
        boolean editable = connected && (!busy || passiveRefresh);
        disconnect.setEnabled(ready); find.setEnabled(ready); refresh.setEnabled(ready); clear.setEnabled(ready);
        clear.setVisible(connected && isAdmin);
        editorScroll.setVisible(connected && isAdmin);
        navigationLabel.setText(isAdmin ? "Quản lý sách" : "Danh mục sách");
        borrow.setEnabled(ready && table.getSelectedRow() >= 0);
        loansButton.setEnabled(ready);
        if (loansDialog != null) loansDialog.setBusy(!ready);
        search.setEnabled(editable); table.setEnabled(editable);
        add.setEnabled(ready && isAdmin && selected == null);
        update.setEnabled(ready && isAdmin && selected != null);
        delete.setEnabled(ready && isAdmin && selected != null);
        for (JTextField field : List.of(code, title, author, year, quantity)) field.setEnabled(editable);
        for (JTextField field : List.of(code, title, author, year, quantity)) field.setEditable(isAdmin);
        dashboardBody.revalidate();
        dashboardBody.repaint();
        setCursor(busy && !passiveRefresh ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }

    private void registerShortcuts() {
        register("control F", "search", () -> { if (connected && !busy) search.requestFocusInWindow(); });
        register("control N", "new", () -> { if (connected && !busy) clearForm(); });
        register("control R", "refresh", () -> { if (connected && !busy) reload(activeQuery, false); });
    }
    private void register(String key, String name, Runnable task) {
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), name);
        root.getActionMap().put(name, new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { task.run(); } });
    }
    private void setStatus(String text, boolean error) { status.setText(text); status.setForeground(error ? Theme.DANGER : Theme.MUTED); }
    private void message(String text, String title, int type) {
        Component parent = loansDialog != null && loansDialog.isShowing() ? loansDialog : this;
        JOptionPane.showMessageDialog(parent, text, title, type);
    }
    private static String now() { return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")); }
    private void closeWindow() {
        dispose();
    }
    @Override public void dispose() {
        if (!disposed) {
            disposed = true;
            if (heartbeat != null) heartbeat.stop();
            Thread cleanup = new Thread(client::close, "client-window-close"); cleanup.setDaemon(true); cleanup.start();
        }
        super.dispose();
    }

    private static final class BookTableModel extends AbstractTableModel {
        private final String[] columns = {"Mã sách", "Tên sách", "Tác giả", "Năm XB", "Số lượng"};
        private List<Book> books = new ArrayList<>();
        void setBooks(List<Book> value) { books = new ArrayList<>(value); fireTableDataChanged(); }
        @Override public int getRowCount() { return books.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int column) { return columns[column]; }
        @Override public Class<?> getColumnClass(int column) { return column >= 3 ? Integer.class : String.class; }
        @Override public Object getValueAt(int row, int column) {
            Book book = books.get(row);
            return switch (column) { case 0 -> book.code(); case 1 -> book.title(); case 2 -> book.author(); case 3 -> book.year(); default -> book.quantity(); };
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton add;
    private javax.swing.JTextField author;
    private javax.swing.JPanel authorGroup;
    private javax.swing.JLabel authorGroupLabel;
    private javax.swing.JButton borrow;
    private javax.swing.JLabel brandLabel;
    private javax.swing.JLabel catalogBreadcrumb;
    private javax.swing.JLabel catalogHeading;
    private javax.swing.JPanel catalogNorth;
    private javax.swing.JPanel catalogPanel;
    private javax.swing.JLabel catalogSubtitle;
    private javax.swing.JLabel catalogTitle;
    private javax.swing.JPanel catalogTitleRow;
    private javax.swing.JButton clear;
    private javax.swing.JTextField code;
    private javax.swing.JPanel codeGroup;
    private javax.swing.JLabel codeGroupLabel;
    private javax.swing.JButton connect;
    private javax.swing.JLabel connectMessage;
    private javax.swing.JLabel connectedLabel;
    private javax.swing.JLabel connectionDescription;
    private javax.swing.JPanel connectionForm;
    private javax.swing.JLabel connectionHint;
    private javax.swing.JLabel connectionKicker;
    private javax.swing.JPanel connectionPage;
    private javax.swing.JPanel connectionRight;
    private javax.swing.JLabel connectionTitle;
    private javax.swing.JLabel copies;
    private javax.swing.JPanel copiesMetric;
    private javax.swing.JLabel copiesMetricCaption;
    private javax.swing.JLabel copiesMetricHint;
    private javax.swing.JLabel count;
    private javax.swing.JPanel dashboard;
    private javax.swing.JPanel dashboardBody;
    private javax.swing.JPanel dashboardTop;
    private javax.swing.JButton delete;
    private javax.swing.JButton disconnect;
    private javax.swing.JPanel editButtons;
    private javax.swing.JPanel editorFields;
    private javax.swing.JPanel editorPanel;
    private javax.swing.JScrollPane editorScroll;
    private javax.swing.JLabel editorTitle;
    private javax.swing.JLabel endpoint;
    private javax.swing.JButton find;
    private javax.swing.JPanel footer;
    private javax.swing.JLabel formState;
    private javax.swing.JPanel heading;
    private javax.swing.JPanel headingActions;
    private javax.swing.JPanel headingWords;
    private javax.swing.JTextField host;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JLabel introDescription;
    private javax.swing.JLabel introHeadline;
    private javax.swing.JPanel introPanel;
    private javax.swing.JPanel introStory;
    private javax.swing.JButton loansButton;
    private javax.swing.JLabel lowStock;
    private javax.swing.JPanel lowStockMetric;
    private javax.swing.JLabel lowStockMetricCaption;
    private javax.swing.JLabel lowStockMetricHint;
    private javax.swing.JPanel managementPage;
    private javax.swing.JPanel metrics;
    private javax.swing.JLabel navigationHint;
    private javax.swing.JLabel navigationLabel;
    private javax.swing.JPanel navigationPanel;
    private javax.swing.JLabel noticeLabel;
    private javax.swing.JPanel numberFields;
    private javax.swing.JPasswordField password;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JTextField port;
    private javax.swing.JLabel portLabel;
    private javax.swing.JTextField quantity;
    private javax.swing.JPanel quantityGroup;
    private javax.swing.JLabel quantityGroupLabel;
    private javax.swing.JButton refresh;
    private javax.swing.JLabel resultCount;
    private javax.swing.JPanel root;
    private javax.swing.JTextField search;
    private javax.swing.JLabel searchHint;
    private javax.swing.JPanel searchRow;
    private javax.swing.JPanel sidebar;
    private javax.swing.JPanel sidebarBottom;
    private javax.swing.JLabel sidebarBrand;
    private javax.swing.JPanel sidebarTop;
    private javax.swing.JLabel signedInLabel;
    private javax.swing.JLabel status;
    private javax.swing.JLabel syncHint;
    private javax.swing.JTable table;
    private javax.swing.JScrollPane tableScroll;
    private javax.swing.JTextField title;
    private javax.swing.JPanel titleGroup;
    private javax.swing.JLabel titleGroupLabel;
    private javax.swing.JPanel titlesMetric;
    private javax.swing.JLabel titlesMetricCaption;
    private javax.swing.JLabel titlesMetricHint;
    private javax.swing.JButton update;
    private javax.swing.JTextField username;
    private javax.swing.JLabel usernameLabel;
    private javax.swing.JLabel workspaceLabel;
    private javax.swing.JTextField year;
    private javax.swing.JPanel yearGroup;
    private javax.swing.JLabel yearGroupLabel;
    // End of variables declaration//GEN-END:variables
}
