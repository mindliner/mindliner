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
public class ProgressDialog  extends JDialog{
    
    public ProgressDialog() {
        initialize();
    }

    private void initialize() {
        setTitle("Analyzing Powerpoint File");
        setSize(350, 140);
        setLocationRelativeTo(MindlinerMain.getInstance());
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        add(new IndetProgressPanel());
    }
}
