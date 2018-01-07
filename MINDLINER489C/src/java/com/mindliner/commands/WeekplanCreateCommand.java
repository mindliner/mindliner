/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.mindliner.commands;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.managers.WorkManagerRemote;
import com.mindliner.serveraccess.RemoteLookupAgent;
import javax.naming.NamingException;

/**
 *
 * @author Marius Messerli
 */
public class WeekplanCreateCommand extends MindlinerOnlineCommand {

    private int year = 1900;
    private int week = -1;
    private int weekPlanId = -1;

    public WeekplanCreateCommand(mlcObject o, int week, int year) {
        super(o, false);
        this.year = year;
        this.week = week;
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        super.execute();
        WorkManagerRemote workManager = (WorkManagerRemote) RemoteLookupAgent.getManagerForClass(WorkManagerRemote.class);
        weekPlanId = workManager.createWeekPlan(year, week);
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        super.undo();
        // no op
    }

    public int getWeekPlanId() {
        return weekPlanId;
    }

    @Override
    public boolean isVersionChecking() {
        return false;
    }

    @Override
    public String getDetails() {
        return "New plan for year=" + year + ", week=" + week;
    }

    @Override
    public String toString() {
        return "Weekplan Creation";
    }
    
    
}
