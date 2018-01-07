/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.synchronization.foreign;

/**
 * This is an abstract representation of an appointment in a foreign data store.
 * Since Mindliner does not know appointments it will be synched to a Collection.
 * @author Marius Messerli
 */
public abstract class ForeignAppointment extends ForeignObject {
    
    // how many extra days after the appointment do we keep it alive?
    public static final int EXTRA_LIFETIME = 30;
    
    public abstract void setLifetime(int days);
    
    public abstract int getLifetime();
}
