/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.connector;

import com.mindliner.gui.ActionDialog;
import com.mindliner.main.MindlinerMain;
import javax.swing.JDialog;

/**
 *
 * @author Dominic Plangger
 */
public class GoogleDriveAuthenticationDialog extends ActionDialog {
    
    public GoogleDriveAuthenticationDialog(GoogleDriveAuthenticationPanel panel) {
        initialize(panel);
    }

    private void initialize(GoogleDriveAuthenticationPanel panel) {
        setTitle("Google Drive Authorization");
        setSize(500, 270);
        setLocationRelativeTo(MindlinerMain.getInstance());
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        add(panel);
        panel.setDialog(this);
    }
    
}
