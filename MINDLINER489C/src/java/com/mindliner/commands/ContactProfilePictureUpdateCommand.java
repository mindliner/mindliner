/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands;

import com.mindliner.clientobjects.MlcImage;
import com.mindliner.clientobjects.mlcContact;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.main.MindlinerMain;
import com.mindliner.managers.ObjectManagerRemote;
import com.mindliner.serveraccess.RemoteLookupAgent;
import javax.naming.NamingException;
import javax.swing.JOptionPane;

/**
 * This command specifically only updates the contact picture of the specified
 * contact.
 *
 * @author Marius Messerli
 */
public class ContactProfilePictureUpdateCommand extends MindlinerOnlineCommand {

    private MlcImage profilePicture;
    private MlcImage previousProfilePicture;

    public ContactProfilePictureUpdateCommand(mlcObject o, MlcImage profilePicture) {
        super(o, true);
        if (!(o instanceof mlcContact)) {
            throw new IllegalArgumentException("This command needs an mlcContact as object");
        }
        mlcContact contact = (mlcContact) o;
        this.profilePicture = profilePicture;
        previousProfilePicture = contact.getProfilePicture();
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        super.execute(); //To change body of generated methods, choose Tools | Templates.
        mlcContact c = (mlcContact) getObject();
        ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
        int profilePictureId = -1;
        if (profilePicture != null) {
            profilePictureId = profilePicture.getId();
        }
        int version = omr.updateContactProfilePicture(c.getId(), profilePictureId);
        getObject().setVersion(version);
        setExecuted(true);
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        super.undo();
        if (isExecuted()) {
            ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
            if (versionCheck() == true) {
                JOptionPane.showMessageDialog(MindlinerMain.getInstance(),
                        "Cannot undo because the object has been updated in the meantime.",
                        "Undo Profile Picture Change",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                int profilePictureId = -1;
                if (previousProfilePicture != null) {
                    profilePictureId = previousProfilePicture.getId();
                }
                int version = omr.updateContactProfilePicture(getObject().getId(), profilePictureId);
                getObject().setVersion(version);
                setUndone(true);
            }
        }
    }

    @Override
    public boolean isVersionChecking() {
        return true;
    }

}
