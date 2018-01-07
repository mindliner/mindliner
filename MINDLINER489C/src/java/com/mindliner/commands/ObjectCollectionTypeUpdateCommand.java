/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.commands;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcObjectCollection;
import com.mindliner.enums.ObjectCollectionType;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.main.BulkUpdater;
import com.mindliner.managers.ObjectManagerRemote;
import com.mindliner.serveraccess.RemoteLookupAgent;
import javax.naming.NamingException;

/**
 *
 * @author Ming
 */
public class ObjectCollectionTypeUpdateCommand extends MindlinerOnlineCommand {
    private ObjectCollectionType type = null;

    /**
     *
     * @param object
     * @param type
     */
    public ObjectCollectionTypeUpdateCommand(mlcObject object, ObjectCollectionType type) {
        super(object, true);
        this.type = type;
        if (!(object instanceof mlcObjectCollection)){
            throw new IllegalArgumentException("This commands needs a mlcObjectCollection as object");
        }
        ((mlcObjectCollection) object).setType(type);
        BulkUpdater.publishUpdate(object);
    }
    
    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        super.execute();
        ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
        int version = omr.setCollectionType(getObject().getId(), type);
        getObject().setVersion(version);
        setExecuted(true);
    }

    @Override
    public boolean isVersionChecking() {
        return false;
    }
    
    @Override
    public String toString() {
        return "Collection Type Update (" + getFormattedId() + ")";
    }

    @Override
    public String getDetails() {
        return "Collection Type: " + type;
    }
    
}
