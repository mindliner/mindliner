/*
 * Confidentiality.java
 *
 * Created on 16. Januar 2006, 11:25
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package com.mindliner.categories;

import com.mindliner.entities.mlsClient;
import java.io.Serializable;
import javax.persistence.*;

/**
 * Manages the confidentiality levels. The static functions manage I/O to/from
 * database and the list of all available levels. The member functions provide
 * methods to process a single confidentiality level.
 *
 * @author Marius Messerli
 */
@Entity
@Table(name = "confidentiality")
@NamedQueries({
    @NamedQuery(name = "mlsConfidentiality.getAll", query = "SELECT c FROM mlsConfidentiality c"),
    @NamedQuery(name = "mlsConfidentiality.getAllowedConfidentialities", query = "SELECT c FROM mlsConfidentiality c where c.clevel <= :cLevel AND c.client.id = :cId"),
    @NamedQuery(name = "mlsConfidentiality.getConfidentialityByNameFragment", query = "SELECT c FROM mlsConfidentiality c WHERE c.name LIKE :fragment AND c.client.id = :cId AND c.clevel <= :cLevel"),
    @NamedQuery(name = "mlsConfidentiality.getAllConfidentialities", query = "SELECT c FROM mlsConfidentiality c WHERE c.client.id = :cId"),
    @NamedQuery(name = "mlsConfidentiality.removeForClient", query = "DELETE FROM mlsConfidentiality c WHERE c.client = :client"),
    @NamedQuery(name = "mlsConfidentiality.getMostPublicForClient", query = "SELECT c FROM mlsConfidentiality c WHERE c.clevel in (SELECT MIN(cc.clevel) FROM mlsConfidentiality cc WHERE cc.client.id = :clientId) AND c.client.id = :clientId")})
public class mlsConfidentiality extends mlsMindlinerCategory implements Comparable, Serializable {

    private int clevel;
    private static final long serialVersionUID = 19640205L;

    public mlsConfidentiality() {
        super();
    }

    public mlsConfidentiality(int clevel, String name) {
        this.name = name;
        this.clevel = clevel;
    }

    @Column(name = "CLEVEL")
    public int getClevel() {
        return clevel;
    }

    public void setClevel(int l) {
        clevel = l;
    }

    /**
     * Function determines whether the specified confidentiality level exceeds
     * the level of this object.
     *
     * @param cl The confidentiality level which is to be tested against this.
     * @return True if the specified conf level is higher than this. False
     * otherwise.
     */
    public boolean exceeds(mlsConfidentiality cl) {
        if (cl.getClevel() > this.getClevel()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int compareTo(Object that) {
        if (that == null) return -1;
        mlsConfidentiality thatConfidentiality = (mlsConfidentiality) that;
        if (this.getClevel() < thatConfidentiality.getClevel()) {
            return -1;
        }
        if (this.getClevel() == thatConfidentiality.getClevel()) {
            return 0;
        }
        return 1;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof mlsConfidentiality)) {
            return false;
        }
        mlsConfidentiality that = (mlsConfidentiality) o;
        return (id == that.id && getClient().equals(that.getClient()));
    }

    @Override
    public int hashCode() {
        return super.hashCode() + getClient().hashCode();
    }

}
