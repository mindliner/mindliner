/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.mindliner.weekplanner;

import com.mindliner.clientobjects.mlcTask;

/**
 *
 * @author M.Messerli
 */
public interface WeekPlanChangeObserver {
    
    /**
     * Called when the current week has changed.
     * @param newYear
     * @param newWeek 
     */
    public void weekOrPlanChanged(int newYear, int newWeek);
        
    /**
     * The object which is currently worked on has changed
     * @param task The new and now current task.
     */
    public void taskUpdated(mlcTask task);
    
    /**
     * The currently selected object has changed.
     * @param task The newly selected task
     */
    public void objectSelectionChanged(mlcTask task);
    
}
