/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.styles;

import com.mindliner.gui.color.FixedKeyColorizer;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.ComboBoxEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;

/**
 *
 * @author marius
 */
public class MlComboBoxUI extends BasicComboBoxUI {

    public static ComboBoxUI createUI(JComponent c, FixedKeyColorizer fkc) {
        return new MlComboBoxUI(fkc);
    }

    Color background;
    Color shadow;
    Color darkShadow;
    Color highlight;
    Color focus;

    public MlComboBoxUI(FixedKeyColorizer colorizer) {
        background = colorizer.getColorForKey(FixedKeyColorizer.FixedKeys.MAIN_DEFAULT_BACKGROUND);
        shadow = colorizer.getColorForKey(FixedKeyColorizer.FixedKeys.TABLE_HEADER_BACKGROUND);
        darkShadow = shadow.darker();
        highlight = colorizer.getColorForKey(FixedKeyColorizer.FixedKeys.MAIN_COMBOBOX_SELECTION);
        focus = colorizer.getColorForKey(FixedKeyColorizer.FixedKeys.MAIN_COMBOBOX_FOCUS);
    }

    @Override
    protected JButton createArrowButton() {
        return new BasicArrowButton(
                BasicArrowButton.SOUTH, background, shadow, darkShadow, highlight);
    }

    // unfortunately no luck with this one either - no change
    
//    @Override
//    public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
//        g.setColor(hasFocus ? focus : background);
//        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
//    }
    
    

}
