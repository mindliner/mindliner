/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.entities;

import com.mindliner.enums.Position;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 *
 * @author Dominic Plangger
 */
@Entity
@Table(name = "objects_containermap_links")
@NamedQueries({
        @NamedQuery(name = "MlsContainermapObjectLink.getObjectLinks", query = "SELECT l FROM MlsContainermapObjectLink l WHERE l.sourceObject.id = :oId OR l.targetObject.id = :oId")})
public class MlsContainermapObjectLink implements Serializable {
    
    private int id;
    private mlsObject srcObject;
    private mlsObject targetObject;
    private MlsContainerMap map;
    private boolean oneWay;
    private double center;
    private double srcOffset;
    private double targetOffset;
    private double labelPosition;
    private Position srcPosition;
    private Position targetPosition;
    private String label;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "SOURCE_OBJ_ID", referencedColumnName = "ID")
    public mlsObject getSourceObject() {
        return srcObject;
    }

    public void setSourceObject(mlsObject srcObject) {
        this.srcObject = srcObject;
    }
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "TARGET_OBJ_ID", referencedColumnName = "ID")
    public mlsObject getTargetObject() {
        return targetObject;
    }

    public void setTargetObject(mlsObject targetObject) {
        this.targetObject = targetObject;
    }

    @Column(name = "ONE_WAY")
    public boolean isOneWay() {
        return oneWay;
    }

    public void setIsOneWay(boolean isOneWay) {
        this.oneWay = isOneWay;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CONTAINER_MAP_ID", referencedColumnName = "ID")
    public MlsContainerMap getContainerMap() {
        return map;
    }

    public void setContainerMap(MlsContainerMap map) {
        this.map = map;
    }

    @Column(name = "CENTER")
    public double getCenter() {
        return center;
    }

    @Column(name = "LABEL_POSITION")
    public double getLabelPosition() {
        return labelPosition;
    }

    @Column(name = "SOURCE_OFFSET")
    public double getSrcOffset() {
        return srcOffset;
    }

    @Column(name = "SOURCE_POSITION")
    @Enumerated(EnumType.STRING)
    public Position getSrcPosition() {
        return srcPosition;
    }

    @Column(name = "TARGET_OFFSET")
    public double getTargetOffset() {
        return targetOffset;
    }

    @Column(name = "TARGET_POSITION")
    @Enumerated(EnumType.STRING)
    public Position getTargetPosition() {
        return targetPosition;
    }

    @Column(name = "LABEL")
    public String getLabel() {
        return label;
    }
    
    public void setCenter(double center) {
        this.center = center;
    }

    public void setLabelPosition(double labelPosition) {
        this.labelPosition = labelPosition;
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

    public void setLabel(String label) {
        this.label = label;
    }

}
