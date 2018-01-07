/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcTask;
import com.mindliner.clientobjects.mlcWeekPlan;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.managers.WorkManagerRemote;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.serveraccess.RemoteLookupAgent;
import com.mindliner.weekplanner.WeekPlanChangeManager;
import javax.naming.NamingException;

/**
 * This command adds a task to a weekplan
 *
 * @author Marius Messerli
 */
public class WeekplanAddTaskCommand extends MindlinerOnlineCommand {

    private mlcWeekPlan plan = null;
    private boolean validArgument = true;

    public WeekplanAddTaskCommand(mlcObject o, mlcWeekPlan p) {
        super(o, false);
        if (o instanceof mlcTask) {
            p.getTasksIds().add(o.getId());
            WeekPlanChangeManager.weekChanged(p.getYear(), p.getWeek());
        } else {
            validArgument = false;
        }
        plan = p;
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        if (validArgument) {
            super.execute();
            WorkManagerRemote workManager = (WorkManagerRemote) RemoteLookupAgent.getManagerForClass(WorkManagerRemote.class);
            workManager.addToWeekPlan(plan.getId(), getObject().getId());
            WeekPlanChangeManager.weekChanged(plan.getYear(), plan.getWeek());
            setExecuted(true);
        }
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        super.undo();
        if (validArgument && isExecuted()) {
            WorkManagerRemote workManager = (WorkManagerRemote) RemoteLookupAgent.getManagerForClass(WorkManagerRemote.class);
            workManager.removeFromWorkPlan(plan.getId(), getObject().getId());
            setUndone(true);
        }
    }

    @Override
    public boolean isVersionChecking() {
        return false;
    }

    @Override
    public String toString() {
        return "Weekplan Addition (" + getFormattedId() + ")";
    }

    @Override
    public String getDetails() {
        return "Adding object id=" + getFormattedId() + " to plan " + plan.toString();
    }
}
