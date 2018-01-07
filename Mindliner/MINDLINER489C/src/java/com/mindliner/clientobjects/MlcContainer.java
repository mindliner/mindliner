/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.clientobjects;

import com.mindliner.enums.CMContainerStrokeStyles;
import java.io.Serializable;

/**
 *
 * @author Dominic Plangger
 */
public class MlcContainer extends mlcObject implements Serializable {

    private int posX;
    private int posY;
    private int width;
    private int height;
    private double opacity;
    private double strokeWidth;
    private CMContainerStrokeStyles strokeStyle;
    private String color;

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public double getOpacity() {
        return opacity;
    }

    public void setOpacity(double opacity) {
        this.opacity = opacity;
    }

    public double getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(double strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public CMContainerStrokeStyles getStrokeStyle() {
        return strokeStyle;
    }

    public void setStrokeStyle(CMContainerStrokeStyles strokeStyle) {
        this.strokeStyle = strokeStyle;
    }
    
    
}
