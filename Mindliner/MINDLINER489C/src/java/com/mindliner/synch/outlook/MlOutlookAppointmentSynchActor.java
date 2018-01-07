/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.synch.outlook;

import com.mindliner.analysis.MlClassHandler;
import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcObjectCollection;
import com.mindliner.commands.CommandRecorder;
import com.mindliner.commands.PrivacyUpdateCommand;
import com.mindliner.commands.SetHeadlineCommand;
import com.mindliner.commands.TextUpdateCommand;
import com.mindliner.exceptions.synch.SynchCacheIOException;
import com.mindliner.exceptions.synch.SynchConnectionException;
import com.mindliner.main.SearchPanel;
import com.mindliner.objects.transfer.mltSynchunit;
import com.mindliner.synch.SynchWorkflowGeneric;
import com.mindliner.synchronization.foreign.ForeignAppointment;
import com.mindliner.synchronization.foreign.ForeignObject;
import com.moyosoft.connector.ms.outlook.appointment.OutlookAppointment;
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

/**
 * This class implements a syncher for Outlook appointments
 *
 * @author Marius Messerli
 */
public class MlOutlookAppointmentSynchActor extends MlOutlookSynchActor {

    private ItemsCollection outlookAppointments;
    SynchWorkflowGeneric workFlow = null;
    private List<mlcObjectCollection> mindlinerCollections = null;

    @Override
    protected FolderType getFolderType() {
        return FolderType.CALENDAR;
    }

    @Override
    protected Class getMindlinerObjectClass() {
        return mlcObjectCollection.class;
    }

    @Override
    public void synchronizeElements() {
        try {
            if (workFlow == null) {
                workFlow = new SynchWorkflowGeneric(this, progressReporter);
            }
            OutlookFolder appointmentFolder = getOutlookFolder();
            outlookAppointments = appointmentFolder.getItems();
            workFlow.run();
        } catch (SynchCacheIOException | SynchConnectionException ex) {
            Logger.getLogger(MlOutlookTaskSyncher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void updateForeignObject(ForeignObject foreignObject, int mindlinerObjectId) {
        mlcObject mo = CacheEngineStatic.getObject(mindlinerObjectId);
        if (!(foreignObject instanceof ForeignAppointment) || !(mo instanceof mlcObjectCollection)) {
            throw new IllegalArgumentException("First argument must be of type ForeignAppointment, second argument must be the id of an mlcObjectCollection");
        }
        ForeignAppointment fa = (ForeignAppointment) foreignObject;
        mlcObjectCollection oc = (mlcObjectCollection) mo;

        fa.setHeadline(oc.getHeadline());
        fa.setDescription(oc.getDescription());

        if (oc.isPrivateAccess() == true) {
            fa.setPrivacyFlag(true);
        } else {
            fa.setPrivacyFlag(false);
        }
        fa.save();
    }

    @Override
    public void updateMindlinerObject(ForeignObject fo, int mindlinerObjectId) {
        mlcObject o = CacheEngineStatic.getObject(mindlinerObjectId);
        if (!(o instanceof mlcObjectCollection) || !(fo instanceof ForeignAppointment)) {
            throw new IllegalArgumentException("The first argument must be a ForeignAppointment, the second must be the ID of an mlcObjectCollection.");
        }
        mlcObjectCollection oc = (mlcObjectCollection) o;
        ForeignAppointment fa = (ForeignAppointment) fo;

        CommandRecorder cr = CommandRecorder.getInstance();

        assert (oc.getDescription() != null) : "The mindliner OC description is null";
        assert (fa.getDescription() != null) : "The foreign object's description is null";

        // update the description before the headline or else the headline would be overwritten again
        if (!oc.getDescription().equals(fa.getDescription())) {
            cr.scheduleCommand(new TextUpdateCommand(oc, fa.getHeadline(), fa.getDescription()));
        }

        // the SetHeadlineCommand also parses tags
        if (!oc.getHeadline().equals(fa.getHeadline())) {
            cr.scheduleCommand(new SetHeadlineCommand(oc, fa.getHeadline()));
        }

        if (oc.isPrivateAccess() != fa.isPrivate()) {
            cr.scheduleCommand(new PrivacyUpdateCommand(oc, fa.isPrivate()));
        }
    }

    @Override
    public ForeignObject getForeignObject(String remoteId) {
        OutlookItem outlookItem = outlookAppointments.getItemById(new OutlookItemID(remoteId));
        if (outlookItem instanceof OutlookAppointment) {
            OutlookAppointment oa = (OutlookAppointment) outlookItem;
            return new MlOutlookAppointmentAdapter(oa);
        }
        return null;
    }

    private ForeignAppointment createOutlookAppointment(mlcObjectCollection mindlinerCollection) {
        OutlookFolder outlookFolder = getOutlookFolder();
        OutlookAppointment outlookAppointment = (OutlookAppointment) outlookFolder.createItem(ItemType.APPOINTMENT);
        MlOutlookAppointmentAdapter foreignAppointment = new MlOutlookAppointmentAdapter(outlookAppointment);
        foreignAppointment.setCategory(syncher.getCategoryName());
        updateForeignObject(foreignAppointment, mindlinerCollection.getId());
        mltSynchunit su = new mltSynchunit(
                mindlinerCollection.getId(),
                foreignAppointment.getId(),
                foreignAppointment.getModificationDate());
        syncher.getSynchUnits().add(su);
        return foreignAppointment;
    }

    @Override
    public ForeignObject createForeignObject(int mindlinerObjectId) {
        mlcObject o = CacheEngineStatic.getObject(mindlinerObjectId);
        if (o instanceof mlcObjectCollection) {
            mlcObjectCollection oc = (mlcObjectCollection) o;
            ForeignAppointment fa = createOutlookAppointment(oc);
            syncher.getSynchUnits().add(new mltSynchunit(mindlinerObjectId, fa.getId(), fa.getModificationDate()));
            return fa;
        } else {
            return null;
        }

    }

    @Override
    public List<ForeignObject> getForeignObjects() {
        ArrayList<ForeignObject> foreignAppointments;
        foreignAppointments = new ArrayList<>();
        ItemsIterator it = outlookAppointments.iterator();

        while (it.hasNext()) {
            Object outlookObject = it.next();
            if (outlookObject instanceof OutlookAppointment) {
                OutlookAppointment oa = (OutlookAppointment) outlookObject;
                foreignAppointments.add(new MlOutlookAppointmentAdapter(oa));
            }
        }
        return foreignAppointments;
    }

    @Override
    public void prepareForContentCheck() {
        mindlinerCollections = (List<mlcObjectCollection>) (List<?>) SearchPanel.getSynchItems(MlClassHandler.MindlinerObjectType.Collection, syncher.isIgnoreCompleted(), true);
    }

    @Override
    public int getMindlinerObjectWithSimilarContent(ForeignObject foreignObject) {
        if (mindlinerCollections == null) {
            System.err.println("Cannot perform content check, need to call prepareForContentCheck first");
            return -1;
        }
        for (mlcObjectCollection oc : mindlinerCollections) {
            if (isEqual(foreignObject, oc)) {
                return oc.getId();
            }
        }
        return -1;
    }
}
