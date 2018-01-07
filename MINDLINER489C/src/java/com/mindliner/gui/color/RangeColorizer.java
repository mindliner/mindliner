/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui.color;

import com.mindliner.analysis.MlClassHandler;
import com.mindliner.clientobjects.MlcLink;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.entities.Colorizer;
import com.mindliner.entities.Colorizer.ColorizerRangeKeys;
import com.mindliner.entities.Colorizer.ColorizerValueType;
import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class maps an object rating value to a color.
 *
 * @author Marius Messerli
 */
public class RangeColorizer extends BaseColorizer<ColorizerRangeKeys> {

    private double minimum = 0D;
    private double maximum = 1D;

    public RangeColorizer(Color defaultColor) {
        super(defaultColor);
    }

    @Override
    public Color getColorForObject(Object o) {
        if (!(o instanceof mlcObject)) {
            throw new IllegalArgumentException("The argument must be instance of FixedKeys");
        }
        mlcObject mo = (mlcObject) o;
        if (mo.getRating() < minimum) {
            Color c = colorMap.get(ColorizerRangeKeys.MinimumBoundary);
            if (c != null) {
                return c;
            } else {
                return getDefaultColor();
            }
        }
        if (mo.getRating() > maximum) {
            Color c = colorMap.get(ColorizerRangeKeys.MaximumBoundary);
            if (c != null) {
                return c;
            } else {
                return getDefaultColor();
            }
        }
        double position = (mo.getRating() - minimum) / (maximum - minimum);
        Color start = colorMap.get(ColorizerRangeKeys.MinimumBoundary);
        Color end = colorMap.get(ColorizerRangeKeys.MaximumBoundary);
        if (start == null) {
            if (end == null) {
                return getDefaultColor();
            } else {
                start = getDefaultColor();
            }
        } else {
            if (end == null) {
                end = getDefaultColor();
            }
        }
        return interpolate(start, end, position);
    }

    @Override
    public Color getColorForLink(MlcLink link) {
        return getDefaultColor();
    }

    @Override
    public int getKeyId(ColorizerRangeKeys key) {
        return key.ordinal();

    }

    @Override
    public List<ColorizerRangeKeys> getInputValueList() {
        List values = new ArrayList<String>();
        values.add(ColorizerRangeKeys.MinimumBoundary);
        values.add(ColorizerRangeKeys.MaximumBoundary);
        return values;
    }

    public double getMaximum() {
        return maximum;
    }

    /**
     * The range minimum is used to define the smallest acceptable input value
     * for colorizers. Smaller values are specified as out of range but still
     * legal.
     */
    public double getMinimum() {
        return minimum;
    }

    /**
     * The range maximum is used to define the largest input value. Larger
     * values are specified (and colorized) out as out of range but still legal.
     */
    public void setMaximum(double max) {
        maximum = max;
    }

    public void setMinimum(double min) {
        minimum = min;
    }

    @Override
    public ColorizerValueType getType() {
        return ColorizerValueType.Continuous;
    }

    @Override
    protected String getSpecificFileNameElement() {
        return MlClassHandler.getClassNameOnly(getClass().getName());
    }

    @Override
    public void storeColorMappingParticulars(ObjectOutputStream oos) {
        try {
            oos.writeDouble(minimum);
            oos.writeDouble(maximum);
        } catch (IOException ex) {
            Logger.getLogger(RangeColorizer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @todo implement loading of color range
     */
    @Override
    protected void loadMappingParticulars(ObjectInputStream ois) {
        try {
            minimum = ois.readDouble();
            maximum = ois.readDouble();
        } catch (IOException ex) {
            Logger.getLogger(RangeColorizer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void initializeSensibleDefaults() {
        colorMap.clear();
        colorMap.put(Colorizer.ColorizerRangeKeys.MaximumBoundary, Color.red);
        colorMap.put(Colorizer.ColorizerRangeKeys.MinimumBoundary, Color.black);
    }

}
