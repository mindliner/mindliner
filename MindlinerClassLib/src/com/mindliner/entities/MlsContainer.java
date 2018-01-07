/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.entities;

import com.mindliner.enums.CMContainerStrokeStyles;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

/**
 *
 * @author Dominic Plangger
 */
@Entity
@Table(name = "containers")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorValue(value = "CNTR")
public class MlsContainer extends mlsObject implements Serializable {

    private int posX;
    private int posY;
    private int width;
    private int height;
    private double strokeWidth = 1;
    private double fill = 0;
    private CMContainerStrokeStyles strokeStyle = CMContainerStrokeStyles.SOLID;
    private String color;

    
    @Column(name = "STROKE_STYLE")
    @Enumerated(EnumType.STRING)
    public CMContainerStrokeStyles getStrokeStyle() {
        return strokeStyle;
    }

    public void setStrokeStyle(CMContainerStrokeStyles strokeStyle) {
        this.strokeStyle = strokeStyle;
    }
    
    @Column(name = "STROKE_WIDTH")
    public double getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(double strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    @Column(name = "FILL")
    public double getFill() {
        return fill;
    }

    public void setFill(double fill) {
        this.fill = fill;
    }
    
    @Column(name = "POS_X")
    public int getPositionX() {
        return posX;
    }

    public void setPositionX(int posX) {
        this.posX = posX;
    }

    @Column(name = "POS_Y")
    public int getPositionY() {
        return posY;
    }

    public void setPositionY(int posY) {
        this.posY = posY;
    }

    @Column(name = "WIDTH")
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Column(name = "HEIGHT")
    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Column(name = "COLOR")
    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

}
