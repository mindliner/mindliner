/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The class implements individually licensed and activated software features.
 *
 * @author Marius Messerli
 */
@Entity
@Table(name = "sys_swfeatures")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "SoftwareFeature.findAll", query = "SELECT s FROM SoftwareFeature s"),
    @NamedQuery(name = "SoftwareFeature.findById", query = "SELECT s FROM SoftwareFeature s WHERE s.id = :id"),
    @NamedQuery(name = "SoftwareFeature.findByName", query = "SELECT s FROM SoftwareFeature s WHERE s.name = :name")})
public class SoftwareFeature implements Serializable {

    /**
     * NOTE: DO NOT CHANGE ANY OF THE VALUES BELOW WITHOUT CHECKING/UPDATING THE DATABASE TABLE users_swfeatures ACCORDINGLY.
     * The sequence of items is arbitrary, name changes are material.
     */
    public static enum CurrentFeatures {

        CONFIDENTIALITY_LEVELS,
        OFFLINE_MODE,
        TIME_MANAGEMENT,
        OBJECT_RATING,
        SYNCH_BASICS, // required for any of synch functionality to be available
        SYNCH_OUTLOOK, // synching to Outlook on the client side
        WORKSPHEREMAP,
        FILE_INDEXING,
        SUBSCRIPTION,
        CUSTOMIZATION // if off all customization functions and gui elements are hidden from the user
    }

    /**
     * Checks if the feature with the specified name still exists in the current version.
     *
     * @param name The feature name which must be the string version of an enum member of CurrentFeatures in order to exist.
     * @return True if the feature exists, false otherwise.
     */
    public static boolean isStillExisting(String name) {
        for (CurrentFeatures f : CurrentFeatures.values()) {
            if (f.name().equals(name)) {
                return true;
            }
        }
        return false;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;
    @Column(name = "NAME")
    private String name;
    @Column(name = "DESCRIPTION")
    private String description;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "sys_users_swfeatures",
            joinColumns = {
        @JoinColumn(name = "FEATURE_ID", referencedColumnName = "ID")},
            inverseJoinColumns = {
        @JoinColumn(name = "USER_ID", referencedColumnName = "ID")})
    Collection<mlsUser> users = new ArrayList<>();
    private static final long serialVersionUID = 19640205L;

    public SoftwareFeature() {
    }

    public SoftwareFeature(Integer id) {
        this.id = id;
    }

    public SoftwareFeature(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
        if (!(object instanceof SoftwareFeature)) {
            return false;
        }
        SoftwareFeature other = (SoftwareFeature) object;
        return (this.id != null || other.id == null) && (this.id == null || this.id.equals(other.id));
    }

    public Collection<mlsUser> getUsers() {
        return users;
    }

    @Override
    public String toString() {
        return name;
    }
}
