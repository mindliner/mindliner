/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.enums.ObjectReviewStatus;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.main.BulkUpdater;
import com.mindliner.managers.ObjectManagerRemote;
import com.mindliner.serveraccess.RemoteLookupAgent;
import javax.naming.NamingException;

/**
 *
 * @author Dominic Plangger
 */
public class StatusUpdateCommand extends MindlinerOnlineCommand {
    
    private ObjectReviewStatus status;
    private ObjectReviewStatus previousStatus;
    
    
    public StatusUpdateCommand(mlcObject o, ObjectReviewStatus status) {
        super(o, true);
        this.status = status;
        previousStatus = o.getStatus();
        
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }

        o.setStatus(status);
        BulkUpdater.publishUpdate(o);
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        super.execute();
        ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
        int version = omr.setStatus(getObject().getId(), status);
        getObject().setVersion(version);
        setExecuted(true);
    }
    
        @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException , MlAuthorizationException{
        super.undo();
        if (previousStatus == null) {
            setUndone(true);
            return;
        }
        getObject().setStatus(previousStatus);
        BulkUpdater.publishUpdate(getObject());
        if (isExecuted()) {
            ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
            if (versionCheck() == false) {
                throw new mlModifiedException("Cannot undo status change because the object has been updated in the meantime.");
            } else {
                int version = omr.setStatus(getObject().getId(), previousStatus);
                getObject().setVersion(version);
                setUndone(true);
            }
        }
    }

    @Override
    public boolean isVersionChecking() {
        return false;
    }

    @Override
    public String toString() {
        return "Status update (" + getFormattedId() + ")";
    }

    @Override
    public String getDetails() {
        return "New status = " + status;
    }
    
}
