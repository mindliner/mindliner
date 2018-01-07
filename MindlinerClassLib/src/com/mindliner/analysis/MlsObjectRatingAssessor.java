/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.analysis;

import com.mindliner.entities.mlsContact;
import com.mindliner.entities.mlsObject;

/**
 *
 * This class provides methods to update the object link count field. The field
 * is statically stored in all the Mindliner objects and serves to compute the
 * importance value.
 *
 * @author Marius Messerli
 */
public class MlsObjectRatingAssessor {

    private final double inheritanceRate;
    private final double expirationDiscount;

    public MlsObjectRatingAssessor(double inheritanceRate, double expirationDiscount) {
        this.inheritanceRate = inheritanceRate;
        if (expirationDiscount < 1.0) {
            System.err.println("warning: expiration factor smaller than 1 that would amplify importance of exired objects: changed to 1.0");
            this.expirationDiscount = 1D;
        } else {
            this.expirationDiscount = expirationDiscount;
        }
    }

    protected double getSpecificRatingComponent(mlsObject o) {
        return 0;
    }

    public void updateRate(mlsObject o) {
        double rate = getSpecificRatingComponent(o);
        if (getInheritanceRate() > 0) {
            for (mlsObject r : o.getRelatives()) {
                if (r.getRatingDetail() == null) {
                    System.err.println("ratingDetails is null for relative " + o);
                } else {
                    rate += (double) r.getRatingDetail().getRating() * getInheritanceRate();
                }
            }
        }
        if (o.isArchived()) {
            rate = rate / getExpirationDiscount();
        }
        o.getRatingDetail().setRating((int) rate);
    }

    /**
     * @return The discount (a factor by which rating is divided) applied to
     * expired objects
     */
    protected double getInheritanceRate() {
        return inheritanceRate;
    }

    /**
     * This is a factor by which the rating of an object is devided if it is
     * expired.
     *
     * @return The discount factor
     */
    protected double getExpirationDiscount() {
        return expirationDiscount;
    }
}
