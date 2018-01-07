/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.analysis.ratingEngineOne;

import com.mindliner.analysis.*;

/**
 *
 * @author Marius Messerli
 */
public interface AssessorFactory {

    /**
     * Creates an assosor for the object class specified
     * @param objectClass The class
     * @param inheritance The factor at which rating from relatives are to be adopted for own rating
     * @param expirationDiscount The rate at which the rating of expired objects is to be divided
     * @return 
     */
    public MlsObjectRatingAssessor getAssessorForClass(Class objectClass, double inheritance, double expirationDiscount);

}
