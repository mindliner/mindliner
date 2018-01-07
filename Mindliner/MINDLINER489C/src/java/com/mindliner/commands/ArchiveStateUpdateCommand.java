/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.contentfilter.Completable;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.main.BulkUpdater;
import com.mindliner.main.MindlinerMain;
import com.mindliner.managers.ObjectManagerRemote;
import com.mindliner.serveraccess.RemoteLookupAgent;
import javax.naming.NamingException;
import javax.swing.JOptionPane;

/**
 * This command updates the archive state of an object.
 *
 * @author Marius Messerli
 */
public class ArchiveStateUpdateCommand extends MindlinerOnlineCommand {

    private boolean archived = false;
    private boolean previousArchived = false;

    public ArchiveStateUpdateCommand(mlcObject o, boolean archiveState) {
        super(o, true);
        previousArchived = o.isArchived();
        o.setArchived(archiveState);
        this.archived = archiveState;
        BulkUpdater.publishUpdate(o);
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        super.execute();
        ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
        int version = omr.setArchived(getObject().getId(), archived);
        getObject().setVersion(version);
        setExecuted(true);
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        super.undo();
        getObject().setArchived(previousArchived);
        BulkUpdater.publishUpdate(getObject());
        if (isExecuted()) {
            if (versionCheck() == false) {
                JOptionPane.showMessageDialog(MindlinerMain.getInstance(),
                        "Cannot undo because the object has been updated in the meantime.",
                        "Undo Archive State",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
                int version = omr.setArchived(getObject().getId(), previousArchived);
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
        return "Archived State Update (" + getFormattedId() + ")";
    }

}
