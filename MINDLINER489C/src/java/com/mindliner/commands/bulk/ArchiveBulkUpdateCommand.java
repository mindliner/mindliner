/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands.bulk;

import com.mindliner.clientobjects.ObjectIdLister;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.managers.ObjectManagerRemote;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.main.BulkUpdater;
import com.mindliner.serveraccess.RemoteLookupAgent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.NamingException;
import javax.swing.JOptionPane;

/**
 *
 * @author Marius Messerli
 */
public class ArchiveBulkUpdateCommand extends BulkUpdateCommand {

    boolean archived = false;
    Map<Integer, Boolean> previousArchiveStates;

    public ArchiveBulkUpdateCommand(List<mlcObject> candidates, boolean state) {
        super(candidates);
        this.archived = state;
        previousArchiveStates = new HashMap<>(objects.size());
        for (mlcObject o : objects) {
            previousArchiveStates.put(o.getId(), o.isArchived());
            o.setArchived(state);
        }
        BulkUpdater.publishUpdates(objects);
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        super.execute();
        ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
        Map<Integer, Integer> versions = omr.bulkSetArchived(ObjectIdLister.getIdList(objects), archived);
        updateObjectVersions(versions);
        setExecuted(true);
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        super.undo();
        for (mlcObject o : getObjects()) {
            Boolean previousState = previousArchiveStates.get(o.getId());
            if (previousState != null) {
                o.setArchived(previousState);
            }
        }
        BulkUpdater.publishUpdates(objects);
        if (isExecuted()) {
            ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
            if (versionCheck() == false) {
                JOptionPane.showMessageDialog(null,
                        "Cannot undo because the object has been updated in the meantime.",
                        "Undo Bulk Archived State",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                for (mlcObject o : getObjects()) {
                    Boolean previousState = previousArchiveStates.get(o.getId());
                    if (previousState != null) {
                        int version = omr.setArchived(o.getId(), previousState);
                        o.setVersion(version);
                    }
                }
            }
        }
        setUndone(true);
    }

    @Override
    public boolean isVersionChecking() {
        return false;
    }

    @Override
    public String toString() {
        return "Bulk update archived to: " + archived;
    }
}
