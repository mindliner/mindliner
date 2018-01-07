/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.entities;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author Marius Messerli (automatic generation from database table by Netbeans)
 */
@Entity
@Table(name = "sys_environment")
@NamedQueries({
    @NamedQuery(name = "SysEnvironment.findAll", query = "SELECT s FROM SysEnvironment s"),
    @NamedQuery(name = "SysEnvironment.findByKey", query = "SELECT s FROM SysEnvironment s WHERE s.key = :key"),
    @NamedQuery(name = "SysEnvironment.findByValue", query = "SELECT s FROM SysEnvironment s WHERE s.value = :value")})
public class SysEnvironment implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 128)
    @Column(name = "ENVKEY")
    private String key;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "ENVVALUE")
    private String value;
    
    public static enum EnvironmentKeys {
        SERVER_HOSTNAME, // the hostname of the server running the application server
        LOCATION_INHOUSE // whether the server is considered on the intranet or extranet
    }

    public SysEnvironment() {
    }

    public SysEnvironment(String key) {
        this.key = key;
    }

    public SysEnvironment(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (key != null ? key.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof SysEnvironment)) {
            return false;
        }
        SysEnvironment other = (SysEnvironment) object;
        if ((this.key == null && other.key != null) || (this.key != null && !this.key.equals(other.key))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mindliner.entities.SysEnvironment[ key=" + key + " ]";
    }
    
}
