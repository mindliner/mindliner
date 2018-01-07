/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.entities;

import com.mindliner.enums.LinkRelativeType;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Dominic Plangger, Marius Messerli
 */
@Entity
@Table(name = "links")
@NamedQueries({
        @NamedQuery(name = "MlsLink.getUserConnectionsCount", query = ""
            + "SELECT l.owner, o1.owner, o2.owner, count(l) "
            + "FROM mlsObject o1, mlsObject o2, MlsLink l "
            + "WHERE o1.id = l.holderId AND l.relativeId = o2.id "
            + "AND l.client.id IN :clientId "
            + "AND l.relativeType = :relativeType  "
            + "AND l.modificationDate < :endTime "
            + "AND l.modificationDate > :startTime "
            + "AND NOT EXISTS ( "
            + "  SELECT l2 FROM MlsLink l2 "
            + "  WHERE l.holderId = l2.relativeId "
            + "  AND l.relativeId = l2.holderId "
            + "  AND l.relativeId < l2.relativeId) "
            + "GROUP BY l.owner, o1.owner, o2.owner"),
    @NamedQuery(name = "MlsLink.getObjectLinks", query = "SELECT l FROM MlsLink l WHERE l.holderId = :id OR l.relativeId = :id"),
    @NamedQuery(name = "MlsLink.getObjectRelatives", query = "SELECT l FROM MlsLink l WHERE l.relativeType = :relativeType AND l.holderId = :id"),
    @NamedQuery(name = "MlsLink.removeObjectLinks", query = "DELETE FROM MlsLink l WHERE l.holderId = :id OR l.relativeId = :id"),
    @NamedQuery(name = "MlsLink.removeLinksForClient", query = "DELETE FROM MlsLink l WHERE l.client = :client"),
    @NamedQuery(name = "MlsLink.removeOneWayLink", query = "DELETE FROM MlsLink l WHERE l.relativeType = :relativeType AND l.holderId = :id1 AND l.relativeId = :id2"),
    @NamedQuery(name = "MlsLink.removeLink", query = "DELETE FROM MlsLink l WHERE l.relativeType = :relativeType AND ((l.holderId = :id1 AND l.relativeId = :id2) OR (l.holderId = :id2 AND l.relativeId = :id1))"),
    @NamedQuery(name="MlsLink.findLinkByObjects", query = "SELECT l FROM MlsLink l WHERE l.holderId = :holderId AND l.relativeId = :relativeId"),
    @NamedQuery(name = "MlsLink.getTotalLinkCount", query = "SELECT count(l) FROM MlsLink l")
})
public class MlsLink implements Serializable {

    private int id = -1;
    private int holderId = -1;
    private int relativeId = -1;
    private String label = "";
    private mlsUser owner = null;
    private Date creationDate = new Date();
    private Date modificationDate = new Date();
    private mlsClient client = null;
    private int relativeListPosition = 0;
    private LinkRelativeType relativeType = LinkRelativeType.OBJECT;
    private static final long serialVersionUID = 19640205L;

    public MlsLink() {
    }
    
    public MlsLink(mlsObject holder, mlsObject relative, mlsUser owner) {
        assert owner != null : "Link owner must not be null";
        holderId = holder.getId();
        relativeId = relative.getId();
        client = holder.getClient();
        relativeType = LinkRelativeType.OBJECT;
        this.owner = owner;
    }

    public MlsLink(int holderId, int relativeId, mlsUser owner, mlsClient client) {
        assert owner != null : "Link owner must not be null";
        assert client != null : "Link client must not be null";
        this.holderId = holderId;
        this.relativeId = relativeId;
        this.client = client;
        relativeType = LinkRelativeType.OBJECT;
        this.owner = owner;
    }

    public MlsLink(mlsObject holder, int relativeId, mlsUser owner, LinkRelativeType relativeType) {
        assert owner != null : "Link owner must not be null";
        holderId = holder.getId();
        client = holder.getClient();
        this.relativeId = relativeId;
        this.owner = owner;
        this.relativeType = relativeType;
    }
    

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LINK_ID")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Column(name = "HOLDER_ID")
    public int getHolderId() {
        return holderId;
    }

    public void setHolderId(int holderId) {
        this.holderId = holderId;
    }

    @Column(name = "RELATIVE_ID")
    public int getRelativeId() {
        return relativeId;
    }

    public void setRelativeId(int relativeId) {
        this.relativeId = relativeId;
    }

    @Column(name = "LABEL")
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "OWNER_ID", referencedColumnName = "ID")
    public mlsUser getOwner() {
        return owner;
    }

    public void setOwner(mlsUser owner) {
        this.owner = owner;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATION_TS")
    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "MODIFICATION_TS")
    public Date getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CLIENT_ID", referencedColumnName = "ID")
    public mlsClient getClient() {
        return client;
    }

    public void setClient(mlsClient client) {
        this.client = client;
    }

    @Column(name = "RELATIVE_TYPE")
    @Enumerated(EnumType.STRING)
    public LinkRelativeType getRelativeType() {
        return relativeType;
    }

    public void setRelativeType(LinkRelativeType relativeType) {
        this.relativeType = relativeType;
    }

    @Column(name="RELATIVE_LIST_POSITION")
    public int getRelativeListPosition() {
        return relativeListPosition;
    }

    public void setRelativeListPosition(int relativeListPosition) {
        this.relativeListPosition = relativeListPosition;
    }
        
}
