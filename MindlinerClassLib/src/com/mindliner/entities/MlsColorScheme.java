/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

/**
 *
 * @author Marius Messerli
 */
@Entity
@NamedQueries({
    @NamedQuery(
            name = "MlsColorScheme.getAccessibleSchemes", 
            query = "SELECT DISTINCT c FROM MlsColorScheme c, IN(c.owner.clients) client, IN(client.users) sharedUser WHERE sharedUser.id = :userId"),
    @NamedQuery(
            name = "MlsColorScheme.getByUser", 
            query = "SELECT c FROM MlsColorScheme c WHERE c.owner.id = :ownerId")})
@Table(name = "colorschemes")
public class MlsColorScheme implements Serializable {

    private int id = -1;
    private int version = -1;
    private String name = "";
    private mlsUser owner = null;
    private Date modification = new Date();
    List<Colorizer> colorizers = new ArrayList<>();
    private static final long serialVersionUID = 19640205L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MlsColorScheme other = (MlsColorScheme) obj;
        if (this.id != other.id) {
            return false;
        }
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + this.id;
        hash = 89 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return name + " by " + owner.getUserName();
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "NAME")
    public String getName() {
        return name;
    }

    public void setOwner(mlsUser o) {
        owner = o;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "OWNER_ID", referencedColumnName = "ID")
    public mlsUser getOwner() {
        return owner;
    }

    public void setModification(Date d) {
        modification = d;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "MODIFICATION")
    public Date getModification() {
        return modification;
    }

    @Version
    @Column(name = "LOCK_VERSION")
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @OneToMany(mappedBy = "scheme", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    public List<Colorizer> getColorizers() {
        return colorizers;
    }

    public void setColorizers(List<Colorizer> colorizers) {
        this.colorizers = colorizers;
    }

}
