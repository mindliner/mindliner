/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.contentfilter;

import java.util.Date;

/**
 * The interface to all objects that have a due date.
 * @author Marius Messerli
 */
public interface Timed {

    
    public Date getDueDate();
    
    /**
     * Defines the due date.
     * @param d A value of null is valid and means: not scheduled
     */
    public void setDueDate(Date d);
    
}
