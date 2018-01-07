/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.objects.transfer;

import com.mindliner.entities.Syncher;
import com.mindliner.entities.Syncher.InitialSynchDirection;
import com.mindliner.entities.Syncher.SourceBrand;
import com.mindliner.entities.Syncher.SourceType;
import com.mindliner.entities.Syncher.SynchConflictResolution;
import com.mindliner.entities.Synchunit;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * The transfer object for a syncher. Some fields only have getters becuase
 * these are determined by the server (for strict authentication) and not the
 * client.
 *
 * @author Marius Messerli
 */
public class mltSyncher implements Serializable {

    public static final int UNPERSISTED_SYNCHER_ID = -1;
    private static final long serialVersionUID = 1L;
    private int id = UNPERSISTED_SYNCHER_ID;
    private int userId;
    private SourceType type;
    private SourceBrand brand;
    private int clientId;
    private boolean ignoreCompleted;
    private boolean immediateForeignUpdate;
    private boolean deleteOnMissingCounterpart;
    private boolean contentCheck;
    private String categoryName;
    private InitialSynchDirection initialDirection;
    private String sourceFolder;
    private SynchConflictResolution conflictResolution;
    private final Collection<mltSynchunit> synchUnits = new ArrayList<>();

    public mltSyncher(Syncher syncher) {
        id = syncher.getId();
        userId = syncher.getUser().getId();
        type = syncher.getSourceType();
        brand = syncher.getSourceBrand();
        clientId = syncher.getClient().getId();
        ignoreCompleted = syncher.getIgnoreCompleted();
        immediateForeignUpdate = syncher.getImmediateForeignUpdate();
        deleteOnMissingCounterpart = syncher.getDeleteOnMissingCounterpart();
        initialDirection = syncher.getInitialDirection();
        sourceFolder = syncher.getSourceFolder();
        conflictResolution = syncher.getConflictResolution();
        contentCheck = syncher.getContentCheck();
        categoryName = syncher.getCategoryName();
        for (Synchunit su : syncher.getSynchUnits()) {
            synchUnits.add(new mltSynchunit(su));
        }
    }

    /**
     * No-arg constructor is required to feed new synchers back to the server.
     */
    public mltSyncher() {
    }

    public SourceType getType() {
        return type;
    }

    public void setType(SourceType type) {
        this.type = type;
    }

    public SourceBrand getBrand() {
        return brand;
    }

    public void setBrand(SourceBrand brand) {
        this.brand = brand;
    }

    public boolean isIgnoreCompleted() {
        return ignoreCompleted;
    }

    /**
     * Whether completed elements are to be ignored or processed.
     *
     * @param ignoreCompleted
     */
    public void setIgnoreCompleted(boolean ignoreCompleted) {
        this.ignoreCompleted = ignoreCompleted;
    }

    public boolean isImmediateForeignUpdate() {
        return immediateForeignUpdate;
    }

    public void setImmediateForeignUpdate(boolean immediateForeignUpdate) {
        this.immediateForeignUpdate = immediateForeignUpdate;
    }

    /**
     * Tells whether objects with missing counterpart will be deleted.
     *
     * @return True if objects with previously synched and now missing
     * counterparts are to be deleted, false if they should be left alone.
     */
    public boolean isDeleteOnMissingCounterpart() {
        return deleteOnMissingCounterpart;
    }

    public void setDeleteOnMissingCounterpart(boolean deleteOnMissingCounterpart) {
        this.deleteOnMissingCounterpart = deleteOnMissingCounterpart;
    }

    public InitialSynchDirection getInitialDirection() {
        return initialDirection;
    }

    public void setInitialDirection(InitialSynchDirection initialDirection) {
        this.initialDirection = initialDirection;
    }

    public String getSourceFolder() {
        return sourceFolder;
    }

    public void setSourceFolder(String sourceFolder) {
        this.sourceFolder = sourceFolder;
    }

    public SynchConflictResolution getConflictResolution() {
        return conflictResolution;
    }

    public void setConflictResolution(SynchConflictResolution conflictResolution) {
        this.conflictResolution = conflictResolution;
    }

    public Collection<mltSynchunit> getSynchUnits() {
        return synchUnits;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public boolean isContentCheck() {
        return contentCheck;
    }

    public void setContentCheck(boolean contentCheck) {
        this.contentCheck = contentCheck;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + this.id;
        hash = 47 * hash + this.userId;
        hash = 47 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 47 * hash + (this.brand != null ? this.brand.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final mltSyncher other = (mltSyncher) obj;
        if (this.id != other.id) {
            return false;
        }
        if (this.userId != other.userId) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        if (this.brand != other.brand) {
            return false;
        }
        return true;
    }

}
