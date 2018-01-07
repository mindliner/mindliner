/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.cache.DefaultObjectAttributes;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.exceptions.ForeignOwnerException;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.main.BulkUpdater;
import com.mindliner.managers.ObjectManagerRemote;
import com.mindliner.serveraccess.RemoteLookupAgent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.swing.JOptionPane;

/**
 *
 * @author Marius Messerli
 */
public class PrivacyUpdateCommand extends MindlinerOnlineCommand {

    private boolean foreignObject = false;
    private boolean privacy = false;
    private boolean previousPrivacy = false;

    public PrivacyUpdateCommand(mlcObject o, boolean p) {
        super(o, true);
        if (!o.getOwner().equals(CacheEngineStatic.getCurrentUser())) {
            JOptionPane.showMessageDialog(null, "Ignoring this change request: only the owner of an object can make this private", "Foreign Object", JOptionPane.ERROR_MESSAGE);
            foreignObject = true;
        } else {
            previousPrivacy = getObject().isPrivateAccess();
            privacy = p;
            getObject().setPrivateAccess(p);
            DefaultObjectAttributes.updatePrivateAccess(privacy);
            BulkUpdater.publishUpdate(o);
        }
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        super.execute();
        if (!foreignObject) {
            ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
            int version;
            try {
                version = omr.setPrivacyFlag(getObject().getId(), privacy);
                getObject().setVersion(version);
            } catch (ForeignOwnerException ex) {
                // should never happen as this case is handled before
                Logger.getLogger(PrivacyUpdateCommand.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        setExecuted(true);
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        super.undo();
        getObject().setPrivateAccess(previousPrivacy);
        BulkUpdater.publishUpdate(getObject());
        if (isExecuted() && !foreignObject) {
            ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
            if (versionCheck() == false) {
                throw new mlModifiedException("Cannot undo privacy change because the object has been updated in the meantime.");
            } else {
                try {
                    int version = omr.setPrivacyFlag(getObject().getId(), previousPrivacy);
                    DefaultObjectAttributes.updatePrivateAccess(previousPrivacy);
                    getObject().setVersion(version);
                    setUndone(true);
                } catch (ForeignOwnerException ex) {
                    // should never happen as this case is handled before
                    Logger.getLogger(PrivacyUpdateCommand.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public boolean isVersionChecking() {
        return false;
    }

    @Override
    public String toString() {
        return "Updating Privacy Flag (" + getFormattedId() + ")";
    }

    @Override
    public String getDetails() {
        return "New privacy flag is " + privacy;
    }
}
