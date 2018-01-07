/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.entities;

import com.mindliner.categories.mlsPriority;
import com.mindliner.contentfilter.Completable;
import com.mindliner.contentfilter.Timed;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;

/**
 * This class implements a Mindliner task which is one of the few base types that
 * are directly accessible and managed by the user.
 * 
 * @author Marius Messerli
 */
@Entity
@Table(name = "tasks")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorValue(value = "TASK")
@NamedQueries({
    @NamedQuery(name = "mlsTask.getOverdue", query = "SELECT t FROM mlsTask t where t.owner = :owner AND t.dueDate <= :endDate AND t.completed = 0"),
    @NamedQuery(name = "mlsTask.getUpcoming", query = "SELECT t FROM mlsTask t where t.owner = :owner AND t.dueDate >= :startDate AND t.dueDate <= :endDate AND t.completed = 0"),
    @NamedQuery(name = "mlsTask.getPriority", query = "SELECT t FROM mlsTask t where t.owner = :owner AND t.priority = :priority AND t.completed = 0")
})
public class mlsTask extends mlsObject implements Timed, Completable, Comparable, Serializable {

    private Date duedate = null;
    private boolean completed = false;
    private mlsPriority priority = null;
    private int effortEstimation = 0;
    private List<mlsWorkUnit> workUnits = new ArrayList<>();
    private static final long serialVersionUID = 19640205L;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PRIORITY_ID")
    public mlsPriority getPriority() {
        return priority;
    }

    public void setPriority(mlsPriority p) {
        priority = p;
    }

    @Override
    @Temporal(javax.persistence.TemporalType.DATE)
    public Date getDueDate() {
        return duedate;
    }

    @Override
    public void setDueDate(Date duedate) {
        this.duedate = duedate;
    }

    @Column(name = "COMPLETED")
    @Override
    public boolean isCompleted() {
        return completed;
    }

    @Override
    public void setCompleted(boolean stat) {
        completed = stat;
    }

    /**
     * Returns the estimated effort in milliseconds.
     *
     * @return
     */
    @Column(name = "EFFORT_ESTIMATION")
    public int getEffortEstimation() {
        return effortEstimation;
    }

    public void setEffortEstimation(int ee) {
        effortEstimation = ee;
    }

    @Override
    public int compareTo(Object o) {
        mlsTask that = (mlsTask) o;
        return this.getDueDate().compareTo(that.getDueDate());
    }

    @OneToMany(mappedBy = "task")
    public List<mlsWorkUnit> getWorkUnits() {
        return workUnits;
    }

    public void setWorkUnits(List<mlsWorkUnit> workUnits) {
        this.workUnits = workUnits;
    }
    
    
}
