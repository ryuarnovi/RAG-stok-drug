package com.pharmastock.view.components;

import com.pharmastock.model.Notification;
import com.pharmastock.service.NotificationService;
import com.pharmastock.view.ThemeConstants;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class NotificationPopup extends JPopupMenu {

    private final NotificationService notificationService;
    private final Runnable onStateChanged;

    public NotificationPopup(NotificationService notificationService, Runnable onStateChanged) {
        this.notificationService = notificationService;
        this.onStateChanged = onStateChanged;
        
        // Setup popup properties
        setBorder(BorderFactory.createLineBorder(ThemeConstants.BORDER, 1));
        setBackground(ThemeConstants.SURFACE);
        setLayout(new BorderLayout());
        
        buildUI();
    }

    private void buildUI() {
        removeAll();
        notificationService.refreshNotifications();
        List<Notification> list = notificationService.getNotifications();

        // Panel header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ThemeConstants.SURFACE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JLabel titleLabel = new JLabel("Notifikasi (" + notificationService.getUnreadCount() + ")");
        titleLabel.setFont(ThemeConstants.fontTitleMd());
        titleLabel.setForeground(ThemeConstants.ON_SURFACE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        if (notificationService.getUnreadCount() > 0) {
            JButton readAllBtn = new JButton("Tandai Semua Dibaca");
            readAllBtn.setFont(ThemeConstants.fontLabelSm());
            readAllBtn.putClientProperty("FlatLaf.styleClass", "outline");
            readAllBtn.addActionListener(e -> {
                notificationService.markAllAsRead();
                buildUI();
                if (onStateChanged != null) onStateChanged.run();
            });
            headerPanel.add(readAllBtn, BorderLayout.EAST);
        }
        add(headerPanel, BorderLayout.NORTH);

        // Content ScrollPane
        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBackground(ThemeConstants.SURFACE);

        if (list.isEmpty()) {
            JPanel emptyPanel = new JPanel(new GridBagLayout());
            emptyPanel.setBackground(ThemeConstants.SURFACE);
            emptyPanel.setPreferredSize(new Dimension(320, 150));
            
            JLabel emptyLabel = new JLabel("Tidak ada notifikasi aktif.");
            emptyLabel.setFont(ThemeConstants.fontBodyMd());
            emptyLabel.setForeground(ThemeConstants.ON_SURFACE_VARIANT);
            emptyPanel.add(emptyLabel);
            itemsPanel.add(emptyPanel);
        } else {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
            for (Notification n : list) {
                JPanel row = new JPanel(new BorderLayout(8, 0));
                row.setBackground(n.isRead() ? ThemeConstants.SURFACE : ThemeConstants.SURFACE_CONTAINER_LOW);
                row.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeConstants.BORDER),
                        BorderFactory.createEmptyBorder(10, 14, 10, 14)
                ));

                // Left status indicator dot
                JPanel indicator = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(n.getType().equals("DANGER") ? ThemeConstants.DANGER : ThemeConstants.WARNING);
                        g2.fillOval(4, 8, 8, 8);
                        g2.dispose();
                    }
                };
                indicator.setOpaque(false);
                indicator.setPreferredSize(new Dimension(16, 24));
                row.add(indicator, BorderLayout.WEST);

                // Message Text Panel
                JPanel textPanel = new JPanel();
                textPanel.setOpaque(false);
                textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

                JLabel titleLbl = new JLabel("<html><b>" + n.getTitle() + "</b></html>");
                titleLbl.setFont(ThemeConstants.fontBodySm());
                titleLbl.setForeground(n.isRead() ? ThemeConstants.OUTLINE : ThemeConstants.ON_SURFACE);

                JLabel msgLbl = new JLabel("<html>" + n.getMessage() + "</html>");
                msgLbl.setFont(ThemeConstants.fontBodySm());
                msgLbl.setForeground(ThemeConstants.ON_SURFACE_VARIANT);

                JLabel timeLbl = new JLabel(n.getTimestamp().format(fmt));
                timeLbl.setFont(ThemeConstants.fontLabelSm());
                timeLbl.setForeground(ThemeConstants.OUTLINE);

                textPanel.add(titleLbl);
                textPanel.add(Box.createVerticalStrut(2));
                textPanel.add(msgLbl);
                textPanel.add(Box.createVerticalStrut(4));
                textPanel.add(timeLbl);
                row.add(textPanel, BorderLayout.CENTER);

                // Right checkmark action button
                if (!n.isRead()) {
                    JButton checkBtn = new JButton("✓");
                    checkBtn.setFont(new Font(ThemeConstants.FONT_BODY, Font.BOLD, 10));
                    checkBtn.putClientProperty("FlatLaf.styleClass", "outline");
                    checkBtn.setMargin(new Insets(2, 4, 2, 4));
                    checkBtn.addActionListener(e -> {
                        notificationService.markAsRead(n.getId());
                        buildUI();
                        if (onStateChanged != null) onStateChanged.run();
                    });
                    
                    JPanel btnWrapper = new JPanel(new GridBagLayout());
                    btnWrapper.setOpaque(false);
                    btnWrapper.add(checkBtn);
                    row.add(btnWrapper, BorderLayout.EAST);
                }

                itemsPanel.add(row);
            }
        }

        JScrollPane scroll = new JScrollPane(itemsPanel);
        scroll.setBorder(null);
        scroll.setPreferredSize(new Dimension(340, 260));
        add(scroll, BorderLayout.CENTER);
        
        revalidate();
        repaint();
    }

    @Override
    public void show(Component invoker, int x, int y) {
        buildUI();
        super.show(invoker, x, y);
    }
}
