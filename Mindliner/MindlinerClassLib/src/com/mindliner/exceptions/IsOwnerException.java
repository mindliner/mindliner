/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.exceptions;

/**
 * This exception is thrown if a contact is to be deleted who is the owner of at
 * least one other mindliner object.
 *
 * @author Marius Messerli
 */
public class IsOwnerException extends Exception {

    public IsOwnerException() {
    }

    public IsOwnerException(String message) {
        super(message);
    }
}
