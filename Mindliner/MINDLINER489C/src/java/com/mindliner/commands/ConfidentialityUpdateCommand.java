/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands;

import com.mindliner.cache.DefaultObjectAttributes;
import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.managers.ObjectManagerRemote;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.main.BulkUpdater;
import com.mindliner.main.MindlinerMain;
import com.mindliner.serveraccess.RemoteLookupAgent;
import javax.naming.NamingException;
import javax.swing.JOptionPane;

/**
 *
 * @author Marius Messerli
 */
public class ConfidentialityUpdateCommand extends MindlinerOnlineCommand {

    mlsConfidentiality confidentiality = null;
    mlsConfidentiality previousConfidentiality = null;

    /**
     * The constructor initializes the current state of the object and immediately applies the requested update. This avoids having to update the object in the code that is creating this command. It
     * also changes the default confidentiality.
     *
     * @param o The object that needs a new confidentiality.
     * @param c The new confidentiality.
     */
    public ConfidentialityUpdateCommand(mlcObject o, mlsConfidentiality c) {
        super(o, true);
        assert o.getClient().getId() == c.getClient().getId() : "The specified confidentiality must belong to the object's data pool";
        confidentiality = c;
        previousConfidentiality = o.getConfidentiality();
        DefaultObjectAttributes.updateConfidentiality(confidentiality);
        o.setConfidentiality(confidentiality);
        BulkUpdater.publishUpdate(o);
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        super.execute();
        ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
        int version = omr.setConfidentiality(getObject().getId(), confidentiality.getId());
        getObject().setVersion(version);
        setExecuted(true);
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        super.undo();
        getObject().setConfidentiality(previousConfidentiality);
        DefaultObjectAttributes.updateConfidentiality(previousConfidentiality);
        BulkUpdater.publishUpdate(getObject());
        if (isExecuted()) {
            ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
            if (versionCheck() == false) {
                JOptionPane.showMessageDialog(MindlinerMain.getInstance(),
                        "Cannot undo because the object has been updated in the meantime.",
                        "Undo Confidentiality Change",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                int version = omr.setConfidentiality(getObject().getId(), previousConfidentiality.getId());
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
        return "Confidentiality Update" + getFormattedId();
    }

    @Override
    public String getDetails() {
        return "Old Conf=" + previousConfidentiality + ", New Conf=" + confidentiality;
    }
    
    
}
