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
 * This command changes the object's flat that tells whether its relatives are
 * fixed-ordered or floating.
 *
 * @author Marius Messerli
 */
public class RelativesOrderedUpdateCommand extends MindlinerOnlineCommand {

    private boolean ordered = false;
    private boolean previousOrdered = false;

    public RelativesOrderedUpdateCommand(mlcObject o, boolean ordered) {
        super(o, true);
        previousOrdered = getObject().isRelativesOrdered();
        this.ordered = ordered;
        getObject().setRelativesOrdered(ordered);
        BulkUpdater.publishUpdate(o);
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        super.execute();
        ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
        int version;
        version = omr.setRelativesOrdered(getObject().getId(), ordered);
        getObject().setVersion(version);
        setExecuted(true);
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        super.undo();
        getObject().setRelativesOrdered(previousOrdered);
        BulkUpdater.publishUpdate(getObject());
        if (isExecuted()) {
            ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
            if (versionCheck() == false) {
                throw new mlModifiedException("Cannot undo relative's ordering because the object has been updated in the meantime.");
            } else {
                int version = omr.setRelativesOrdered(getObject().getId(), previousOrdered);
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
        return "Updating Relatives Order Flag (" + getFormattedId() + ")";
    }

    @Override
    public String getDetails() {
        return "New relative ordering is " + ordered;
    }
}
