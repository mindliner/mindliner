/*
 * Priority.java
 *
 * Created on 19. Juli 2006, 15:44
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.mindliner.categories;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 *
 * @author Marius Messerli
 */
@Entity
@Table(name = "taskpriorities")
public class mlsPriority extends mlsMindlinerCategory implements Comparable, java.io.Serializable {

    public static final int PRIORITY_NORMAL = 1;
    public static final int PRIORITY_HIGH = 2;
    public static final int PRIORITY_LOW = 3;
    
    int importance = -1;
    private static final long serialVersionUID = 19640205L;

    public void setImportance(int i) {
        importance = i;
    }

    @Column(name = "IMPORTANCE")
    public int getImportance() {
        return importance;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final mlsPriority other = (mlsPriority) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + this.importance;
        return hash;
    }

    @Override
    public int compareTo(Object o) {
        mlsPriority that = (mlsPriority) o;
        if (this.getImportance() < that.getImportance()) {
            return -1;
        }
        if (this.getImportance() == that.getImportance()) {
            return 0;
        }
        return 1;
    }
}
