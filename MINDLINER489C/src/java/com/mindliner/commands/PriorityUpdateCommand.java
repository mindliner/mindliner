/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands;

import com.mindliner.cache.DefaultObjectAttributes;
import com.mindliner.categories.mlsPriority;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcTask;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.managers.ObjectManagerRemote;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.main.BulkUpdater;
import com.mindliner.serveraccess.RemoteLookupAgent;
import javax.naming.NamingException;

/**
 *
 * @author Marius Messerli
 */
public class PriorityUpdateCommand extends MindlinerOnlineCommand {

    mlsPriority priority = null;
    mlsPriority previousPriority = null;

    public PriorityUpdateCommand(mlcObject o, mlsPriority p) {
        super(o, true);
        if (!(o instanceof mlcTask)){
            throw new IllegalArgumentException("This commands needs a mlcTask as object");
        }
        if (p == null) {
            throw new IllegalArgumentException("priority must not be null");
        }
        previousPriority = ((mlcTask) getObject()).getPriority();
        ((mlcTask) getObject()).setPriority(p);
        DefaultObjectAttributes.updatePriority(p);
        priority = p;
        BulkUpdater.publishUpdate(o);
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        super.execute();
            ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
            int version = omr.setPriority(getObject().getId(), priority.getId());
            getObject().setVersion(version);
            setExecuted(true);
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        super.undo();
        ((mlcTask)getObject()).setPriority(previousPriority);
        DefaultObjectAttributes.updatePriority(previousPriority);
        BulkUpdater.publishUpdate(getObject());
        if (isExecuted()) {
            ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
            if (versionCheck() == false) {
                throw new mlModifiedException("Cannot undo priority change because the object has been updated in the meantime.");
            } else {
                int version = omr.setPriority(getObject().getId(), previousPriority.getId());
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
        return "Task Priority Update (" + getFormattedId() + ")";
    }

    @Override
    public String getDetails() {
        return "New task priority = " + priority.getName();
    }
    
    
}
