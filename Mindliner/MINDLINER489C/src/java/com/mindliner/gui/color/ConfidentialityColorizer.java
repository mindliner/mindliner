/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui.color;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.clientobjects.MlcLink;
import com.mindliner.clientobjects.mlcObject;
import java.awt.Color;
import java.util.List;

/**
 *
 * @author M.Messerli
 */
public class ConfidentialityColorizer extends BaseColorizer<mlsConfidentiality> {

    public ConfidentialityColorizer(Color defaultColor) {
        super(defaultColor);
    }

    @Override
    public List<mlsConfidentiality> getInputValueList() {
        return CacheEngineStatic.getConfidentialities();
    }

    @Override
    public int getKeyId(mlsConfidentiality key) {
        return key.getId();
    }

    /**
     * @todo the following lookup in the map returns null all the time
     * @param o
     * @return
     */
    @Override
    public Color getColorForObject(Object o) {
        if (!(o instanceof mlcObject)) {
            throw new IllegalArgumentException("The argument must be instance of mlcObject");
        }
        mlcObject mo = (mlcObject) o;
        Color c = colorMap.get(mo.getConfidentiality());
        if (c == null) {
            return getDefaultColor();
        }
        return c;
    }

    @Override
    public Color getColorForLink(MlcLink link) {
        return getDefaultColor();
    }

}
