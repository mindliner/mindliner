/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.objects.transfer;

import com.mindliner.entities.MlsContainermapObjectLink;
import com.mindliner.enums.Position;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dominic Plangger
 */
public class MltContainermapObjectLink implements Serializable {
    
    private int sourceObjId;
    private int targetObjId;
    private boolean isOneWay;
    private double srcOffset, targetOffset;
    private Position srcPosition, targetPosition;
    private double center;
    private double labelPosition;
    private String label;

    public MltContainermapObjectLink() {
    }
    
    public MltContainermapObjectLink(MlsContainermapObjectLink link) {
        if (link == null || link.getSourceObject() == null ||link.getTargetObject() == null) {
            Logger.getLogger(MltContainermapObjectLink.class.getName()).log(Level.SEVERE, "Link {0} has null source/target", (link == null ? " null " : link.getId()));
        }
        this.sourceObjId = link.getSourceObject().getId();
        this.targetObjId = link.getTargetObject().getId();
        this.isOneWay = link.isOneWay();
        this.srcOffset = link.getSrcOffset();
        this.targetOffset = link.getTargetOffset();
        this.srcPosition = link.getSrcPosition();
        this.targetPosition = link.getTargetPosition();
        this.center = link.getCenter();
        this.labelPosition = link.getLabelPosition();
        this.label = link.getLabel();
    }

    public int getSourceObjId() {
        return sourceObjId;
    }

    public int getTargetObjId() {
        return targetObjId;
    }

    public boolean isIsOneWay() {
        return isOneWay;
    }

    public double getSrcOffset() {
        return srcOffset;
    }

    public double getTargetOffset() {
        return targetOffset;
    }

    public Position getSrcPosition() {
        return srcPosition;
    }

    public Position getTargetPosition() {
        return targetPosition;
    }

    public double getCenter() {
        return center;
    }

    public double getLabelPosition() {
        return labelPosition;
    }

    public String getLabel() {
        return label;
    }
    
    public void setSourceObjId(int sourceObjId) {
        this.sourceObjId = sourceObjId;
    }

    public void setTargetObjId(int targetObjId) {
        this.targetObjId = targetObjId;
    }

    public void setIsOneWay(boolean isOneWay) {
        this.isOneWay = isOneWay;
    }

    public void setSrcOffset(double srcOffset) {
        this.srcOffset = srcOffset;
    }

    public void setTargetOffset(double targetOffset) {
        this.targetOffset = targetOffset;
    }

    public void setSrcPosition(Position srcPosition) {
        this.srcPosition = srcPosition;
    }

    public void setTargetPosition(Position targetPosition) {
        this.targetPosition = targetPosition;
    }

    public void setCenter(double center) {
        this.center = center;
    }

    public void setLabelPosition(double labelPosition) {
        this.labelPosition = labelPosition;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    
}
