/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.NonExistingObjectException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.managers.HeadlineParserRemote;
import com.mindliner.managers.ObjectManagerRemote;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.serveraccess.RemoteLookupAgent;
import javax.naming.NamingException;
import javax.swing.JOptionPane;

/**
 * @todo This command must not interact with the server directly. Should all go
 * via Caching subsystem.
 *
 * @author Marius Messerli
 */
public class TextUpdateCommand extends MindlinerOnlineCommand {

    private String headline = "";
    private String previousHeadline = null;
    private String description = "";
    private String previousDescription = "";

    /**
     * This command upates the headline and optionally the description field
     *
     * @param o The object to be updated
     * @param headline The new headline of which tags are going to be
     * interpreted and applied
     * @param description The new description. Specify null if you don't want to
     * change the description
     */
    public TextUpdateCommand(mlcObject o, String headline, String description) {
        super(o, true);
        previousHeadline = getObject().getHeadline();
        previousDescription = getObject().getDescription();
        this.headline = headline;
        this.description = description;
        getObject().setHeadline(headline);
        if (description != null) {
            getObject().setDescription(description);
        }
    }

    @Override
    public void execute() throws mlModifiedException, NamingException , MlAuthorizationException{
        try {
            super.execute();
            ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
            HeadlineParserRemote hpr = (HeadlineParserRemote) RemoteLookupAgent.getManagerForClass(HeadlineParserRemote.class);
            int version = hpr.updateHeadline(getObject().getId(), headline);
            if (description != null) {
                version = omr.setDescription(getObject().getId(), description);
            }
            getObject().setVersion(version);
            setExecuted(true);
        } catch (NonExistingObjectException ex) {
            JOptionPane.showMessageDialog(null, "Could not update object because it no longer exists on the server", "Text Update Command", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        super.undo();
        getObject().setHeadline(previousHeadline);
        if (description != null) {
            getObject().setDescription(previousDescription);
        }
        if (isExecuted()) {
            ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
            if (versionCheck() == false) {
                JOptionPane.showMessageDialog(null,
                        "Cannot undo because the object has been updated in the meantime.",
                        "Undo Lifetime Change",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                int version = omr.setHeadline(getObject().getId(), previousHeadline);
                if (description != null) {
                    version = omr.setDescription(getObject().getId(), previousDescription);
                }
                getObject().setVersion(version);
                setUndone(true);
            }
        }
    }

    @Override
    public String getDetails() {
        return "H = " + headline + description == null ? "" : ", D = " + description;
    }

    @Override
    public boolean isVersionChecking() {
        return false;
    }

    @Override
    public String toString() {
        return "Headline/Description Update (" + getFormattedId() + ")";
    }
}
