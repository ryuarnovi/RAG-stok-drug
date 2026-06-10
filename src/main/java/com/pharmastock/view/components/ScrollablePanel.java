package com.pharmastock.view.components;

import javax.swing.*;
import java.awt.*;

public class ScrollablePanel extends JPanel implements Scrollable {
    
    private boolean trackWidth = true;
    private boolean trackHeight = false;

    public ScrollablePanel() {
        super();
    }

    public ScrollablePanel(LayoutManager layout) {
        super(layout);
    }

    public void setTrackWidth(boolean trackWidth) {
        this.trackWidth = trackWidth;
    }

    public void setTrackHeight(boolean trackHeight) {
        this.trackHeight = trackHeight;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 16;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 64;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return trackWidth;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return trackHeight;
    }
}
