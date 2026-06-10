package com.pharmastock.view.components;

import com.pharmastock.view.ThemeConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Chip filter kategori obat dengan toggle active/inactive.
 */
public class CategoryChip extends JToggleButton {

    public CategoryChip(String text) {
        super(text);
        setFont(ThemeConstants.fontLabelMd());
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(8, 16, 8, 16));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        RoundRectangle2D shape = new RoundRectangle2D.Float(0, 0, w - 1, h - 1, h, h);

        if (isSelected()) {
            g2.setColor(ThemeConstants.PRIMARY);
            g2.fill(shape);
            g2.setColor(ThemeConstants.ON_PRIMARY);
        } else {
            g2.setColor(ThemeConstants.SURFACE);
            g2.fill(shape);
            g2.setColor(ThemeConstants.OUTLINE_VARIANT);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(shape);
            g2.setColor(ThemeConstants.ON_SURFACE_VARIANT);
        }

        FontMetrics fm = g2.getFontMetrics(getFont());
        g2.setFont(getFont());
        int textX = (w - fm.stringWidth(getText())) / 2;
        int textY = (h + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(getText(), textX, textY);

        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        int w = fm.stringWidth(getText()) + 32;
        int h = fm.getHeight() + 16;
        return new Dimension(w, h);
    }
}
