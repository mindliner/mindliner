/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.clientobjects;

import com.mindliner.objects.transfer.mltWorkUnit;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import java.util.Objects;

/**
 *
 * @author Marius Messerli
 */
public class mlcWorkUnit implements Serializable {

    private int id = -1;
    private int taskId = -1;
    private int userId = -1;
    private Date start = null;
    private Date end = null;
    private String timeZoneId = "";
    private boolean plan = false;
    private static final long serialVersionUID = 19640205L;

    public mlcWorkUnit(mltWorkUnit tw) {
        if (tw.getEnd() == null) {
            throw new IllegalStateException("Slot end is null.");
        }
        if (tw.getStart() == null) {
            throw new IllegalStateException("Slot start is null.");
        }
        id = tw.getId();
        start = tw.getStart();
        end = tw.getEnd();
        plan = tw.isPlan();
        timeZoneId = tw.getTimeZoneId();
        taskId = tw.getTaskId();
        userId = tw.getUserId();
    }

    public mlcWorkUnit(mlcUser user, mlcTask task, mlcWeekPlan weekPlan, Date start, Date end, String timezoneId, boolean isPlan) {
        this.taskId = task.getId();
        this.start = start;
        this.end = end;
        this.timeZoneId = timezoneId;
        plan = isPlan;
        userId = user.getId();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int id) {
        this.taskId = id;
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

    public boolean isPlan() {
        return plan;
    }

    public int getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        DateFormat dft = DateFormat.getTimeInstance(DateFormat.SHORT);
        Long millis = end.getTime() - start.getTime();
        int minutes = (int) (millis / 1000 / 60);
        return df.format(start) + " to " + dft.format(end) + " " + Integer.toString(minutes) + " min";
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 13 * hash + this.id;
        hash = 13 * hash + this.taskId;
        hash = 13 * hash + Objects.hashCode(this.start);
        hash = 13 * hash + Objects.hashCode(this.timeZoneId);
        hash = 13 * hash + (this.plan ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final mlcWorkUnit other = (mlcWorkUnit) obj;
        if (this.id != other.id) {
            return false;
        }
        if (this.taskId != other.taskId) {
            return false;
        }
        if (!Objects.equals(this.start, other.start)) {
            return false;
        }
        if (!Objects.equals(this.end, other.end)) {
            return false;
        }
        if (!Objects.equals(this.timeZoneId, other.timeZoneId)) {
            return false;
        }
        if (this.plan != other.plan) {
            return false;
        }
        return true;
    }

}
