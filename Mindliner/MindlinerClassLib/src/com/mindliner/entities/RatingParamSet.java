/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.entities;

import com.mindliner.exceptions.NonExistingObjectException;
import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * This class represents a parameter set for object rating.
 *
 * @author Marius Messerli
 */
@Entity
@Table(name = "ratingparametersets")
@NamedQueries({
    @NamedQuery(name = "RatingParamSet.findAll", query = "SELECT r FROM RatingParamSet r"),
    @NamedQuery(name = "RatingParamSet.findById", query = "SELECT r FROM RatingParamSet r WHERE r.id = :id"),
    @NamedQuery(name = "RatingParamSet.findBySetName", query = "SELECT r FROM RatingParamSet r WHERE r.setName = :setName"),
    @NamedQuery(name = "RatingParamSet.findByClientId", query = "SELECT r FROM RatingParamSet r WHERE r.clientId = :clientId"),
    @NamedQuery(name = "RatingParamSet.findByActive", query = "SELECT r FROM RatingParamSet r WHERE r.active = :active")})
public class RatingParamSet implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 64)
    @Column(name = "SET_NAME")
    private String setName;
    @Basic(optional = false)
    @NotNull
    @Column(name = "CLIENT_ID")
    private int clientId;
    @Column(name = "ACTIVE")
    private Boolean active;
    @OneToMany(mappedBy = "set")
    Collection<RatingParam> parameters;

    public RatingParamSet() {
    }

    public RatingParamSet(Integer id) {
        this.id = id;
    }

    public RatingParamSet(Integer id, String setName, int clientId) {
        this.id = id;
        this.setName = setName;
        this.clientId = clientId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSetName() {
        return setName;
    }

    public void setSetName(String setName) {
        this.setName = setName;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Collection<RatingParam> getParameters() {
        return parameters;
    }

    public void setParameters(Collection<RatingParam> parameters) {
        this.parameters = parameters;
    }

    /**
     * Returns the value for the specified key or null if no such key exists
     *
     * @param key The parameter key
     * @return The parameter value
     * @throws com.mindliner.exceptions.NonExistingObjectException Thrown if no
     * such parameter exists
     */
    @Transient
    public String getParameterValue(String key) throws NonExistingObjectException {
        for (RatingParam p : parameters) {
            if (key.equals(p.getParameterName())) {
                return p.getParameterValue();
            }
        }
        throw new NonExistingObjectException("Parameter is not defined");
    }

    /**
     * Returns the integer value for the specified key
     *
     * @param key The parameter key
     * @return The parameter integer value
     * @throws com.mindliner.exceptions.NonExistingObjectException
     * @throws NumberFormatException If the parameter cannot be converted to
     * Integer
     */
    @Transient
    public int getParameterIntValue(String key) throws NonExistingObjectException, NumberFormatException {
        String val = getParameterValue(key);
        if (val == null) {
            throw new NonExistingObjectException("Parameter does not exist");
        }
        return Integer.parseInt(val);
    }

    @Transient
    public double getParameterDoubleValue(String key) throws NonExistingObjectException, NumberFormatException {
        String val = getParameterValue(key);
        if (val == null) {
            throw new NonExistingObjectException("Parameter does not exist");
        }
        return Double.parseDouble(val);
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
        if (!(object instanceof RatingParamSet)) {
            return false;
        }
        RatingParamSet other = (RatingParamSet) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mindliner.managers.RatingParamSet[ id=" + id + " ]";
    }

}
