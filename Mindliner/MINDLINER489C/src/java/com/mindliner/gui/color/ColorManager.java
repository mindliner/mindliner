/*
 * CategoryManager.java
 *
 * Created on 19.05.2007, 09:35:29
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.mindliner.gui.color;

import com.mindliner.clientobjects.mlcNews;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcTask;
import com.mindliner.entities.Colorizer.ColorDriverAttribute;
import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 *
 * This class determins the color of a Mindliner object.
 *
 * The color is determined from the object's attributes, a color driver that
 * defines which of the attributes determins the color, and a list of colors,
 * one for each value the color driver might take.
 *
 * We should have a base class ColorizerBase which is the root of all colorizers
 * and has a method Color getColor(mlcObject o). It should have a field
 * colorDriver that defines which field of the object drives its color and it
 * should have list ColorValues.
 *
 * A singleton to take care of the row color handling. Colors are not stored to
 * the database but kept local on the user's client machine.
 *
 * @author Marius Messerli
 */
public class ColorManager {

    // 8-bit color values below this value are considered dark, values above as bright
    public static int DARKNESS_THRESHOLD = 140;
    private static final Map<ColorDriverAttribute, BaseColorizer> colorizers = new EnumMap<>(ColorDriverAttribute.class);
    private static final boolean INITIALIZED = false;

    /**
     * see: http://en.wikipedia.org/wiki/Luminance_(relative)
     *
     * @param rgbColor An integer having the red component as 8-bit value in the
     * least significant 8 bits, followed by green, followed by blue
     * @return
     */
    public static int getBrightness(int rgbColor) {
        int r = rgbColor & 0xff;
        int g = rgbColor & 0xff00 >> 8;
        int b = rgbColor & 0xff0000 >> 16;
        return ColorManager.getBrightness(new Color((int) (0.2126 * r), (int) (0.7152 * g), (int) (0.0722 * b)));
    }

    public static int getBrightness(Color c) {
        float[] hsbvals = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        return (int) (hsbvals[2] * 255);
    }

    /**
     * This function returns the label color for the specified background color.
     * If the background is bright the label color is BLAKE otherwise it is
     * WHITE.
     *
     * @param backgroundColor The background color.
     * @return Black or White depeinding on background
     */
    public static Color getForegroundColor(Color backgroundColor) {
        return ColorManager.getBrightness(backgroundColor) <= DARKNESS_THRESHOLD ? Color.WHITE : Color.BLACK;
    }

    public static void registerColorizer(BaseColorizer c) {
        if (!colorizers.containsKey(c.getDriverAttribute())) {
            colorizers.put(c.getDriverAttribute(), c);
        }
    }

    public static void unregisterColorizer(BaseColorizer c) {
        colorizers.remove(c.getDriverAttribute());
    }

    public static void storeColorDefinitions() {
        for (BaseColorizer c : colorizers.values()) {
            c.storeColorizer();
        }
    }

    public static List<BaseColorizer> getColorizers() {
        return new ArrayList<>(colorizers.values());
    }

    public static void initialize() {

        if (!INITIALIZED) {

            BaseColorizer colorizer = ColorizerFactory.createColorizer(ColorDriverAttribute.Confidentiality);
            if (colorizer != null) {
                registerColorizer(colorizer);
            }

            colorizer = ColorizerFactory.createColorizer(ColorDriverAttribute.ModificationAge);
            if (colorizer != null) {
                registerColorizer(colorizer);
            }

            colorizer = ColorizerFactory.createColorizer(ColorDriverAttribute.Owner);
            if (colorizer != null) {
                registerColorizer(colorizer);
            }

            colorizer = ColorizerFactory.createColorizer(ColorDriverAttribute.TaskPriority);
            if (colorizer != null) {
                registerColorizer(colorizer);
            }

            colorizer = ColorizerFactory.createColorizer(ColorDriverAttribute.Rating);
            if (colorizer != null) {
                registerColorizer(colorizer);
            }

            colorizer = ColorizerFactory.createColorizer(ColorDriverAttribute.DataPool);
            if (colorizer != null) {
                registerColorizer(colorizer);
            }

            colorizer = ColorizerFactory.createColorizer(ColorDriverAttribute.FixedKey);
            if (colorizer != null) {
                registerColorizer(colorizer);
            }
            colorizer = ColorizerFactory.createColorizer(ColorDriverAttribute.Brizwalk);
            if (colorizer != null) {
                registerColorizer(colorizer);
            }

            for (BaseColorizer c : colorizers.values()) {
                c.initializeColorizer();
            }
        }
    }

    public static BaseColorizer getColorizerForType(ColorDriverAttribute attrib) {
        return colorizers.get(attrib);
    }

    /**
     * This function picks the most specific colorizer for the object class and
     * uses it to colorize the object. For example, the task priority is the
     * more specific for tasks than their confidentiality, an attribute that is
     * shared by all objects.
     *
     * @param o The object to be colorized
     * @return The most specific color for that object.
     */
    public static Color getMostSpecificColor(mlcObject o) {
        if (o instanceof mlcTask) {
            BaseColorizer c = getColorizerForType(ColorDriverAttribute.TaskPriority);
            if (c != null) {
                return c.getColorForObject(o);
            }
        } else {
            BaseColorizer c = getColorizerForType(ColorDriverAttribute.Confidentiality);
            if (c != null) {
                return c.getColorForObject(o);
            }
        }
        return Color.black;
    }
}
