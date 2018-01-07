/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.events;

import com.mindliner.clientobjects.mlcObject;

/**
 * This interface must be implemented by all classes who wish to be notified
 * if a mindliner object changes.
 * @author Marius Messerli
 */
public interface ObjectChangeObserver {

    public void objectChanged(mlcObject o);

    public void objectDeleted(mlcObject o);
    
    public void objectCreated(mlcObject o);
    
    public void objectReplaced(int oldId, mlcObject o);
}
