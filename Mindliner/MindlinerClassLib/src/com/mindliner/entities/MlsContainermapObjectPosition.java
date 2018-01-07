/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this MAP file, choose Tools | Templates
 * and open the MAP in the editor.
 */
package com.mindliner.entities;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
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
 * For each node of a container map there exists a MlsContainermapObjectPosition
 * entry in the container map. It simply specifies where on the map the node resides.
 * @author Dominic Plangger
 */
@Entity
@Table(name = "objects_containermap_positions")
@NamedQueries({
        @NamedQuery(name = "MlsContainermapObjectPosition.getObjectPositions", query = "SELECT p FROM MlsContainermapObjectPosition p WHERE p.object.id = :oId")})
public class MlsContainermapObjectPosition implements Serializable {
    
    private int id;
    private mlsObject object;
    private MlsContainerMap map;
    private int posX;
    private int posY;
    
    
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
    @JoinColumn(name = "CONTAINER_MAP_ID", referencedColumnName = "ID")
    public MlsContainerMap getContainerMap() {
        return map;
    }

    public void setContainerMap(MlsContainerMap map) {
        this.map = map;
    }
    

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "OBJECT_ID", referencedColumnName = "ID")
    public mlsObject getObject() {
        return object;
    }

    public void setObject(mlsObject object) {
        this.object = object;
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
}
