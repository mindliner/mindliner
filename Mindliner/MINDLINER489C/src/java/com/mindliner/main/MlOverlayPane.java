/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.main;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * This class is used to provide an icon for the overlay panes in case they are
 * embedded as tabbed panes.
 *
 * @author Marius Messerli
 */
public class MlOverlayPane {

    JPanel panel;
    private ImageIcon icon;

    public MlOverlayPane(JPanel panel, ImageIcon icon) {
        this.panel = panel;
        this.icon = icon;
    }

    public JPanel getPanel() {
        return panel;
    }

    public ImageIcon getIcon() {
        return icon;
    }

}
