/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcTask;
import com.mindliner.clientobjects.mlcWeekPlan;
import com.mindliner.clientobjects.mlcWorkUnit;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.managers.WorkManagerRemote;
import com.mindliner.objects.transfer.mltWorkUnit;
import com.mindliner.serveraccess.OnlineManager;
import com.mindliner.serveraccess.RemoteLookupAgent;
import com.mindliner.weekplanner.WeekPlanChangeManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import javax.naming.NamingException;
import javax.swing.JOptionPane;

/**
 * This command creates a new work unit. The term work unit is used for both
 * actual time spent as well as planned time.
 *
 * @author Marius Messerli
 */
public class WorkUnitCreationCommand extends MindlinerOnlineCommand {

    private final Date start;
    private final Date end;

    // indicates whether this work unit is for actual spent time (plan==false) or planned work (plan==true)
    private final boolean isPlanUnit;

    private final String timeZoneId;
    private mlcWorkUnit workUnit;
    private final mlcWeekPlan weekPlan;

    public WorkUnitCreationCommand(mlcObject o, Date startTime, Date endTime, String timeZoneId, boolean plan) {
        super(o, false);
        this.start = startTime;
        this.end = endTime;
        this.timeZoneId = timeZoneId;
        // @todo the following call forces the client to wait for the server even in asynch mode
        this.weekPlan = CacheEngineStatic.getWeekPlan(start);
        this.isPlanUnit = plan;
        // create temp object and post task update in case we are offline
        workUnit = new mlcWorkUnit(CacheEngineStatic.getCurrentUser(), (mlcTask) o, weekPlan, start, end, timeZoneId, plan);
        if (!OnlineManager.waitForServerMessages()) {
            mlcTask t = (mlcTask) o;
            t.getWorkUnits().add(workUnit);
            WeekPlanChangeManager.taskUpdated(t);
        }
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        super.execute();
        WorkManagerRemote workManager = (WorkManagerRemote) RemoteLookupAgent.getManagerForClass(WorkManagerRemote.class);
        TimeZone t = TimeZone.getTimeZone(timeZoneId);
        mltWorkUnit tUnit = workManager.createWorkUnit(getObject().getId(), start, end, t.getID(), isPlanUnit);
        if (tUnit == null) {
            JOptionPane.showMessageDialog(null, "Work unit not created.", "Work Unit Creation Error", JOptionPane.ERROR_MESSAGE);
        }
        workUnit = new mlcWorkUnit(tUnit);
        setExecuted(true);
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        super.undo();

        if (isExecuted()) {
            WorkManagerRemote workManager = (WorkManagerRemote) RemoteLookupAgent.getManagerForClass(WorkManagerRemote.class
            );
            workManager.removeWorkUnit(workUnit.getId());
            setUndone(isPlanUnit);
        }
    }

    @Override
    public String toString() {
        return "Workunit Addition (" + getFormattedId() + ")";
    }

    @Override
    public String getDetails() {
        SimpleDateFormat sdf = new SimpleDateFormat();
        return "Adding work unit: start=" + sdf.format(start) + ", end=" + sdf.format(end) + " to plan=" + weekPlan.toString();
    }

    @Override
    public boolean isVersionChecking() {
        return false;
    }

    /**
     * Post execution it returns the work unit id, otherwise returns -1
     *
     * @return The work unit's id or -1 if the work unit has not been registered
     * with the server yet
     */
    public mlcWorkUnit getWorkUnit() {
        return workUnit;
    }
}
