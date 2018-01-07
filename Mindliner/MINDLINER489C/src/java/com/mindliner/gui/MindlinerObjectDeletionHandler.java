/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.commands.CommandRecorder;
import com.mindliner.commands.ObjectDeletionCommand;
import com.mindliner.events.ObjectChangeManager;
import com.mindliner.main.MindlinerMain;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * Convenience class to delete several Mindliner objects at once.
 *
 * @author Marius Messerli
 */
public class MindlinerObjectDeletionHandler {

    /**
     * This call deletes the selected objects from the client application and (if or when online) also from the server.
     * The call prompts the user if the objects should really be deleted.
     *
     * @param objects The objects to be deleted
     */
    public static void delete(List<mlcObject> objects) {
        String msg = objects.size() == 1 ? "Are you sure you want to delete selected object?" : "Are you sure you want to delete selected objects?";
        int rep = JOptionPane.showConfirmDialog(MindlinerMain.getInstance(), msg, "Deletion", JOptionPane.YES_NO_OPTION);
        if (rep == JOptionPane.YES_OPTION) {
            CommandRecorder cr = CommandRecorder.getInstance();
            if (!objects.isEmpty()) {
                ObjectDeletionCommand delCmd = new ObjectDeletionCommand(objects);
                cr.scheduleCommand(delCmd);
                ObjectChangeManager.objectsDeleted(delCmd.getDeletedObjects());
            }
        }
    }

    public static void delete(mlcObject object) {
        List<mlcObject> objects = new ArrayList<>();
        objects.add(object);
        delete(objects);
    }
}
