/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.exceptions;

/**
 *
 * @author Marius Messerli
 */
public class UserCreationException extends Exception{

    public UserCreationException() {
    }

    public UserCreationException(String message) {
        super(message);
    }
    
}
