/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.analysis.ratingEngineOne;

import com.mindliner.analysis.MlsObjectRatingAssessor;
import com.mindliner.entities.mlsObject;

/**
 *
 * @author Marius Messerli
 */
public class ContactAssessor extends MlsObjectRatingAssessor {

    public ContactAssessor(double inheritanceRate, double expirationDiscount) {
        super(inheritanceRate, expirationDiscount);
    }

    @Override
    public double getSpecificRatingComponent(mlsObject mo) {
        return 0D;
    }
}
