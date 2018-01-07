/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.synch.outlook;

import com.mindliner.analysis.MlClassHandler;
import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcTask;
import com.mindliner.commands.CommandRecorder;
import com.mindliner.commands.CompletionUpdateCommand;
import com.mindliner.commands.DueDateUpdateCommand;
import com.mindliner.commands.PriorityUpdateCommand;
import com.mindliner.commands.PrivacyUpdateCommand;
import com.mindliner.commands.SetHeadlineCommand;
import com.mindliner.commands.TextUpdateCommand;
import com.mindliner.events.ObjectChangeObserver;
import com.mindliner.exceptions.synch.SynchCacheIOException;
import com.mindliner.exceptions.synch.SynchConnectionException;
import com.mindliner.main.SearchPanel;
import com.mindliner.objects.transfer.mltSynchunit;
import com.mindliner.synch.SynchWorkflowGeneric;
import com.mindliner.synchronization.SynchActor;
import com.mindliner.synchronization.foreign.ForeignObject;
import com.mindliner.synchronization.foreign.ForeignTask;
import com.moyosoft.connector.ms.outlook.folder.FolderType;
import com.moyosoft.connector.ms.outlook.folder.OutlookFolder;
import com.moyosoft.connector.ms.outlook.item.ItemType;
import com.moyosoft.connector.ms.outlook.item.ItemsCollection;
import com.moyosoft.connector.ms.outlook.item.ItemsIterator;
import com.moyosoft.connector.ms.outlook.item.OutlookItem;
import com.moyosoft.connector.ms.outlook.item.OutlookItemID;
import com.moyosoft.connector.ms.outlook.task.OutlookTask;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements a synch worker for the object type Task and the source
 * brand Outlook. It contains all the specifics on how to deal with Outlook
 * tasks.
 *
 * @author Marius Messerli
 */
public class MlOutlookTaskSyncher extends MlOutlookSynchActor implements ObjectChangeObserver {

    private ItemsCollection outlookTasks;
    SynchWorkflowGeneric workFlow = null;
    List<mlcTask> mindlinerTasks = null;

    @Override
    public void synchronizeElements() {
        try {
            if (workFlow == null) {
                workFlow = new SynchWorkflowGeneric(this, progressReporter);
            }
            OutlookFolder taskFolder = getOutlookFolder();
            outlookTasks = taskFolder.getItems();
            workFlow.run();
        } catch (SynchCacheIOException ex) {
            Logger.getLogger(MlOutlookTaskSyncher.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SynchConnectionException ex) {
            Logger.getLogger(MlOutlookTaskSyncher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public ForeignObject getForeignObject(String remoteId) {
        OutlookItem outlookItem = outlookTasks.getItemById(new OutlookItemID(remoteId));
        if (outlookItem instanceof OutlookTask) {
            OutlookTask ot = (OutlookTask) outlookItem;
            return new MlOutlookTaskAdapter(ot);
        }
        return null;
    }

    @Override
    protected FolderType getFolderType() {
        return FolderType.TASKS;
    }

    @Override
    public void updateForeignObject(ForeignObject foreignObject, int mindlinerObjectId) {
        mlcObject mo = CacheEngineStatic.getObject(mindlinerObjectId);
        if (!(foreignObject instanceof ForeignTask) || !(mo instanceof mlcTask)) {
            System.err.println("MloutlookTaskSyncher: got wrong type to synch: FO " + foreignObject.getClass() + " and MO " + mo.getClass() + " MOid = " + mo.getId());
            throw new IllegalArgumentException("First argument must be of type ForeignTask, the second argument must be the id of an mlcTask");
        }
        ForeignTask ftask = (ForeignTask) foreignObject;
        mlcTask mtask = (mlcTask) mo;

        ftask.setHeadline(mtask.getHeadline());
        ftask.setDescription(mtask.getDescription());

        // due date - make sure due date is after start date
        Date oStartDate = ftask.getDueDate();
        if (mtask.getDueDate() != null) {
            if (oStartDate == null || (mtask.getDueDate().compareTo(oStartDate) > 0)) {
                ftask.setDueDate(mtask.getDueDate());
            } else {
                progressReporter.printLine("warning: cannot update due date of Outlook task: "
                        + "due date of Mindliner task is before start date of Outlook task (mTaskId=" + mtask.getId());
            }
        }
        ftask.setOwnerName(mtask.getOwner().getLoginName());

        ftask.setPriority(mtask.getPriority());
        if (mtask.isPrivateAccess() == true) {
            ftask.setPrivacyFlag(true);
        } else {
            ftask.setPrivacyFlag(false);
        }

        ftask.setCompletionState(mtask.isCompleted());
        ftask.save();
    }

    @Override
    public void updateMindlinerObject(ForeignObject fo, int mindlinerObjectId) {
        mlcObject o = CacheEngineStatic.getObject(mindlinerObjectId);
        if (!(o instanceof mlcTask) || !(fo instanceof ForeignTask)) {
            return;
        }
        mlcTask t = (mlcTask) o;
        ForeignTask ft = (ForeignTask) fo;

        CommandRecorder cr = CommandRecorder.getInstance();

        // update the description before the headline or else the headline would be overwritten again
        if (t.getDescription().compareTo(ft.getDescription()) != 0) {
            cr.scheduleCommand(new TextUpdateCommand(t, ft.getHeadline(), ft.getDescription()));
        }

        // the SetHeadlineCommand also parses tags
        if (t.getHeadline().compareTo(ft.getHeadline()) != 0) {
            cr.scheduleCommand(new SetHeadlineCommand(t, ft.getHeadline()));
        }

        cr.scheduleCommand(new DueDateUpdateCommand(t, ft.getDueDate()));

        if (t.isPrivateAccess() != ft.isPrivate()) {
            cr.scheduleCommand(new PrivacyUpdateCommand(t, ft.isPrivate()));
        }

        if (t.isCompleted() != ft.isCompleted()) {
            cr.scheduleCommand(new CompletionUpdateCommand(t, ft.isCompleted()));
        }

        // todo ** bad bad The following lookup of priorities by string is very fragile as values can change in the database!
        if (!ft.getTaskPriority().equals(t.getPriority())) {
            cr.scheduleCommand(new PriorityUpdateCommand(t, ft.getTaskPriority()));
        }
    }

    /**
     * Returns the outlook task corresponding to the the specified mindliner
     * task
     *
     * @param mindlinerTask
     * @return The outlook task or null if none found.
     */
    private OutlookTask getOutlookTask(mlcTask mindlinerTask) {
        mltSynchunit synchUnit = getSynchUnit(mindlinerTask.getId());
        if (synchUnit != null) {
            OutlookFolder taskFolder = getOutlookFolder();
            ItemsCollection items = taskFolder.getItems();
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i) instanceof OutlookTask) {
                    OutlookTask ot = (OutlookTask) items.get(i);
                    if (synchUnit.getForeignObjectId().equals(ot.getItemId().getEntryId())) {
                        return ot;
                    }
                }
            }
        }
        return null;
    }

    private mltSynchunit getSynchUnitForObject(mlcObject o) {
        for (mltSynchunit s : syncher.getSynchUnits()) {
            if (s.getMindlinerObjectId() == o.getId()) {
                return s;
            }
        }
        return null;
    }

    @Override
    public void objectChanged(mlcObject o) {
        if (o != null) {
            if (syncher.isImmediateForeignUpdate() && o instanceof mlcTask) {
                mltSynchunit s = getSynchUnitForObject(o);
                if (s != null) {
                    ForeignObject foreignObject = getForeignObject(s.getForeignObjectId());
                    if (foreignObject != null) {
                        updateForeignObject(foreignObject, o.getId());
                    }
                }
            }
        }
    }

    @Override
    public void objectDeleted(mlcObject o) {
        if (o instanceof mlcTask) {
            mlcTask t = (mlcTask) o;
            OutlookTask outlookTask = getOutlookTask(t);
            if (outlookTask != null) {
                // the following works because the adapter just wraps the actual object which will be referenced not copied
                safeDeleteForeignObject(new MlOutlookTaskAdapter(outlookTask));
            }
        }
    }

    @Override
    public void objectCreated(mlcObject o) {
        if (syncher.isImmediateForeignUpdate() && o instanceof mlcTask) {
            System.err.println("New task created but immediate synch with Outlook not yet implemented. Will be done the next time you manually run a synch.");
        }
    }

    @Override
    public void objectReplaced(int oldId, mlcObject o) {
        // TODO: if oldId was task -> safeDeleteForeignObject. if o is task -> objectCreated
    }
    
    

    private ForeignTask createOutlookTask(mlcTask mindlinerTask) {
        OutlookFolder outlookFolder = getOutlookFolder();
        OutlookTask outlookTask = (OutlookTask) outlookFolder.createItem(ItemType.TASK);
        MlOutlookTaskAdapter foreignTask = new MlOutlookTaskAdapter(outlookTask);
        foreignTask.setCategory(syncher.getCategoryName());
        updateForeignObject(foreignTask, mindlinerTask.getId());
        mltSynchunit su = new mltSynchunit(
                mindlinerTask.getId(),
                foreignTask.getId(),
                foreignTask.getModificationDate());
        syncher.getSynchUnits().add(su);
        return foreignTask;
    }

    /**
     * Creates a new outlook task
     * @return a new task
     */
    @Override
    public ForeignObject createForeignObject(int mindlinerObjectId) {
        mlcObject o = CacheEngineStatic.getObject(mindlinerObjectId);
        if (o instanceof mlcTask) {
            mlcTask t = (mlcTask) o;
            return createOutlookTask(t);
        } else {
            return null;
        }
    }

    @Override
    protected Class getMindlinerObjectClass() {
        return mlcTask.class;
    }

    @Override
    public List<ForeignObject> getForeignObjects() {
        ArrayList<ForeignObject> foreignTasks;
        foreignTasks = new ArrayList<>();
        ItemsIterator it = outlookTasks.iterator();
        while (it.hasNext()) {
            Object outlookObject = it.next();
            if (outlookObject instanceof OutlookTask) {
                OutlookTask ot = (OutlookTask) outlookObject;
                if (!syncher.isIgnoreCompleted() || !ot.isComplete()) {
                    foreignTasks.add(new MlOutlookTaskAdapter(ot));
                }
            }
        }
        return foreignTasks;
    }

    @Override
    public void prepareForContentCheck() {
        mindlinerTasks = (List<mlcTask>) (List<?>) SearchPanel.getSynchItems(MlClassHandler.MindlinerObjectType.Task, syncher.isIgnoreCompleted(), true);
    }

    @Override
    public int getMindlinerObjectWithSimilarContent(ForeignObject foreignObject) {
        if (mindlinerTasks == null) {
            System.err.println("Cannot perform content check, need to call prepareForContentCheck first");
            return -1;
        }
        for (mlcTask t : mindlinerTasks) {
            if (isEqual(foreignObject, t)) {
                return t.getId();
            }
        }
        return -1;

    }
}
