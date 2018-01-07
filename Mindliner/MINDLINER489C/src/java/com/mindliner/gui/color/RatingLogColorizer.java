/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui.color;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.entities.Colorizer.ColorizerRangeKeys;
import java.awt.Color;

/**
 *
 * @author M.Messerli
 */
public class RatingLogColorizer extends RangeColorizer{

    public RatingLogColorizer(Color defaultColor) {
        super(defaultColor);
    }
    
    @Override
    public Color getColorForObject(Object o) {
        if (!(o instanceof mlcObject)) throw new IllegalArgumentException("The argument must be instance of mlcObject");
        mlcObject mo = (mlcObject) o;
        if (mo.getRating() < getMinimum()) {
            return colorMap.get(ColorizerRangeKeys.MinimumBoundary);
        }
        if (mo.getRating() > getMaximum()) {
            return colorMap.get(ColorizerRangeKeys.MaximumBoundary);
        }
        
        // @todo change this to log
        double position = (mo.getRating() - getMinimum()) / (getMaximum() - getMinimum());
        
        return interpolate(
                colorMap.get(ColorizerRangeKeys.MinimumBoundary),
                colorMap.get(ColorizerRangeKeys.MaximumBoundary),
                position);
    }

    
}
