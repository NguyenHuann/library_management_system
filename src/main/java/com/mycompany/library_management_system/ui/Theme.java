package com.mycompany.library_management_system.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/** Shared visual language for the Swing client and server. */
public final class Theme {
    public static final Color NAVY = new Color(23, 43, 57);
    public static final Color TEAL = new Color(13, 119, 110);
    public static final Color BACKGROUND = new Color(245, 247, 248);
    public static final Color MUTED = new Color(106, 122, 132);
    public static final Color BORDER = new Color(224, 231, 234);
    public static final Color SOFT_TEAL = new Color(230, 244, 240);
    public static final Color DANGER = new Color(174, 62, 67);

    private Theme() { }

    public static void install() {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) { }
        Font normal = font(13, Font.PLAIN);
        for (Object key : UIManager.getDefaults().keySet().toArray()) {
            if (UIManager.get(key) instanceof Font) UIManager.put(key, normal);
        }
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("Label.foreground", NAVY);
        UIManager.put("TextField.selectionBackground", new Color(184, 225, 215));
        UIManager.put("TextField.selectionForeground", NAVY);
        UIManager.put("Table.selectionBackground", SOFT_TEAL);
        UIManager.put("Table.selectionForeground", NAVY);
        UIManager.put("Table.gridColor", BORDER);
        UIManager.put("TableHeader.background", new Color(246, 248, 249));
        UIManager.put("TableHeader.foreground", MUTED);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("Panel.font", normal);
        UIManager.put("Button.focus", new Color(0, 0, 0, 0));
        UIManager.put("ToolTip.background", NAVY);
        UIManager.put("ToolTip.foreground", Color.WHITE);
    }

    public static Font font(int size, int style) { return new Font("Segoe UI", style, size); }
    public static JLabel label(String text, int size, int style, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font(size, style));
        label.setForeground(color);
        return label;
    }
    public static JPanel panel(LayoutManager layout, Color color) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(color);
        return panel;
    }
    public static JPanel padded(LayoutManager layout, Color color, int top, int left, int bottom, int right) {
        JPanel p = panel(layout, color);
        p.setBorder(new EmptyBorder(top, left, bottom, right));
        return p;
    }
    public static JTextField field(String initial) {
        JTextField field = new JTextField(initial);
        field.setFont(font(14, Font.PLAIN));
        field.setForeground(NAVY);
        field.setCaretColor(TEAL);
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER), new EmptyBorder(9, 11, 9, 11)));
        field.setPreferredSize(new Dimension(160, 40));
        return field;
    }
    public static JButton primary(String text) { return button(text, TEAL, Color.WHITE); }
    public static JButton secondary(String text) { return button(text, Color.WHITE, NAVY); }
    public static JButton button(String text, Color background, Color foreground) {
        JButton button = new JButton(text) {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = getBackground();
                if (!isEnabled()) base = new Color(230, 235, 237);
                else if (getModel().isPressed()) base = base.darker();
                else if (getModel().isRollover()) base = base.equals(Color.WHITE) ? new Color(243, 247, 247) : base.brighter();
                g.setColor(base);
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 9, 9);
                if (getBackground().equals(Color.WHITE)) {
                    g.setColor(BORDER);
                    g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 9, 9);
                }
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFont(font(13, Font.BOLD));
        button.setBorder(new EmptyBorder(10, 16, 10, 16));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }
    public static JPanel card(LayoutManager layout) {
        JPanel card = panel(layout, Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER), new EmptyBorder(18, 20, 18, 20)));
        return card;
    }
    public static JComponent bookIcon(Color color, int size) {
        return new JComponent() {
            { setPreferredSize(new Dimension(size, size)); setMinimumSize(getPreferredSize()); }
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(color);
                g.setStroke(new BasicStroke(Math.max(2f, size / 16f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int x = size / 6, y = size / 5, mid = size / 2, h = size * 3 / 5;
                g.drawRoundRect(x, y, size - 2 * x, h, size / 10, size / 10);
                g.drawLine(mid, y + 2, mid, y + h - 2);
                g.drawLine(x + 5, y + h / 3, mid - 5, y + h / 3);
                g.drawLine(mid + 5, y + h / 3, size - x - 5, y + h / 3);
                g.dispose();
            }
        };
    }
}
