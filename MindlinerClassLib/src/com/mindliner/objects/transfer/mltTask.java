/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.objects.transfer;

import com.mindliner.entities.mlsTask;
import com.mindliner.entities.mlsWorkUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Marius Messerli
 */
public class mltTask extends MltObject {

    private Date dueDate = null;
    private short priorityOrdinal = -1;
    private int effortEstimation = -1;
    private boolean completed = false;
    private final List<mltWorkUnit> workUnits = new ArrayList<>();

    public mltTask(mlsTask t) {
        super(t);
        priorityOrdinal = (short) t.getPriority().getId();
        effortEstimation = t.getEffortEstimation();
        completed = t.isCompleted();
        dueDate = t.getDueDate();
        for (mlsWorkUnit w : t.getWorkUnits()){
            workUnits.add(new mltWorkUnit(w));
        }
    }
    
    public mltTask() {
    }

    public int getPriorityOrdinal() {
        return priorityOrdinal;
    }

    public int getEffortEstimation() {
        return effortEstimation;
    }

    public void setEffortEstimation(int e) {
        throw new IllegalStateException("Please set the value using the constructor only.");
    }

    public Date getDueDate() {
        return dueDate;
    }

    public boolean getCompleted() {
        return completed;
    }

    public List<mltWorkUnit> getWorkUnits() {
        return workUnits;
    }
    
}
