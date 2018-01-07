/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.exceptions;

/**
 * If undo() is called on an alredy undone command this exception is thrown. Many
 * commands can only be undone once.
 * 
 * @author Marius Messerli
 */
public class UndoAlreadyUndoneException extends Exception{

    public UndoAlreadyUndoneException() {
    }

    public UndoAlreadyUndoneException(String message) {
        super(message);
    }
    
}
