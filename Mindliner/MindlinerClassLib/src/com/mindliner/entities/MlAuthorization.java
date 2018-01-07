/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.entities;

import com.mindliner.categories.mlsConfidentiality;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * This class holds enrollment requests for users to join data pools or for data
 * pool admins to invite users.
 *
 * @author Marius Messerli
 */
@Entity
@Table(name = "authorizations")
@NamedQueries({
    @NamedQuery(name = "MlAuthorization.findByCompleted", query = "SELECT a FROM MlAuthorization a WHERE a.completed = :completed"),
    @NamedQuery(name = "MlAuthorization.findPendingRequests", query = "SELECT a FROM MlAuthorization a WHERE a.completed = false AND a.dataPool = :dataPool AND a.user = :user AND a.authorizationType = :type")})
public class MlAuthorization implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "TOKEN")
    private String token;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "USER_ID")
    private mlsUser user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "DATAPOOL_ID")
    
    private mlsClient dataPool;

    @Enumerated(EnumType.STRING)
    @Column(name = "AUTHORIZATION_TYPE")
    private AuthorizationType authorizationType;

    @Column(name = "COMPLETED")
    private boolean completed;
    
    @Column(name = "EXPIRATION_TS")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiration;
    
    @Column(name = "EMAIL")
    private String email;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MAX_CONFIDENTIALITY_ID")
    private mlsConfidentiality maxConfidentiality;

    public static enum AuthorizationType {
        // user wants to join someone's data pool and creates an enrollment authorization for that pool's admin
        DataPoolEnrollmentRequest,
        // the pool admin authorizes a user to join his pool
        DataPoolAccess,
        // data pool A's admin authorizes data pool B's admin to merge all of A's data and users into B
        DataPoolMerger
    }

    public MlAuthorization() {
        // set the default expiration to one month from now
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        expiration = cal.getTime();
    }

    public AuthorizationType getAuthorizationType() {
        return authorizationType;
    }

    public void setAuthorizationType(AuthorizationType authType) {
        this.authorizationType = authType;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public mlsUser getUser() {
        return user;
    }

    public void setUser(mlsUser user) {
        this.user = user;
    }

    public mlsClient getDataPool() {
        return dataPool;
    }

    public void setDataPool(mlsClient dataPool) {
        this.dataPool = dataPool;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Date getExpiration() {
        return expiration;
    }

    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public mlsConfidentiality getMaxConfidentiality() {
        return maxConfidentiality;
    }

    public void setMaxConfidentiality(mlsConfidentiality maxConfidentiality) {
        this.maxConfidentiality = maxConfidentiality;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.token);
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
        final MlAuthorization other = (MlAuthorization) obj;
        if (!Objects.equals(this.token, other.token)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mindliner.entities.EnrollmentRequest[ token=" + token + " ]";
    }

}
