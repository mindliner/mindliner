package com.mindliner.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Islands are collections of object networks so that none of the objects of an
 * island is linked, directly or indirectly, to any of the objects outside the
 * island.
 *
 * @author Marius Messerli
 */
@Entity
@Table(name = "islands")
@NamedQueries({
    @NamedQuery(name = "Island.findAll", query = "SELECT i FROM Island i"),
    @NamedQuery(name = "Island.findAllForClient", query = "SELECT i FROM Island i WHERE i.client.id = :clientId"),
    @NamedQuery(name = "Island.deleteForClient", query = "DELETE FROM Island i where i.client.id = :clientId")})
public class Island implements Serializable {

    private static final long serialVersionUID = 1L;
    private Integer id;
    private List<mlsObject> objects = new ArrayList<>();
    private mlsClient client;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @OneToMany(mappedBy = "island")
    public List<mlsObject> getObjects() {
        return objects;
    }

    public void setObjects(List<mlsObject> objects) {
        this.objects = objects;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CLIENT_ID", referencedColumnName = "ID")
    public mlsClient getClient() {
        return client;
    }

    public void setClient(mlsClient client) {
        this.client = client;
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
        if (!(object instanceof Island)) {
            return false;
        }
        Island other = (Island) object;
        return (this.id != null || other.id == null) && (this.id == null || this.id.equals(other.id));
    }

    @Override
    public String toString() {
        return "Islands:" + id;
    }

}
