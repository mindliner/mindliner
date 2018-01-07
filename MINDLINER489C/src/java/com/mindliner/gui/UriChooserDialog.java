/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui;

import com.mindliner.main.MindlinerMain;
import javax.swing.JDialog;

/**
 *
 * @author Dominic Plangger
 */
public class UriChooserDialog extends JDialog {
        
    public UriChooserDialog(UriChooserPanel panel) {
        initialize(panel);
    }

    private void initialize(UriChooserPanel panel) {
        setTitle("URIs to open");
        setSize(600, 300);
        setLocationRelativeTo(MindlinerMain.getInstance());
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        add(panel);
    }
}
