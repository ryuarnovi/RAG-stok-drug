package com.pharmastock.view.components;

import javax.swing.JPanel;

public abstract class BasePanel extends JPanel {
    
    protected BasePanel() {
    }
    
    /**
     * Inisialisasi komponen GUI di dalam panel.
     */
    protected abstract void initUI();
    
    /**
     * Memuat ulang data panel dari database.
     */
    public abstract void refreshData();
}
