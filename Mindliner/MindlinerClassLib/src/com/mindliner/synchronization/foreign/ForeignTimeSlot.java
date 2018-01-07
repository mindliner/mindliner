/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.synchronization.foreign;

import java.util.Date;

/**
 * This class is used to synchronize a work units to a calendar provider
 * 
 * @author Marius Messerli
 */
public abstract class ForeignTimeSlot extends ForeignObject{
    
    public abstract Date getSlotStart();
    
    public abstract Date getSlotEnd();
    
    public abstract void setSlotStart(Date date);
    
    public abstract void setSlotEnd(Date date);
    
    /**
     * Determins if this is an actual or a planned time slot.
     * @return True if this time slot is a plan slot, false if it is an actual work slot.
     */
    public abstract boolean isPlan();

    
}
