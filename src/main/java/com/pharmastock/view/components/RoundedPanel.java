package com.pharmastock.view.components;

import com.pharmastock.view.ThemeConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class RoundedPanel extends JPanel {

    private int radius;
    private Color borderColor;
    private Color shadowColor;
    private boolean hasShadow;

    public RoundedPanel(int radius) {
        this(radius, null);
    }

    public RoundedPanel(int radius, Color borderColor) {
        this.radius = radius;
        this.borderColor = borderColor;
        this.shadowColor = ThemeConstants.SHADOW;
        this.hasShadow = false;
        setOpaque(false);
    }

    public void setRadius(int radius) {
        this.radius = radius;
        repaint();
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        repaint();
    }

    public void setHasShadow(boolean hasShadow) {
        this.hasShadow = hasShadow;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int x = hasShadow ? 1 : 0;
        int y = hasShadow ? 1 : 0;
        int w = getWidth() - (hasShadow ? 3 : 1);
        int h = getHeight() - (hasShadow ? 3 : 1);

        // Shadow
        if (hasShadow) {
            g2.setColor(shadowColor);
            g2.fill(new RoundRectangle2D.Float(x + 1, y + 1, w, h, radius, radius));
        }

        // Background
        g2.setColor(getBackground());
        g2.fill(new RoundRectangle2D.Float(x, y, w, h, radius, radius));

        // Border
        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(x, y, w - 1, h - 1, radius, radius));
        }

        g2.dispose();
    }
}
