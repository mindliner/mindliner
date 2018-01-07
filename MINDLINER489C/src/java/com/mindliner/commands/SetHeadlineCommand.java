/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.managers.ObjectManagerRemote;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.serveraccess.RemoteLookupAgent;
import javax.naming.NamingException;
import javax.swing.JOptionPane;

/**
 * This command sets the headline as specified and does not interpret any tags.
 * @author Marius Messerli
 */
public class SetHeadlineCommand extends MindlinerOnlineCommand {

    private String headline = "";
    private String previousHeadline = null;

    public SetHeadlineCommand(mlcObject o, String headline) {
        super(o, true);
        this.headline = headline;
        o.setHeadline(headline);
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        super.execute();
        previousHeadline = getObject().getHeadline();
        ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
        int version = omr.setHeadline(getObject().getId(), headline);
        getObject().setVersion(version);
        setExecuted(true);
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        super.undo();
        if (isExecuted()) {
            ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
            if (versionCheck() == false) {
                JOptionPane.showMessageDialog(null,
                        "Cannot undo because the object has been updated in the meantime.",
                        "Undo Headline Update",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                int version = omr.setHeadline(getObject().getId(), previousHeadline);
                getObject().setVersion(version);
                setUndone(true);
            }
        }
    }

    // @todo Change this again to true once the map import is debugged.
    @Override
    public boolean isVersionChecking() {
        return false;
    }

    @Override
    public String toString() {
        return "Headline Update (" + getFormattedId() + ")";
    }

    @Override
    public String getDetails() {
        return "New headline = " + headline;
    }
}
