/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.analysis.ratingEngineOne;

import com.mindliner.analysis.MlsObjectRatingAssessor;
import com.mindliner.entities.*;

/**
 *
 * @author Marius Messerli
 */
public class AssessorFactoryDEC08 implements AssessorFactory {

    @Override
    public MlsObjectRatingAssessor getAssessorForClass(Class c, double inheritance, double expirationDiscount) {
        if (c == mlsTask.class) {
            return new TaskAssessor(inheritance, expirationDiscount);
        } else if (c == mlsContact.class) {
            return new ContactAssessor(inheritance, expirationDiscount);
        }  else {
            return new MlsObjectRatingAssessor(inheritance, expirationDiscount);
        }
    }
}
