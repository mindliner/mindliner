/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.entities;

import java.io.Serializable;
import javax.persistence.CascadeType;
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
 *
 * @author Ming
 */
@Entity
@Table(name = "worksphere_mapnodes")
@NamedQueries({
    @NamedQuery(name = "mlsWorksphereMapNode.getNodesByMapId", query = "SELECT n FROM mlsWorksphereMapNode n WHERE n.map.id = :mapId")
})
public class mlsWorksphereMapNode implements Serializable {
    
    private int id;
    private mlsObject object = null;
    private mlsObject map = null;
    private int positionX = 0;
    private int positionY = 0;
    private int height = 0;
    private int width = 0;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "OBJECT_ID", referencedColumnName = "ID")
    public mlsObject getObject() {
        return object;
    }

    public void setObject(mlsObject object) {
        this.object = object;
    }

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "MAP_ID", referencedColumnName = "ID")
    public mlsObject getMap() {
        return map;
    }

    public void setMap(mlsObject map) {
        this.map = map;
    }

    @Column(name = "POSITION_X")
    public int getPositionX() {
        return positionX;
    }

    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }

    @Column(name = "POSITION_Y")
    public int getPositionY() {
        return positionY;
    }

    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }

    @Column(name = "HEIGHT")
    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Column(name = "WIDTH")
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}
