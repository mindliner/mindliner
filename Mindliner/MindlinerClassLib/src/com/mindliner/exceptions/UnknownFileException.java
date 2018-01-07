/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.exceptions;

/**
 *
 * @author Marius Messerli
 */
public class UnknownFileException extends Exception{

    public UnknownFileException() {
    }

    public UnknownFileException(String message) {
        super(message);
    }
    
}
