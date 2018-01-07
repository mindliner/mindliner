/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui;

import com.mindliner.main.MindlinerMain;
import javax.swing.JDialog;

/**
 * @author Dominic Plangger
 */
public class PowerpointExportDialog  extends JDialog {
    
    
    public PowerpointExportDialog(PowerpointExportPanel panel) { 
        initialize(panel);
    }

    private void initialize(PowerpointExportPanel panel) {
        setTitle("Powerpoint Export");
        setSize(430, 250);
        setLocationRelativeTo(MindlinerMain.getInstance());
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        add(panel);
        panel.setDialog(this);
    }
    
    
}
