/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.analysis;

import com.mindliner.analysis.ratingEngineOne.RatingEngineDec08;

/**
 * This class creates rating engines based on the rating strategy.
 *
 * @author Marius Messerli
 */
public class RatingEngineFactory {

    public enum EngineType {

        One
    }

    public static RatingEngine getRatingEngine(EngineType engine) {
        switch (engine) {

            case One:
                return new RatingEngineDec08();

            default:
                throw new IllegalArgumentException("Unknown engine type");
        }
    }

}
