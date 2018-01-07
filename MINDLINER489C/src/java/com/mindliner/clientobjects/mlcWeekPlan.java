/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.clientobjects;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.cache.MlCacheException;
import com.mindliner.objects.transfer.mltWeekPlan;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents a weekplan. Its organization differs from the
 * corresponding server class mlsWorkUnit and the transfer class mltWorkUnit to
 * offer more convenience on the client. It merges the work objects and work
 * units into
 *
 * @author Marius Messerli
 */
public class mlcWeekPlan implements Serializable {

    private int id;
    private int version;
    private mlcUser user = null;
    private int year = 0;
    private int week = 0;
    private final List<Integer> taskIds = new ArrayList<>();
    private static final long serialVersionUID = 19640205L;

    public mlcWeekPlan(mltWeekPlan twp) {
        try {
            id = twp.getId();
            version = twp.getVersion();
            user = CacheEngineStatic.getUser(twp.getUserId());
            year = twp.getYear();
            week = twp.getWeekInYear();
            List<mlcObject> objects = CacheEngineStatic.getObjects(twp.getTaskIds());
            for (mlcObject o : objects) {
                if (o instanceof mlcTask) {
                    mlcTask t = (mlcTask) o;
                    taskIds.add(t.getId());
                } else {
                    System.err.println("Warning: ignoring non-task object id= " + o.getId() + " found in weekplan id=" + twp.getId());
                }
            }
        } catch (MlCacheException ex) {
            Logger.getLogger(mlcWeekPlan.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getId() {
        return id;
    }

    public int getWeek() {
        return week;
    }

    public int getYear() {
        return year;
    }

    public List<Integer> getTasksIds() {
        return taskIds;
    }

    public mlcUser getUser() {
        return user;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + this.id;
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
        final mlcWeekPlan other = (mlcWeekPlan) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    public Date getStart() {
        Calendar cal = getCalendarForWeekStart();
        return cal.getTime();
    }

    private Calendar getCalendarForWeekStart() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, getYear());
        cal.set(Calendar.WEEK_OF_YEAR, getWeek());
        cal.set(Calendar.DAY_OF_WEEK, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        // @todo I don't know why this is offset by one week but it works with the offset and fails without (??)
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        return cal;
    }

    private Date getStart(int dayOfWeek) {
        Calendar cal = getCalendarForWeekStart();
        cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        return cal.getTime();
    }

    private Date getEnd(int dayOfWeek) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getStart(dayOfWeek));
        cal.add(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    public Date getEnd() {
        Calendar cal = getCalendarForWeekStart();
        cal.add(Calendar.DAY_OF_YEAR, 7);
        return cal.getTime();
    }

    /**
     * Returns the work units for the specified user, week plan, and day
     *
     * @param user
     * @param task
     * @return
     */
    public List<mlcWorkUnit> getWorkUnitsForWeek(mlcUser user, mlcTask task) {
        List<mlcWorkUnit> resultList = new ArrayList<>();
        Date start = getStart();
        Date end = getEnd();
        for (mlcWorkUnit wu : task.getWorkUnits()) {
            if (start.compareTo(wu.getStart()) <= 0 && wu.getStart().compareTo(end) < 0 && !wu.isPlan()) {
                resultList.add(wu);
            }
        }
        return resultList;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "Year=" + year + ", Week=" + week;
    }
}
