/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.entities;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Marius Messerli
 */
@Entity
@Table(name = "workunits")
@NamedQueries({
    @NamedQuery(name = "mlsWorkUnit.findAll", query = "SELECT w FROM mlsWorkUnit w"),
    @NamedQuery(name = "mlsWorkUnit.getMonthReport", query = "SELECT w FROM mlsWorkUnit w WHERE w.start >= :periodStart AND w.start < :periodEnd AND w.user.id = :userId")    
})
public class mlsWorkUnit implements Serializable {

    private int id;
    private mlsTask task = null;
    private Date start = null;
    private Date end = null;
    private String timeZoneId = "";
    private mlsUser user = null;
    private boolean plan = false; // TRUE if this work unit is a plan unit, FALSE if it represents actual work
    private static final long serialVersionUID = 19640205L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int getId() {
        return id;
    }

    @ManyToOne
    @JoinColumn(name = "TASK_ID", referencedColumnName = "ID")
    public mlsTask getTask() {
        return task;
    }

    public void setTask(mlsTask task) {
        this.task = task;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    @Column(name = "TIME_ZONE_ID")
    public String getTimeZoneId() {
        return timeZoneId;
    }

    @Column(name = "SLOT_END")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getEnd() {
        return end;
    }

    @Column(name = "SLOT_START")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getStart() {
        return start;
    }

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    public mlsUser getUser() {
        return user;
    }

    public void setUser(mlsUser u) {
        user = u;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) id;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof mlsWorkUnit)) {
            return false;
        }
        mlsWorkUnit other = (mlsWorkUnit) object;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        DateFormat dft = DateFormat.getTimeInstance(DateFormat.SHORT);
        Long millis = end.getTime() - start.getTime();
        int minutes = (int) (millis / 1000 / 60);
        return df.format(start) + " to " + dft.format(end) + " " + Integer.toString(minutes) + " min";
    }

    @Column(name = "PLAN")
    public boolean isPlan() {
        return plan;
    }

    public void setPlan(boolean plan) {
        this.plan = plan;
    }
}
