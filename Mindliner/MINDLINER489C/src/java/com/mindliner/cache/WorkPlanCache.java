/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.mindliner.cache;

import com.mindliner.analysis.CurrentWorkTask;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcTask;
import com.mindliner.clientobjects.mlcUser;
import com.mindliner.clientobjects.mlcWeekPlan;
import com.mindliner.clientobjects.mlcWorkUnit;
import com.mindliner.contentfilter.TimeFilter.TimePeriod;
import java.util.Date;
import java.util.List;

/**
 *
 * @author M.Messerli
 *
 * 8-NOV-2011
 *
 * This interface describes how work items and planning items are cached and
 * transfered to and from the server. The rest of the application should obtain
 * all work items through this interface and not get them directly from the
 * server in order to enable off-line mode.
 */
public interface WorkPlanCache {

    /**
     * Returns a client version of the weekplan with the specified id.
     *
     * @param id The week plan id.
     * @return The weekplan.
     */
    mlcWeekPlan getWeekPlan(int id);

    /**
     * Returns the weekplan for the specified week (and user) or creates a new
     * one if no such plan exists.
     *
     * @param week The week number for which the plan is requested.
     * @param year The year of the week for which the weekplan is requested.
     * @return The weekplan or null if no such plan exists and no plan can be
     * created.
     */
    mlcWeekPlan getWeekplan(int year, int week);
    
    /**
     * Get the weekplan of others for the specified week
     *
     * @param year
     * @param week
     * @return The weekplans of others or null if no such plans exist.
     */
    List<mlcWeekPlan> getForeignWeekPlans(int year, int week);

    mlcWeekPlan createWeekplan(int year, int week);

    /**
     * Get the total number of elements of work units in cache.
     * @return 
     */
    public int getCount();

    /**
     * Creates a new work unit with the specified parameters and adds them to
     * the local cache. If in online mode the work unit is created on the
     * server, otherwise a dummy work unit is created locally and the command is
     * executed when online.
     *
     * @param start The start time of the work unit
     * @param end The work unit's end date and time
     * @param task The task that was worked on.
     * @param timeZoneId The time zone as provided by getTimeZone.getId()
     * @param plan If true this is a planned work unit, if false this reflectes
     * actual work done.
     * @return
     */
    public int createWorkUnit(mlcTask task, Date start, Date end, String timeZoneId, boolean plan);

    /**
     * Removes the specified work unit from cache and server.
     *
     * @param workUnit The workunit to be removed.
     */
    void removeWorkUnit(mlcWorkUnit workUnit);

    /**
     * Returns the number of minutes that was worked in the specified day and on
     * the specified object. Work units that span outside the day boundary are
     * ignored.
     *
     * @param task The task for which the daily minutes are requsted.
     * @param weekPlan The weekplan
     * @param dayOfWeek The day of the specified week for which the work minutes
     * are needed. The index of Monday is 0. @plan If true only plan units will
     * be counted, if false only actual units will be counted.
     * @param plan Specify true for the planned minutes and false for the actual
     * minutes
     * @param user The user for which the daily work minutes are needed
     * @return
     */
    int getWorkMinutesForDay(mlcUser user, mlcTask task, mlcWeekPlan weekPlan, int dayOfWeek, boolean plan);
   

    /**
     * This call evalutes whether the specified task has been worked on in the
     * specified week and by the calling user
     *
     * @param task The task to analyze
     * @param plan The plan that defines the week and the user
     * @param user The user for which we need to know has she worked on the item
     * in hte specified week
     * @return True if there are actual work units this week, false otherwise
     * (regardless of plan units)
     */
    public boolean hasWorkInWeek(mlcUser user, mlcTask task, mlcWeekPlan plan);

    /**
     * This function calculates the average number of minutes that were spent
     * (by the caller) on the specified object.
     *
     * @param taskId The task for which the past week average is needed. Using
     * id instead of mlcTask object to avoid secondary (outdated) versions
     * @param weekPlanId The id of the current week plan
     * @param numberOfPastWeeks The number of weeks into the past to compute the
     * average with.
     * @return
     */
    int getActualPastWeekAveragesForObject(int taskId, int weekPlanId, int numberOfPastWeeks);

    /**
     * Integrates all the work slots that were registered for the specified
     * week.
     *
     * @param weekPlan The week for which the work unit total's is requested.
     * @return The total number of minutes that the caller has worked in this
     * week.
     */
    int getIntegratedWorkMinutes(mlcWeekPlan weekPlan);

    /**
     * This function is used to back-fill any time between the current time and
     * when the last registered work unit ended.
     *
     * @return Returns the end time of the caller's last registered work unit.
     */
    Date getEndOfLastWorkUnit();

    /**
     * This routine verifies if the cache is in synch with the server by
     * comparing weekplan and workunit counts.
     *
     * @return True if cache is in synch with server, false otherwise.
     */
    boolean isCacheUpToDate();

    /**
     * This call returns a list of tasks that are due in the specified week
     *
     * @param year
     * @param week
     */
    public void ensureMyTasksDueInWeekAreOnPlan(int year, int week);
    
    /**
     * Returns the caller's overdue tasks
     * @return 
     */
    public List<mlcTask> getMyOverdueTasks();
    
    /**
     * Returns the caller's upcoming tasks
     * @param lookAhead The look-ahead period
     * @return 
     */
    public List<mlcTask> getMyUpcomingTasks(TimePeriod lookAhead);
    
    /**
     * Returns the callers priority tasks
     * @return 
     */
    public List<mlcTask> getMyPriorityTasks();
    
    /**
     * Returns a list of non-archived stand-alone objects
     * @return 
     */
    public List<mlcObject> getStandAloneObjects();
    
    /**
     * Sets the specified task as the current work object for the current user
     * @param t The current work task or null to clear the current work task
     */
    public void setCurrentWorkObject(mlcTask t);
    
    /**
     * Returns the list of current tasks.
     * @return 
     */
    public List<CurrentWorkTask> getCurrentWorkTasks();
    
    /**
     * Returns a list of people working on the current task
     * @param task The task for which the workers are requested
     * @return A list of users that are working on the specified task
     */
    public List<mlcUser> getCurrentWorkers(mlcTask task);

}
