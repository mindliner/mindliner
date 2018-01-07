/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcWeekPlan;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.managers.WorkManagerRemote;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.serveraccess.RemoteLookupAgent;
import com.mindliner.weekplanner.WeekPlanChangeManager;
import javax.naming.NamingException;

/**
 * This command removes a task from a weekplan.
 * 
 * @author Marius Messerli
 */
public class WeekPlanRemoveTaskCommand extends MindlinerOnlineCommand {

    private mlcWeekPlan plan = null;

    public WeekPlanRemoveTaskCommand(mlcObject o, mlcWeekPlan w) {
        super(o, true);
        plan = w;
        // remove from cache for offline mode
        plan.getTasksIds().remove((Integer) o.getId());
        WeekPlanChangeManager.weekChanged(plan.getYear(), plan.getWeek());
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        super.execute();
        WorkManagerRemote workManager = (WorkManagerRemote) RemoteLookupAgent.getManagerForClass(WorkManagerRemote.class);
        workManager.removeFromWorkPlan(plan.getId(), getObject().getId());
        WeekPlanChangeManager.weekChanged(plan.getYear(), plan.getWeek());
        setExecuted(true);
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        super.undo();
        if (isExecuted()) {
            WorkManagerRemote workManager = (WorkManagerRemote) RemoteLookupAgent.getManagerForClass(WorkManagerRemote.class);
            workManager.addToWeekPlan(plan.getId(), getObject().getId());
            setUndone(true);
        }
    }

    @Override
    public boolean isVersionChecking() {
        return false;
    }

    @Override
    public String toString() {
        return "Weekplan Object Removal (" + getFormattedId() + ")";
    }

    @Override
    public String getDetails() {
        return "Removing object id=" + getFormattedId() + " from plan " + plan.toString();
    }

}
