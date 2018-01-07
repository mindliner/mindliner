/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.objects.transfer;

import com.mindliner.entities.mlsObject;
import com.mindliner.entities.mlsTask;
import com.mindliner.entities.mlsWeekPlan;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Transfer object for weekplans. This transfer object contains two arrays, one
 * for object signatures, one for work units
 *
 * @author Marius Messerli
 */
public class mltWeekPlan implements Serializable {

    private final int id;
    private final int version;
    private int userId = -1;
    private int year = 0;
    private int weekInYear = 0;
    private final List<Integer> taskIds = new ArrayList<>();
    private static final long serialVersionUID = 19640205L;

    public mltWeekPlan(mlsWeekPlan wp) {
        userId = wp.getUser().getId();
        id = wp.getId();
        version = wp.getVersion();
        year = wp.getYear();
        weekInYear = wp.getWeekInYear();
        for (mlsObject o : wp.getObjects()) {
            if (!(o instanceof mlsTask)) {
                System.err.println("Warning: ignoring non-task object id= " + o.getId() + " found in weekplan id=" + wp.getId());
            } else {
                taskIds.add(o.getId());
            }
        }
    }

    public int getId() {
        return id;
    }

    public int getWeekInYear() {
        return weekInYear;
    }

    public int getYear() {
        return year;
    }

    public List<Integer> getTaskIds() {
        return taskIds;
    }

    public int getUserId() {
        return userId;
    }

    public int getVersion() {
        return version;
    }
}
