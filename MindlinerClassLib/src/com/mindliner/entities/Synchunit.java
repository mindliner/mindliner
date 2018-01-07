/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.entities;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class represents a synchronization record between one Mindliner object
 * and one ForeignObject.
 *
 * @author Marius Messerli
 */
@Entity
@Table(name = "synchunits")
@XmlRootElement
public class Synchunit implements Serializable {

    public static final int UNPERSISTED_ID = -1;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id = UNPERSISTED_ID;
    /**
     * I don't map this to the object on purpose, it should be a detached
     * record, hence id, version, and client id are treated as plain ints
     */
    @ManyToOne
    @JoinColumn(name = "MINDLINER_OBJECT_ID", referencedColumnName = "ID")
    private mlsObject mindlinerObject;
    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "FOREIGN_OBJECT_ID")
    private String foreignObjectId;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "LAST_SYNCHED")
    private Date lastSynched;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "SYNCHER_ID", referencedColumnName = "ID")
    private Syncher syncher;

    public Synchunit() {
    }

    public Synchunit(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public mlsObject getMindlinerObject() {
        return mindlinerObject;
    }

    public void setMindlinerObject(mlsObject mindlinerObject) {
        this.mindlinerObject = mindlinerObject;
    }

    public String getForeignObjectId() {
        return foreignObjectId;
    }

    public Syncher getSyncher() {
        return syncher;
    }

    public void setSyncher(Syncher syncher) {
        this.syncher = syncher;
    }

    public void setForeignObjectId(String foreignObjectId) {
        this.foreignObjectId = foreignObjectId;
    }

    public Date getLastSynched() {
        return lastSynched;
    }

    public void setLastSynched(Date lastSynched) {
        this.lastSynched = lastSynched;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Synchunit)) {
            return false;
        }
        Synchunit other = (Synchunit) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mindliner.entities.Synchunit[ id=" + id + " ]";
    }
}
