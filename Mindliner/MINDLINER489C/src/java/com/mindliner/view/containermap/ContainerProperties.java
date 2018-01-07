/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.containermap;

import com.mindliner.entities.Colorizer;
import com.mindliner.enums.CMContainerStrokeStyles;
import com.mindliner.gui.color.BaseColorizer;
import com.mindliner.gui.color.ColorManager;
import com.mindliner.gui.color.FixedKeyColorizer;
import javafx.scene.paint.Color;

/**
 *
 * @author Dominic Plangger
 */
public class ContainerProperties {

    private FixedKeyColorizer.FixedKeys key;
    private String label;
    BaseColorizer fixedKeyColorizer;
    private double opacity = 0;
    private double strokeWidth = 1;
    private String webColor = null;
    private CMContainerStrokeStyles strokeStyle = CMContainerStrokeStyles.SOLID;

    public ContainerProperties(FixedKeyColorizer.FixedKeys key, String label) {
        this.key = key;
        this.label = label;
        fixedKeyColorizer = ColorManager.getColorizerForType(Colorizer.ColorDriverAttribute.FixedKey);
    }


    // keep colors dynamic in case the user changes the colors during a session
    public Color getColor() {
        return ContainerMapUtils.convertAwtToFx(fixedKeyColorizer.getColorForObject(key));
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    
    
    public void setColor(Color c) {
        java.awt.Color awtColor = new java.awt.Color((float)c.getRed(), (float)c.getGreen(), (float)c.getBlue(), (float)c.getOpacity());
        fixedKeyColorizer.setColor(key, awtColor);
    }
    
    public void setColorSilent(String c) {
        webColor = c;
    }
    
    public String getColorSilent() {
        return webColor;
    }

    public void setOpacity(double opacity) {
        this.opacity = opacity;
    }

    public void setStrokeWidth(double strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public void setStrokeStyle(CMContainerStrokeStyles strokeStyle) {
        this.strokeStyle = strokeStyle;
    }

    public double getOpacity() {
        return opacity;
    }

    public double getStrokeWidth() {
        return strokeWidth;
    }

    public CMContainerStrokeStyles getStrokeStyle() {
        return strokeStyle;
    }
    
}
