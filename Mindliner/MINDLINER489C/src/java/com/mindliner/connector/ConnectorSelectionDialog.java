/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.connector;

import com.mindliner.gui.ActionDialog;
import com.mindliner.main.MindlinerMain;

/**
 *
 * @author Dominic Plangger
 */
public class ConnectorSelectionDialog extends ActionDialog {

    public ConnectorSelectionDialog(ConnectorSelectionPanel panel) {
        initialize(panel);
    }

    private void initialize(ConnectorSelectionPanel panel) {
        setTitle("File Import");
        setSize(500, 250);
        setLocationRelativeTo(MindlinerMain.getInstance());
        add(panel);
        panel.setDialog(this);

    }
}
