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
public class SftpAuthenticationDialog extends ActionDialog {
    
    public SftpAuthenticationDialog(SftpAuthenticationPanel panel) {
        initialize(panel);
    }

    private void initialize(SftpAuthenticationPanel panel) {
        setTitle("SFTP Authorization");
        setSize(500, 270);
        setLocationRelativeTo(MindlinerMain.getInstance());
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        add(panel);
        panel.setDialog(this);
    }
    
}
