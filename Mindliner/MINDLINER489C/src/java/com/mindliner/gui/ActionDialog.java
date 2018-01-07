/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * Custom dialog that saves state whether it has been closed for no action ('X' button) or not
 * @author Dominic Plangger
 */
public class ActionDialog extends JDialog {
    
    protected boolean noAction = false;
    
    public ActionDialog() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                noAction = true;
                super.windowClosing(e); //To change body of generated methods, choose Tools | Templates.
            }
        });
    }    
    
    public boolean isNoAction() {
        return noAction;
    }
    
}
