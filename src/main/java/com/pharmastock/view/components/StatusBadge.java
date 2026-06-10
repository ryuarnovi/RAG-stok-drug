package com.pharmastock.view.components;

import com.pharmastock.view.ThemeConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Badge status berbentuk pill (IN STOCK, LOW STOCK, EXPIRED, dsb).
 */
public class StatusBadge extends JLabel {

    private final Color textColor;
    private final Color bgColor;

    public StatusBadge(String text, Color textColor, Color bgColor) {
        super(text.toUpperCase());
        this.textColor = textColor;
        this.bgColor = bgColor;
        setFont(ThemeConstants.fontLabelSm());
        setForeground(textColor);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bgColor);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 6, 6));
        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, d.height);
    }
}
