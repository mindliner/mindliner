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
public class ForeignOwnerException extends InsufficientAccessRightException implements Serializable{

    public ForeignOwnerException() {
    }

    public ForeignOwnerException(String message) {
        super(message);
    }
    
}
