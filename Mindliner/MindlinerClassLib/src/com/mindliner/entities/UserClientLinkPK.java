/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.entities;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

/**
 *
 * @author Marius Messerli
 */
@Embeddable
public class UserClientLinkPK implements Serializable {
    @Basic(optional = false)
    @NotNull
    @Column(name = "USER_ID")
    private int userId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "CLIENT_ID")
    private int clientId;

    public UserClientLinkPK() {
    }

    public UserClientLinkPK(int userId, int clientId) {
        this.userId = userId;
        this.clientId = clientId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + this.userId;
        hash = 59 * hash + this.clientId;
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
        final UserClientLinkPK other = (UserClientLinkPK) obj;
        if (this.userId != other.userId) {
            return false;
        }
        if (this.clientId != other.clientId) {
            return false;
        }
        return true;
    }



    @Override
    public String toString() {
        return "com.mindliner.entities.UserClientLinkPK[ userId=" + userId + ", clientId=" + clientId + " ]";
    }

}
