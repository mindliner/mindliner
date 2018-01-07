/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands.bulk;

import com.mindliner.clientobjects.ObjectIdLister;
import com.mindliner.clientobjects.mlcNews;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcTask;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.managers.ObjectManagerRemote;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.main.BulkUpdater;
import com.mindliner.serveraccess.RemoteLookupAgent;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.NamingException;
import javax.swing.JOptionPane;

/**
 *
 * @author Marius Messerli
 */
public class DueDateBulkUpdateCommand extends BulkUpdateCommand {

    Map<Integer, Date> previousDueDates;
    private Date date = null;

    public DueDateBulkUpdateCommand(List<mlcObject> candidates, Date date) {
        super(candidates);
        List<Class> complist = new ArrayList<Class>();
        complist.add(mlcTask.class);
        rejectIncompatibleObjects(complist);

        this.date = date;
        previousDueDates = new HashMap<Integer, Date>(objects.size());
        for (mlcObject o : objects) {
            mlcTask t = (mlcTask) o;
            previousDueDates.put(t.getId(), t.getDueDate());
            t.setDueDate(date);
        }
        BulkUpdater.publishUpdates(objects);
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        super.execute();
        ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
        Map<Integer, Integer> versions = omr.bulkSetDueDate(ObjectIdLister.getIdList(objects), date);
        updateObjectVersions(versions);
        setExecuted(true);
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        super.undo();
        for (mlcObject o : getObjects()) {
            Date pdd = previousDueDates.get(o.getId());
            if (pdd != null) {
                ((mlcTask) o).setDueDate(pdd);
            }
        }
        BulkUpdater.publishUpdates(objects);
        if (isExecuted()) {
            ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
            if (versionCheck() == false) {
                JOptionPane.showMessageDialog(null,
                        "Cannot undo because the object has been updated in the meantime.",
                        "Undo Bulk Duedate Update",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                for (mlcObject o : getObjects()) {
                    Date pdd = previousDueDates.get(o.getId());
                    if (pdd != null) {
                        int version = omr.setDueDate(o.getId(), pdd);
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
        return "Duedate bulk update to: " + date;
    }
}
