/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.entities;

import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.enums.ObjectReviewStatus;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;

/**
 * This entity is the core of all Mindliner information objects. It maps all
 * common attributes and the relations to other objects. Besides this class
 * there are transfer classes (derived from mltObject) and client classes
 * (derived from mlcObject) that are used for the desktop application.
 *
 * @author Marius Messerli
 */
@Entity
@Table(name = "objects")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "DTYPE", discriminatorType = DiscriminatorType.STRING, length = 4)
@NamedQueries({
    @NamedQuery(name = "mlsObject.getObjectsForConfidentiality", query = "SELECT o FROM mlsObject o where o.confidentiality.id = :confidentialityId"),
    @NamedQuery(name = "mlsObject.getByHeadlineAndDescription", query = "SELECT o FROM mlsObject o where o.headline = :headline AND o.description = :description AND o.client.id = :clientId"),
    @NamedQuery(name = "mlsObject.getObjectsForClient", query = "SELECT o FROM mlsObject o where o.client.id = :clientId"),
    @NamedQuery(name = "mlsObject.getObjectForRatingGeneration", query = "SELECT o FROM mlsObject o WHERE o.client.id = :clientId AND o.ratingDetail.generation < :ratingGeneration"),
    @NamedQuery(name = "mlsObject.getCountByOwner", query = "SELECT count(o) FROM mlsObject o WHERE o.owner.id = :ownerId"),
    @NamedQuery(name = "mlsObject.getTotalObjectCount", query = "SELECT count(o) FROM mlsObject o"),
    @NamedQuery(name = "mlsObject.getObjectLinks", query = "SELECT o, l FROM mlsObject o, MlsLink l WHERE (l.holderId = :holderId OR l.relativeId = :holderId) AND l.relativeType IN :relativeType AND o.id = l.relativeId"),
    @NamedQuery(name = "mlsObject.getCreationCount",
            query = "SELECT count(o), o.owner.id FROM mlsObject o, in (o.client.users) user WHERE user.id = :currentUserId AND o.creationDate < :endTime AND o.creationDate > :startTime GROUP BY o.owner"),
    @NamedQuery(name = "mlsObject.getModificationCount",
            query = "SELECT count(o), o.owner.id FROM mlsObject o, in (o.client.users) user WHERE user.id = :currentUserId AND o.modificationDate < :endTime AND o.modificationDate > :startTime GROUP BY o.owner"),
    @NamedQuery(name = "mlsObject.getCountByClient", query = "SELECT count(o) FROM mlsObject o WHERE o.client.id = :clientId"),
    @NamedQuery(name = "mlsObject.removeForClient", query = "DELETE FROM mlsObject o WHERE o.client = :client"),
    @NamedQuery(name = "mlsObject.getStandaloneObjectsForClient", query = "SELECT o FROM mlsObject o where o.island IS NULL AND o.client.id = :clientId"),
    @NamedQuery(name = "mlsObject.getIslandStatsForClient", query = "SELECT count(o), o.island.id FROM mlsObject o where o.client.id = :clientId GROUP BY o.island"),
    @NamedQuery(name = "mlsObject.findByIdRange", query = "SELECT o FROM mlsObject o where o.id IN :ids")
})
public abstract class mlsObject implements Serializable {

    /**
     * Returns a list of object ids for the objects specified
     *
     * @param objects
     * @return
     */
    public static List<Integer> getIds(List<mlsObject> objects) {
        List<Integer> idlist = new ArrayList<>();
        if (objects != null) {
            for (mlsObject o : objects) {
                idlist.add(o.getId());
            }
        }
        return idlist;
    }

    private int id = -1;
    private mlsClient client = null;
    private String headline = "";
    private Date modificationDate = new Date();
    private Date creationDate = new Date();
    private mlsConfidentiality confidentiality = null;
    private mlsUser owner = null;
    private int version = -1;
    private boolean privateAccess = false;
    private List<mlsObject> relatives = new ArrayList<>();
    private String description = "";
    private double rating = 0D;
    private mlsRatingDetail ratingDetail = new mlsRatingDetail();
    private List<Synchunit> synchUnits;
    private static final long serialVersionUID = 19640205L;
    private ObjectReviewStatus status = ObjectReviewStatus.REVIEWED;
    private Island island;
    private boolean relativesOrdered = false;
    private boolean archived = false;
    private int relativeCount = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    public ObjectReviewStatus getStatus() {
        return status;
    }

    public void setStatus(ObjectReviewStatus status) {
        this.status = status;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CLIENT_ID", referencedColumnName = "ID")
    public mlsClient getClient() {
        return client;
    }

    public void setClient(mlsClient c) {
        client = c;
    }

    public void setConfidentiality(mlsConfidentiality c) {
        confidentiality = c;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CONFIDENTIALITY_ID")
    public mlsConfidentiality getConfidentiality() {
        return confidentiality;
    }

    public void setOwner(mlsUser o) {
        owner = o;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "OWNER_ID", referencedColumnName = "ID")
    public mlsUser getOwner() {
        return owner;
    }

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "ISLAND_ID")
    public Island getIsland() {
        return island;
    }

    public void setIsland(Island island) {
        this.island = island;
    }

    @Column(name = "HEADLINE")
    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String line) {
        headline = line;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "MODIFICATION")
    public Date getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(Date d) {
        modificationDate = d;
    }

    public void setVersion(int v) {
        version = v;
    }

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "links",
            joinColumns = {
                @JoinColumn(name = "HOLDER_ID")},
            inverseJoinColumns = {
                @JoinColumn(name = "RELATIVE_ID")})
    public List<mlsObject> getRelatives() {
        return relatives;
    }

    /**
     * This method is required by ORM but should not be called directly. Use the
     * LinkerBean to create links between objects otherwise the attributes of
     * the links are not guaranteed to be set.
     *
     * @param relatives ignored
     * @deprecated
     */
    @Deprecated
    public void setRelatives(List<mlsObject> relatives) {
        this.relatives = relatives;
    }

    @Version
    @Column(name = "LOCK_VERSION")
    public int getVersion() {
        return version;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    /**
     * This is the object's current rating.
     *
     * @return
     */
    @Column(name = "RATING")
    public double getRating() {
        return rating;
    }

    public void setPrivateAccess(boolean b) {
        privateAccess = b;
    }

    @Column(name = "PRIVATE")
    public boolean getPrivateAccess() {
        return privateAccess;
    }

    public void setDescription(String s) {
        description = s;
    }

    @Column(name = "DESCRIPTION")
    public String getDescription() {
        return description;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATION_DATE")
    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @OneToMany(mappedBy = "mindlinerObject")
    public List<Synchunit> getSynchUnits() {
        return synchUnits;
    }

    public void setSynchUnits(List<Synchunit> synchUnits) {
        this.synchUnits = synchUnits;
    }

    @Column(name = "RELATIVES_ORDERED")
    public boolean isRelativesOrdered() {
        return relativesOrdered;
    }

    public void setRelativesOrdered(boolean relativesOrdered) {
        this.relativesOrdered = relativesOrdered;
    }

    @Column(name = "ARCHIVED")
    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    @Column(name = "RELATIVE_COUNT")
    public int getRelativeCount() {
        return relativeCount;
    }

    public void setRelativeCount(int relativeCount) {
        this.relativeCount = relativeCount;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || (!(o instanceof mlsObject))) {
            return false;
        }
        mlsObject that = (mlsObject) o;
        return this.getId() == that.getId();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + this.id;
        hash = 59 * hash + (this.headline != null ? this.headline.hashCode() : 0);
        hash = 59 * hash + (this.modificationDate != null ? this.modificationDate.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return getHeadline();
    }

    public void setRatingDetail(mlsRatingDetail rd) {
        ratingDetail = rd;
    }

    /**
     * This attribute is in a separate class to avoid an object version update
     * during the object rating process. The RatingDetail class is only holding
     * a field or two and would not justify an extra class if it wasn't for the
     * fact that we need to prevent unnecessary object cache misses on the
     * client. This attribute is only relevant to the rating process itself.
     * Otherwise use getRating() instead.
     *
     * @return
     */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "RATINGDETAIL_ID", referencedColumnName = "ID")
    public mlsRatingDetail getRatingDetail() {
        return ratingDetail;
    }

    @Transient
    public String getDiscriminatorValue() {
        DiscriminatorValue val = this.getClass().getAnnotation(DiscriminatorValue.class);

        if (val == null) {
            Logger.getLogger(mlsObject.class.getName()).log(Level.SEVERE, "No discriminator value specified [dtype]");
            return null;
        }

        return val.value();
    }

    /**
     * This is a hook for any cleanup operations that may be required.
     */
    public void beforeDeletion() {
    }

    public String getObjectTypeAsString() {
        //TODOWEB
        if (this instanceof mlsTask) {
            return "task";
        } else if (this instanceof mlsKnowlet) {
            return "knowlet";
        } else if (this instanceof mlsObjectCollection) {
            return "collection";
        }
        /*else if (object instanceof mlsContact) {
            return bundle.getString("Contact");
        } else if (object instanceof MlsNews) {
            return bundle.getString("News");
        } else if (object instanceof MlsCell) {
            return bundle.getString("Cell");
        } else if (object instanceof MlsSpreadsheet) {
            return bundle.getString("Spreadsheet");
        } else if (object instanceof MlsImage) {
            return bundle.getString("Image");
        } else if (object instanceof MlsContainer) {
            return bundle.getString("Container");
        } else if (object instanceof MlsContainerMap) {
           return bundle.getString("ContainterMap");
        }*/
        return "object";
    }

}
