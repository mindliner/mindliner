/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.clientobjects;

import com.mindliner.enums.LinkRelativeType;
import com.mindliner.objects.transfer.MltLink;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Dominic Plangger
 */
public class MlcLink implements Serializable {

    private int id = -1;
    private int holderId = -1;
    private int relativeId = -1;
    private String label = "";
    private mlcUser owner = null;
    private Date creationDate = new Date();
    private Date modificationDate = new Date();
    private mlcClient client = null;
    private LinkRelativeType relativeType = LinkRelativeType.OBJECT;
    private boolean isOneWay;
    private int relativeListPosition = 0;
    private static final long serialVersionUID = 19640205L;

    public MlcLink(MltLink link, mlcUser owner, mlcClient client) {
        id = link.getId();
        holderId = link.getHolderId();
        relativeId = link.getRelativeId();
        label = link.getLabel();
        creationDate = link.getCreationDate();
        modificationDate = link.getModificationDate();
        relativeType = link.getRelativeType();
        isOneWay = link.isIsOneWay();
        this.owner = owner;
        this.client = client;
        this.relativeListPosition = link.getRelativeListPosition();
    }

    public MlcLink(mlcObject origin, mlcObject target, mlcUser owner, mlcClient client) {
        holderId = origin.getId();
        relativeId = target.getId();
        this.client = client;
        this.owner = owner;
        relativeType = LinkRelativeType.OBJECT;
        isOneWay = false;
    }

    public boolean isIsOneWay() {
        return isOneWay;
    }

    public void setIsOneWay(boolean isOneWay) {
        this.isOneWay = isOneWay;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHolderId() {
        return holderId;
    }

    public void setHolderId(int holderId) {
        this.holderId = holderId;
    }

    public int getRelativeId() {
        return relativeId;
    }

    public void setRelativeId(int relativeId) {
        this.relativeId = relativeId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public mlcUser getOwner() {
        return owner;
    }

    public void setOwner(mlcUser owner) {
        this.owner = owner;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    public mlcClient getClient() {
        return client;
    }

    public void setClient(mlcClient client) {
        this.client = client;
    }

    public LinkRelativeType getRelativeType() {
        return relativeType;
    }

    public void setRelativeType(LinkRelativeType relativeType) {
        this.relativeType = relativeType;
    }

    public int getRelativeListPosition() {
        return relativeListPosition;
    }

    public void setRelativeListPosition(int relativeListPosition) {
        this.relativeListPosition = relativeListPosition;
    }

    @Override
    public String toString() {
        return holderId + " to " + relativeId + " (id: " + id + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MlcLink other = (MlcLink) obj;
        if (getRelativeId() != other.getRelativeId() || getHolderId() != other.getHolderId()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.id;
        return hash;
    }
}
