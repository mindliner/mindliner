/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;

/**
 * This class holds the work tasks for one user and one week.
 *
 * @author Marius Messerli
 */
@Entity
@Table(name = "weekplans")
@NamedQueries({
    @NamedQuery(name = "mlsWeekPlan.getAllPlans", query = "SELECT w FROM mlsWeekPlan w"),
    @NamedQuery(name = "mlsWeekPlan.getPlanForYearAndWeek", query = "SELECT w FROM mlsWeekPlan w WHERE w.year = :year AND w.weekInYear = :week")})
public class mlsWeekPlan implements Serializable {

    private int id;
    private int version;
    private mlsUser owner = null;
    private int year = 0;
    private int weekInYear = 0;
    private List<mlsObject> objects = new ArrayList<>();
    private static final long serialVersionUID = 19640205L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "USER_ID", referencedColumnName = "ID")
    public mlsUser getUser() {
        return owner;
    }

    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "weekplans_objects",
            joinColumns = {
                @JoinColumn(name = "WEEKPLAN_ID")},
            inverseJoinColumns = {
                @JoinColumn(name = "OBJECT_ID")})
    public List<mlsObject> getObjects() {
        return objects;
    }

    @Column(name = "WEEK_IN_YEAR")
    public int getWeekInYear() {
        return weekInYear;
    }

    @Column(name = "WEEK_YEAR")
    public int getYear() {
        return year;
    }

    public void setWeekInYear(int weekInYear) {
        this.weekInYear = weekInYear;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setUser(mlsUser user) {
        this.owner = user;
    }

    public void setObjects(List objects) {
        this.objects = objects;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final mlsWeekPlan other = (mlsWeekPlan) obj;
        if (this.id != other.id) {
            return false;
        }
        if (this.year != other.year) {
            return false;
        }
        if (this.weekInYear != other.weekInYear) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + this.id;
        hash = 53 * hash + this.year;
        hash = 53 * hash + this.weekInYear;
        return hash;
    }

    @Override
    public String toString() {
        return "com.mindliner.entities.WeekPlan[id=" + id + "]";
    }

    @Version
    @Column(name = "LOCK_VERSION")
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
