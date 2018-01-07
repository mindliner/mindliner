/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.main;

import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.categories.mlsPriority;
import com.mindliner.clientobjects.mlcClient;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcUser;
import com.mindliner.commands.*;
import com.mindliner.commands.bulk.*;
import com.mindliner.enums.ObjectReviewStatus;
import com.mindliner.events.ObjectChangeManager;
import com.mindliner.serveraccess.OnlineManager;
import java.util.Date;
import java.util.List;

/**
 * This class provides functionality to update several mindliner objects at
 * once.
 *
 * @author Marius Messerli
 */
public class BulkUpdater {

    public static void publishUpdates(List<mlcObject> items) {
        items.stream().forEach((o) -> {
            publishUpdate(o);
        });
    }

    public static void publishUpdate(mlcObject item) {
        if (!OnlineManager.waitForServerMessages()) {
            ObjectChangeManager.objectChanged(item);
        } else {
            // updates occur as a response to the incoming messages
        }
    }

    public static void publishDeletion(mlcObject item) {
        // here, unlike in the publishUpdate case we need to publish the deletion 
        // in any case as the message listener may not find the object in the cache anymore and ignore the message
        ObjectChangeManager.objectDeleted(item);
    }

    public void updateConfidentiality(List<mlcObject> items, mlsConfidentiality conf) {
        CommandRecorder cr = CommandRecorder.getInstance();
        switch (items.size()) {
            case 0:
                break;
            case 1:
                cr.scheduleCommand(new ConfidentialityUpdateCommand(items.get(0), conf));
                break;
            default:
                cr.scheduleCommand(new ConfidentialityBulkUpdateCommand(items, conf));
                break;
        }
    }

    public void updateDataPool(List<mlcObject> items, mlcClient dataPool) {
        CommandRecorder cr = CommandRecorder.getInstance();
        switch (items.size()) {
            case 0:
                break;
            case 1:
                cr.scheduleCommand(new DataPoolUpdateCommand(items.get(0), dataPool));
                break;
            default:
                cr.scheduleCommand(new DataPoolBulkUpdateCommand(items, dataPool));
                break;
        }
    }

    public void updateDueDate(List<mlcObject> items, Date dueDate) {
        CommandRecorder cr = CommandRecorder.getInstance();
        switch (items.size()) {
            case 0:
                break;
            case 1:
                cr.scheduleCommand(new DueDateUpdateCommand(items.get(0), dueDate));
                break;

            default:
                cr.scheduleCommand(new DueDateBulkUpdateCommand(items, dueDate));
                break;
        }
    }

    public void updateOwner(List<mlcObject> items, mlcUser owner) {
        CommandRecorder cr = CommandRecorder.getInstance();
        switch (items.size()) {
            case 0:
                break;
            case 1:
                cr.scheduleCommand(new SetOwnerCommand(items.get(0), owner));
                break;
            default:
                cr.scheduleCommand(new BulkUpdateOwnerCommand(items, owner));
                break;
        }
    }

    public void updatePrivacyState(List<mlcObject> items, boolean newState) {
        CommandRecorder cr = CommandRecorder.getInstance();
        switch (items.size()) {
            case 0:
                break;
            case 1:
                cr.scheduleCommand(new PrivacyUpdateCommand(items.get(0), newState));
                break;
            default:
                cr.scheduleCommand(new PrivacyBulkUpdateCommand(items, newState));
                break;
        }
    }

    public void updateCompletionState(List<mlcObject> items, boolean newState) {
        CommandRecorder cr = CommandRecorder.getInstance();
        switch (items.size()) {
            case 0:
                break;
            case 1:
                cr.scheduleCommand(new CompletionUpdateCommand(items.get(0), newState));
                break;
            default:
                cr.scheduleCommand(new CompletionBulkUpdateCommand(items, newState));
                break;
        }
    }

    public void updateArchiveState(List<mlcObject> items, boolean newState) {
        CommandRecorder cr = CommandRecorder.getInstance();
        switch (items.size()) {
            case 0:
                break;
            case 1:
                cr.scheduleCommand(new ArchiveStateUpdateCommand(items.get(0), newState));
                break;
            default:
                cr.scheduleCommand(new ArchiveBulkUpdateCommand(items, newState));
                break;
        }
    }

    public void updateTaskPriority(List<mlcObject> items, mlsPriority p) {
        CommandRecorder cr = CommandRecorder.getInstance();
        switch (items.size()) {
            case 0:
                break;
            case 1:
                cr.scheduleCommand(new PriorityUpdateCommand(items.get(0), p));
                break;
            default:
                cr.scheduleCommand(new PriorityBulkUpdateCommand(items, p));
                break;
        }
    }

    public void updateObjectStatus(List<mlcObject> items, ObjectReviewStatus s) {
        CommandRecorder cr = CommandRecorder.getInstance();
        switch (items.size()) {
            case 0:
                break;
            case 1:
                cr.scheduleCommand(new StatusUpdateCommand(items.get(0), s));
                break;
            default:
                cr.scheduleCommand(new StatusBulkUpdateCommand(items, s));
                break;
        }
    }

}
