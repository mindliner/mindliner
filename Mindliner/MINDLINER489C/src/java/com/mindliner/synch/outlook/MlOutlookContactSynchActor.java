/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.synch.outlook;

import com.mindliner.analysis.MlClassHandler;
import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.MlcImage;
import com.mindliner.clientobjects.mlcContact;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.commands.CommandRecorder;
import com.mindliner.commands.ConfidentialityUpdateCommand;
import com.mindliner.commands.ContactProfilePictureUpdateCommand;
import com.mindliner.commands.ContactUpdateCommand;
import com.mindliner.commands.DataPoolUpdateCommand;
import com.mindliner.commands.ImageUpdateCommand;
import com.mindliner.commands.ObjectCreationCommand;
import com.mindliner.commands.ObjectDeletionCommand;
import com.mindliner.commands.PrivacyUpdateCommand;
import com.mindliner.entities.MlsImage;
import com.mindliner.exceptions.synch.SynchCacheIOException;
import com.mindliner.exceptions.synch.SynchConnectionException;
import com.mindliner.main.SearchPanel;
import com.mindliner.objects.transfer.mltSynchunit;
import com.mindliner.synch.SynchWorkflowGeneric;
import com.mindliner.synchronization.foreign.ForeignContact;
import com.mindliner.synchronization.foreign.ForeignObject;
import com.moyosoft.connector.ms.outlook.contact.OutlookContact;
import com.moyosoft.connector.ms.outlook.folder.FolderType;
import com.moyosoft.connector.ms.outlook.folder.OutlookFolder;
import com.moyosoft.connector.ms.outlook.item.ItemType;
import com.moyosoft.connector.ms.outlook.item.ItemsCollection;
import com.moyosoft.connector.ms.outlook.item.ItemsIterator;
import com.moyosoft.connector.ms.outlook.item.OutlookItem;
import com.moyosoft.connector.ms.outlook.item.OutlookItemID;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;

/**
 *
 * @author Marius Messerli
 */
public class MlOutlookContactSynchActor extends MlOutlookSynchActor {

    private ItemsCollection outlookContacts;
    SynchWorkflowGeneric workFlow = null;
    private List<mlcContact> mindlinerContacts = null;

    @Override
    protected FolderType getFolderType() {
        return FolderType.CONTACTS;
    }

    @Override
    public void synchronizeElements() {
        try {
            if (workFlow == null) {
                workFlow = new SynchWorkflowGeneric(this, progressReporter);
            }
            OutlookFolder contactFolder = getOutlookFolder();
            outlookContacts = contactFolder.getItems();
            workFlow.run();
        } catch (SynchCacheIOException ex) {
            Logger.getLogger(MlOutlookContactSynchActor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SynchConnectionException ex) {
            Logger.getLogger(MlOutlookContactSynchActor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void updateForeignObject(ForeignObject foreignObject, int mindlinerObjectId) {
        mlcObject mo = CacheEngineStatic.getObject(mindlinerObjectId);
        if (!(foreignObject instanceof ForeignContact) || !(mo instanceof mlcContact)) {
            throw new IllegalArgumentException("First argument must be of type ForeignContact, second argument must be the id of an mlcContact");
        }
        ForeignContact fc = (ForeignContact) foreignObject;
        mlcContact c = (mlcContact) mo;
        fc.setFirstname(c.getFirstName());
        fc.setMiddlename(c.getMiddleName());
        fc.setLastname(c.getLastName());
        fc.setEmailAddress(c.getEmail());
        fc.setDescription(c.getDescription());
        fc.save();
    }

    @Override
    public void updateMindlinerObject(ForeignObject foreignObject, int mindlinerObjectId) {
        mlcObject o = CacheEngineStatic.getObject(mindlinerObjectId);
        if (!(o instanceof mlcContact) || !(foreignObject instanceof ForeignContact)) {
            throw new IllegalArgumentException("First argument must be of type ForeignContact, second argument must be the id of an mlcContact");
        }
        mlcContact c = (mlcContact) o;
        ForeignContact fc = (ForeignContact) foreignObject;

        CommandRecorder cr = CommandRecorder.getInstance();

        MlcImage localProfilePicture = c.getProfilePicture();
        ImageIcon foreignProfilePicture = fc.getProfilePicture();
        if (foreignProfilePicture != null) {
            if (localProfilePicture != null) {
                // both have profile pictures, it seems impossible to me to say if Outlook has a newer one so I just assume its the same and don't do anything
            }
            else {
                // foreign contact has profile picture but ML doesn't so create one
                ObjectCreationCommand imageCreationCommand = new ObjectCreationCommand(null, MlcImage.class, "", "");
                cr.scheduleCommand(imageCreationCommand);
                localProfilePicture = (MlcImage) imageCreationCommand.getObject();
                if (localProfilePicture != null) {
                    cr.scheduleCommand(new DataPoolUpdateCommand(localProfilePicture, c.getClient()));
                    cr.scheduleCommand(new ConfidentialityUpdateCommand(localProfilePicture, c.getConfidentiality()));
                    cr.scheduleCommand(new ImageUpdateCommand(localProfilePicture, foreignProfilePicture, "Profile picture for " + c.getName(), MlsImage.ImageType.ProfilePicture, null));
                    cr.scheduleCommand(new ContactProfilePictureUpdateCommand(c, localProfilePicture));
                }
            }
        } else {            
            if (localProfilePicture != null) {
                // foreign contact does not have a picture but local has so delete it
                cr.scheduleCommand(new ObjectDeletionCommand(c.getProfilePicture()));
                localProfilePicture = null;
            }
        }
        cr.scheduleCommand(new ContactUpdateCommand(c,
                fc.getFirstname(), fc.getMiddlename(), fc.getLastname(), fc.getEmailAddress(),
                fc.getWorkPhone(), fc.getMobile(), fc.getDescription()));
        if (c.isPrivateAccess() != fc.isPrivate()) {
            cr.scheduleCommand(new PrivacyUpdateCommand(c, fc.isPrivate()));
        }
    }

    @Override
    public ForeignObject getForeignObject(String remoteId) {
        OutlookItem outlookItem = outlookContacts.getItemById(new OutlookItemID(remoteId));
        if (outlookItem instanceof OutlookContact) {
            OutlookContact oc = (OutlookContact) outlookItem;
            return new MlOutlookContactAdapter(oc);
        }
        return null;
    }

    private ForeignContact createOutlookContact(mlcContact mindlinerContact) {
        OutlookFolder outlookFolder = getOutlookFolder();
        OutlookContact outlookContact = (OutlookContact) outlookFolder.createItem(ItemType.CONTACT);
        MlOutlookContactAdapter foreignContact = new MlOutlookContactAdapter(outlookContact);
        foreignContact.setCategory(syncher.getCategoryName());
        updateForeignObject(foreignContact, mindlinerContact.getId());
        mltSynchunit su = new mltSynchunit(
                mindlinerContact.getId(),
                foreignContact.getId(),
                foreignContact.getModificationDate());
        syncher.getSynchUnits().add(su);
        return foreignContact;
    }

    @Override
    public boolean isEqual(ForeignObject foreignObject, Object o) {
        if (!(o instanceof mlcContact) || !(foreignObject instanceof ForeignContact)) {
            throw new IllegalArgumentException("First argument must be of type ForeignContact, second argument must be the id of an mlcContact");
        }
        mlcContact c = (mlcContact) o;
        ForeignContact fc = (ForeignContact) foreignObject;

        // we need the E-mail field to match to call two object identical
        if (c.getEmail() == null || c.getEmail().isEmpty() || fc.getEmailAddress().isEmpty()) {
            return false;
        }
        // we need the first names to match
        if (c.getFirstName().isEmpty() || fc.getFirstname().isEmpty()) {
            return false;
        }
        // also need the lastnames to match
        if (c.getLastName().isEmpty() || fc.getLastname().isEmpty()) {
            return false;
        }
        return c.getEmail().equals(fc.getEmailAddress())
                && c.getFirstName().equals(fc.getFirstname())
                && c.getLastName().equals(fc.getLastname());
    }

    @Override
    public void prepareForContentCheck() {
        mindlinerContacts = (List<mlcContact>) (List<?>) SearchPanel.getSynchItems(MlClassHandler.MindlinerObjectType.Contact, syncher.isIgnoreCompleted(), true);
    }

    @Override
    public int getMindlinerObjectWithSimilarContent(ForeignObject foreignObject) {
        if (mindlinerContacts == null) {
            System.err.println("Cannot perform content check, need to call prepareForContentCheck first");
            return -1;
        }
        for (mlcContact c : mindlinerContacts) {
            if (isEqual(foreignObject, c)) {
                return c.getId();
            }
        }
        return -1;
    }

    @Override
    public ForeignObject createForeignObject(int mindlinerObjectId) {
        mlcObject o = CacheEngineStatic.getObject(mindlinerObjectId);
        if (o instanceof mlcContact) {
            mlcContact c = (mlcContact) o;
            return createOutlookContact(c);
        } else {
            return null;
        }
    }

    @Override
    public List<ForeignObject> getForeignObjects() {
        ArrayList<ForeignObject> foreignContactcs;
        foreignContactcs = new ArrayList<ForeignObject>();
        ItemsIterator it = outlookContacts.iterator();
        while (it.hasNext()) {
            Object outlookObject = it.next();
            if (outlookObject instanceof OutlookContact) {
                OutlookContact oc = (OutlookContact) outlookObject;
                foreignContactcs.add(new MlOutlookContactAdapter(oc));
            }
        }
        return foreignContactcs;
    }

    @Override
    protected Class getMindlinerObjectClass() {
        return mlcContact.class;
    }
}
