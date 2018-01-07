/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui;

import com.mindliner.main.MindlinerMain;
import javax.swing.JDialog;

/**
 * The dialog which will be shown after the user pressed CTRL + V (i.e. paste).
 * It suggests a number of objects to the user which should be created out of the clipboard content
 * 
 * @author Dominic Plangger
 */
public class ClipboardObjectsDialog  extends JDialog {
    
    public ClipboardObjectsDialog(ClipboardObjectsPanel panel) { 
        initialize(panel);
    }

    private void initialize(ClipboardObjectsPanel panel) {
        setTitle("Clipboard objects");
        setSize(750, 800);
        setLocationRelativeTo(MindlinerMain.getInstance());
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        add(panel);
        panel.setDialog(this); // the panel needs the reference to close the dialog. It is safe as 'this' is initialized completely at this point
    }
}
