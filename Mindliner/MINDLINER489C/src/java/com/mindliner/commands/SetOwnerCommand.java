/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcUser;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.managers.ObjectManagerRemote;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.main.BulkUpdater;
import com.mindliner.serveraccess.RemoteLookupAgent;
import javax.naming.NamingException;
import javax.swing.JOptionPane;

/**
 *
 * @author Marius Messerli
 */
public class SetOwnerCommand extends MindlinerOnlineCommand {

    mlcUser owner;
    mlcUser previousOwner = null;

    /**
     * Constructor creates an instance <b>and</b> set the owner of the specified
     * object to the specified owner. This ensures that the client side is kept
     * updated even if no execution occurs (system in offline mode).
     *
     * @param o The object who's owner needs to be set.
     * @param owner The new owner.
     */
    public SetOwnerCommand(mlcObject o, mlcUser owner) {
        super(o, true);
        this.owner = owner;
        previousOwner = o.getOwner();
        if (owner.getClientIds().contains(o.getClient().getId())) {
            o.setOwner(owner);
            BulkUpdater.publishUpdate(o);
        }
    }

    @Override
    public void execute() throws mlModifiedException, NamingException {
        try {
            super.execute();
            ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
            int version = omr.setOwner(getObject().getId(), owner.getId());
            getObject().setVersion(version);
        } catch (MlAuthorizationException ex) {
            JOptionPane.showMessageDialog(null,
                    ex.getMessage(),
                    "Owner Update",
                    JOptionPane.ERROR_MESSAGE);
        }
        setExecuted(true);
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        super.undo();
        getObject().setOwner(previousOwner);
        BulkUpdater.publishUpdate(getObject());
        if (isExecuted()) {
            ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
            if (versionCheck() == false) {
                JOptionPane.showMessageDialog(null,
                        "Cannot undo because the object has been updated in the meantime.",
                        "Undo Owner Update",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                int version;
                try {
                    version = omr.setOwner(getObject().getId(), previousOwner.getId());
                    getObject().setVersion(version);
                } catch (MlAuthorizationException ex) {
                    // should never happen unless model is broken
                    System.err.println("Unexpected: previous owner seems to be missing from objects's data pool?");
                }
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
        return "Owner Update (" + getFormattedId() + ")";
    }

    @Override
    public String getDetails() {
        return "New owner = " + owner;
    }
}
