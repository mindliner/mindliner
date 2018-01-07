/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.contentfilter.Timed;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.managers.ObjectManagerRemote;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.main.BulkUpdater;
import com.mindliner.main.MindlinerMain;
import com.mindliner.serveraccess.RemoteLookupAgent;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.naming.NamingException;
import javax.swing.JOptionPane;

/**
 *
 * @author Marius Messerli
 */
public class DueDateUpdateCommand extends MindlinerOnlineCommand {

    private Date dueDate = null;
    private Date previousDueDate = null;

    /**
     * Sets the due date for a task.
     * @param o The object for which the due date is set (if the object does not have a due date the command has no effect)
     * @param d The date or null if the task is to have no due date.
     */
    public DueDateUpdateCommand(mlcObject o, Date d) {
        super(o, true);
        if (!(o instanceof Timed)) {
            throw new IllegalArgumentException("This command needs an object that implements Timed");
        }
        dueDate = d;
        previousDueDate = ((Timed) o).getDueDate();
        ((Timed) o).setDueDate(dueDate);
        o.setModificationDate(new Date());
        BulkUpdater.publishUpdate(o);
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        super.execute();
        if (getObject() instanceof Timed) {
            ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
            int version = omr.setDueDate(getObject().getId(), dueDate);
            getObject().setVersion(version);
            setExecuted(true);
        }
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        super.undo();
        ((Timed)getObject()).setDueDate(previousDueDate);
        BulkUpdater.publishUpdate(getObject());
        if (isExecuted()) {
            ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
            if (versionCheck() == false) {
                JOptionPane.showMessageDialog(MindlinerMain.getInstance(),
                        "Cannot undo because the object has been updated in the meantime.",
                        "Undo Duedate Update",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                int version = omr.setDueDate(getObject().getId(), previousDueDate);
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
        return "Due Date Update (" + getFormattedId() + ")";
    }

    @Override
    public String getDetails() {
        String dateString = "";
        SimpleDateFormat sdf = new SimpleDateFormat();
        if (dueDate == null) {
            dateString = "unscheduled";
        } else {
            dateString = sdf.format(dueDate);
        }
        return "new due date = " + dateString;
    }
}
