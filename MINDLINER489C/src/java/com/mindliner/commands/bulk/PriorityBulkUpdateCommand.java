/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands.bulk;

import com.mindliner.cache.DefaultObjectAttributes;
import com.mindliner.categories.mlsPriority;
import com.mindliner.clientobjects.ObjectIdLister;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcTask;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.managers.ObjectManagerRemote;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.main.BulkUpdater;
import com.mindliner.serveraccess.RemoteLookupAgent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.NamingException;
import javax.swing.JOptionPane;

/**
 *
 * @author Marius Messerli
 */
public class PriorityBulkUpdateCommand extends BulkUpdateCommand {

    mlsPriority priority = null;
    Map<Integer, mlsPriority> previousPriorities;
    mlsPriority previousPriorityDefault;

    public PriorityBulkUpdateCommand(List<mlcObject> candidates, mlsPriority p) {
        super(candidates);
        List<Class> complist = new ArrayList<Class>();
        complist.add(mlcTask.class);
        rejectIncompatibleObjects(complist);

        priority = p;
        previousPriorities = new HashMap<>(objects.size());
        for (mlcObject o : objects) {
            mlcTask t = (mlcTask) o;
            previousPriorities.put(t.getId(), t.getPriority());
            t.setPriority(p);
        }
        previousPriorityDefault = DefaultObjectAttributes.getPriority();
        DefaultObjectAttributes.updatePriority(p);
        BulkUpdater.publishUpdates(objects);
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        super.execute();
        ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
        Map<Integer, Integer> versions = omr.bulkSetPriority(ObjectIdLister.getIdList(objects), priority.getId());
        updateObjectVersions(versions);
        setExecuted(true);
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        super.undo();
        DefaultObjectAttributes.updatePriority(previousPriorityDefault);
        for (mlcObject o : getObjects()) {
            mlsPriority p = previousPriorities.get(o.getId());
            if (p != null) {
                ((mlcTask) o).setPriority(p);
            }
        }
        BulkUpdater.publishUpdates(objects);
        if (isExecuted()) {
            ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
            if (versionCheck() == false) {
                JOptionPane.showMessageDialog(null,
                        "Cannot undo because the object has been updated in the meantime.",
                        "Undo Bulk Priority Update",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                for (mlcObject o : getObjects()) {
                    mlsPriority p = previousPriorities.get(o.getId());
                    if (p != null) {
                        int version = omr.setPriority(o.getId(), p.getId());
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
        return "Updating priority to " + priority;
    }
}
