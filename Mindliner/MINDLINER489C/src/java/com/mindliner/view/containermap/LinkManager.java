/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.containermap;

import com.mindliner.enums.Position;
import com.mindliner.objects.transfer.MltContainermapObjectLink;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

/**
 * Internal link manager responsible for creating and removing links in the container map.
 * @author Dominic Plangger
 */
public class LinkManager {
    
    private final Group nodeGroup;
    private MapLink currentLink = null;
    private final List<MapLink> links = new ArrayList<>();
    

    public LinkManager(Group parent) {
        this.nodeGroup = parent;
    }

    public void removeLink(MapLink link) {
        links.remove(link);
        nodeGroup.getChildren().remove(link);
    }
    
    public void createLink(Node startNode) {
        if (startNode instanceof ContainerMapElement) {
            currentLink = new MapLink(startNode);
            nodeGroup.getChildren().add(currentLink);
        }
    }
    
    public void createLink(Node startNode, MltContainermapObjectLink link) {
        createLink(startNode);
        currentLink.setStartOffset(link.getSrcOffset());
        currentLink.setStartPosition(link.getSrcPosition());
        currentLink.setCenter(link.getCenter());
        currentLink.setLabel(link.getLabel());
        currentLink.setLabelPosition(link.getLabelPosition());
        currentLink.setIsOneWay(link.isIsOneWay());
    }

    public void updateLinks(List<MltContainermapObjectLink> newLinks) {
        List<MapLink> toDelete = new ArrayList<>();
        List<MltContainermapObjectLink> toAdd = new ArrayList<>(newLinks);
        for (MapLink link : links) {
            boolean found = false;
            ContainerMapElement startn = (ContainerMapElement) link.getStartNode();
                ContainerMapElement endn = (ContainerMapElement) link.getEndNode();
            for (MltContainermapObjectLink newLink : newLinks) {
                if (startn.getObject().getId() == newLink.getSourceObjId() && endn.getObject().getId() == newLink.getTargetObjId()) {
                    link.setCenter(newLink.getCenter());
                    link.setIsOneWay(newLink.isIsOneWay());
                    link.setLabel(newLink.getLabel());
                    link.setLabelPosition(newLink.getLabelPosition());
                    link.recompute();
                    found = true;
                    toAdd.remove(newLink);
                }
            }
            if (!found) {
                toDelete.add(link);
            }
        }
        
        for (MapLink td : toDelete) {
            removeLink(td);
        }
        
        // TODO: refactor. 
        // the controller calls the LinkManager for updating the links. Latter might call again the controller for displaying new links, which then again calls the Link manager to create them.
        for (MltContainermapObjectLink ta : toAdd) {
            FXMLController.getInstance().displayLink(ta);
        }
        
    }
    
    public void recomputeLink(Node node) {
        for (MapLink link : links) {
            if (link.getStartNode().equals(node) || link.getEndNode().equals(node)) {
                link.recompute();
            }
        }
    }
    
    public void recomputeLinks() {
        for (MapLink link : links) {
            link.recompute();
        }
    }
    
    public void mouseDragged(MouseEvent event) {
        if (currentLink == null) {
            return;
        }
        Node node = ContainerMapUtils.findElement(event.getX(), event.getY());
        Bounds bip = currentLink.getStartNode().getBoundsInParent();
        if (node != null && !node.equals(currentLink.getStartNode())) {
            Position p = ContainerMapUtils.computeNearestEdge(node.getBoundsInParent(), event.getX(), event.getY());
            double offset = ContainerMapUtils.computeEdgeOffset(node.getBoundsInParent(), p, event.getX(), event.getY());
            currentLink.setEndNode(node, p, offset);
        }
        else if (!bip.contains(event.getX(), event.getY())) {
            if (currentLink.getStartPosition() == null) {
                Position p = ContainerMapUtils.computeNearestEdge(bip, event.getX(), event.getY());
                double offset = ContainerMapUtils.computeEdgeOffset(bip, p, event.getX(), event.getY());
                currentLink.setStartPosition(p);
                currentLink.setStartOffset(offset);
            }
           currentLink.setEndCoordinates(event.getX(), event.getY());
        }
    }
    
    public MapLink finishLink(Node end, Position p, double offset) {
        MapLink ret = null;
        if (end == null || currentLink.getStartNode().equals(end)) {
            nodeGroup.getChildren().remove(currentLink);
        }
        else {
            currentLink.setEndNode(end, p, offset);
            if (!currentLink.isValid() || existsAlready()) {
                nodeGroup.getChildren().remove(currentLink);
            }
            else {
                links.add(currentLink);
                ret = currentLink;
                currentLink.finish();
            }
        }
        currentLink = null;
        return ret;
    }
    
    public MapLink finishLink(Point2D endPoint) {
        Node end = ContainerMapUtils.findElement(endPoint.getX(), endPoint.getY());
        Position p = null;
        double offset = 0;
        if (end != null) {
            p = ContainerMapUtils.computeNearestEdge(end.getBoundsInParent(), endPoint.getX(), endPoint.getY());
            offset = ContainerMapUtils.computeEdgeOffset(end.getBoundsInParent(), p, endPoint.getX(), endPoint.getY());
        }
        return finishLink(end, p, offset);
    }
    
    private boolean existsAlready() {
        for (MapLink l : links) {
            if (l.getStartNode().equals(currentLink.getStartNode()) && l.getEndNode().equals(currentLink.getEndNode())){
                return true;
            }
        }
        return false;
    }
    

    /**
     * Selects/Deselects all links that are connected with <code>origin</code>
     * @param origin
     * @param isSelected
     */
    public void setLinksSelected(Node origin, boolean isSelected) {
        List<MapLink> connectedLinks = getLinks(origin);
        for (MapLink link : connectedLinks) {
            link.setSelected(isSelected);
        }
    }
    
    /**
     * Returns all links for the given object
     * @param origin
     * @return 
     */
    public List<MapLink> getLinks(Node origin) {
        List<MapLink> connectedLinks = new ArrayList<>();
        for (MapLink link : links) {
            Node start = link.getStartNode();
            Node end = link.getEndNode();
            if (origin.equals(start) || origin.equals(end)) {
                connectedLinks.add(link);
            }
        }
        return connectedLinks;
    }
    
    public void clear() {
        currentLink = null;
    }
    
    public void clearAll() {
        links.clear();
        currentLink = null;
    }
    
    
}
