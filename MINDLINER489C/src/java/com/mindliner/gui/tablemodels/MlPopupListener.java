/*
 * MindlinkerTablePopupListener.java
 *
 * Created on 10. Juli 2006, 16:23
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.mindliner.gui.tablemodels;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;

/**
 * This class displays the popups for all the tables in the application.
 * @author messerli
 */
public class MlPopupListener extends MouseAdapter {
    
    private JPopupMenu popUpMenu;
    private boolean checkPopupTrigger = false;

    
    /**
     * Creates a new instance of MindlinerTablePopupListener
     * @param pum The popup menu.
     * @param checkPopupTrigger If true the class will check the event for isPopupTrigger 
     * before showing the menu (to prevent the popup on a selection click on a 1-button mouse)
     */
    public MlPopupListener(JPopupMenu pum, boolean checkPopupTrigger) {
        popUpMenu = pum;
        this.checkPopupTrigger = checkPopupTrigger;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) {
        if (checkPopupTrigger == false || e.isPopupTrigger()) {
            popUpMenu.show(e.getComponent(),
                       e.getX(), e.getY());
        }
    }
    
}
