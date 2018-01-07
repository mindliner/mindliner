/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui.color;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.categories.MlsNewsType;
import com.mindliner.clientobjects.MlcLink;
import com.mindliner.clientobjects.mlcNews;
import java.awt.Color;
import java.util.List;

/**
 *
 * @author M.Messerli
 */
public class NewTypeColorizer extends BaseColorizer<MlsNewsType> {

    public NewTypeColorizer(Color defaultColor) {
        super(defaultColor);
    }

    @Override
    public List<MlsNewsType> getInputValueList() {
        return CacheEngineStatic.getActionItemTypes();
    }

    @Override
    public int getKeyId(MlsNewsType key) {
        return key.getId();
    }

    @Override
    public Color getColorForObject(Object o) {
        if (!(o instanceof mlcNews)) {
            return getDefaultColor();
        }
        mlcNews ai = (mlcNews) o;
        Color c = colorMap.get(ai.getNewsType());
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
