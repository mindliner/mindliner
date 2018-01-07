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
import com.mindliner.view.containermap.FXMLController;
import com.mindliner.view.containermap.MapContainer;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javax.naming.NamingException;

/**
 *
 * @author Dominic Plangger
 */
public class CMDeleteCommand extends MindlinerOnlineCommand {
    
    private final MlcContainerMap map;
    private List<Integer> intrs;
    private double layoutX;
    private double layoutY;

    public CMDeleteCommand(mlcObject obj, MlcContainerMap map, double layoutX, double layoutY, List<MapContainer> intrs) {
        super(obj, true);
        if (obj == null || map == null) {
            throw new IllegalArgumentException("Map or object must not be null");
        }
        this.map = map;
        this.layoutY = layoutY;
        this.layoutX = layoutX;
        this.intrs = new ArrayList<>();
        intrs.stream().forEach((c) -> { this.intrs.add(c.getObject().getId()); });
        if (intrs.isEmpty()) {
            this.intrs.add(map.getId());
        }
    }

    
    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        if (!isExecuted()) {
            super.execute();
            ContainerMapManagerRemote cmmr = (ContainerMapManagerRemote) RemoteLookupAgent.getManagerForClass(ContainerMapManagerRemote.class);
            cmmr.deleteNode(map.getId(), getObject().getId());
            setExecuted(true);
        }
    }
    

    @Override
    public void undo() throws mlModifiedException, NamingException, UndoAlreadyUndoneException, MlAuthorizationException {
        if (isExecuted()) {
            super.undo();
            CMAddCommand cmd = new CMAddCommand(map, getObject(), intrs, (int) layoutX, (int) layoutY);
            FXMLController controller = FXMLController.getInstance();
            controller.addCommand(cmd, true);
            Platform.runLater(() -> {controller.displayObject(getObject(), layoutX, layoutX);});
        }
    }

    @Override
    public boolean isVersionChecking() {
        return false;
    }
    
    @Override
    public String toString() {
        return "Node Deletion (" + getFormattedId() + ")";
    }
    
}
