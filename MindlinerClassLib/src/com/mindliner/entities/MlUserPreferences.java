/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.entities;

import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.categories.mlsPriority;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * This class maps the object defaults for one particular user.
 *
 * @author Marius Messerli
 */
@Entity
@Table(name = "userpreferences")
public class MlUserPreferences implements Serializable {

    private static final long serialVersionUID = 1L;
    private mlsUser user;
    private boolean privateflag;
    private mlsPriority priority;
    private mlsClient dataPool;
    private Date newsLastDigest = null;
    private List<MlObjectDefaultsConfidentialities> objectDefaultsConfidentialities;

    public MlUserPreferences() {
    }

    @Basic(optional = false)
    @Column(name = "PRIVATEFLAG")
    public boolean getPrivateflag() {
        return privateflag;
    }

    public void setPrivateflag(boolean privateflag) {
        this.privateflag = privateflag;
    }

    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "USER_ID", referencedColumnName = "ID")
    public mlsUser getUser() {
        return user;
    }

    public void setUser(mlsUser user) {
        this.user = user;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PRIORITY_ID")
    public mlsPriority getPriority() {
        return priority;
    }

    public void setPriority(mlsPriority priority) {
        this.priority = priority;
    }

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "userPreference")
    public List<MlObjectDefaultsConfidentialities> getObjectDefaultsConfidentialities() {
        return objectDefaultsConfidentialities;
    }

    public mlsConfidentiality getConfidentiality(mlsClient client) {
        if (client != null) {
            for (MlObjectDefaultsConfidentialities oc : getObjectDefaultsConfidentialities()) {
                if (oc.getClient().equals(client)) {
                    return oc.getConfidentiality();
                }
            }
        }
        return null;
    }

    public void setObjectDefaultsConfidentialities(List<MlObjectDefaultsConfidentialities> objectDefaultsConfidentialities) {
        this.objectDefaultsConfidentialities = objectDefaultsConfidentialities;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CLIENT_ID")
    public mlsClient getDataPool() {
        return dataPool;
    }

    public void setDataPool(mlsClient dataPool) {
        this.dataPool = dataPool;
    }

    @Column(name = "NEWS_LAST_DIGEST")
    @Temporal(value = TemporalType.TIMESTAMP)
    public Date getNewsLastDigest() {
        return newsLastDigest;
    }

    public void setNewsLastDigest(Date newsLastDigest) {
        this.newsLastDigest = newsLastDigest;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.user);
        hash = 31 * hash + (this.privateflag ? 1 : 0);
        hash = 31 * hash + Objects.hashCode(this.priority);
        hash = 31 * hash + Objects.hashCode(this.dataPool);
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
        final MlUserPreferences other = (MlUserPreferences) obj;
        if (!Objects.equals(this.user, other.user)) {
            return false;
        }
        if (this.privateflag != other.privateflag) {
            return false;
        }
        if (!Objects.equals(this.priority, other.priority)) {
            return false;
        }
        if (!Objects.equals(this.dataPool, other.dataPool)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Object defaults for " + user;
    }

}
