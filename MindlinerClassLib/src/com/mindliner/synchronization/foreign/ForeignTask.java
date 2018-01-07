/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.synchronization.foreign;

import com.mindliner.categories.mlsPriority;
import java.util.Date;

/**
 *
 * @author Marius Messerli
 */
public abstract class ForeignTask extends ForeignObject {
    
    public abstract mlsPriority getTaskPriority();
    
    public abstract void setPriority (mlsPriority priority);
    
    public abstract Date getDueDate();
    
    public abstract void setDueDate(Date duedate);
    
    public abstract void setCompletionState(boolean completed);
        
}
