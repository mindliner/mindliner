/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands.bulk;

import com.mindliner.clientobjects.ObjectIdLister;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.enums.ObjectReviewStatus;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.main.BulkUpdater;
import com.mindliner.managers.ObjectManagerRemote;
import com.mindliner.serveraccess.RemoteLookupAgent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.NamingException;
import javax.swing.JOptionPane;

/**
 *
 * @author Dominic Plangger
 */
public class StatusBulkUpdateCommand extends BulkUpdateCommand {

    private ObjectReviewStatus status;
    private Map<Integer, ObjectReviewStatus> previousStatus;

    public StatusBulkUpdateCommand(List<mlcObject> candidates, ObjectReviewStatus s) {
        super(candidates);
        
        if (s == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        status = s;
        previousStatus = new HashMap<Integer, ObjectReviewStatus>(objects.size());
        for (mlcObject o : objects) {
            previousStatus.put(o.getId(), o.getStatus());
            o.setStatus(s);
        }
        BulkUpdater.publishUpdates(objects);
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        super.execute();
        ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
        Map<Integer, Integer> versions = omr.bulkSetStatus(ObjectIdLister.getIdList(objects), status);
        updateObjectVersions(versions);
        setExecuted(true);
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        super.undo();
        for (mlcObject o : getObjects()) {
            ObjectReviewStatus s = previousStatus.get(o.getId());
            if (s != null) {
                o.setStatus(s);
            }
        }
        BulkUpdater.publishUpdates(objects);
        if (isExecuted()) {
            ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
            if (versionCheck() == false) {
                JOptionPane.showMessageDialog(null,
                        "Cannot undo because the object has been updated in the meantime.",
                        "Undo Bulk Status Update",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                for (mlcObject o : getObjects()) {
                    ObjectReviewStatus s = previousStatus.get(o.getId());
                    if (s != null) {
                        int version = omr.setStatus(o.getId(), s);
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
        return "Updating status to " + status;
    }
}
