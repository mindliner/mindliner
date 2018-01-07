/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.objects.transfer;

import com.mindliner.entities.MlsContainer;
import com.mindliner.enums.CMContainerStrokeStyles;

/**
 *
 * @author Dominic Plangger
 */
public class MltContainer extends MltObject {
    
    private final int posX;
    private final int posY;
    private final int width;
    private final int height;
    private final double strokeWidth;
    private final double fill;
    private final CMContainerStrokeStyles strokeStyle;
    private final String color;

    public MltContainer(MlsContainer container) {
        super(container);
        this.posX = container.getPositionX();
        this.posY = container.getPositionY();
        this.width = container.getWidth();
        this.height = container.getHeight();
        this.color = container.getColor();
        this.fill = container.getFill();
        this.strokeStyle = container.getStrokeStyle();
        this.strokeWidth = container.getStrokeWidth();
    }

    public MltContainer(int posX, int posY, int width, int height, String color, double strokeWidth, double fill, CMContainerStrokeStyles style) {
        this.posX = posX;
        this.posY = posY;
        this.width = width;
        this.height = height;
        this.color = color;
        this.strokeWidth = strokeWidth;
        this.fill = fill;
        this.strokeStyle = style;
    }
    
    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getColor() {
        return color;
    }

    public double getFill() {
        return fill;
    }

    public CMContainerStrokeStyles getStrokeStyle() {
        return strokeStyle;
    }

    public double getStrokeWidth() {
        return strokeWidth;
    }
    
}
