/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * This class represents an authentication group, used by the jdbc-realm to
 * associate a user with a role.
 *
 * @author Marius Messerli
 */
@Entity
@Table(name = "groups")
@NamedQueries({
    @NamedQuery(name = "MlAuthenticationGroups.findAll", query = "SELECT m FROM MlAuthenticationGroups m"),
    @NamedQuery(name = "MlAuthenticationGroups.findById", query = "SELECT m FROM MlAuthenticationGroups m WHERE m.id = :id"),
    @NamedQuery(name = "MlAuthenticationGroups.findUserGroup", query = "SELECT m FROM MlAuthenticationGroups m WHERE m.code = 'user'"),
    @NamedQuery(name = "MlAuthenticationGroups.findAdminGroup", query = "SELECT m FROM MlAuthenticationGroups m WHERE m.code = 'cadm'"),
    @NamedQuery(name = "MlAuthenticationGroups.findMasterAdminGroup", query = "SELECT m FROM MlAuthenticationGroups m WHERE m.code = 'madm'"),
    @NamedQuery(name = "MlAuthenticationGroups.findUnconfirmedGroup", query = "SELECT m FROM MlAuthenticationGroups m WHERE m.code = 'unconf'")})
public class MlAuthenticationGroups implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "GROUPNAME")
    private String groupname;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "CODE")
    private String code;
    @ManyToMany
    @JoinTable(name = "users_groups",
            joinColumns = {@JoinColumn(name = "GROUP_ID", referencedColumnName = "ID")},
            inverseJoinColumns = {@JoinColumn(name = "USER_ID", referencedColumnName = "ID")})
    private List<mlsUser> users = new ArrayList<>();
    
    public MlAuthenticationGroups() {
    }

    public MlAuthenticationGroups(Integer id) {
        this.id = id;
    }

    public MlAuthenticationGroups(Integer id, String groupname, String code) {
        this.id = id;
        this.groupname = groupname;
        this.code = code;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getGroupname() {
        return groupname;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<mlsUser> getUsers() {
        return users;
    }

    public void setUsers(List<mlsUser> users) {
        this.users = users;
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
        if (!(object instanceof MlAuthenticationGroups)) {
            return false;
        }
        MlAuthenticationGroups other = (MlAuthenticationGroups) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return groupname;
    }
}
