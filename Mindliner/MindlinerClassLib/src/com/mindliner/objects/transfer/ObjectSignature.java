/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.objects.transfer;

import java.io.Serializable;

/**
 * This class represents a most simplified state of objects to allowed such
 * things as update check for synchronization or cache maintenance.
 *
 * @author Marius Messerli
 */
public class ObjectSignature implements Serializable {

    private int id;
    private int version;

    public ObjectSignature(int id, int version) {
        this.id = id;
        this.version = version;
    }

    public int getId() {
        return id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.id;
        hash = 89 * hash + this.version;
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
        final ObjectSignature other = (ObjectSignature) obj;
        if (this.id != other.id) {
            return false;
        }
        if (this.version != other.version) {
            return false;
        }
        return true;
    }
}
