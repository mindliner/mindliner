/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.synch.outlook;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.categories.mlsPriority;
import com.mindliner.synchronization.foreign.ForeignTask;
import com.moyosoft.connector.ms.outlook.item.ImportanceType;
import com.moyosoft.connector.ms.outlook.item.SensitivityType;
import com.moyosoft.connector.ms.outlook.task.OutlookTask;
import com.moyosoft.connector.ms.outlook.task.TaskStatus;
import java.util.Date;

/**
 * This class implements a Foreign Task for MS-Outlook.
 *
 * @author Marius Messerli
 */
public class MlOutlookTaskAdapter extends ForeignTask {

    private OutlookTask outlookTask;

    public MlOutlookTaskAdapter(OutlookTask foreignTask) {
        this.outlookTask = foreignTask;
    }

    @Override
    public String getId() {
        return outlookTask.getItemId().getEntryId();
    }

    @Override
    public String getHeadline() {
        return outlookTask.getSubject();
    }

    @Override
    public String getDescription() {
        return outlookTask.getBody();
    }

    @Override
    public Date getModificationDate() {
        return outlookTask.getLastModificationTime();
    }

    @Override
    public Date getCreationDate() {
        return outlookTask.getCreationTime();
    }

    @Override
    public mlsPriority getTaskPriority() {
        mlsPriority priority;
        if (outlookTask.getImportance() == ImportanceType.HIGH) {
            priority = CacheEngineStatic.getPriority("high");
        } else if (outlookTask.getImportance() == ImportanceType.NORMAL) {
            priority = CacheEngineStatic.getPriority("norm");
        } else {
            priority = CacheEngineStatic.getPriority("low");
        }
        return priority;
    }

    @Override
    public void setPriority(mlsPriority priority) {
        ImportanceType importance;
        switch (priority.getImportance()) {
            case 4:
                importance = ImportanceType.HIGH;
                break;

            case 2:
                importance = ImportanceType.NORMAL;
                break;

            case 1:
                importance = ImportanceType.LOW;
                break;

            default:
                importance = ImportanceType.NORMAL;
                break;
        }
        outlookTask.setImportance(importance);
    }

    @Override
    public void setHeadline(String headline) {
        outlookTask.setSubject(headline);
    }

    @Override
    public void setDescription(String description) {
        outlookTask.setBody(description);
    }

    @Override
    public boolean isPrivate() {
        if (outlookTask.getSensitivity().equals(SensitivityType.PRIVATE)) {
            return true;
        }
        return false;
    }

    @Override
    public void setPrivacyFlag(boolean privacy) {
        if (privacy) {
            outlookTask.setSensitivity(SensitivityType.PRIVATE);
        } else {
            outlookTask.setSensitivity(SensitivityType.NORMAL);
        }
    }

    @Override
    public Date getDueDate() {
        return outlookTask.getStartDate();
    }

    @Override
    public void setDueDate(Date duedate) {
        outlookTask.setStartDate(duedate);
    }

    @Override
    public void setCompletionState(boolean completed) {
        if (completed) {
            outlookTask.setStatus(TaskStatus.COMPLETE);
        } else {
            outlookTask.setStatus(TaskStatus.NOT_STARTED);
        }
    }

    @Override
    public boolean isCompleted() {
        if (outlookTask.getStatus().equals(TaskStatus.COMPLETE)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void setOwnerName(String name) {
        outlookTask.setOwner(name);
    }

    @Override
    public String getOwnerName() {
        return outlookTask.getOwner();
    }

    @Override
    public void save() {
        outlookTask.save();
    }

    @Override
    public void setCategory(String category) {
        outlookTask.setCategories(category);
    }

    @Override
    public String getCategory() {
        return outlookTask.getCategories();
    }

    @Override
    public void delete() {
        outlookTask.delete();
    }
}
