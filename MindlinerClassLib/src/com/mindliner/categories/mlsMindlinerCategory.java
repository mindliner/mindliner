/*
 * MindlinerCategory.java
 *
 * Created on 19. Juli 2006, 16:11
 *
 * This is the base class for all elements read from and stored to sql tables.
 * It provides the functions to read, write, and manage the rows.
 */
package com.mindliner.categories;

import com.mindliner.entities.mlsClient;
import java.io.Serializable;
import javax.persistence.*;

/**
 * Abstract category class
 *
 * @author messerli
 */
@MappedSuperclass
public abstract class mlsMindlinerCategory implements Serializable, Comparable {

    protected int id;
    protected String name;
    private mlsClient client = null;
    private static final long serialVersionUID = 19640205L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    public int getId() {
        return id;
    }

    protected void setId(int ident) {
        id = ident;
    }

    @Column(name = "NAME")
    @Basic(fetch = FetchType.EAGER)
    public String getName() {
        return name;
    }

    public void setName(String nam) {
        name = nam;
    }

    public void setClient(mlsClient c) {
        client = c;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CLIENT_ID")
    public mlsClient getClient() {
        return client;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.id;
        hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || (!(o instanceof mlsMindlinerCategory))) {
            return false;
        }
        mlsMindlinerCategory that = (mlsMindlinerCategory) o;
        return this.getId() == that.getId();
    }

    @Override
    public int compareTo(Object o) {
        mlsMindlinerCategory that = (mlsMindlinerCategory) o;
        return getName().compareTo(that.getName());
    }
}
