/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author Dominic Plangger
 */
@Entity
@Table(name = "containermaps")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorValue(value = "CMAP")
public class MlsContainerMap extends mlsObject implements Serializable {

    public MlsContainerMap() {
    }

    private List<MlsContainermapObjectPosition> objectPositions = new ArrayList<>();
    private List<MlsContainermapObjectLink> objectLinks = new ArrayList<>();

    @OneToMany(mappedBy = "containerMap", targetEntity = MlsContainermapObjectPosition.class)
    public List<MlsContainermapObjectPosition> getObjectPositions() {
        return objectPositions;
    }

    public void setObjectPositions(List<MlsContainermapObjectPosition> objectPositions) {
        this.objectPositions = objectPositions;
    }

    @OneToMany(mappedBy = "containerMap", targetEntity = MlsContainermapObjectLink.class)
    public List<MlsContainermapObjectLink> getObjectLinks() {
        return objectLinks;
    }

    public void setObjectLinks(List<MlsContainermapObjectLink> links) {
        this.objectLinks = links;
    }

}
