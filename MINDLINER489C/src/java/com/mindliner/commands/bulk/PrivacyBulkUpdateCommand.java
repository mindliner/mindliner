/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands.bulk;

import com.mindliner.cache.DefaultObjectAttributes;
import com.mindliner.clientobjects.ObjectIdLister;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.exceptions.ForeignOwnerException;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.managers.ObjectManagerRemote;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.main.BulkUpdater;
import com.mindliner.serveraccess.RemoteLookupAgent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.swing.JOptionPane;

/**
 *
 * @author Marius Messerli
 */
public class PrivacyBulkUpdateCommand extends BulkUpdateCommand {

    boolean privacy = false;
    private final Map<Integer, Boolean> previousPrivacies;
    private final boolean prevDefaultPrivacy;

    public PrivacyBulkUpdateCommand(List<mlcObject> candidates, boolean privacy) {
        super(candidates);
        previousPrivacies = new HashMap<>(objects.size());
        for (mlcObject o : objects) {
            previousPrivacies.put(o.getId(), o.isPrivateAccess());
            o.setPrivateAccess(privacy);
        }
        this.privacy = privacy;
        prevDefaultPrivacy = DefaultObjectAttributes.getPrivateAccess();
        DefaultObjectAttributes.updatePrivateAccess(privacy);
        BulkUpdater.publishUpdates(objects);
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        super.execute();
        ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
        Map<Integer, Integer> versions = omr.bulkSetPrivacy(ObjectIdLister.getIdList(objects), privacy);
        updateObjectVersions(versions);
        setExecuted(true);
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        super.undo();
        DefaultObjectAttributes.updatePrivateAccess(prevDefaultPrivacy);
        for (mlcObject o : getObjects()) {
            Boolean p = previousPrivacies.get(o.getId());
            if (p != null) {
                o.setPrivateAccess(p);
            }
        }
        BulkUpdater.publishUpdates(objects);
        if (isExecuted()) {
            ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
            if (versionCheck() == false) {
                JOptionPane.showMessageDialog(null,
                        "Cannot undo because the object has been updated in the meantime.",
                        "Undo Bulk Privacy Update",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                for (mlcObject o : getObjects()) {
                    Boolean p = previousPrivacies.get(o.getId());
                    if (p != null) {
                        try {
                            int version = omr.setPrivacyFlag(o.getId(), p);
                            o.setVersion(version);
                        } catch (ForeignOwnerException ex) {
                            Logger.getLogger(PrivacyBulkUpdateCommand.class.getName()).log(Level.SEVERE, null, ex);
                        }
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
        return "Privacy Update To " + Boolean.toString(privacy);
    }
}
