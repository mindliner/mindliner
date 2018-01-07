/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui;

import com.mindliner.clientobjects.mlcContact;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.main.MindlinerMain;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

/**
 * This class opens the appropriate object editor and registers the editor
 * dialog.
 *
 * @author Marius Messerli
 */
public class ObjectEditorLauncher {

    private static final List<JDialog> editors = new ArrayList<>();

    public static void registerAsOpenEditor(JDialog editor) {
        if (!editors.contains(editor)) {
            editors.add(editor);
        }
    }

    /**
     * Removes the specified editor from the list of current editors
     * @param editor
     */
    public static void unregisterFromOpenEditors(JDialog editor) {
        editors.remove(editor);
    }
    
    public static int getEditorCount(){
        return editors.size();
    }

    public static void popEditors() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (JDialog d : editors) {
                    d.toFront();
                }
            }
        });
    }

    /**
     * Creates and opens the adequate editor for the specified object type.
     * The windows is shown on the same 
     *
     * @param objects The objects to be edited
     */
    public static void showEditor(List<mlcObject> objects) {
        if (objects == null || objects.isEmpty()) {
            return;
        }
        if (objects.size() == 1 & objects.get(0) instanceof mlcContact) {
            ContactEditor contactEditor = new ContactEditor(null, true);
            contactEditor.showEditor((mlcContact) objects.get(0));
            editors.add(contactEditor);
        } else {
            ObjectEditor editor = new ObjectEditor(objects);
            editor.setLocationRelativeTo(MindlinerMain.getInstance());
            editor.setVisible(true);
            editors.add(editor);
        }
    }

}
