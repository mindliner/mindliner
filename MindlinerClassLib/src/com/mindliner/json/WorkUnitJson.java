/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.json;

import com.mindliner.entities.mlsWorkUnit;
import java.util.Date;

/**
 *
 * @author Marius Messerli
 */
public class WorkUnitJson {

    private final int id;
    private int taskId;
    private int taskVersion;
    private int userId;
    private Date start;
    private Date end;
    private String timeZoneId;
    private boolean plan;

    public WorkUnitJson(mlsWorkUnit w) {
        id = w.getId();
        taskId = w.getTask().getId();
        taskVersion = w.getTask().getVersion();
        start = w.getStart();
        end = w.getEnd();
        timeZoneId = w.getTimeZoneId();
        plan = w.isPlan();
        userId = w.getUser().getId();
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getTaskVersion() {
        return taskVersion;
    }

    public void setTaskVersion(int taskVersion) {
        this.taskVersion = taskVersion;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    public boolean isPlan() {
        return plan;
    }

    public void setPlan(boolean plan) {
        this.plan = plan;
    }
    
    
    
    
}
