package com.pharmastock.view.components;

import com.pharmastock.view.ThemeConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Sidebar navigasi dengan item-item menu bertema putih (Light Theme) sesuai
 * mockup.
 */
public class SidebarNav extends JPanel {

    private final Map<String, JPanel> menuItems = new LinkedHashMap<>();
    private String activeItem = "";
    private Consumer<String> onNavigate;

    public SidebarNav() {
        setBackground(ThemeConstants.SIDEBAR_BG);
        setPreferredSize(new Dimension(ThemeConstants.SIDEBAR_WIDTH, 0));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ThemeConstants.BORDER));

        buildHeader();
        add(Box.createVerticalStrut(24));
        buildMenuItems();
        add(Box.createVerticalGlue());
        buildProfileFooter();
    }

    private void buildHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(24, 20, 0, 20));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel logo = new JLabel("PharmaStock");
        logo.setFont(ThemeConstants.fontHeadlineMd());
        logo.setForeground(ThemeConstants.PRIMARY); // Biru utama
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Clinical Management");
        subtitle.setFont(ThemeConstants.fontBodySm());
        subtitle.setForeground(Color.decode("#64748B")); // Slate gray
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(logo);
        header.add(Box.createVerticalStrut(2));
        header.add(subtitle);
        add(header);
    }

    private void buildMenuItems() {
        addMenuItem("Dashboard", "dashboard");
        addMenuItem("Inventory", "inventory");
        addMenuItem("AI Chat", "ai");
        addMenuItem("Supplier", "supplier");
        addMenuItem("Laporan", "report");

        add(Box.createVerticalStrut(16));
        JSeparator sep = new JSeparator();
        sep.setForeground(ThemeConstants.BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        add(sep);
        add(Box.createVerticalStrut(16));

        addMenuItem("Profil", "profile");
        addMenuItem("Logout", "logout");
    }

    private void addMenuItem(String label, String key) {
        JPanel item = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (key.equals(activeItem)) {
                    g.setColor(ThemeConstants.SIDEBAR_ACTIVE_BORDER); // Garis aksen biru di kiri
                    g.fillRect(0, 0, 4, getHeight());
                }
            }
        };
        item.setOpaque(false);
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        item.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel textLabel = new JLabel(label);
        textLabel.setFont(ThemeConstants.fontBodyMd());
        textLabel.setForeground(ThemeConstants.SIDEBAR_TEXT);
        item.add(textLabel, BorderLayout.CENTER);

        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setActive(key);
                if (onNavigate != null) {
                    onNavigate.accept(key);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!key.equals(activeItem)) {
                    item.setBackground(Color.decode("#F1F5F9")); // Soft hover gray-blue
                    item.setOpaque(true);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!key.equals(activeItem)) {
                    item.setOpaque(false);
                }
                item.repaint();
            }
        });

        menuItems.put(key, item);
        add(item);
    }

    public void setActive(String key) {
        this.activeItem = key;
        for (Map.Entry<String, JPanel> entry : menuItems.entrySet()) {
            JPanel item = entry.getValue();
            if (entry.getKey().equals(key)) {
                item.setBackground(ThemeConstants.SIDEBAR_ACTIVE_BG);
                item.setOpaque(true);
                for (Component c : item.getComponents()) {
                    if (c instanceof JLabel label) {
                        label.setForeground(ThemeConstants.PRIMARY); // Teks biru saat aktif
                        label.setFont(ThemeConstants.fontTitleMd());
                    }
                }
            } else {
                item.setOpaque(false);
                for (Component c : item.getComponents()) {
                    if (c instanceof JLabel label) {
                        label.setForeground(ThemeConstants.SIDEBAR_TEXT); // Default gray saat tidak aktif
                        label.setFont(ThemeConstants.fontBodyMd());
                    }
                }
            }
        }
        repaint();
    }

    private void buildProfileFooter() {
        JPanel footer = new JPanel(new BorderLayout(10, 0));
        footer.setBackground(Color.decode("#F8FAFC")); // Background abu-abu muda
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeConstants.BORDER),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        // Circular avatar
        RoundedPanel avatar = new RoundedPanel(16);
        avatar.setBackground(ThemeConstants.PRIMARY_TINT_10);
        avatar.setPreferredSize(new Dimension(32, 32));
        avatar.setLayout(new GridBagLayout());

        JLabel avatarLabel = new JLabel("DR");
        avatarLabel.setFont(ThemeConstants.fontLabelSm());
        avatarLabel.setForeground(ThemeConstants.PRIMARY);
        avatar.add(avatarLabel);

        // Text details
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel("Dr. Rayyan");
        nameLabel.setFont(ThemeConstants.fontLabelMd());
        nameLabel.setForeground(ThemeConstants.ON_SURFACE);

        JLabel roleLabel = new JLabel("Chief Pharmacist");
        roleLabel.setFont(ThemeConstants.fontBodySm());
        roleLabel.setForeground(ThemeConstants.ON_SURFACE_VARIANT);

        textPanel.add(nameLabel);
        textPanel.add(roleLabel);

        footer.add(avatar, BorderLayout.WEST);
        footer.add(textPanel, BorderLayout.CENTER);

        add(footer);
    }

    public void setOnNavigate(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
    }
}
