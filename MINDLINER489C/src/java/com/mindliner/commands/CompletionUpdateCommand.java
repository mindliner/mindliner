/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.contentfilter.Completable;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.managers.ObjectManagerRemote;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.main.BulkUpdater;
import com.mindliner.main.MindlinerMain;
import com.mindliner.serveraccess.RemoteLookupAgent;
import javax.naming.NamingException;
import javax.swing.JOptionPane;

/**
 *
 * @author Marius Messerli
 */
public class CompletionUpdateCommand extends MindlinerOnlineCommand {

    private boolean completion = false;
    private boolean previousCompletion = false;

    public CompletionUpdateCommand(mlcObject o, boolean completion) {
        super(o, true);
        if (!(o instanceof Completable)){
            throw new IllegalArgumentException("This command needs an object that implements Completable");
        }
        previousCompletion = ((Completable) getObject()).isCompleted();
        ((Completable) getObject()).setCompleted(completion);
        this.completion = completion;
        BulkUpdater.publishUpdate(o);
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        super.execute();
        if (getObject() instanceof Completable) {
            ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
            int version = omr.setCompletionState(getObject().getId(), completion);
            getObject().setVersion(version);
            setExecuted(true);
        }
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        super.undo();
        ((Completable) getObject()).setCompleted(previousCompletion);
        BulkUpdater.publishUpdate(getObject());
        if (isExecuted()) {
            if (versionCheck() == false) {
                JOptionPane.showMessageDialog(MindlinerMain.getInstance(),
                        "Cannot undo because the object has been updated in the meantime.",
                        "Undo Lifetime Change",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
                int version = omr.setCompletionState(getObject().getId(), previousCompletion);
                getObject().setVersion(version);
                setUndone(true);
            }
        }
    }

    @Override
    public boolean isVersionChecking() {
        return true;
    }

    @Override
    public String toString() {
        return "Completion State Update (" + getFormattedId() + ")";
    }
}
