/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;

/**
 *
 * @author Marius Messerli
 */
public class MlDialogUtils {

    public static void addEscapeListener(final JDialog dialog) {
        ActionListener escListener = (ActionEvent e) -> {
            ObjectEditorLauncher.unregisterFromOpenEditors(dialog);
            dialog.dispose();
        };
        dialog.getRootPane().registerKeyboardAction(escListener,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

}
