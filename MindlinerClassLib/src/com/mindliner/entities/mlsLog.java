/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.entities;

import com.mindliner.categories.MlsEventType.EventType;
import java.util.Date;
import java.util.Objects;
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

/**
 * The log entity stores information about objectcs changes. This info is used
 * for history and for statistics.
 *
 * @author Marius Messerli
 */
@Entity
@Table(name = "log")
@NamedQueries({
    @NamedQuery(name = "mlsLog.getRecentRecords", query = "SELECT l FROM mlsLog l WHERE l.user = :user ORDER BY l.time DESC"),
    @NamedQuery(name = "mlsLog.findChangedObjects", query = "SELECT l FROM mlsLog l WHERE l.time < :endTime AND l.time > :startTime"),
    @NamedQuery(name = "mlsLog.findAfterStart", query = "SELECT l FROM mlsLog l WHERE l.time > :startTime"),
    @NamedQuery(name = "mlsLog.getDeletionCount", query = "SELECT count(l), l.user.id FROM mlsLog l WHERE l.time < :endTime AND l.time > :startTime AND (l.method = 'removeObjects' OR l.method = 'remove') GROUP BY l.user"),
    @NamedQuery(name = "mlsLog.deleteForClient", query = "DELETE FROM mlsLog l WHERE l.dataPool = :client"),
    @NamedQuery(name = "mlsLog.getObjectLog", query = "SELECT l FROM mlsLog l WHERE l.user.id = :userId AND l.objectId = :objectId ORDER BY l.time DESC")
})

public class mlsLog implements java.io.Serializable {

    private static final long serialVersionUID = 19640205L;

    private int id;
    private String headline = "";
    private String description = "";
    private boolean hasObject = false;
    private int objectId = 0;
    private int linkObjectId = 0;
    private mlsUser user = null;
    private mlsClient dataPool = null;
    private Date time = null;
    private String method = "";

    private Type type;
    private EventType eventType = EventType.Any;

    public enum Type {

        Create, Modify, Remove, Link, Info
    }

    @Column(name = "EVENT")
    @Enumerated(value = EnumType.STRING)
    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Column(name = "USER_OBJECT_ID")
    public int getObjectId() {
        return objectId;
    }

    protected void setObjectId(int userObjectId) {
        this.objectId = userObjectId;
    }

    @Column(name = "LINK_OBJECT_ID")
    public int getLinkObjectId() {
        return linkObjectId;
    }

    public void setLinkObjectId(int linkObjectId) {
        this.linkObjectId = linkObjectId;
    }

    public void setUserObject(mlsObject mbo) {
        if (mbo != null) {
            setObjectId(mbo.getId());
            setHasObject(true);
        }
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "USER_ID", referencedColumnName = "ID")
    public mlsUser getUser() {
        return user;
    }

    public void setUser(mlsUser u) {
        user = u;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    @Column(name = "OPERATION_TIMESTAMP")
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    public Date getTime() {
        return time;
    }

    @Column(name = "METHOD")
    public String getMethod() {
        return method;
    }

    public void setMethod(String s) {
        method = s;
    }

    public void setDescription(String d) {
        description = d;
    }

    @Column(name = "DESCRIPTION")
    public String getDescription() {
        return description;
    }

    @Column(name = "HAS_USER_OBJECT")
    public boolean isHasObject() {
        return hasObject;
    }

    public void setHasObject(boolean hasUserObject) {
        this.hasObject = hasUserObject;
    }

    @Column(name = "TYPE")
    @Enumerated(EnumType.STRING)
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CLIENT_ID", referencedColumnName = "ID")
    public mlsClient getDataPool() {
        return dataPool;
    }

    public void setDataPool(mlsClient client) {
        this.dataPool = client;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + this.id;
        hash = 73 * hash + this.objectId;
        hash = 73 * hash + Objects.hashCode(this.eventType);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final mlsLog other = (mlsLog) obj;
        if (this.id != other.id) {
            return false;
        }
        if (this.objectId != other.objectId) {
            return false;
        }
        if (this.eventType != other.eventType) {
            return false;
        }
        return true;
    }

}
