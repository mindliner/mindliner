/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.connector;

/**
 *
 * @author Dominic Plangger
 */
public class SftpAuthenticationDeletionPanel extends SftpAuthenticationPanel {

    public SftpAuthenticationDeletionPanel(String filename) {
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/mindliner/gui/GuiElements"); 
        setSubTitle("Remote file to delete: " + filename); 
        setTitle(bundle.getString("SftpDeletion")); 
    }
}
