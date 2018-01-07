/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.objects.transfer;

import com.mindliner.categories.MlsEventType;
import com.mindliner.entities.mlsLog;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Marius Mesesrli
 */
public class MltLog implements Serializable {

    private static final long serialVersionUID = 19640205L;

    private int id;
    private String headline;
    private String description;
    private int objectId;
    private int linkObjectId;
    private int userId;
    private int dataPoolId;
    private Date time;
    private mlsLog.Type type;
    private MlsEventType.EventType eventType;

    public MltLog(mlsLog log) {
        id = log.getId();
        headline = log.getHeadline();
        description = log.getDescription();
        objectId = log.getObjectId();
        linkObjectId = log.getLinkObjectId();
        userId = log.getUser().getId();
        dataPoolId = log.getDataPool().getId();
        time = log.getTime();
        type = log.getType();
        eventType = log.getEventType();
    }

    public int getId() {
        return id;
    }

    public String getHeadline() {
        return headline;
    }

    public String getDescription() {
        return description;
    }

    public int getObjectId() {
        return objectId;
    }

    public int getLinkObjectId() {
        return linkObjectId;
    }

    public int getUserId() {
        return userId;
    }

    public int getDataPoolId() {
        return dataPoolId;
    }

    public Date getTime() {
        return time;
    }

    public mlsLog.Type getType() {
        return type;
    }

    public MlsEventType.EventType getEventType() {
        return eventType;
    }

}
