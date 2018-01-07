/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.exceptions.mlModifiedException;
import java.io.Serializable;
import javax.naming.NamingException;

/**
 * Mindliner's command base class. Commands are required to work asynchronously
 * or offline and to support the undo system.
 *
 * @author Marius Messerli
 */
public class MindlinerCommand implements Serializable {

    private boolean executed = false;
    private boolean undone = false;
    private  mlcObject object = null;
    private boolean overriding = false;
    private static final long serialVersionUID = 19640205L;

    /**
     * Creator.
     *
     * @param o The object on which this command operates.
     * @param overriding If true then the execution of this command will
     * override any previous execution on the same object.
     */
    public MindlinerCommand(mlcObject o, boolean overriding) {
        object = o;
        this.overriding = overriding;
    }

    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        if (executed == true) {
            throw new IllegalStateException("This command can only be executed once.");
        }
    }

    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        if (undone == true) {
            throw new UndoAlreadyUndoneException("Command has been undone already.");
        }
    }

    public mlcObject getObject() {
        return object;
    }

    public void setObject(mlcObject object) {
        this.object = object;
    }

    public boolean isOverriding() {
        return overriding;
    }

    /**
     * Describe the details of the current command if not already covered by
     * toString()
     *
     * @return
     */
    public String getDetails() {
        return "";
    }

    public boolean isExecuted() {
        return executed;
    }

    public boolean isUndone() {
        return undone;
    }

    public void setExecuted(boolean executed) {
        this.executed = executed;
    }

    public void setUndone(boolean undone) {
        this.undone = undone;
    }

    public void setOverriding(boolean overriding) {
        this.overriding = overriding;
    }
    
    
}
