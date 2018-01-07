/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.entities;

import com.mindliner.categories.MlsEventType.EventType;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Basic;
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

/**
 * This class describes a user subscription to a particular Mindliner event, and
 * for a particular object and a particular actor (fellow user).
 *
 * @author Marius Messerli
 */
@Entity
@Table(name = "subscriptions")
@NamedQueries({
    @NamedQuery(name = "MlsSubscription.findByObjectAndUser", query = "SELECT s FROM MlsSubscription s WHERE s.object.id = :objectId AND s.user.id = :userId"),
    @NamedQuery(name = "MlsSubscription.findAllForUser", query = "SELECT s FROM MlsSubscription s WHERE s.user.id = :userId"),
    @NamedQuery(name = "MlsSubscription.findAll", query = "SELECT s FROM MlsSubscription s")
}
)
public class MlsSubscription implements Serializable {

    private static final long serialVersionUID = 1L;
    private Integer id;
    private mlsUser user;
    private mlsObject object;
    private mlsUser actor;
    private EventType eventType;
    private boolean reverse = false;

    public MlsSubscription() {
    }

    public MlsSubscription(mlsUser user, EventType event, mlsObject object, mlsUser actor, boolean reverse) {
        this.user = user;
        this.eventType = event;
        this.object = object;
        this.actor = actor;
        this.reverse = reverse;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Enumerated(value = EnumType.STRING)
    @Column(name = "EVENT_TYPE")
    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    @Basic
    @Column(name = "REVERSE_SUBSCRIPTION")
    public boolean isReverse() {
        return reverse;
    }

    public void setReverse(boolean reverse) {
        this.reverse = reverse;
    }

    @ManyToOne
    @JoinColumn(name = "ACTOR_USER_ID", referencedColumnName = "ID")
    public mlsUser getActor() {
        return actor;
    }

    public void setActor(mlsUser actor) {
        this.actor = actor;
    }

    /**
     * This object for which this subscription is for or null if this
     * subscription is for all objects.
     *
     * @return The object to which this subscription is for or null if this
     * subscription is for all objects
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "OBJECT_ID", referencedColumnName = "ID")
    public mlsObject getObject() {
        return object;
    }

    /**
     * Set the object for which this subscription is valid and specify a null
     * object if you want this subscription to be valid for all objects.
     *
     * @param object
     */
    public void setObject(mlsObject object) {
        this.object = object;
    }

    @ManyToOne
    @JoinColumn(name = "USER_ID", referencedColumnName = "ID")
    public mlsUser getUser() {
        return user;
    }

    public void setUser(mlsUser user) {
        this.user = user;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.id);
        hash = 37 * hash + Objects.hashCode(this.user);
        hash = 37 * hash + Objects.hashCode(this.eventType);
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
        final MlsSubscription other = (MlsSubscription) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.user, other.user)) {
            return false;
        }
        if (!Objects.equals(this.eventType, other.eventType)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(id);
        if (object != null) {
            sb.append(": ").append(object.getHeadline().length() > 12 ? object.getHeadline().substring(0, 12) + "..." : object.getHeadline());
        }
        if (!eventType.equals(EventType.Any)) {
            sb.append(", e=").append(eventType.toString());
        }
        if (actor != null) {
            sb.append(", a=").append(actor.getUserName());
        }
        if (reverse) {
            sb.append(", reverse");
        }
        return sb.toString();
    }

}
