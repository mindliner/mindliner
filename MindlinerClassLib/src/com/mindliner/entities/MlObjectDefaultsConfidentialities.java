/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.entities;

import com.mindliner.categories.mlsConfidentiality;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * This class holds the confidentiality defaults, one for each data pool.
 *
 * @author Marius Messerli
 */
@Entity
@Table(name = "objectdefaultsconfidentialities")
@NamedQuery(name = "objectdefaultsconfidentialities.deleteByClient", query = "DELETE FROM MlObjectDefaultsConfidentialities o WHERE o.client = :client")
public class MlObjectDefaultsConfidentialities implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CLIENT_ID")
    private mlsClient client;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CONFIDENTIALITY_ID")
    private mlsConfidentiality confidentiality;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID")
    private MlUserPreferences userPreference;

    private static final long serialVersionUID = 1L;

    public MlObjectDefaultsConfidentialities() {
    }

    public MlObjectDefaultsConfidentialities(mlsClient client, mlsConfidentiality confidentiality) {
        this.client = client;
        this.confidentiality = confidentiality;
    }

    public mlsClient getClient() {
        return client;
    }

    public void setClient(mlsClient client) {
        this.client = client;
    }

    public mlsConfidentiality getConfidentiality() {
        return confidentiality;
    }

    public void setConfidentiality(mlsConfidentiality confidentiality) {
        this.confidentiality = confidentiality;
    }

    public MlUserPreferences getObjectDefaults() {
        return userPreference;
    }

    public void setObjectDefaults(MlUserPreferences objectDefaults) {
        this.userPreference = objectDefaults;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.id);
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
        final MlObjectDefaultsConfidentialities other = (MlObjectDefaultsConfidentialities) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

}
