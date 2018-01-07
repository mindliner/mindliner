/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui.color;

import com.mindliner.clientobjects.MlcLink;
import com.mindliner.gui.color.BrizwalkStaticColorizer.BrizwalkColorKey;
import static com.mindliner.gui.color.BrizwalkStaticColorizer.BrizwalkColorKey.CONTROLS_BACKGROUND;
import static com.mindliner.gui.color.BrizwalkStaticColorizer.BrizwalkColorKey.FONT_COLOR;
import static com.mindliner.gui.color.BrizwalkStaticColorizer.BrizwalkColorKey.LEVEL_EIGHT;
import static com.mindliner.gui.color.BrizwalkStaticColorizer.BrizwalkColorKey.LEVEL_FIVE;
import static com.mindliner.gui.color.BrizwalkStaticColorizer.BrizwalkColorKey.LEVEL_FOUR;
import static com.mindliner.gui.color.BrizwalkStaticColorizer.BrizwalkColorKey.LEVEL_ONE;
import static com.mindliner.gui.color.BrizwalkStaticColorizer.BrizwalkColorKey.LEVEL_SEVEN;
import static com.mindliner.gui.color.BrizwalkStaticColorizer.BrizwalkColorKey.LEVEL_SIX;
import static com.mindliner.gui.color.BrizwalkStaticColorizer.BrizwalkColorKey.LEVEL_THREE;
import static com.mindliner.gui.color.BrizwalkStaticColorizer.BrizwalkColorKey.LEVEL_TWO;
import static com.mindliner.gui.color.BrizwalkStaticColorizer.BrizwalkColorKey.LEVEL_ZERO;
import static com.mindliner.gui.color.BrizwalkStaticColorizer.BrizwalkColorKey.NEW_NODE_COLOR;
import static com.mindliner.gui.color.BrizwalkStaticColorizer.BrizwalkColorKey.SELECTION_COLOR;
import static com.mindliner.gui.color.BrizwalkStaticColorizer.BrizwalkColorKey.SHELF_BACKGROUND;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;

/**
 * A class that defines the static custom colors for Brizwalk.
 *
 * @author Marius Messerli
 */
public class BrizwalkStaticColorizer extends BaseColorizer<BrizwalkColorKey> {

    public static enum BrizwalkColorKey {

        LEVEL_ZERO,
        LEVEL_ONE,
        LEVEL_TWO,
        LEVEL_THREE,
        LEVEL_FOUR,
        LEVEL_FIVE,
        LEVEL_SIX,
        LEVEL_SEVEN,
        LEVEL_EIGHT,
        FONT_COLOR,
        CONTROLS_BACKGROUND,
        SHELF_BACKGROUND,
        SELECTION_COLOR,
        NEW_NODE_COLOR
    }

    @Override
    public Color getColorForLink(MlcLink link) {
        return getDefaultColor();
    }

    @Override
    public int getKeyId(BrizwalkColorKey key) {
        return key.ordinal();
    }

    @Override
    public Color getColorForObject(Object keyObject) {
        BrizwalkColorKey key = (BrizwalkColorKey) keyObject;
        Color c = colorMap.get(key);
        if (c != null) {
            return c;
        }
        return getDefaultColor();
    }

    @Override
    public List<BrizwalkColorKey> getInputValueList() {
        return Arrays.asList(BrizwalkColorKey.values());
    }

    public BrizwalkStaticColorizer(Color defaultColor) {
        super(defaultColor);
    }

    @Override
    public void initializeSensibleDefaults() {
        colorMap.clear();
        colorMap.put(LEVEL_ZERO, Color.LIGHT_GRAY);
        colorMap.put(LEVEL_ONE, new Color(0xFFC9D9));
        colorMap.put(LEVEL_TWO, new Color(0xD4F0FF));
        colorMap.put(LEVEL_THREE, new Color(0xD5FFDA));
        colorMap.put(LEVEL_FOUR, new Color(0xE7D7FF));
        colorMap.put(LEVEL_FIVE, new Color(0xFBDBC4));
        colorMap.put(LEVEL_SIX, new Color(0xFFFFCD));
        colorMap.put(LEVEL_SEVEN, new Color(0xDEFFFF));
        colorMap.put(LEVEL_EIGHT, new Color(0xD0E07D));
        colorMap.put(FONT_COLOR, new Color(0x010101));
        colorMap.put(CONTROLS_BACKGROUND, new Color(0xEDF2F5));
        colorMap.put(SHELF_BACKGROUND, new Color(0xEDF2F5));
        colorMap.put(SELECTION_COLOR, new Color(0x00CC2F));
        colorMap.put(NEW_NODE_COLOR, Color.BLACK);
    }

}
