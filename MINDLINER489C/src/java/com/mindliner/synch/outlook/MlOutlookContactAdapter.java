/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.synch.outlook;

import com.mindliner.synchronization.foreign.ForeignContact;
import com.moyosoft.connector.com.ComponentObjectModelException;
import com.moyosoft.connector.ms.outlook.attachment.AttachmentsCollection;
import com.moyosoft.connector.ms.outlook.attachment.AttachmentsIterator;
import com.moyosoft.connector.ms.outlook.attachment.OutlookAttachment;
import com.moyosoft.connector.ms.outlook.contact.OutlookContact;
import com.moyosoft.connector.ms.outlook.item.SensitivityType;
import java.awt.MediaTracker;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;

/**
 *
 * @author Marius Messerli
 */
public class MlOutlookContactAdapter extends ForeignContact {

    private OutlookContact outlookContact;

    public MlOutlookContactAdapter(OutlookContact outlookContact) {
        this.outlookContact = outlookContact;
    }

    @Override
    public void setFirstname(String firstname) {
        outlookContact.setFirstName(firstname);
    }

    @Override
    public String getFirstname() {
        return outlookContact.getFirstName();
    }

    @Override
    public ImageIcon getProfilePicture() {
        AttachmentsCollection attachments = outlookContact.getAttachments();
        AttachmentsIterator iterator = attachments.iterator();
        while (iterator.hasNext()) {
            OutlookAttachment a = (OutlookAttachment) iterator.next();
            try{
            String fileName = a.getFileName();
            if (fileName.contains("ContactPicture") || fileName.contains("ContactPhoto")) {
                try {
                    File temp = File.createTempFile("temp", Long.toString(System.nanoTime()));
                    a.saveAsFile(temp);
                    ImageIcon img = new ImageIcon(temp.getCanonicalPath());
                    while (img.getImageLoadStatus() == MediaTracker.LOADING) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(MlOutlookContactAdapter.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
//                    temp.delete();
                    if (img.getImageLoadStatus() == MediaTracker.COMPLETE) {
                        return img;
                    }
                    else{
                        Logger.getLogger(MlOutlookContactAdapter.class.getName()).log(Level.WARNING, null, "Failed to load profile picture from temporary file.");
                    }
                } catch (IOException ex) {
                    Logger.getLogger(MlOutlookContactAdapter.class.getName()).log(Level.SEVERE, null, ex);
                }
            }}
            catch (ComponentObjectModelException ex) {
                Logger.getLogger(MlOutlookContactAdapter.class.getName()).log(Level.WARNING, null, "Error reading contact attachment: " + ex.getMessage());
            }
        }
        return null;
    }

    @Override
    public void setMiddlename(String middlename) {
        outlookContact.setMiddleName(middlename);
    }

    @Override
    public String getMiddlename() {
        return outlookContact.getMiddleName();
    }

    @Override
    public void setLastname(String lastname) {
        outlookContact.setLastName(lastname);
    }

    @Override
    public String getLastname() {
        return outlookContact.getLastName();
    }

    @Override
    public void setEmailAddress(String email) {
        outlookContact.setEmail1Address(email);
    }

    @Override
    public String getEmailAddress() {
        return outlookContact.getEmail1Address();
    }

    @Override
    public String getId() {
        return outlookContact.getItemId().getEntryId();
    }

    @Override
    public String getHeadline() {
        return outlookContact.getSubject();
    }

    @Override
    public void setHeadline(String headline) {
        outlookContact.setSubject(headline);
    }

    @Override
    public String getDescription() {
        return outlookContact.getBody();
    }

    @Override
    public void setDescription(String description) {
        outlookContact.setBody(description);
    }

    @Override
    public Date getModificationDate() {
        return outlookContact.getLastModificationTime();
    }

    @Override
    public Date getCreationDate() {
        return outlookContact.getCreationTime();
    }

    @Override
    public boolean isPrivate() {
        if (outlookContact.getSensitivity().equals(SensitivityType.PRIVATE)) {
            return true;
        }
        return false;
    }

    @Override
    public void setPrivacyFlag(boolean privacy) {
        if (privacy) {
            outlookContact.setSensitivity(SensitivityType.PRIVATE);
        } else {
            outlookContact.setSensitivity(SensitivityType.NORMAL);
        }
    }

    @Override
    public void setOwnerName(String name) {
    }

    @Override
    public String getOwnerName() {
        return "";
    }

    @Override
    public void save() {
        outlookContact.save();
    }

    @Override
    public void delete() {
        outlookContact.delete();
    }

    @Override
    public void setCategory(String category) {
        outlookContact.setCategories(category);
    }

    @Override
    public String getCategory() {
        return outlookContact.getCategories();
    }

    @Override
    public boolean isCompleted() {
        return false;
    }

    @Override
    public String getWorkPhone() {
        return outlookContact.getBusinessTelephoneNumber();
    }

    @Override
    public void setWorkPhone(String workPhone) {
        outlookContact.setBusinessTelephoneNumber(workPhone);
    }

    @Override
    public String getMobile() {
        return outlookContact.getMobileTelephoneNumber();
    }

    @Override
    public void setMobile(String mobileNumber) {
        outlookContact.setMobileTelephoneNumber(mobileNumber);
    }
    
    
}
