package com.pharmastock.view.components;

import com.pharmastock.view.ThemeConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Chat bubble untuk AI chat interface.
 */
public class ChatBubble extends JPanel {

    private final boolean isUser;
    private final String message;
    private final String timestamp;

    public ChatBubble(String message, String timestamp, boolean isUser) {
        this.isUser = isUser;
        this.message = message;
        this.timestamp = timestamp;
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        buildUI();
    }

    private void buildUI() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.X_AXIS));
        wrapper.setOpaque(false);

        if (!isUser) {
            // Bot icon
            JPanel iconPanel = new JPanel(new GridBagLayout());
            iconPanel.setOpaque(false);
            iconPanel.setPreferredSize(new Dimension(32, 32));
            iconPanel.setMaximumSize(new Dimension(32, 32));

            JLabel iconLabel = new JLabel("\u2022");
            iconLabel.setFont(new Font(ThemeConstants.FONT_BODY, Font.BOLD, 14));
            iconLabel.setForeground(ThemeConstants.ON_PRIMARY);

            RoundedPanel iconBg = new RoundedPanel(8);
            iconBg.setBackground(ThemeConstants.PRIMARY);
            iconBg.setPreferredSize(new Dimension(32, 32));
            iconBg.setMaximumSize(new Dimension(32, 32));
            iconBg.setLayout(new GridBagLayout());

            JLabel botLabel = new JLabel("AI");
            botLabel.setFont(ThemeConstants.fontLabelSm());
            botLabel.setForeground(ThemeConstants.ON_PRIMARY);
            iconBg.add(botLabel);

            wrapper.add(iconBg);
            wrapper.add(Box.createHorizontalStrut(8));
        } else {
            wrapper.add(Box.createHorizontalGlue());
        }

        JPanel bubble = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };
        bubble.setOpaque(false);
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        if (isUser) {
            bubble.setBackground(ThemeConstants.PRIMARY);
        } else {
            bubble.setBackground(ThemeConstants.SURFACE_CONTAINER_LOW);
        }

        JTextArea messageArea = new JTextArea(message);
        messageArea.setFont(ThemeConstants.fontBodyMd());
        messageArea.setForeground(isUser ? ThemeConstants.ON_PRIMARY : ThemeConstants.ON_SURFACE);
        messageArea.setOpaque(false);
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        messageArea.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));
        bubble.add(messageArea);

        if (timestamp != null) {
            JLabel timeLabel = new JLabel(timestamp);
            timeLabel.setFont(ThemeConstants.fontLabelSm());
            timeLabel.setForeground(isUser ? new Color(137, 206, 255) : ThemeConstants.ON_SURFACE_VARIANT);
            timeLabel.setAlignmentX(isUser ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
            bubble.add(Box.createVerticalStrut(4));
            bubble.add(timeLabel);
        }

        bubble.setMaximumSize(new Dimension(450, Integer.MAX_VALUE));
        wrapper.add(bubble);

        if (isUser) {
            // no trailing space
        } else {
            wrapper.add(Box.createHorizontalGlue());
        }

        wrapper.setAlignmentX(isUser ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
        add(wrapper);
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension pref = getPreferredSize();
        return new Dimension(Integer.MAX_VALUE, pref.height);
    }
}
