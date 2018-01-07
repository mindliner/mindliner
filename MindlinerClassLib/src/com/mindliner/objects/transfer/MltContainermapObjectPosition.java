/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.objects.transfer;

import com.mindliner.entities.MlsContainermapObjectPosition;
import java.io.Serializable;

/**
 *
 * @author Dominic Plangger
 */
public class MltContainermapObjectPosition implements Serializable {
    
    private final int objectId;
    private final int posX;
    private final int posY;

    public MltContainermapObjectPosition(MlsContainermapObjectPosition objPos) {
        if (objPos.getObject() == null) {
            this.objectId = -1;
        }
        else {
            this.objectId = objPos.getObject().getId();
        }
        this.posX = objPos.getPositionX();
        this.posY = objPos.getPositionY();
    }

    public int getObjectId() {
        return objectId;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }
    
    
    
}
