/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.main.BulkUpdater;
import com.mindliner.managers.ObjectManagerRemote;
import com.mindliner.serveraccess.RemoteLookupAgent;
import javax.naming.NamingException;

/**
 * This command updates the description of an object
 *
 * @author Marius Messerli
 */
public class DescriptionUpdateCommand extends MindlinerOnlineCommand {

    private final String description;
    private final String previousDescription;

    public DescriptionUpdateCommand(mlcObject o, String description) {
        super(o, true);
        previousDescription = getObject().getDescription();
        this.description = description;
        getObject().setDescription(description);
        BulkUpdater.publishUpdate(o);
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        super.execute();
        ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
        if (!versionCheck()) {
            throw new mlModifiedException("Cannot undo description change because the object has been updated in the meantime.");
        } else {
            int version = omr.setDescription(getObject().getId(), description);
            getObject().setVersion(version);
            setExecuted(true);
        }
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {

        super.undo();
        getObject().setDescription(previousDescription);
        BulkUpdater.publishUpdate(getObject());
        if (isExecuted()) {
            ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
            if (versionCheck() == false) {
                throw new mlModifiedException("Cannot undo description change because the object has been updated in the meantime.");
            } else {
                int version = omr.setDescription(getObject().getId(), previousDescription);
                getObject().setVersion(version);
                setUndone(true);
            }
        }
    }

    @Override
    public boolean isVersionChecking() {
        return true;
    }

    @Override
    public String toString() {
        return "Updating Description (" + getFormattedId() + ")";
    }

    @Override
    public String getDetails() {
        return "New description is " + description;
    }
}
