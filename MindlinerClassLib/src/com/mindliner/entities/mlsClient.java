/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.entities;

import com.mindliner.categories.mlsConfidentiality;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.*;

/**
 *
 * @author Marius Messerli
 */
@Entity
@Table(name = "clients")
@NamedQueries({
    @NamedQuery(name = "mlsClient.findAll", query = "SELECT c FROM mlsClient c"),
    @NamedQuery(name = "mlsClient.findById", query = "SELECT c FROM mlsClient c WHERE c.id = :id"),
    @NamedQuery(name = "mlsClient.findByName", query = "SELECT c FROM mlsClient c WHERE c.name = :name"),
    @NamedQuery(name = "mlsClient.findByOwner", query = "SELECT c FROM mlsClient c WHERE c.owner = :owner")})
public class mlsClient implements Serializable {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "NAME")
    private String name = "";
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "users_clients",
            joinColumns = {
                @JoinColumn(name = "CLIENT_ID", referencedColumnName = "ID")},
            inverseJoinColumns = {
                @JoinColumn(name = "USER_ID", referencedColumnName = "ID")})
    private List<mlsUser> users = new ArrayList<>();
    @Column(name = "ACTIVE")
    private boolean active = false;
    @Version
    @Column(name = "LOCK_VERSION")
    private int version = -1;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "OWNER_ID", referencedColumnName = "ID")
    private mlsUser owner;
    @OneToMany(mappedBy = "client", fetch = FetchType.EAGER)
    private List<mlsConfidentiality> confidentialities = new ArrayList<>();
    @OneToOne(mappedBy = "client")
    private mlsForeignDataSource foreignSource = null;
    @OneToMany(mappedBy = "client")
    private Collection<Island> islands;
    private static final long serialVersionUID = 19640205L;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getVersion() {
        return version;
    }
    
    public mlsUser getOwner() {
        return owner;
    }
    
    public void setOwner(mlsUser owner) {
        this.owner = owner;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean a) {
        active = a;
    }

    public String getName() {
        return name;
    }

    public void setName(String n) {
        name = n;
    }

    /**
     * Don't use this function to add a user to a client. Create a new
     * UserClientLink object instead and call updateCacheEntity()
     *
     * @return
     * @deprecated
     */
    @Deprecated
    public List<mlsUser> getUsers() {
        return users;
    }

    public List<mlsConfidentiality> getConfidentialities() {
        return confidentialities;
    }

    public void setConfidentialities(List<mlsConfidentiality> confidentialities) {
        this.confidentialities = confidentialities;
    }

    public void setUsers(List<mlsUser> users) {
        this.users = users;
    }

    public Collection<Island> getIslands() {
        return islands;
    }

    public void setIslands(Collection<Island> islands) {
        this.islands = islands;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) id;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("equals() with null argument not allowed and points to a problem in the application");
        }
        if (!(object instanceof mlsClient)) {
            return false;
        }
        mlsClient other = (mlsClient) object;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    public mlsForeignDataSource getForeignSource() {
        return foreignSource;
    }

    public void setForeignSource(mlsForeignDataSource foreignSource) {
        this.foreignSource = foreignSource;
    }

    @Override
    public String toString() {
        return name;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public static List<Integer> getIds(List<mlsClient> clients) {
        List<Integer> ids = new ArrayList<>();
        for (mlsClient c : clients) {
            ids.add(c.getId());
        }
        return ids;
    }
}
