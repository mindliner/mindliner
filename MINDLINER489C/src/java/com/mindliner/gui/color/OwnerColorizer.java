/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui.color;

import com.mindliner.analysis.MlClassHandler;
import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.MlcLink;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcUser;
import com.mindliner.entities.Colorizer.ColorizerValueType;
import java.awt.Color;
import java.util.List;

/**
 *
 * @author Marius Messerli
 */
public class OwnerColorizer extends BaseColorizer<mlcUser> {

    public OwnerColorizer(Color defaultColor) {
        super(defaultColor);
        type = ColorizerValueType.DiscreteStates;
    }

    @Override
    public Color getColorForObject(Object o) {
        if (!(o instanceof mlcObject)) {
            throw new IllegalArgumentException("The argument must be instance of mlcObject");
        }

        mlcObject mo = (mlcObject) o;
        if (colorMap == null) {
            return getDefaultColor();
        }
        Color c = (Color) colorMap.get(mo.getOwner());
        if (c == null) {
            return getDefaultColor();
        }
        return c;
    }
    
    /**
     * This method uses the link owner instead of the object owner to determine the color.
     * 
     * @param link The link for which the color is requested
     * @return 
     */
    @Override
    public Color getColorForLink(MlcLink link){
        if (colorMap == null) {
            return getDefaultColor();
        }
        Color c = (Color) colorMap.get(link.getOwner());
        if (c == null) {
            return getDefaultColor();
        }
        return c;        
    }

    @Override
    public int getKeyId(mlcUser key) {
        return key.getId();
    }

    @Override
    protected String getSpecificFileNameElement() {
        return MlClassHandler.getClassNameOnly(getClass().getName());
    }

    @Override
    public List<mlcUser> getInputValueList() {
        List<mlcUser> users = CacheEngineStatic.getUsers();
        return users;
    }
}
