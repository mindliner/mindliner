/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.commands.CommandRecorder;
import com.mindliner.commands.ObjectDeletionCommand;
import com.mindliner.events.ObjectChangeManager;
import com.mindliner.main.MindlinerMain;
import javax.swing.JOptionPane;

/**
 * This class deletes the specified node after promting the user.
 *
 * @author Marius Messerli
 */
public class MlObjectDeletionHandler {

    public static boolean deleteNode(mlcObject object, boolean prompt) {
        int rep;
        if (prompt) {
            rep = JOptionPane.showConfirmDialog(MindlinerMain.getInstance(),
                    "Sure you want to delete the selected object?", "Object Deletion", JOptionPane.YES_NO_OPTION);
            if (rep != JOptionPane.YES_OPTION) {
                return false;
            }
        } else {
            rep = JOptionPane.YES_OPTION;
        }
        if (rep == JOptionPane.YES_OPTION) {
            CommandRecorder cr = CommandRecorder.getInstance();
            ObjectDeletionCommand delCmd = new ObjectDeletionCommand(object);
            cr.scheduleCommand(delCmd);
            // I have to call the following manually because the message listener will not work as the object is already deleted
            ObjectChangeManager.objectsDeleted(delCmd.getDeletedObjects());
            return true;
        }
        return false;
    }
}
