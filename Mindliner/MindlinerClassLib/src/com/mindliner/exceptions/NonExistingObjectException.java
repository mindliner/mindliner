/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.exceptions;

import java.io.Serializable;

/**
 * This exception is thrown if an operation specifies one or several IDs for
 * objects to be updated and at least one of those objects no longer exist in the database.
 * 
 * @author Marius Messerli
 * Created on 16.10.2012, 08:43:08
 */
public class NonExistingObjectException extends Exception implements Serializable{

    public NonExistingObjectException(String message) {
        super(message);
    }

    public NonExistingObjectException() {
    }

}
