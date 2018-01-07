package com.mindliner.clientobjects;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.entities.ObjectAttributes;
import com.mindliner.entities.mlsClient;
import com.mindliner.enums.ObjectReviewStatus;
import com.mindliner.objects.transfer.MltObject;
import com.mindliner.objects.transfer.mltSynchunit;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The base class for the client objects. These are flat objects without any
 * relations. The relations are built dynamically on the server and delivered to
 * the client as lists.
 *
 * @author Marius Messerli
 */
public abstract class mlcObject implements Serializable {

    public static final int NEW_OBJECT_ID = -1;
    private int id = NEW_OBJECT_ID;
    private mlcClient client = null;
    protected String headline = "";
    private Date modificationDate = new Date();
    private Date creationDate = new Date();
    private mlsConfidentiality confidentiality = null;
    private mlcUser owner;
    private int version = -1;
    private double rating = 0;
    private boolean privateAccess = false;
    private String description = "";
    private List<mltSynchunit> synchUnits = null;
    private List<MlcImage> icons = null;
    private int islandId = MltObject.STAND_ALONE_ID;
    private int relativeCount = 0;
    private static final long serialVersionUID = 100L;
    private ObjectReviewStatus status = ObjectReviewStatus.REVIEWED;
    private boolean relativesOrdered = false;
    private boolean archived = false;
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public mlcClient getClient() {
        return client;
    }

    public void setClient(mlcClient c) {
        client = c;
    }

    public void setConfidentiality(mlsConfidentiality c) {
        confidentiality = c;
    }

    public mlsConfidentiality getConfidentiality() {
        return confidentiality;
    }

    public void setOwner(mlcUser o) {
        owner = o;
    }

    public mlcUser getOwner() {
        return owner;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String line) {
        if (line != null) {
            headline = line;
        } else {
            System.err.println("ignoring attempt to set headline to null for object id = " + getId());
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String d) {
        if (description != null) {
            description = d;
        } else {
            System.err.println("ignoring attempt to set description to null for object id = " + getId());
        }
    }

    public Date getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(Date d) {
        modificationDate = d;
    }

    public void setVersion(int v) {
        version = v;
    }

    public int getVersion() {
        return version;
    }

    public void setRating(double count) {
        rating = count;
    }

    public double getRating() {
        return rating;
    }

    public void setPrivateAccess(boolean b) {
        privateAccess = b;
    }

    public boolean isPrivateAccess() {
        return privateAccess;
    }

    public String getConcatenatedText() {
        return headline + " " + description;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public List<mltSynchunit> getSynchUnits() {
        return synchUnits;
    }

    public void setSynchUnits(List<mltSynchunit> synchUnits) {
        this.synchUnits = synchUnits;
    }

    public ObjectReviewStatus getStatus() {
        return status;
    }

    public void setStatus(ObjectReviewStatus status) {
        this.status = status;
    }

    public List<MlcImage> getIcons() {
        return icons;
    }

    public void setIcons(List<MlcImage> icons) {
        this.icons = icons;
    }

    /**
     * Returns the id of the island (i.e. connected component) this object is a
     * part of.
     *
     * @return The id of the island or STAND_ALONE_ID (-1) if this object isn's
     * on any island.
     */
    public int getIslandId() {
        return islandId;
    }

    public void setIslandId(int island_id) {
        this.islandId = island_id;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + this.id;
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
        final mlcObject other = (mlcObject) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getHeadline();
    }

    /**
     * Override this function if the object needs any special cleanup before its
     * deletion.
     */
    public void beforeDeletion() {
    }

    /**
     * Returns true if the current user created this object
     */
    public boolean isOwnedByCurrentUser() {
        return CacheEngineStatic.getCurrentUser().equals(owner);
    }

    public int getRelativeCount() {
        return relativeCount;
    }

    public void setRelativeCount(int relativeCount) {
        this.relativeCount = relativeCount;
    }

    public boolean isRelativesOrdered() {
        return relativesOrdered;
    }

    public void setRelativesOrdered(boolean relativesOrdered) {
        this.relativesOrdered = relativesOrdered;
    }

    /**
     * This call returns a list of attributes that have changes since the
     * previous state.
     *
     * @param previousState The previous state to compare the fields to
     * @return The list of changes
     */
    public List<ObjectAttributes> getChanges(mlcObject previousState) {
        List<ObjectAttributes> changes = new ArrayList<>();

        // here we only compare the common fields, see subclasses for specific fields
        if (!headline.equals(previousState.getHeadline())) {
            changes.add(ObjectAttributes.Headline);
        }
        if (id != previousState.getId()) {
            changes.add(ObjectAttributes.Id);
        }
        if (!client.equals(previousState.getClient())) {
            changes.add(ObjectAttributes.DataPool);
        }
        if (!confidentiality.equals(previousState.getConfidentiality())) {
            changes.add(ObjectAttributes.Confidentiality);
        }
        if (!creationDate.equals(previousState.getCreationDate())) {
            changes.add(ObjectAttributes.CreationDate);
        }
        if (!description.equals(previousState.getDescription())) {
            changes.add(ObjectAttributes.Description);
        }
        if (!modificationDate.equals(previousState.getModificationDate())) {
            changes.add(ObjectAttributes.ModificationDate);
        }
        if (!owner.equals(previousState.getOwner())) {
            changes.add(ObjectAttributes.Owner);
        }
        if (privateAccess != previousState.isPrivateAccess()) {
            changes.add(ObjectAttributes.Privacy);
        }
        if (rating != previousState.getRating()) {
            changes.add(ObjectAttributes.Rating);
        }
        if (!status.equals(previousState.getStatus())) {
            changes.add(ObjectAttributes.ReviewStatus);
        }
        if (!relativesOrdered == previousState.relativesOrdered) {
            changes.add(ObjectAttributes.RelativesOrdered);
        }
        return changes;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            mlcObject clone = this.getClass().newInstance();
            clone.setId(id);
            clone.setClient(client);
            clone.setHeadline(headline);
            clone.setModificationDate(modificationDate);
            clone.setCreationDate(creationDate);
            clone.setConfidentiality(confidentiality);
            clone.setOwner(owner);
            clone.setVersion(version);
            clone.setRating(rating);
            clone.setPrivateAccess(privateAccess);
            clone.setArchived(archived);
            clone.setDescription(description);
            clone.setSynchUnits(synchUnits);
            clone.setIcons(icons);
            clone.setIslandId(islandId);
            clone.setRelativeCount(relativeCount);
            clone.setStatus(status);
            clone.setRelativesOrdered(relativesOrdered);
            return clone;
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(mlcObject.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

}
