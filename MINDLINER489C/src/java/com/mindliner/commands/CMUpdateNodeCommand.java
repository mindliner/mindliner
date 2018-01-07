/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands;

import com.mindliner.clientobjects.MlcContainerMap;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.managers.ContainerMapManagerRemote;
import com.mindliner.serveraccess.RemoteLookupAgent;
import java.util.ArrayList;
import java.util.List;
import javax.naming.NamingException;

/**
 *
 * @author Dominic Plangger
 */
public class CMUpdateNodeCommand extends MindlinerOnlineCommand {

    private final List<Integer> toDelete = new ArrayList<>();
    private final List<Integer> toAdd = new ArrayList<>();
    private final List<Integer> toAddTransformedIds = new ArrayList<>();
    private final List<Integer> toDeleteTransformedIds = new ArrayList<>();
    private mlcObject obj;
    private MlcContainerMap map;
    private final int newX;
    private final int oldX;
    private final int newY;
    private final int oldY;
    transient private CommandRecorder cr;

    public CMUpdateNodeCommand(mlcObject obj, MlcContainerMap map, List<mlcObject> toDelete, List<mlcObject> toAdd, int newX, int newY, int oldX, int oldY) {
        super(obj, true);
        if (obj == null || map == null) {
            throw new IllegalArgumentException("source object nor map can be null");
        }
        this.obj = obj;
        this.map = map;
        this.newX = newX;
        this.oldX = oldX;
        this.newY = newY;
        this.oldY = oldY;
        if (toDelete != null) {
            toDelete.stream().forEach((del) -> {
                this.toDelete.add(del.getId());
            });
        }
        if (toAdd != null) {
            toAdd.stream().forEach((add) -> {
                this.toAdd.add(add.getId());
            });
        }
        cr = CommandRecorder.getInstance();
    }

    @Override
    public boolean isVersionChecking() {
        return false;
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        if (!isExecuted()) {
            super.execute();
            // Convert any temporary IDs to real IDs
            toAdd.stream().forEach((id) -> { toAddTransformedIds.add(cr.mapId(id));});
            toDelete.stream().forEach((id) -> { toDeleteTransformedIds.add(cr.mapId(id));});
            
            ContainerMapManagerRemote cmmr = (ContainerMapManagerRemote) RemoteLookupAgent.getManagerForClass(ContainerMapManagerRemote.class);
            cmmr.updateNode(map.getId(), getObject().getId(), newX, newY, toAddTransformedIds, toDeleteTransformedIds);
            setExecuted(true);
        }
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        super.undo();
        if (isExecuted()) {
            ContainerMapManagerRemote cmmr = (ContainerMapManagerRemote) RemoteLookupAgent.getManagerForClass(ContainerMapManagerRemote.class);
            cmmr.updateNode(map.getId(), getObject().getId(), oldX, oldY, toDeleteTransformedIds, toAddTransformedIds);
            setUndone(true);
        }
    }

    @Override
    public String toString() {
        return "Node Update (" + getFormattedId() + ")";
    }

    @Override
    public String getDetails() {
        return "new X = " + newX + ", new y = " + newY;
    }

    @Override
    public boolean equals(Object obj) {
        boolean equal = super.equals(obj);
        if (equal) {
            CMUpdateNodeCommand nobj = (CMUpdateNodeCommand) obj;
            return this.newX == nobj.newX && this.newY == nobj.newY;
        }
        else {
            return false;
        }
    }
}
