/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui.color;

import com.mindliner.analysis.MlClassHandler;
import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.MlcLink;
import com.mindliner.entities.Colorizer.ColorDriverAttribute;
import com.mindliner.entities.Colorizer.ColorizerValueType;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The colorizer classes implement a rather generic way to specify the attribute
 * and the mechanism that drive the color for a particular object. Some
 * attributes are discrete (categories) others are a threshold or a scalar value
 * that gradually changes the colors between two boundaries.
 *
 * @author Marius Messerli
 */
public abstract class BaseColorizer<T> {

    /**
     * This is the attribute of the object that represents the lookup key of the
     * color map.
     */
    private Color defaultColor = Color.black;
    protected ColorizerValueType type = ColorizerValueType.DiscreteStates;
    protected Map<T, Color> colorMap = new HashMap<>();
    private ColorDriverAttribute driverAttribute;

    public BaseColorizer(Color defaultColor) {
        this.defaultColor = defaultColor;
    }

    public abstract List<T> getInputValueList();

    /**
     * For most colorizers the argument is of type mlcObject but for at least
     * one FixedKeyColorizer it is a key so the signature here is open to
     * Object.
     *
     * @param o The object to be colorized.
     * @return The color for the specified object.
     */
    public abstract Color getColorForObject(Object o);
    
    public abstract Color getColorForLink(MlcLink link);

    public Color getColorForKey(T key) {
        return colorMap.get(key);
    }

    public abstract int getKeyId(T key);

    public void setColor(T key, Color color) {
        colorMap.put(key, color);
    }

    public ColorDriverAttribute getDriverAttribute() {
        return driverAttribute;
    }

    public void setDriverAttribute(ColorDriverAttribute attrib) {
        driverAttribute = attrib;
    }

    /**
     * Determines whether the colormap is empty.
     *
     * @return The first key of the color map (mainly to determine the key
     * class) or null if there are no records in the map.
     */
    public T getFirstKey() {
        if (colorMap.isEmpty()) {
            return null;
        }
        Set<T> keySet = colorMap.keySet();
        Iterator<T> it = keySet.iterator();
        if (!it.hasNext()) {
            throw new IllegalStateException("If the map isnt empty it should have at least one element.");
        }
        return it.next();
    }

    protected String getSpecificFileNameElement() {
        return MlClassHandler.getClassNameOnly(getClass().getName());
    }

    /**
     * Removes all color definitions from the map. Afterwards the color
     * subsystem return the default color for each of the colorizers.
     */
    public void clearColorMap() {
        colorMap.clear();
    }
    
    public void initializeSensibleDefaults(){
        colorMap.clear();
    }

    public void initializeColorizer() {
        ObjectInputStream ois;
        try {
            File f = new File(CacheEngineStatic.getColorCacheFilePath(getSpecificFileNameElement()));
            if (f.exists()) {
                FileInputStream fis = new FileInputStream(f);
                ois = new ObjectInputStream(fis);
                colorMap = (Map<T, Color>) ois.readObject();
                loadMappingParticulars(ois);
                ois.close();
                fis.close();
            }
            else{
                initializeSensibleDefaults();
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(BaseColorizer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BaseColorizer.class.getName()).log(Level.WARNING, null, ex);
        }
    }

    protected void loadMappingParticulars(ObjectInputStream ois) {
    }

    /**
     * @todo implement this function
     *
     * @param start range starting color
     * @param end range ending color
     * @param position The position between start (0.0) and end (1.0).
     * @return The interpolated color at position. For position outside of the
     * range 0..1 the default color is returned.
     */
    protected Color interpolate(Color start, Color end, double position) {
        if (position < 0 || position > 1) {
            return getDefaultColor();
        }
        int red = (int) ((double) start.getRed() * (1 - position) + (double) end.getRed() * position);
        int green = (int) ((double) start.getGreen() * (1 - position) + (double) end.getGreen() * position);
        int blue = (int) ((double) start.getBlue() * (1 - position) + (double) end.getBlue() * position);

        return new Color(red, green, blue);
    }

    public void storeColorizer() {
        if (!colorMap.isEmpty()) {
            try {
                ObjectOutputStream oos;
                String filename = CacheEngineStatic.getColorCacheFilePath(getSpecificFileNameElement());
                File f = new File(filename);
                try (FileOutputStream fos = new FileOutputStream(f)) {
                    oos = new ObjectOutputStream(fos);
                    oos.writeObject(colorMap);
                    storeColorMappingParticulars(oos);
                    oos.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(BaseColorizer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Some colorizers need more than a map. The range colorizers, for instance,
     * need the boundary values as well. This default implementation, the noop,
     * takes care of those mappers that do not have anything in addition to a
     * color map.
     *
     * Make sure that you store the extra values in the same sequences as you
     * will be loading them again using
     *
     * @param oos
     * @see loadMappingParticulars()
     */
    protected void storeColorMappingParticulars(ObjectOutputStream oos) {
    }

    public ColorizerValueType getType() {
        return type;
    }

    public Color getDefaultColor() {
        return defaultColor;
    }

    @Override
    public String toString() {
        return driverAttribute.name();
    }

    public Map<T, Color> getColorMap() {
        return colorMap;
    }
}
