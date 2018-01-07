/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui.color;

import com.mindliner.analysis.MlClassHandler;
import com.mindliner.clientobjects.MlcLink;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.entities.Colorizer.ColorizerThresholdKeys;
import com.mindliner.entities.Colorizer.ColorizerValueType;
import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * This class assigns the object color based on the time interval since the
 * last modification.
 * 
 * @author Marius Messerli
 */
public class ModificationAgeColorizer extends BaseColorizer<ColorizerThresholdKeys> implements ThresholdColorizer {

    private double threshold = 1;

    public ModificationAgeColorizer(Color defaultColor) {
        super(defaultColor);
    }
    
    private Color getAgeColor(double ageInMinutes){
        if (ageInMinutes <= threshold) {
            Color inRangeColor = colorMap.get(ColorizerThresholdKeys.InRange);
            if (inRangeColor == null) {
                return getDefaultColor();
            } else {
                return inRangeColor;
            }
        } else {
            Color outOfRangeColor = colorMap.get(ColorizerThresholdKeys.OutOfRange);
            if (outOfRangeColor == null) {
                return getDefaultColor();
            } else {
                return outOfRangeColor;
            }
        }
    }

    @Override
    public Color getColorForObject(Object o) {
        if (! (o instanceof mlcObject)) throw new IllegalArgumentException("The argument must be instance of mlcObject");
        mlcObject mo = (mlcObject) o;
        double ageInMinutes = ((new Date()).getTime() - mo.getModificationDate().getTime()) / 60 / 60 / 1000;
        return getAgeColor(ageInMinutes);
    }

    @Override
    public Color getColorForLink(MlcLink link) {
        double ageInMinutes = ((new Date()).getTime() - link.getModificationDate().getTime()) / 60 / 60 / 1000;
        return getAgeColor(ageInMinutes);
    }
    
    

    @Override
    protected void loadMappingParticulars(ObjectInputStream ois) {
        try {
            threshold = ois.readDouble();
        } catch (IOException ex) {
            Logger.getLogger(ModificationAgeColorizer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void storeColorMappingParticulars(ObjectOutputStream oos) {
        try {
            oos.writeDouble(threshold);
        } catch (IOException ex) {
            Logger.getLogger(ModificationAgeColorizer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public int getKeyId(ColorizerThresholdKeys key) {
        return key.ordinal();
    }

    @Override
    protected String getSpecificFileNameElement() {
        return MlClassHandler.getClassNameOnly(getClass().getName());
    }

    @Override
    public List<ColorizerThresholdKeys> getInputValueList() {
        List values = new ArrayList<>();
        values.add(ColorizerThresholdKeys.InRange);
        values.add(ColorizerThresholdKeys.OutOfRange);
        return values;
    }

    /**
     * Define the maximum age in milli-seconds for an item to be in-range.
     *
     * @param threshold The maximum modification age, in minutes, for the object
     * to be displayed with the "in-range" color. Objects with larger
     * modification age are shown in "out-of-range" color.
     */
    @Override
    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    @Override
    public double getThreshold() {
        return threshold;
    }

    @Override
    public String getThresholdDescription() {
        return "hours";
    }

    @Override
    public ColorizerValueType getType() {
        return ColorizerValueType.Threshold;
    }

    @Override
    public void initializeSensibleDefaults() {
        colorMap.clear();
        colorMap.put(ColorizerThresholdKeys.InRange, new Color(0x00DA25));
        colorMap.put(ColorizerThresholdKeys.OutOfRange, Color.black);
    }
    
    
}
