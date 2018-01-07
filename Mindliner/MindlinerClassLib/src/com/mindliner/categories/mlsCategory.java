/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.categories;

import com.mindliner.entities.mlsClient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;

/**
 * The core category entity class. Categories can contain subCategories which
 * allows for cascaded trees.
 *
 * @author Marius
 */
@Entity
@Table(name = "categories")
public class mlsCategory implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "NAME")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CLIENT_ID", referencedColumnName = "ID")
    public mlsClient getClient() {
        return client;
    }

    public void setClient(mlsClient c) {
        client = c;
    }

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "categories_categories",
    joinColumns = {
        @JoinColumn(name = "HOLDER_ID")},
    inverseJoinColumns = {
        @JoinColumn(name = "MEMBER_ID")})
    public List<mlsCategory> getSubCategories() {
        return subCategories;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PARENT_ID", referencedColumnName = "ID")
    public mlsCategory getParent() {
        return parent;
    }

    public void setParent(mlsCategory parent) {
        this.parent = parent;
    }

    public void setSubCategories(List<mlsCategory> clist) {
        subCategories = clist;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof mlsCategory)) {
            return false;
        }
        mlsCategory other = (mlsCategory) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Indicates whether this category applies to a single class only or to all
     * Mindliner object classes.
     *
     * @return True if this category only applies to one class, false if it
     * applies to all classes.
     */
    @Column(name = "SINGLE_TARGET")
    public boolean isSingleTargetClass() {
        return singleTargetClass;
    }

    /**
     * Returns the class name of objects to which this category applies. This
     * return value is valid only if isSingleClassTaregt is TRUE
     */
    @Column(name = "SINGLE_TARGET_CLASS_NAME")
    public String getSingleTargetClassName() {
        return singleTargetClassName;
    }

    public void setSingleTargetClassName(String singleTargetClassName) {
        this.singleTargetClassName = singleTargetClassName;
    }

    public void setSingleTargetClass(boolean singleTargetClass) {
        this.singleTargetClass = singleTargetClass;
    }
    private Long id;
    private String name = "";
    private mlsClient client = null;
    private List<mlsCategory> subCategories = new ArrayList<mlsCategory>();
    private mlsCategory parent = null;
    private boolean singleTargetClass = false;
    private String singleTargetClassName = "";
    private static final long serialVersionUID = 19640205L;
}
