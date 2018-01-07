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
import com.mindliner.serveraccess.RemoteLookupAgent;
import com.mindliner.weekplanner.WeekPlanChangeManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.naming.NamingException;

/**
 *
 * @todo This command needs to initalize the weekplanid; undo does not work
 * currently!!
 * @author Marius Messerli
 */
public class WorkUnitRemovalCommand extends MindlinerOnlineCommand {

    private mlcWorkUnit workUnit = null;
    private int id;
    private final Date start;
    private final Date end;
    private final String timezoneId;
    private final boolean isPlan;

    /**
     * Deletes a work unit
     *
     * @param ignored The pro-forma argument to comply with the super-class
     * @param w The work unit to be deleted
     */
    public WorkUnitRemovalCommand(mlcObject ignored, mlcWorkUnit w) {
        super(null, true);
        workUnit = w;
        start = w.getStart();
        end = w.getEnd();
        timezoneId = w.getTimeZoneId();
        isPlan = w.isPlan();
        mlcObject object = CacheEngineStatic.getObject(w.getTaskId());
        if (object != null) {
            if (object instanceof mlcTask) {
                mlcTask task = (mlcTask) CacheEngineStatic.getObject(w.getTaskId());
                task.getWorkUnits().remove(w);
            }
        }
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        // I don't call super() here because this command has no "object"
        WorkManagerRemote workManager = (WorkManagerRemote) RemoteLookupAgent.getManagerForClass(WorkManagerRemote.class);
        workManager.removeWorkUnit(workUnit.getId());
        mlcWeekPlan weekPlan = CacheEngineStatic.getWeekPlan(start);
        setExecuted(true);
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        super.undo();
        if (isExecuted()) {
            WorkManagerRemote workManager = (WorkManagerRemote) RemoteLookupAgent.getManagerForClass(WorkManagerRemote.class);
            workManager.createWorkUnit(id, start, end, timezoneId, isPlan);
            setUndone(true);
        }
    }

    @Override
    public boolean isVersionChecking() {
        return false;
    }

    @Override
    public String toString() {
        return "Workunit Removal";
    }

    @Override
    public String getDetails() {
        SimpleDateFormat sdf = new SimpleDateFormat();
        return "Removing unit (start=" + sdf.format(workUnit.getStart()) + ", end=" + sdf.format(workUnit.getEnd())
                + ") for object id=" + workUnit.getTaskId();
    }
}
