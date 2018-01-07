/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.containermap;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.MlcContainer;
import com.mindliner.clientobjects.MlcLink;
import com.mindliner.clientobjects.MlcContainerMap;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.enums.LinkRelativeType;
import com.mindliner.main.SearchPanel;
import com.mindliner.objects.transfer.MltContainermapObjectLink;
import com.mindliner.objects.transfer.MltContainermapObjectPosition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.concurrent.Task;

/**
 * Background thread to display a new container map. Loads all the
 * releveant childrens of the container map and continuously displays them
 * with the javafx thread
 * @author Dominic Plangger
 */
public class ContainerMapDisplayer extends Task<Void>{
    
    private final FXMLController controller;
    private final MlcContainerMap root;

    public ContainerMapDisplayer(FXMLController controller, MlcContainerMap root) {
        this.controller = controller;
        this.root = root;
    }

    
    @Override
    protected Void call() throws Exception {
        Platform.runLater(() -> {
            controller.init(root);
        });
        final Map<Integer, MltContainermapObjectPosition> positions = new HashMap<>();
        for (MltContainermapObjectPosition pos : root.getObjectPositions()) {
            positions.put(pos.getObjectId(), pos);
        }
        
        // LOAD CONTAINERS
        List<mlcObject> relatives = CacheEngineStatic.getLinkedObjects(root);
        final List<MlcContainer> containers = new ArrayList<>();
        for (mlcObject relative : relatives) {
            MlcLink link = CacheEngineStatic.getLink(root.getId(), relative.getId());
            // We only display links of type TEMPLATE_NODE in the template 
            // (such that the user can link the template root object with other objects in the mindmap without changing the template itself)
            if (LinkRelativeType.CONTAINER_MAP.equals(link.getRelativeType())) {
                if (relative instanceof MlcContainer) {
                    containers.add((MlcContainer) relative);
                }
            }
        }
        
        Platform.runLater(() -> {
            for (MlcContainer container : containers) {
                controller.displayContainer(container);
            }
        });
        
        // LOAD NODES & LINKS
        List<mlcObject> elements = SearchPanel.filterObjects(CacheEngineStatic.getObjects(new ArrayList<>(positions.keySet())));
        final List<mlcObject> felements = SearchPanel.filterObjects(elements);
        Platform.runLater(() -> {
            // display nodes
            for (mlcObject element : felements) {
                MltContainermapObjectPosition pos = positions.get(element.getId());
                controller.displayObject(element, pos.getPosX(), pos.getPosY());
            }
            // display links
            for (MltContainermapObjectLink link : root.getObjLinks()) {
                controller.displayLink(link);
            }
        });
        
        return null;
    }

}
