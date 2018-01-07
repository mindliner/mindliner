/*
 * Country.java
 *
 * Created on 11. Juli 2006, 15:21
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.mindliner.entities;

import java.util.LinkedList;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author messerli
 */
@Entity
@Table(name = "countries")
public class mlsCountry implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public mlsCountry() {
    }

    @Column(name = "NAME")
    public String getName() {
        return name;
    }

    public void setName(String n) {
        name = n;
    }

    @Column(name = "CODE")
    public String getCode() {
        return code;
    }

    public void setCode(String c) {
        code = c;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof mlsCountry)) {
            return false;
        }
        mlsCountry c = (mlsCountry) o;
        if (c.getId() == this.getId()) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 53 * hash + (this.code != null ? this.code.hashCode() : 0);
        hash = 53 * hash + (this.used ? 1 : 0);
        return hash;
    }
    /**
     * Member variables
     */
    private String name;
    private int id;
    private String code;
    boolean used;
    private static LinkedList countryList = new LinkedList();
}
