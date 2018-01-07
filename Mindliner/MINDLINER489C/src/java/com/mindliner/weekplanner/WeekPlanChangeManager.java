/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.mindliner.weekplanner;

import com.mindliner.clientobjects.mlcTask;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages update tasks in case the week plan of any of its elements have
 * changed
 *
 * @author M.Messerli
 */
public class WeekPlanChangeManager {

    private static final List<WeekPlanChangeObserver> changeObservers = new ArrayList<>();

    public static void addObserver(WeekPlanChangeObserver observer) {
        if (observer == null) {
            throw new IllegalArgumentException("The observer must not be null.");
        }
        changeObservers.add(observer);
    }

    public static void removeObserver(WeekPlanChangeObserver observer) {
        changeObservers.remove(observer);
    }

    /**
     * Notifies all observers that the current week has changed.
     *
     * @param newYear
     * @param newWeek
     */
    public static void weekChanged(int newYear, int newWeek) {
        for (WeekPlanChangeObserver o : changeObservers) {
            o.weekOrPlanChanged(newYear, newWeek);
        }
    }
    
    
    public static void taskUpdated(mlcTask t){
        for (WeekPlanChangeObserver o : changeObservers) {
            o.taskUpdated(t);
        }
    }

    public static void selectionChanged(mlcTask task) {
        if (task != null) {
            for (WeekPlanChangeObserver observer : changeObservers) {
                observer.objectSelectionChanged(task);
            }
        }
    }

}
