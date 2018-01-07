/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.background;

import com.mindliner.entities.Colorizer;
import com.mindliner.gui.color.BaseColorizer;
import com.mindliner.gui.color.ColorManager;
import com.mindliner.gui.color.FixedKeyColorizer;
import java.awt.Color;
import java.awt.Graphics2D;
import javax.swing.JPanel;

/**
 *
 * @author M.Messerli
 */
public class DefaultBackgroundPainter implements BackgroundPainter {

    protected Color background = Color.lightGray;
    protected int canvasSize = 3000;

    public static enum BackgroundPainterType {

        SingleColor, Image
    }

    @Override
    public void setBackground(Color bg) {
        background = bg;
    }

    @Override
    public void paint(Graphics2D g2, JPanel panel) {
        g2.setBackground(background);
        g2.clearRect(0, 0, canvasSize, canvasSize);
    }

    @Override
    public void initialize() {
        BaseColorizer fkc = ColorManager.getColorizerForType(Colorizer.ColorDriverAttribute.FixedKey);
        background = fkc.getColorForObject(FixedKeyColorizer.FixedKeys.MAP_BACKGROUND);
    }

    @Override
    public Color getBackground() {
        return background;
    }

    @Override
    public boolean isDark() {
        float[] hsb = Color.RGBtoHSB(background.getRed(), background.getGreen(), background.getBlue(), null);
        return hsb[2] < (float) (ColorManager.DARKNESS_THRESHOLD / 255);
    }

}
