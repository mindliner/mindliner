/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.objects.transfer;

import com.mindliner.entities.mlsWorksphereMapNode;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Ming
 */
public class mltWorksphereMapNode implements Serializable {
    
    private int id = 0;
    private int objectId = 0;
    private int mapId = 0;
    private int positionX = 0;
    private int positionY = 0;
    private int height = 0;
    private int width = 0;
    
    public mltWorksphereMapNode() {
    }
    
    public mltWorksphereMapNode(mlsWorksphereMapNode node) {
        this.id = node.getId();
        this.objectId = node.getObject().getId();
        this.mapId = node.getMap().getId();
        this.positionX = node.getPositionX();
        this.positionY = node.getPositionY();
        this.height = node.getHeight();
        this.width = node.getWidth();
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getObjectId() {
        return objectId;
    }

    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    public int getPositionX() {
        return positionX;
    }

    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}
