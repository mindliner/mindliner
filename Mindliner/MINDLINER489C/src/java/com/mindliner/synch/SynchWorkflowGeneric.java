/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.synch;

import com.mindliner.analysis.MlClassHandler.MindlinerObjectType;
import com.mindliner.synchronization.foreign.ForeignObject;
import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.commands.CommandRecorder;
import com.mindliner.commands.ModificationUpdateCommand;
import com.mindliner.commands.ObjectDeletionCommand;
import com.mindliner.exceptions.synch.SynchCacheIOException;
import com.mindliner.exceptions.synch.SynchConnectionException;
import com.mindliner.main.MindlinerMain;
import com.mindliner.main.SearchPanel;
import com.mindliner.objects.transfer.mltSynchunit;
import com.mindliner.synchronization.MlSynchProgressReporter;
import com.mindliner.synchronization.SynchActor;
import com.moyosoft.connector.com.ComponentObjectModelException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implement a generic workflow for synchronization operations and
 * constitutes the outermost synch framework in Mindliner. Any and all synch
 * operations should use this framework and inject the specific syncher via
 * constructor argument.
 *
 * @author Marius Messerli
 */
public class SynchWorkflowGeneric {

    private final MlSynchProgressReporter progress;
    private final SynchActor synchActor;

    public SynchWorkflowGeneric(SynchActor sa, MlSynchProgressReporter progress) {
        this.synchActor = sa;
        this.progress = progress;
    }

    private boolean isModified(ForeignObject fo, mltSynchunit su) {
        // need to clip precision down to 1 second because mysql timestamp does not do millis
        long foModTimeClipped = fo.getModificationDate().getTime() / 1000;
        long suModTimeClipped = su.getLastSynched().getTime() / 1000;
        return foModTimeClipped > suModTimeClipped;
    }

    private boolean isModified(mlcObject mo, mltSynchunit su) {
        // need to clip precision down to 1 second because mysql timestamp does not do millis
        long moModTimeClipped = mo.getModificationDate().getTime() / 1000;
        long suModTimeClipped = su.getLastSynched().getTime() / 1000;
        return moModTimeClipped > suModTimeClipped;
    }

    /**
     * This does the actual work. Normally this method must be private and is
     * only public during debugging phase so I can call it directly rather than
     * in a separate thread.
     *
     * @throws SynchCacheIOException
     * @throws SynchConnectionException
     */
    public void run() throws SynchCacheIOException, SynchConnectionException {
        boolean synchUnitUpdate;
        CommandRecorder cr = CommandRecorder.getInstance();
        ContentComparisonDialog ccd = new ContentComparisonDialog(null, true);
        ccd.setLocationRelativeTo(MindlinerMain.getInstance());

        progress.printLine(
                "-- Type = " + synchActor.getSyncher().getType().name()
                + ", Brand = " + synchActor.getSyncher().getBrand().name()
                + ", Data Pool = " + CacheEngineStatic.getClient(synchActor.getSyncher().getClientId())
        );

        // re-synchronize previously synched objects
        if (synchActor.getSynchUnits().isEmpty()) {
            progress.printLine("1ST - this appears to be the first synchronization for this type and brand.");
        } else {
            progress.printLine("CHK - checking for updates on " + synchActor.getSynchUnits().size() + " previously synchronized items.");

            Iterator it = synchActor.getSynchUnits().iterator();

            while (it.hasNext() && progress.cancelled() == false) {
                mltSynchunit su = (mltSynchunit) it.next();
                synchUnitUpdate = false;
                mlcObject mindlinerObject = null;
                if (su.getMindlinerObjectId() > -1) {
                    mindlinerObject = CacheEngineStatic.getObject(su.getMindlinerObjectId());
                }
                ForeignObject foreignObject = null;
                try {
                    foreignObject = synchActor.getForeignObject(su.getForeignObjectId());
                } catch (ComponentObjectModelException ex) {
                    // do nothing, will handle below as foreignObject==null
                }
                if (mindlinerObject != null) {

                    if (foreignObject == null) {
                        if (synchActor.getSyncher().isDeleteOnMissingCounterpart()) {
                            progress.printLine("-FD - foreign object deleted, removing mindliner object: "
                                    + mindlinerObject.getHeadline() + " (id=" + mindlinerObject.getId() + ")");
                            cr.scheduleCommand(new ObjectDeletionCommand(mindlinerObject));
                            it.remove();
                        } else {
                            progress.printLine("-FL - foreign object deleted leaving Mindliner object untouched as specified: " + mindlinerObject.getHeadline() + " (id=" + mindlinerObject.getId() + ")");
                            it.remove();
                        }
                    } else {
                        boolean mindlinerModified = isModified(mindlinerObject, su);
                        boolean foreignModified = isModified(foreignObject, su);
                        if (mindlinerModified) {
                            if (foreignModified) { // conflict: both modified
                                switch (synchActor.getSyncher().getConflictResolution()) {
                                    case MindlinerWins:
                                        progress.printLine("BM - both items modified: Mindliner object wins for :" + mindlinerObject.getHeadline());
                                        synchActor.updateForeignObject(foreignObject, mindlinerObject.getId());
                                        synchActor.updateSynchUnit(foreignObject, mindlinerObject.getId(), su);
                                        synchUnitUpdate = true;
                                        break;

                                    case ForeignWins:
                                        progress.printLine("BF - both items modified, foreign object wins for : " + foreignObject.getHeadline());
                                        synchActor.updateMindlinerObject(foreignObject, mindlinerObject.getId());
                                        mindlinerObject = CacheEngineStatic.getObject(mindlinerObject.getId());
                                        synchActor.updateSynchUnit(foreignObject, mindlinerObject.getId(), su);
                                        synchUnitUpdate = true;
                                        break;

                                    case Manual:
                                        ccd.setComparisonContent(mindlinerObject, foreignObject, ContentComparisonDialog.ComparisonMode.ChooseWinner);
                                        ccd.promptUser();
                                        progress.printLine("BA - both items modified, manually resolved to: " + ccd.getConflictResolution() + " for mindliner id " + mindlinerObject.getId());
                                        switch (ccd.getConflictResolution()) {
                                            case ForeignWins:
                                                synchActor.updateMindlinerObject(foreignObject, mindlinerObject.getId());
                                                mindlinerObject = CacheEngineStatic.getObject(mindlinerObject.getId());
                                                synchActor.updateSynchUnit(foreignObject, mindlinerObject.getId(), su);
                                                synchUnitUpdate = true;
                                                break;
                                            case MindlinerWins:
                                                synchActor.updateForeignObject(foreignObject, mindlinerObject.getId());
                                                synchActor.updateSynchUnit(foreignObject, mindlinerObject.getId(), su);
                                                synchUnitUpdate = true;
                                                break;
                                            case Manual:
                                                break;
                                            default:
                                                throw new AssertionError();
                                        }
                                        break;
                                }
                            } else { // mindliner modified, foreign object is not not modified
                                progress.printLine("MU - Mindliner object updated, synchronizing foreign object: " + mindlinerObject.getHeadline());
                                synchActor.updateForeignObject(foreignObject, mindlinerObject.getId());
                                synchActor.updateSynchUnit(foreignObject, mindlinerObject.getId(), su);
                                synchUnitUpdate = true;
                            }
                        } else {
                            if (foreignModified == true) {
                                progress.printLine("FU - foreign object updated, synchronizating mindliner object: " + foreignObject.getHeadline());
                                synchActor.updateMindlinerObject(foreignObject, mindlinerObject.getId());
                                mindlinerObject = CacheEngineStatic.getObject(mindlinerObject.getId());
                                synchActor.updateSynchUnit(foreignObject, mindlinerObject.getId(), su);
                                synchUnitUpdate = true;
                            } else {
                                // neither object was modified, if user does not want to synch completed items then remove the synch unit now
                                if (mindlinerObject.isArchived() && foreignObject.isCompleted() && synchActor.getSyncher().isIgnoreCompleted()) {
                                    it.remove();
                                    progress.printLine("-L - removed synchronization link for completed object: " + foreignObject.getHeadline());
                                }
                            }
                        }
                    }
                } else { // mindliner object is null
                    if (foreignObject != null) {
                        if (synchActor.safeDeleteForeignObject(foreignObject) == true) {
                            it.remove();
                        }
                    }
                }
                if (synchUnitUpdate == true) {
                    // need to patch the modification time of the mindliner object to avoid swinging synch operations
                    cr.scheduleCommand(new ModificationUpdateCommand(mindlinerObject, su.getLastSynched()));
                }
            }
        }

        // initial synchronization for previously un-synched items
        switch (synchActor.getSyncher().getInitialDirection()) {
            case Export:
                initialSynchOutbound(progress);
                break;

            case Import:
                initialSynchInbound(progress);
                break;

            case Both:
                initialSynchOutbound(progress);
                initialSynchInbound(progress);
                break;

            default:
                progress.printLine("synchronization preference not implemented" + synchActor.getSyncher().getInitialDirection());
                break;

        }
        synchActor.store();
    }

    /**
     * This function searches for previously unsynched Outlook objects of the
     * category MindlinerCategory and copies these to Mindliner.
     */
    private void initialSynchInbound(MlSynchProgressReporter progress) {
        ContentComparisonDialog ccd = new ContentComparisonDialog(null, true);
        if (synchActor.getSyncher().isContentCheck()) {
            synchActor.prepareForContentCheck();

        }
        List<ForeignObject> foreignObjects = synchActor.getForeignObjects();
        for (ForeignObject fo : foreignObjects) {
            String categoryString = fo.getCategory();
            if (categoryString.equals(synchActor.getSyncher().getCategoryName())
                    && synchActor.getSynchUnit(fo) == null
                    && (!synchActor.getSyncher().isIgnoreCompleted() || !fo.isCompleted())) {

                // if content checking is configured then check if a similar object already exists
                mlcObject mindlinerObject = null;
                if (synchActor.getSyncher().isContentCheck()) {
                    int similarMindlinerObjectId = synchActor.getMindlinerObjectWithSimilarContent(fo);
                    if (similarMindlinerObjectId != -1) {
                        mindlinerObject = CacheEngineStatic.getObject(similarMindlinerObjectId);
                        ccd.setComparisonContent(mindlinerObject, fo, ContentComparisonDialog.ComparisonMode.CheckSimilarity);
                        ccd.setLocationRelativeTo(MindlinerMain.getInstance());
                        ccd.promptUser();
                        if (!ccd.isObjectSimilarity()) {
                            mindlinerObject = null;
                        }
                    }
                }
                if (mindlinerObject == null) {
                    int newObjectId = synchActor.createMindlinerObject();
                    mindlinerObject = CacheEngineStatic.getObject(newObjectId);
                    try {
                        // this is a bad temp fix for a race condition between the thread creating the object and this one
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(SynchWorkflowGeneric.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                synchActor.updateMindlinerObject(fo, mindlinerObject.getId());
                // now load updated object to make sure the synch unit reflects the latest version
                mindlinerObject = CacheEngineStatic.getObject(mindlinerObject.getId());
                mltSynchunit su = new mltSynchunit(mindlinerObject.getId(), fo.getId(), fo.getModificationDate());
                synchActor.addSynchUnit(su);
                progress.printLine("IN - inbound synch for " + fo.getHeadline());
            }
        }
    }

    private void initialSynchOutbound(MlSynchProgressReporter progress) {
        MindlinerObjectType objectType;
        switch (synchActor.getSyncher().getType()) {
            case TaskType:
                objectType = MindlinerObjectType.Task;
                break;

            case AppointmentType:
                objectType = MindlinerObjectType.Collection;
                break;

            case ContactType:
                objectType = MindlinerObjectType.Contact;
                break;

            case InfoType:
                objectType = MindlinerObjectType.Knowlet;
                break;

            default:
                throw new IllegalStateException("Synch actor type is not defined");

        }
        List<mlcObject> mindlinerSynchCandidates = SearchPanel.getSynchItems(objectType, synchActor.getSyncher().isIgnoreCompleted(), false);
        Iterator mindlinerObjectIterator = mindlinerSynchCandidates.iterator();

        while (mindlinerObjectIterator.hasNext()) {
            mlcObject mo;
            mo = (mlcObject) mindlinerObjectIterator.next();
            if (synchActor.getSynchUnit(mo.getId()) == null) {
                synchActor.createForeignObject(mo.getId());
                progress.printLine("OUT - outbound synch  for " + mo.getHeadline());
            }
        }
    }
}
