/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.exceptions;

/**
 *
 * This exception is thrown if an operation is tried on an objct that does not
 * belong to the caller's client.
 *
 * @author Marius Messerli Created on 12.10.2012, 12:44:45
 */
public class ForeignClientException extends InsufficientAccessRightException {

    public ForeignClientException(String message) {
        super(message);
    }

    public ForeignClientException() {
    }
}
