/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui.color;

import com.mindliner.analysis.MlClassHandler;
import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.categories.mlsPriority;
import com.mindliner.clientobjects.MlcLink;
import com.mindliner.clientobjects.mlcTask;
import java.awt.Color;
import java.util.List;

/**
 * This class colorizes mlcTasks based on its mlsPriority field.
 *
 * @author M.Messerli
 */
public class TaskPriorityColorizer extends BaseColorizer<mlsPriority> {

    public TaskPriorityColorizer(Color defaultColor) {
        super(defaultColor);
    }

    @Override
    protected String getSpecificFileNameElement() {
        return MlClassHandler.getClassNameOnly(getClass().getName());
    }

    @Override
    public List<mlsPriority> getInputValueList() {
        return CacheEngineStatic.getPriorities();
    }

    @Override
    public int getKeyId(mlsPriority key) {
        return key.getId();
    }
    

    @Override
    public Color getColorForObject(Object o) {
        if (!(o instanceof mlcTask)) {
            return getDefaultColor();
        }
        if (colorMap == null) {
            return getDefaultColor();
        }
        mlcTask t = (mlcTask) o;
        Color c = (Color) colorMap.get(t.getPriority());
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
