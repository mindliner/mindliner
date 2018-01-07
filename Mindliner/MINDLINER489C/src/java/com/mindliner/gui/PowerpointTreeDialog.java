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
public class PowerpointTreeDialog  extends JDialog {
    
    
    public PowerpointTreeDialog(PowerpointTreePanel panel) { 
        initialize(panel);
    }

    private void initialize(PowerpointTreePanel panel) {
        setTitle("Powerpoint Import");
        setSize(800, 700);
        setLocationRelativeTo(MindlinerMain.getInstance());
        setModalityType(JDialog.ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        add(panel);
        panel.setDialog(this);
    }
    
    
}
