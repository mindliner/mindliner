/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands;

import com.mindliner.clientobjects.MlcContainer;
import com.mindliner.clientobjects.MlcContainerMap;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.managers.ContainerMapManagerRemote;
import com.mindliner.serveraccess.RemoteLookupAgent;
import com.mindliner.view.containermap.ContainerMapElement;
import com.mindliner.view.containermap.FXMLController;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.Node;
import javax.naming.NamingException;

/**
 *
 * @author Dominic Plangger
 */
public class CMAddCommand extends MindlinerOnlineCommand {

    private final MlcContainerMap map;
    private final mlcObject obj;
    private final List<Integer> toAdd;
    private final int x;
    private final int y;

    public CMAddCommand(MlcContainerMap map, mlcObject obj, List<Integer> toAdd, int x, int y) {
        super(obj, false);
        if (map == null || obj == null) {
            throw new IllegalArgumentException("Map or object must not be null");
        }

        this.map = map;
        this.obj = obj;
        this.x = x;
        this.y = y;
        this.toAdd = toAdd;
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        if (!isExecuted()) {
            super.execute();
            CommandRecorder cr = CommandRecorder.getInstance();
            List<Integer> toAddReal = new ArrayList<>();
            for (int id : toAdd) {
                int newId = cr.mapId(id);
                toAddReal.add(newId);
            }
            ContainerMapManagerRemote cmmr = (ContainerMapManagerRemote) RemoteLookupAgent.getManagerForClass(ContainerMapManagerRemote.class);
            cmmr.addNode(map.getId(), getObject().getId(), x, y, toAddReal);
            setExecuted(true);
        }
    }

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        if (isExecuted()) {
            super.undo();
            ContainerMapManagerRemote cmmr = (ContainerMapManagerRemote) RemoteLookupAgent.getManagerForClass(ContainerMapManagerRemote.class);
            cmmr.deleteNode(map.getId(), obj.getId());
            ContainerMapElement elem = FXMLController.getInstance().findMapElement(obj.getId());
            if (elem == null) {
                Logger.getLogger(CMAddCommand.class.getName()).log(Level.WARNING, "Object {0} does not exist in containermap", obj.getId());
                return;
            }
            Platform.runLater(() -> {FXMLController.getInstance().getNodeGroup().getChildren().remove((Node) elem);});
        }
    }

    @Override
    public boolean isVersionChecking() {
        return false;
    }

    @Override
    public String toString() {
        return "Node Creation (" + getFormattedId() + ")";
    }

}
