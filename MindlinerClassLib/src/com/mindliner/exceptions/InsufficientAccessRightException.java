/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.exceptions;

import java.io.Serializable;

/**
 *
 * @author Marius Messerli
 */
public class InsufficientAccessRightException extends Exception implements Serializable {

    public InsufficientAccessRightException() {
    }

    public InsufficientAccessRightException(String message) {
        super(message);
    }
}
