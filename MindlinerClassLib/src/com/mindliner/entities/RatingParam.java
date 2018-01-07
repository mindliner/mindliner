/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.entities;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * This class represents one rating parameter.
 * 
 * @author Marius Messerli
 */
@Entity
@Table(name = "ratingparameters")
@NamedQueries({
    @NamedQuery(name = "RatingParam.findAll", query = "SELECT r FROM RatingParam r"),
    @NamedQuery(name = "RatingParam.findById", query = "SELECT r FROM RatingParam r WHERE r.id = :id"),
    @NamedQuery(name = "RatingParam.findBySetId", query = "SELECT r FROM RatingParam r WHERE r.setId = :setId"),
    @NamedQuery(name = "RatingParam.findByParameterName", query = "SELECT r FROM RatingParam r WHERE r.parameterName = :parameterName"),
    @NamedQuery(name = "RatingParam.findByParameterValue", query = "SELECT r FROM RatingParam r WHERE r.parameterValue = :parameterValue")})
public class RatingParam implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "ID")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "SET_ID")
    private RatingParamSet set;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 64)
    @Column(name = "PARAMETER_NAME")
    private String parameterName;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 128)
    @Column(name = "PARAMETER_VALUE")
    private String parameterValue;

    public RatingParam() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public RatingParamSet getSet() {
        return set;
    }

    public void setSet(RatingParamSet set) {
        this.set = set;
    }


    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getParameterValue() {
        return parameterValue;
    }

    public void setParameterValue(String parameterValue) {
        this.parameterValue = parameterValue;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.id != null ? this.id.hashCode() : 0);
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
        final RatingParam other = (RatingParam) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }



    @Override
    public String toString() {
        return getParameterName() + ": " + getParameterValue();
    }
    
}
