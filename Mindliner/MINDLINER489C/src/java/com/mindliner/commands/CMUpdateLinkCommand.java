/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands;

import com.mindliner.clientobjects.MlcContainerMap;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.managers.ContainerMapManagerRemote;
import com.mindliner.objects.transfer.MltContainermapObjectLink;
import com.mindliner.serveraccess.RemoteLookupAgent;
import javax.naming.NamingException;

/**
 * Either deletes or adds a container map link. Is also used for updating an existing link
 * @author Dominic Plangger
 */
public class CMUpdateLinkCommand  extends MindlinerOnlineCommand{
    
    private final boolean isAdding;
    private final MlcContainerMap map;
    private final MltContainermapObjectLink link;
    private MltContainermapObjectLink oldLink = null;

    public CMUpdateLinkCommand(MlcContainerMap map, MltContainermapObjectLink link, boolean isAdding) {
        super(map, true);
        
        this.isAdding = isAdding;
        this.map = map;
        this.link = link;
    }
    
    
    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        if (!isExecuted()) {
            super.execute();
            CommandRecorder cr = CommandRecorder.getInstance();
            // maybe link was created in offline mode
            int srcId = cr.mapId(link.getSourceObjId());
            int trgId = cr.mapId(link.getTargetObjId());
            link.setSourceObjId(srcId);
            link.setTargetObjId(trgId);
            ContainerMapManagerRemote cmmr = (ContainerMapManagerRemote) RemoteLookupAgent.getManagerForClass(ContainerMapManagerRemote.class);
            if (isAdding) {
                // addLink is also used for updating an existing one
                oldLink = cmmr.addLink(map.getId(), link);
            }
            else {
                oldLink = link;
                cmmr.deleteLink(map.getId(), link.getSourceObjId(), link.getTargetObjId());
            }
            setExecuted(true);
        }
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        if (isExecuted()) {
            super.undo();
            ContainerMapManagerRemote cmmr = (ContainerMapManagerRemote) RemoteLookupAgent.getManagerForClass(ContainerMapManagerRemote.class);
            if (oldLink == null) {
                // means that a link create needs to be undone
                cmmr.deleteLink(map.getId(), link.getSourceObjId(), link.getTargetObjId());
            }
            else {
                // means that either a link update oder link delete needs to be undone
                cmmr.addLink(map.getId(), oldLink);
            }
        }
    }
    

    @Override
    public boolean isVersionChecking() {
        return false;
    }
    
    @Override
    public boolean equals(Object obj) {
        boolean equal = super.equals(obj);
        if (equal) {
            CMUpdateLinkCommand nobj = (CMUpdateLinkCommand) obj;
            return this.link.equals(nobj.link);
        }
        else {
            return false;
        }
    }
    
    @Override
    public String toString() {
        return "Linking";
    }
    
}
