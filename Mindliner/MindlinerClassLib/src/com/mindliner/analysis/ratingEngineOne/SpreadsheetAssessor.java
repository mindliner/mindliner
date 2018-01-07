/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.analysis.ratingEngineOne;

import com.mindliner.analysis.MlsObjectRatingAssessor;
import com.mindliner.entities.mlsObject;

/**
 * @todo Implement this assessor properly.
 * @author Marius Messerli
 */
public class SpreadsheetAssessor extends MlsObjectRatingAssessor {

    public SpreadsheetAssessor(double inheritanceRate, double expirationDiscount) {
        super(inheritanceRate, expirationDiscount);
    }

    @Override
    public double getSpecificRatingComponent(mlsObject object) {
        return 0D;

    }
}
