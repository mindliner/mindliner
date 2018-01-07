/*
 * Task.java
 *
 * Created on 27. Dezember 2005, 22:28
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package com.mindliner.clientobjects;

import com.mindliner.categories.mlsPriority;
import com.mindliner.contentfilter.Completable;
import com.mindliner.contentfilter.Timed;
import com.mindliner.entities.ObjectAttributes;
import com.mindliner.main.MindlinerMain;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author Marius Messerli
 */
public class mlcTask extends mlcObject implements Timed, Completable, Comparable, Serializable {

    public static Date parseDate(String dateString) {
        DateFormat dateFormatInstance = DateFormat.getDateInstance(DateFormat.SHORT);
        if (dateString.equals("none")) {
            return null;
        }
        try {
            return dateFormatInstance.parse(dateString);
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(MindlinerMain.getInstance(), ex.getMessage(), "Date format invalid.", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public static String formatDate(Date date) {
        if (date == null) {
            return "none";
        } else {
            DateFormat sdf = DateFormat.getDateInstance(DateFormat.SHORT);
            return sdf.format(date);
        }
    }

    private Date dueDate = null;
    private boolean completed = false;
    private mlsPriority priority = null;
    private int effortEstimation = -1;
    private List<mlcWorkUnit> workUnits = new ArrayList<>();
    private static final long serialVersionUID = 19640205L;

    public mlcTask() {
        super();
    }

    public mlsPriority getPriority() {
        return priority;
    }

    public void setPriority(mlsPriority p) {
        priority = p;
    }

    @Override
    public void setDueDate(Date d) {
        dueDate = d;
    }

    @Override
    public Date getDueDate() {
        return dueDate;
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    @Override
    public void setCompleted(boolean stat) {
        completed = stat;
    }

    public void setEffortEstimation(int effortEstimate) {
        this.effortEstimation = effortEstimate;
    }

    public int getEffortEstimation() {
        return effortEstimation;
    }

    @Override
    public int compareTo(Object o) {
        mlcTask that = (mlcTask) o;
        return this.getDueDate().compareTo(that.getDueDate());
    }

    public List<mlcWorkUnit> getWorkUnits() {
        return workUnits;
    }

    public void setWorkUnitIds(List<mlcWorkUnit> workUnits) {
        this.workUnits = workUnits;
    }

    /**
     * Obtain the total amount of work spent on this task
     *
     * @return The number of minutes that has been worked on this task by any
     * user.
     */
    public int getAccumulatedActualWorkMinutes() {
        long mins = 0L;
        long minmillis = 60 * 1000;
        for (mlcWorkUnit u : getWorkUnits()) {
            if (u.getEnd().compareTo(u.getStart()) < 0) {
                System.err.println("work unit with end before start with id " + u.getId());
            } else {
                if (!u.isPlan()) {
                    mins += (u.getEnd().getTime() - u.getStart().getTime()) / minmillis;
                }
            }
        }
        return (int) mins;
    }

    @Override
    public List<ObjectAttributes> getChanges(mlcObject previousState) {
        List<ObjectAttributes> changes = super.getChanges(previousState);
        if (!(previousState instanceof mlcTask)) {
            return changes;
        }
        mlcTask previousTask = (mlcTask) previousState;

        if (dueDate != null && !dueDate.equals(previousTask.getDueDate())) {
            changes.add(ObjectAttributes.DueDate);
        }
        if (!priority.equals(previousTask.getPriority())) {
            changes.add(ObjectAttributes.TaskPriority);
        }
        if (completed != previousTask.isCompleted()) {
            changes.add(ObjectAttributes.Completion);
        }
        return changes;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        mlcTask clone = (mlcTask) super.clone();
        clone.setCompleted(completed);
        clone.setPriority(priority);
        clone.setDueDate(dueDate);
        clone.setEffortEstimation(effortEstimation);
        clone.setWorkUnitIds(workUnits);
        return clone;
    }

}
