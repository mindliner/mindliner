/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.commands;

/**
 *
 * @author Marius Messerli
 */
public interface UndoObserver {

    /**
     * Returns a short description of the command that sits on top of the stack.
     */
    public void setUndoControlItemText(String text);

}
