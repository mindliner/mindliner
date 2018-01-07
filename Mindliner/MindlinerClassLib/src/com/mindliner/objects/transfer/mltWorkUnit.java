/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.objects.transfer;

import com.mindliner.entities.mlsWorkUnit;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Marius Messerli
 */
public class mltWorkUnit implements Serializable {

    private final int id;
    private int taskId = -1;
    private int taskVersion = -1;
    private int userId = -1;
    private Date start = null;
    private Date end = null;
    private String timeZoneId = "";
    private boolean plan = false;
    private static final long serialVersionUID = 19640205L;

    public mltWorkUnit(mlsWorkUnit w) {
        id = w.getId();
        taskId = w.getTask().getId();
        taskVersion = w.getTask().getVersion();
        start = w.getStart();
        end = w.getEnd();
        timeZoneId = w.getTimeZoneId();
        plan = w.isPlan();
        userId = w.getUser().getId();
    }

    public int getId() {
        return id;
    }

    public int getTaskId() {
        return taskId;
    }

    public Date getEnd() {
        return end;
    }

    public Date getStart() {
        return start;
    }

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public int getTaskVersion() {
        return taskVersion;
    }

    /**
     * Indicates whether this is actual or planned work.
     *
     * @return True if this work unit is a plan unit, false if it is an actual
     * work unit.
     */
    public boolean isPlan() {
        return plan;
    }

    public int getUserId() {
        return userId;
    }

}
