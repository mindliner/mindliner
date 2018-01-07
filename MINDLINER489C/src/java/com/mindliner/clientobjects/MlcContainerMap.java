/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.clientobjects;

import com.mindliner.objects.transfer.MltContainermapObjectLink;
import com.mindliner.objects.transfer.MltContainermapObjectPosition;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dominic Plangger
 */
public class MlcContainerMap extends mlcObject implements Serializable {

    private List<MltContainermapObjectPosition> objPositions = new ArrayList<>();

    private List<MltContainermapObjectLink> objLinks = new ArrayList<>();

    public void setObjectPositions(List<MltContainermapObjectPosition> objPositions) {
        this.objPositions = objPositions;
    }

    public List<MltContainermapObjectPosition> getObjectPositions() {
        return objPositions;
    }

    public List<MltContainermapObjectLink> getObjLinks() {
        return objLinks;
    }

    public void setObjLinks(List<MltContainermapObjectLink> objLinks) {
        this.objLinks = objLinks;
    }
}
