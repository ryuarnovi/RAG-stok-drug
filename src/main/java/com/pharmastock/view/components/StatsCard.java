package com.pharmastock.view.components;

import com.pharmastock.view.ThemeConstants;

import javax.swing.*;
import java.awt.*;

/**
 * Kartu statistik dashboard menampilkan ikon, nilai, dan label.
 */
public class StatsCard extends RoundedPanel {

    public StatsCard(String iconText, String value, String label, Color iconBg, Color iconFg) {
        super(ThemeConstants.RADIUS_XL, ThemeConstants.BORDER);
        setBackground(ThemeConstants.SURFACE);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setHasShadow(true);

        // Icon
        JPanel iconPanel = new JPanel();
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(36, 36));
        iconPanel.setLayout(new GridBagLayout());

        JLabel iconLabel = new JLabel(iconText);
        iconLabel.setFont(new Font(ThemeConstants.FONT_BODY, Font.PLAIN, 20));
        iconLabel.setForeground(iconFg);
        iconPanel.add(iconLabel);

        JPanel iconContainer = new RoundedPanel(ThemeConstants.RADIUS_LG);
        iconContainer.setBackground(iconBg);
        iconContainer.setPreferredSize(new Dimension(36, 36));
        iconContainer.setLayout(new GridBagLayout());
        iconContainer.add(iconLabel);

        // Value + Label
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(ThemeConstants.fontHeadlineMd());
        valueLabel.setForeground(ThemeConstants.ON_SURFACE);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel textLabel = new JLabel(label);
        textLabel.setFont(ThemeConstants.fontBodySm());
        textLabel.setForeground(ThemeConstants.ON_SURFACE_VARIANT);
        textLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(valueLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(textLabel);

        add(iconContainer, BorderLayout.NORTH);
        add(Box.createVerticalStrut(12), BorderLayout.CENTER);
        add(textPanel, BorderLayout.SOUTH);
    }
}
