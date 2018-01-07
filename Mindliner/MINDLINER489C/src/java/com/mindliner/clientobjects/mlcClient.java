/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.clientobjects;

import com.mindliner.entities.mlsUser;
import java.io.Serializable;

/**
 *
 * @author Marius Messerli
 */
public class mlcClient implements Serializable {

    private int id;
    private String name = "";
    private boolean active = false;
    private int version = -1;
    private mlsUser owner;
    private static final long serialVersionUID = 19640205L;

    public boolean isActive() {
        return active;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getVersion() {
        return version;
    }
    
    public mlsUser getOwnerId() {
        return owner;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(int version) {
        this.version = version;
    }
    
    public void setOwner(mlsUser owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + this.id;
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
        final mlcClient other = (mlcClient) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }
    
    
}
