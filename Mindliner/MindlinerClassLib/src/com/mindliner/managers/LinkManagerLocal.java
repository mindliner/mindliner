/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import javax.ejb.Local;

/**
 *
 * @author Marius Messerli
 */
@Local
public interface LinkManagerLocal {
    
    /**
     * This function strips all icons off an object. In Mindliner these
     * are linked to the object in much the same way as other objects, so
     * it effectively unlinks them all.
     * 
     * @param objectId The id of the object from which the icons are to be removed
     */
    public void unlinkAllIcons(int objectId);
    
    /**
     * This is an admin function which re-calculates the relative count
     * field of all objects and updates the field if necessary.
     */
    public void reconcileObjectsRelativeCountField();
    
    
    
}
