/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.entities;

import com.mindliner.categories.mlsConfidentiality;
import java.io.Serializable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * This class represents a link between users and clients and is required
 * to manage the extra attribute in the link table.
 * @author Marius Messerli
 */
@Entity
@Table(name = "users_clients")
@NamedQueries({
    @NamedQuery(name = "UserClientLink.findAll", query = "SELECT u FROM UserClientLink u"),
@NamedQuery(name = "UserClientLink.remove", query = "DELETE FROM UserClientLink u WHERE u.userClientLinkPK.clientId = :clientId and u.userClientLinkPK.userId = :userId")})
public class UserClientLink implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected UserClientLinkPK userClientLinkPK;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MAX_CONFIDENTIALITY_ID", referencedColumnName = "ID")
    private mlsConfidentiality maxConfidentiality;

    public UserClientLink() {
    }

    public UserClientLink(int userId, int clientId) {
        this.userClientLinkPK = new UserClientLinkPK(userId, clientId);
    }

    public UserClientLinkPK getUserClientLinkPK() {
        return userClientLinkPK;
    }

    public void setUserClientLinkPK(UserClientLinkPK userClientLinkPK) {
        this.userClientLinkPK = userClientLinkPK;
    }

    public mlsConfidentiality getMaxConfidentiality() {
        return maxConfidentiality;
    }

    public void setMaxConfidentiality(mlsConfidentiality maxConfidentiality) {
        this.maxConfidentiality = maxConfidentiality;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (userClientLinkPK != null ? userClientLinkPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UserClientLink)) {
            return false;
        }
        UserClientLink other = (UserClientLink) object;
        if ((this.userClientLinkPK == null && other.userClientLinkPK != null) || (this.userClientLinkPK != null && !this.userClientLinkPK.equals(other.userClientLinkPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mindliner.entities.UserClientLink[ userClientLinkPK=" + userClientLinkPK + " ]";
    }

}
