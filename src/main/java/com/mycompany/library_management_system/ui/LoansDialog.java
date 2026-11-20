package com.mycompany.library_management_system.ui;

import com.mycompany.library_management_system.model.Loan;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

/** NetBeans Dialog Form displaying the signed-in user's borrowing history. */
public final class LoansDialog extends JDialog {
    private final String username;
    private final Runnable reload;
    private final Consumer<Loan> returnAction;
    private LoanTableModel model;
    private Loan selected;
    private boolean busy;

    public LoansDialog(Window owner, String username, Runnable reload, Consumer<Loan> returnAction) {
        super(owner, "Phiếu mượn • " + username, Dialog.ModalityType.APPLICATION_MODAL);
        this.username = username;
        this.reload = reload;
        this.returnAction = returnAction;
        initComponents();
        setTitle("Phiếu mượn • " + username);
        model = new LoanTableModel();
        loansTable.setModel(model);
        loansTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        loansTable.setShowVerticalLines(false);
        loansTable.getColumnModel().getColumn(0).setPreferredWidth(62);
        loansTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        loansTable.getColumnModel().getColumn(2).setPreferredWidth(215);
        loansTable.getColumnModel().getColumn(3).setPreferredWidth(145);
        loansTable.getColumnModel().getColumn(4).setPreferredWidth(145);
        loansTable.getColumnModel().getColumn(5).setPreferredWidth(90);
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                    boolean focused, int row, int column) {
                super.getTableCellRendererComponent(table, value, selected, false, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 6));
                setFont(Theme.font(12, Font.PLAIN));
                setHorizontalAlignment(column == 0 || column == 5 ? SwingConstants.CENTER : SwingConstants.LEFT);
                if (!selected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 252, 252));
                    setForeground(column == 5 && "Đang mượn".equals(value) ? Theme.TEAL : Theme.NAVY);
                }
                return this;
            }
        };
        loansTable.setDefaultRenderer(Object.class, renderer);
        loansTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && loansTable.getSelectedRow() >= 0) {
                selected = model.rows.get(loansTable.convertRowIndexToModel(loansTable.getSelectedRow()));
                returnButton.setEnabled(!busy && selected.returnedAt() == null);
            }
        });
        setSize(900, 520);
        setMinimumSize(new Dimension(760, 440));
        setLocationRelativeTo(owner);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        root = new JPanel();
        header = new JPanel();
        titleLabel = new JLabel();
        accountLabel = new JLabel();
        tableScroll = new JScrollPane();
        loansTable = new JTable();
        footer = new JPanel();
        messageLabel = new JLabel();
        reloadButton = new JButton();
        returnButton = new JButton();
        closeButton = new JButton();
        buttons = new JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createEmptyBorder(20, 22, 16, 22));
        root.setLayout(new BorderLayout(0, 14));
        header.setBackground(Color.WHITE);
        header.setOpaque(false);
        header.setLayout(new java.awt.GridLayout(2, 1, 0, 4));
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 21));
        titleLabel.setForeground(Theme.NAVY);
        titleLabel.setText("Phiếu mượn của tôi");
        header.add(titleLabel);
        accountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        accountLabel.setForeground(Theme.MUTED);
        accountLabel.setText("Lịch sử mượn sách của " + username);
        header.add(accountLabel);
        root.add(header, BorderLayout.NORTH);
        tableScroll.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        loansTable.setAutoCreateRowSorter(true);
        loansTable.setFillsViewportHeight(true);
        loansTable.setRowHeight(38);
        tableScroll.setViewportView(loansTable);
        root.add(tableScroll, BorderLayout.CENTER);
        footer.setBackground(Color.WHITE);
        footer.setLayout(new BorderLayout(10, 0));
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        messageLabel.setForeground(Theme.MUTED);
        messageLabel.setText("Chọn một khoản đang mượn để trả sách.");
        footer.add(messageLabel, BorderLayout.WEST);
        buttons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        reloadButton.setText("Làm mới");
        reloadButton.addActionListener(event -> reloadButtonActionPerformed(event));
        buttons.add(reloadButton);
        returnButton.setBackground(Theme.TEAL);
        returnButton.setForeground(Color.WHITE);
        returnButton.setText("Trả sách");
        returnButton.setEnabled(false);
        returnButton.addActionListener(event -> returnButtonActionPerformed(event));
        buttons.add(returnButton);
        closeButton.setText("Đóng");
        closeButton.addActionListener(event -> closeButtonActionPerformed(event));
        buttons.add(closeButton);
        footer.add(buttons, BorderLayout.EAST);
        root.add(footer, BorderLayout.SOUTH);
        setContentPane(root);
        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void reloadButtonActionPerformed(java.awt.event.ActionEvent event) {//GEN-FIRST:event_reloadButtonActionPerformed
        reload.run();
    }//GEN-LAST:event_reloadButtonActionPerformed

    private void returnButtonActionPerformed(java.awt.event.ActionEvent event) {//GEN-FIRST:event_returnButtonActionPerformed
        if (selected != null && selected.returnedAt() == null) returnAction.accept(selected);
    }//GEN-LAST:event_returnButtonActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent event) {//GEN-FIRST:event_closeButtonActionPerformed
        dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    public void showLoans(List<Loan> loans, String message) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> showLoans(loans, message));
            return;
        }
        selected = null;
        model.setRows(loans == null ? List.of() : loans);
        returnButton.setEnabled(false);
        if (message != null && !message.isBlank()) setMessage(message);
        else setMessage(model.rows.isEmpty() ? "Chưa có lịch sử mượn sách." : "Chọn một khoản đang mượn để trả sách.");
    }

    public void setMessage(String message) {
        messageLabel.setText(message == null ? "" : message);
        messageLabel.setToolTipText(message);
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
        reloadButton.setEnabled(!busy);
        closeButton.setEnabled(!busy);
        returnButton.setEnabled(!busy && selected != null && selected.returnedAt() == null);
    }

    private static final class LoanTableModel extends AbstractTableModel {
        private final String[] columns = {"Mã", "Mã sách", "Tên sách", "Ngày mượn", "Ngày trả", "Trạng thái"};
        private List<Loan> rows = new ArrayList<>();
        void setRows(List<Loan> values) { rows = new ArrayList<>(values); fireTableDataChanged(); }
        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int column) { return columns[column]; }
        @Override public Object getValueAt(int row, int column) {
            Loan loan = rows.get(row);
            return switch (column) {
                case 0 -> loan.id(); case 1 -> loan.code(); case 2 -> loan.title();
                case 3 -> loan.borrowedAt(); case 4 -> loan.returnedAt() == null ? "—" : loan.returnedAt();
                default -> loan.returnedAt() == null ? "Đang mượn" : "Đã trả";
            };
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel footer;
    private JPanel header;
    private JLabel accountLabel;
    private JButton closeButton;
    private JPanel buttons;
    private JLabel messageLabel;
    private JButton reloadButton;
    private JButton returnButton;
    private JPanel root;
    private JScrollPane tableScroll;
    private JLabel titleLabel;
    private JTable loansTable;
    // End of variables declaration//GEN-END:variables
}
