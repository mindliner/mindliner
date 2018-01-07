/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.objects.transfer;

import com.mindliner.entities.Synchunit;
import com.mindliner.entities.mlsObject;
import com.mindliner.enums.ObjectReviewStatus;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * The base class for the transfer versions of Mindliner information objects. It
 * designed to transport the content of mlsObjects to the desktop application in
 * a leight-weight manner and allows for both the client (mlcObject) versions
 * and the server (mlsObject) versions to adapt to their specific needs.
 *
 * @author Marius Messerli
 */
public class MltObject implements Serializable {

    public static final int STAND_ALONE_ID = -1;

    private int id = -1;
    private int clientId = -1;
    private int version = -1;
    private String headline = "";
    private Date modificationDate = new Date();
    private Date creationDate = new Date();
    private int confidentialityId = -1;
    private int ownerId;
    private double rating = -1;
    private boolean privateAccess = false;
    private String description = "";
    // the relative count is used so that the client side knows whether there are relatives even prior to loading them
    private int relativeCount = 0;
    private final List<mltSynchunit> synchUnits = new ArrayList<>();
    private static final long serialVersionUID = 19640205L;
    private ObjectReviewStatus status = ObjectReviewStatus.REVIEWED;
    private boolean relativesOrdered = false;
    private int island_id = STAND_ALONE_ID;
    private boolean archived = false;

    public MltObject() {
    }

    public MltObject(mlsObject o) {
        id = o.getId();
        version = (short) o.getVersion();
        headline = o.getHeadline();
        description = o.getDescription();
        modificationDate = o.getModificationDate();
        creationDate = o.getCreationDate();
        confidentialityId = (short) o.getConfidentiality().getId();
        ownerId = o.getOwner().getId();
        rating = o.getRating();
        privateAccess = o.getPrivateAccess();
        clientId = o.getClient().getId();
        status = o.getStatus();
        relativeCount = o.getRelativeCount();
        relativesOrdered = o.isRelativesOrdered();
        archived = o.isArchived();
        if (o.getIsland() == null) {
            island_id = STAND_ALONE_ID;
        } else {
            island_id = o.getIsland().getId();
        }
        if (o.getSynchUnits() != null) {
            for (Synchunit s : o.getSynchUnits()) {
                synchUnits.add(new mltSynchunit(s));
            }
        }
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getVersion() {
        return version;
    }

    public String getHeadline() {
        return headline;
    }

    public Date getModificationDate() {
        return modificationDate;
    }

    public int getConfidentialityId() {
        return confidentialityId;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public double getRating() {
        return rating;
    }

    public boolean isPrivateAccess() {
        return privateAccess;
    }

    public String getDescription() {
        return description;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public int getClientId() {
        return clientId;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setConfidentialityId(int confidentialityId) {
        this.confidentialityId = confidentialityId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public void setPrivateAccess(boolean privateAccess) {
        this.privateAccess = privateAccess;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<mltSynchunit> getSynchUnits() {
        return synchUnits;
    }

    public ObjectReviewStatus getStatus() {
        return status;
    }

    public void setStatus(ObjectReviewStatus status) {
        this.status = status;
    }

    /**
     * Returns the id of the island (i.e. connected component) this object is a
     * part of.
     *
     * @return The id of the island or STAND_ALONE_ID (-1) if this object isn's
     * on any island.
     */
    public int getIsland_id() {
        return island_id;
    }

    public void setIsland_id(int island_id) {
        this.island_id = island_id;
    }

    /**
     * Returns the number of relatives that this object has. Note that this may
     * vary from the number of displayed objects in the case where the caller is
     * not authorized to see all of the relatives.
     *
     * @return The total number of relatives (accessible and non-accessible) of
     * this object.
     */
    public int getRelativeCount() {
        return relativeCount;
    }

    public boolean isRelativesOrdered() {
        return relativesOrdered;
    }

    public void setRelativesOrdered(boolean relativesOrdered) {
        this.relativesOrdered = relativesOrdered;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

}
