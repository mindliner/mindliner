/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.objects.transfer;

import com.mindliner.entities.MlsLink;
import com.mindliner.entities.mlsUser;
import com.mindliner.enums.LinkRelativeType;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Dominic Plangger
 */
public class MltLink implements Serializable {

    private final int id;
    private final int holderId;
    private final int relativeId;
    private final String label;
    private final int ownerId;
    private final Date creationDate;
    private final Date modificationDate;
    private final int clientId;
    private final LinkRelativeType relativeType;
    private boolean isOneWay;
    private int relativeListPosition = 0;
    private static final long serialVersionUID = 19640205L;

    public MltLink(MlsLink link) {
        id = link.getId();
        holderId = link.getHolderId();
        relativeId = link.getRelativeId();
        label = link.getLabel();
        mlsUser u = link.getOwner();
        ownerId = u.getId();
        creationDate = link.getCreationDate();
        modificationDate = link.getModificationDate();
        clientId = link.getClient().getId();
        relativeType = link.getRelativeType();
        relativeListPosition = link.getRelativeListPosition();
    }

    public int getId() {
        return id;
    }

    public int getHolderId() {
        return holderId;
    }

    public int getRelativeId() {
        return relativeId;
    }

    public String getLabel() {
        return label;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Date getModificationDate() {
        return modificationDate;
    }

    public int getClientId() {
        return clientId;
    }

    public LinkRelativeType getRelativeType() {
        return relativeType;
    }

    public boolean isIsOneWay() {
        return isOneWay;
    }

    public void setIsOneWay(boolean isOneWay) {
        this.isOneWay = isOneWay;
    }

    public int getRelativeListPosition() {
        return relativeListPosition;
    }

}
