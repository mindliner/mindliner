/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.objects.transfer;

import com.mindliner.entities.Synchunit;
import java.io.Serializable;
import java.util.Date;

/**
 * The transfer object (data object) for Synchunit.
 *
 * @author Marius Messerli
 */
public class mltSynchunit implements Serializable {

    private Integer id = Synchunit.UNPERSISTED_ID;
    private int mindlinerObjectId = -1;
    private String foreignObjectId;
    private Date lastSynched;
    private int syncherId;
    private static final long serialVersionUID = 19640205L;

    public mltSynchunit(Synchunit s) {
        id = s.getId();
        if (s.getMindlinerObject() != null) {
            mindlinerObjectId = s.getMindlinerObject().getId();
        }
        foreignObjectId = s.getForeignObjectId();
        lastSynched = s.getLastSynched();
        syncherId = s.getSyncher().getId();
    }

    public mltSynchunit(int mindlinerObjectId, String foreignObjectId, Date lastSynched) {
        this.mindlinerObjectId = mindlinerObjectId;
        this.foreignObjectId = foreignObjectId;
        this.lastSynched = lastSynched;
    }

    public Integer getId() {
        return id;
    }

    public int getMindlinerObjectId() {
        return mindlinerObjectId;
    }

    public String getForeignObjectId() {
        return foreignObjectId;
    }

    public Date getLastSynched() {
        return lastSynched;
    }

    public void setMindlinerObjectId(int mindlinerObjectId) {
        this.mindlinerObjectId = mindlinerObjectId;
    }

    public void setForeignObjectId(String foreignObjectId) {
        this.foreignObjectId = foreignObjectId;
    }

    public void setLastSynched(Date lastSynched) {
        this.lastSynched = lastSynched;
    }

    public int getSyncherId() {
        return syncherId;
    }

}
