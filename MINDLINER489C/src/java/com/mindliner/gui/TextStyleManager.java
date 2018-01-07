/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.gui;

import java.awt.Color;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 *
 * @author Marius Messerli
 */
public class TextStyleManager {

    private static TextStyleManager instance = null;

    public static TextStyleManager getUniqueInstance(){
        synchronized (TextStyleManager.class) {
            if (instance == null) {
                instance = new TextStyleManager();
            }
        }
        return instance;
    }


    public TextStyleManager() {
        StyleConstants.setForeground(headerStyle, new Color (0, 102, 204));
        StyleConstants.setFontFamily(headerStyle, "Arial");
        StyleConstants.setFontSize(headerStyle, 12);

        StyleConstants.setForeground(bodyStyle, new Color (0, 0, 0));
        StyleConstants.setFontFamily(bodyStyle, "Arial");
        StyleConstants.setFontSize(bodyStyle, 10);
    }

    public SimpleAttributeSet getBodyStyle() {
        return bodyStyle;
    }

    public SimpleAttributeSet getHeaderStyle() {
        return headerStyle;
    }

    private SimpleAttributeSet headerStyle = new SimpleAttributeSet();
    private SimpleAttributeSet bodyStyle = new SimpleAttributeSet();
}
