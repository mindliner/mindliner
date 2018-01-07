/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.exceptions.synch;

/**
 * This exception is thrown if a second syncher with the same brand and type as an existing one is being registered with the SynchronizationManager
 * @author Marius Messerli
 */
public class SynchExistingSyncherException extends Exception{

    public SynchExistingSyncherException() {
    }

    public SynchExistingSyncherException(String message) {
        super(message);
    }
    
}
