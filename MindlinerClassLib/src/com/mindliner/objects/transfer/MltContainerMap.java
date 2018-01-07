/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.objects.transfer;

import com.mindliner.entities.MlsContainerMap;
import com.mindliner.entities.MlsContainermapObjectLink;
import com.mindliner.entities.MlsContainermapObjectPosition;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dominic Plangger
 */
public class MltContainerMap extends MltObject {

    private final List<MltContainermapObjectPosition> objPositions = new ArrayList<>();
    private final List<MltContainermapObjectLink> objLinks = new ArrayList<>();

    public MltContainerMap(MlsContainerMap map) {
        super(map);
        if (map.getObjectPositions() != null) {
            for (MlsContainermapObjectPosition pos : map.getObjectPositions()) {
                objPositions.add(new MltContainermapObjectPosition(pos));
            }
        }
        if (map.getObjectLinks() != null) {
            for (MlsContainermapObjectLink link : map.getObjectLinks()) {
                if (link.getSourceObject() == null || link.getTargetObject() == null) {
                    System.err.println("ignoring broken container map link id " + link.getId() + " with one or both sides missing");
                } else {
                    objLinks.add(new MltContainermapObjectLink(link));
                }
            }
        }
    }

    public List<MltContainermapObjectPosition> getObjPositions() {
        return objPositions;
    }

    public List<MltContainermapObjectLink> getObjLinks() {
        return objLinks;
    }
}
