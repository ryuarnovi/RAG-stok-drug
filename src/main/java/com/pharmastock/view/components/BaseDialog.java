package com.pharmastock.view.components;

import javax.swing.JDialog;
import java.awt.Frame;

public abstract class BaseDialog extends JDialog {
    
    protected BaseDialog(java.awt.Window owner, String title, boolean modal) {
        super(owner, title, modal ? ModalityType.APPLICATION_MODAL : ModalityType.MODELESS);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    /**
     * Inisialisasi komponen GUI di dalam dialog.
     */
    protected abstract void initUI();
}
