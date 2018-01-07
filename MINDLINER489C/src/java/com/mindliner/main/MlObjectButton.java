/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.main;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.entities.Colorizer;
import com.mindliner.gui.color.BaseColorizer;
import com.mindliner.gui.color.ColorManager;
import com.mindliner.gui.color.FixedKeyColorizer;
import java.awt.Color;
import javax.swing.JButton;

/**
 * A button that is bound to a Mindliner object.
 *
 * @author Marius Messerli
 */
public final class MlObjectButton extends JButton {

    private mlcObject object;
    private final String nullObjectText;

    /**
     * Constructor
     *
     * @param o The object which is bound to the button, specify null if no
     * object is bound yet
     * @param nullObjectText The text of the button in case the object is null
     */
    public MlObjectButton(mlcObject o, String nullObjectText) {
        this.nullObjectText = nullObjectText;
        setObject(o);
        configureComponent();
    }

    private void configureComponent() {
        BaseColorizer fkc = ColorManager.getColorizerForType(Colorizer.ColorDriverAttribute.FixedKey);
        Color bg = fkc.getColorForKey(FixedKeyColorizer.FixedKeys.MAIN_DEFAULT_BACKGROUND);
        Color fg = fkc.getColorForKey(FixedKeyColorizer.FixedKeys.MAIN_DEFAULT_TEXT);
        setForeground(fg);
        setBackground(bg);
    }

    public mlcObject getObject() {
        return object;
    }

    public void setObject(mlcObject thing) {
        if (thing == null) {
            setText(nullObjectText);
            setEnabled(false);
        } else {
            if (thing.getHeadline().length() == 0) {
                setText(thing.getClass().getSimpleName());
            } else {
                setText(thing.getHeadline());
            }
            setToolTipText("drop your favorite object on this button for fast access");
            setEnabled(true);
        }
        this.object = thing;
    }

}
