/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands.bulk;

import com.mindliner.cache.DefaultObjectAttributes;
import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.clientobjects.ObjectIdLister;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.managers.ObjectManagerRemote;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.main.BulkUpdater;
import com.mindliner.serveraccess.RemoteLookupAgent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.naming.NamingException;
import javax.swing.JOptionPane;

/**
 *
 * @author Marius Messerli
 */
public class ConfidentialityBulkUpdateCommand extends BulkUpdateCommand {

    mlsConfidentiality confidentiality = null;
    Map<Integer, mlsConfidentiality> previoudConfidentialities;
    mlsConfidentiality previousConfidentialityDefault;

    /**
     * This call removes all objects that do not belong to the client of the
     * first object
     *
     * @param objects
     */
    private void removeForeignClientObjects(List<mlcObject> objects) {
        if (objects.isEmpty() || objects.size() == 1) {
            return;
        }
        int clientId = objects.get(0).getClient().getId();
        Iterator it = objects.iterator();
        while (it.hasNext()) {
            mlcObject o = (mlcObject) it.next();
            if (o.getClient().getId() != clientId) {
                it.remove();
            }
        }
    }

    /**
     * The constructor will
     *
     * @param candidates Object submitted to the confi change; objects that
     * don't belong to the client of the first object will be removed from the
     * list in the constructor
     */
    public ConfidentialityBulkUpdateCommand(List<mlcObject> candidates, mlsConfidentiality confi) {
        super(candidates);
        removeForeignClientObjects(objects);
        confidentiality = confi;
        previoudConfidentialities = new HashMap<>(objects.size());
        for (mlcObject o : objects) {
            previoudConfidentialities.put(o.getId(), o.getConfidentiality());
            o.setConfidentiality(confi);
        }
        if (!objects.isEmpty()) {
            mlcObject firstObject = objects.get(0);
            previousConfidentialityDefault = DefaultObjectAttributes.getConfidentiality(firstObject.getId());
            DefaultObjectAttributes.updateConfidentiality(confi);
        }
        BulkUpdater.publishUpdates(objects);
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        super.execute();
        ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
        Map<Integer, Integer> versions = omr.bulkSetConfidentiality(ObjectIdLister.getIdList(objects), confidentiality.getId());
        updateObjectVersions(versions);
        setExecuted(true);
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        super.undo();
        for (mlcObject o : getObjects()) {
            mlsConfidentiality pc = previoudConfidentialities.get(o.getId());
            if (pc != null) {
                o.setConfidentiality(pc);
            }
        }
        BulkUpdater.publishUpdates(objects);
        if (isExecuted()) {
            ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
            if (versionCheck() == false) {
                JOptionPane.showMessageDialog(null,
                        "Cannot undo because the object has been updated in the meantime.",
                        "Undo Bulk Confidentiality Update",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                for (mlcObject o : getObjects()) {
                    mlsConfidentiality pc = previoudConfidentialities.get(o.getId());
                    if (pc != null) {
                        int version = omr.setConfidentiality(o.getId(), pc.getId());
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
        return "bulk update conf to " + confidentiality;
    }
}
