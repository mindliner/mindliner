/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui.color;

import com.mindliner.clientobjects.MlcLink;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author marius
 */
public class DefaultColorizer extends BaseColorizer<Void> {

    public DefaultColorizer(Color defaultColor) {
        super(defaultColor);
    }

    @Override
    public List<Void> getInputValueList() {
        return new ArrayList<Void>();
    }

    @Override
    public Color getColorForObject(Object o) {
        return getDefaultColor();
    }

    @Override
    public Color getColorForLink(MlcLink link) {
        return getDefaultColor();
    }

    @Override
    public int getKeyId(Void key) {
        return -1;
    }

}
