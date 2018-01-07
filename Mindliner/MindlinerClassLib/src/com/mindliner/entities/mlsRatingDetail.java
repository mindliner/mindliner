/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.entities;

import java.io.Serializable;
import javax.persistence.*;

/**
 *
 * @author Marius Messerli
 */
@Entity
@Table(name = "ratingdetails")
public class mlsRatingDetail implements Serializable {

    private Integer id;
    private int generation = 0;
    private double rating = 0D;
    private static final long serialVersionUID = 19640205L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof mlsRatingDetail)) {
            return false;
        }
        mlsRatingDetail other = (mlsRatingDetail) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mindliner.entities.RatingDetail[id=" + id + "]";
    }

    public void setGeneration(int g) {
        generation = g;
    }

    @Column(name = "GENERATION")
    public int getGeneration() {
        return generation;
    }

    public void setRating(double r) {
        rating = r;
    }

    @Column(name = "RATING")
    public double getRating() {
        return rating;
    }
}
