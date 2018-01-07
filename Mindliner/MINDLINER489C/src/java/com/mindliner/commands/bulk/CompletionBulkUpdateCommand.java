/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands.bulk;

import com.mindliner.clientobjects.ObjectIdLister;
import com.mindliner.clientobjects.mlcNews;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcTask;
import com.mindliner.contentfilter.Completable;
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
public class CompletionBulkUpdateCommand extends BulkUpdateCommand {

    boolean completion = false;
    Map<Integer, Boolean> previousCompletionStates;

    public CompletionBulkUpdateCommand(List<mlcObject> candidates, boolean completion) {
        super(candidates);
        List<Class> complist = new ArrayList<>();
        complist.add(mlcTask.class);
        complist.add(mlcNews.class);
        rejectIncompatibleObjects(complist);
        this.completion = completion;
        previousCompletionStates = new HashMap<>(objects.size());
        for (mlcObject o : objects) {
            Completable c = (Completable) o;
            previousCompletionStates.put(o.getId(), c.isCompleted());
            c.setCompleted(completion);
        }
        BulkUpdater.publishUpdates(objects);
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        super.execute();
        ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
        Map<Integer, Integer> versions = omr.bulkSetCompletion(ObjectIdLister.getIdList(objects), completion);
        updateObjectVersions(versions);
        setExecuted(true);
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        super.undo();
        for (mlcObject o : getObjects()) {
            Boolean pcs = previousCompletionStates.get(o.getId());
            if (pcs != null) {
                ((Completable) o).setCompleted(pcs);
            }
        }
        BulkUpdater.publishUpdates(objects);
        if (isExecuted()) {
            ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
            if (versionCheck() == false) {
                JOptionPane.showMessageDialog(null,
                        "Cannot undo because the object has been updated in the meantime.",
                        "Undo Bulk Completion Update",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                for (mlcObject o : getObjects()) {
                    Boolean pcs = previousCompletionStates.get(o.getId());
                    if (pcs != null) {
                        int version = omr.setCompletionState(o.getId(), pcs);
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
        return "Bulk update completion to: " + completion;
    }

}
