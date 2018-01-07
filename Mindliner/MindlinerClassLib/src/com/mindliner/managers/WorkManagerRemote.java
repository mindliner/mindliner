/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.analysis.WeekPlanSignature;
import com.mindliner.objects.transfer.mltWeekPlan;
import com.mindliner.objects.transfer.mltWorkUnit;
import java.util.List;
import javax.ejb.Remote;

/**
 *
 * @author marius
 */
@Remote
public interface WorkManagerRemote {

    /**
     * Creates a new work unit for the specified week.
     *
     * @param taskId The task id.
     * @param startTime
     * @param endTime
     * @param timeZoneId
     * @param plan If true this work unit is a plan unit, if false it is actual
     * work.
     * @return The transfer object of the new work unit
     */
    public mltWorkUnit createWorkUnit(int taskId, java.util.Date startTime, java.util.Date endTime, String timeZoneId, boolean plan);

    /**
     * Returns the weekplan for the specified week and the calling user. If no weekplan exists 
     * it will be created and a new and empty weekplan is returned.
     *
     * @param year The year of the weekplan
     * @param week The week number in the year
     * @return The weekplan transfer object. Null is returned if no weekplan
     * exists and none could be created for the calling user and the specified
     * date.
     */
    public mltWeekPlan getWeekPlan(int year, int week);
        
    /**
     * Returns the week plan for the current week
     * @return 
     */
    public mltWeekPlan getCurrentWeekPlan();

    /**
     * Checks the specified key parameters against the server's version and returns
     * the ids of the weekplans that are out of date.
     * current.
     *
     * @param clientWeekplanSignatures The signatures of the weekplans stored in the client cache
     * @return The ids of those weekplans that are out of date
     */
    public List<Integer> getOutdatedWeekplanIds(List<WeekPlanSignature> clientWeekplanSignatures);
    
    /**
     * This call returns the foreign (not me) weekplans for the specified week or null if none exists.
     * @param year
     * @param week
     * @return The weekplans or null if no such weekplan exists.
     */
    public List<mltWeekPlan> getForeignWeekPlans(int year, int week);
        
    /**
     * Returns the weekplan for the specified date.
     *
     * @param id The id of an existing plan.
     * @return The weekplan or null if no plan exists for the calling user and
     * the specified date.
     */
    public mltWeekPlan getWeekPlanById(int id);

    /**
     * Add a task to the specified weekplan if it is not already part of it. If
     *
     * @param planId The plan id.
     * @param taskId The task to be added. If a non-task id is specified, then it will be ignored.
     * 
     */
    public void addToWeekPlan(int planId, int taskId);

    public void removeFromWorkPlan(int planId, int taskId);

    /**
     * Creates a new weekplan for the calling user. There can only be one
     * weekplan per week and user. If a weekplan exists already it is returned
     * instead of a new plan.
     *
     * @param week The week number in the current year
     * @return The id of the weekplan.
     */
    public int createWeekPlan(int year, int week);


    /**
     * Deletes the specified work unit.
     *
     * @param id
     */
    public void removeWorkUnit(int id);
    
}
