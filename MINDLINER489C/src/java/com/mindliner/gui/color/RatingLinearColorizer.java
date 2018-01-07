/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui.color;

import com.mindliner.analysis.MlClassHandler;
import java.awt.Color;

/**
 *
 * @author Marius Messerli
 */
public class RatingLinearColorizer extends RangeColorizer {

    public RatingLinearColorizer(Color defaultColor) {
        super(defaultColor);
    }

    @Override
    protected String getSpecificFileNameElement() {
        return MlClassHandler.getClassNameOnly(getClass().getName());
    }
    
    
}
