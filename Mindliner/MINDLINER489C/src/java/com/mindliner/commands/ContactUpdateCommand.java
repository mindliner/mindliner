/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands;

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
 *
 * @author Marius Messerli
 */
public class ContactUpdateCommand extends MindlinerOnlineCommand {

    private String firstName = "";
    private String middleName = "";
    private String lastName = "";
    private String eMail = "";
    private String description = "";
    private String previousFirstName = "";
    private String previousMiddleName = "";
    private String previousLastName = "";
    private String previousEmail = "";
    private String previousDescription = "";
    private String previousPhoneNumber = "";
    private String phoneNumber = "";
    private String previousMobileNumber = "";
    private String mobileNumber = "";

    public ContactUpdateCommand(mlcObject o, 
            String firstName, String middleName, String lastName, 
            String eMail, String phoneNumber, String mobileNumber, String description) {
        super(o, true);
        if (!(o instanceof mlcContact)) {
            throw new IllegalArgumentException("This command needs an mlcContact as object");
        }
        mlcContact contact = (mlcContact) o;

        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.eMail = eMail;
        this.description = description;
        this.mobileNumber = mobileNumber;
        this.phoneNumber = phoneNumber;

        previousFirstName = contact.getFirstName();
        previousMiddleName = contact.getMiddleName();
        previousLastName = contact.getLastName();
        previousEmail = contact.getEmail();
        previousDescription = contact.getDescription();
        previousMobileNumber = contact.getMobileNumber();
        previousPhoneNumber = contact.getPhoneNumber();

        contact.setFirstName(firstName);
        contact.setLastName(lastName);
        contact.setMiddleName(middleName);
        contact.setEmail(eMail);
        contact.setDescription(description);
        contact.setPhoneNumber(phoneNumber);
        contact.setMobileNumber(mobileNumber);

        // also set the headline for general information
        StringBuilder sb = new StringBuilder();
        if (!firstName.isEmpty()) {
            sb.append(firstName);
        }
        if (!middleName.isEmpty()) {
            sb.append(" ").append(middleName);
        }
        if (!lastName.isEmpty()) {
            sb.append(" ").append(lastName);
        }
        contact.setHeadline(sb.toString());
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        super.execute();
        mlcContact c = (mlcContact) getObject();
        ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
        int version = omr.updateContactDetails(c.getId(), firstName, middleName, lastName, eMail, phoneNumber, mobileNumber, description);
        getObject().setVersion(version);
        setExecuted(true);
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        super.undo();
        mlcContact contact = (mlcContact) getObject();
        contact.setFirstName(previousFirstName);
        contact.setMiddleName(previousMiddleName);
        contact.setLastName(previousLastName);
        contact.setEmail(previousEmail);
        if (isExecuted()) {
            ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
            if (versionCheck() == true) {
                JOptionPane.showMessageDialog(MindlinerMain.getInstance(),
                        "Cannot undo because the object has been updated in the meantime.",
                        "Undo Lifetime Change",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                int version = omr.updateContactDetails(getObject().getId(), 
                        previousFirstName, previousMiddleName, previousLastName, previousEmail, previousPhoneNumber, previousMobileNumber, previousDescription);
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
        return "Contact Update (" + getFormattedId() + ")";
    }

    @Override
    public String getDetails() {
        return "new firstname=" + firstName + ", lastname=" + lastName + " email=" + eMail;
    }
}
