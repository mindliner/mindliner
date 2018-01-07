/*
 * ListItemColorChangeListener.java
 *
 * Created on 18. Juli 2006, 10:59
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.mindliner.prefs;

import com.mindliner.gui.color.BaseColorizer;
import java.awt.Color;
import javax.swing.JColorChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Marius Messerli
 */
public class MlColorChangeListener implements ChangeListener {

    private Object key;
    private BaseColorizer colorizer;
    private JColorChooser chooser;

    public MlColorChangeListener(BaseColorizer colorizer, Object key, JColorChooser chooser) {
        this.chooser = chooser;
        this.key = key;
        this.colorizer = colorizer;
        Color mapColor = colorizer.getColorForKey(key);
        if (mapColor != null) {
            chooser.setColor(mapColor);
        } else {
            chooser.setColor(Color.black);
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        Color c = chooser.getColor();
        colorizer.setColor(key, c);
    }
}
