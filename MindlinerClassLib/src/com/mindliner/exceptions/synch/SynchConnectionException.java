/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.exceptions.synch;

/**
 * This exception is thrown if Mindliner fails to conntect to a foreign repository 
 * who's elements are to be synched.
 * @author Marius Messerli
 */
public class SynchConnectionException extends Exception {

    public SynchConnectionException() {
    }

    public SynchConnectionException(String message) {
        super(message);
    }
}
