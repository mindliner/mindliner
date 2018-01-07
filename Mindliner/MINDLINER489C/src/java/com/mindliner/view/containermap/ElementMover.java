/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.containermap;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Bounds;
import javafx.scene.Node;

/**
 * Implements the logic to move an element in the CM. Currently used by
 * MapNodes and MapContainers
 * @author Dominic Plangger
 * @param <T>
 */
public class ElementMover<T extends Node & ContainerMapElement> {
    
    private final Class<T> intersectionType;
    private final Node moveTarget;
    private Node layoutTarget;
    private final FXMLController mapController;
    private double beforePanX;
    private double beforePanY;
    private double lastPanX;
    private double lastPanY;
    private List<T> currIntersections = new ArrayList<>();
    
    

    public ElementMover(Node moveTarget, Class<T> type) {
        this.intersectionType = type;
        this.moveTarget = moveTarget;
        mapController = FXMLController.getInstance();
        layoutTarget = moveTarget;
    }

    public void setLayoutTarget(Node layoutTarget) {
        this.layoutTarget = layoutTarget;
    }
    
    public void init(double sx, double sy) {
        lastPanX = sx;
        lastPanY = sy;
        beforePanX = moveTarget.getLayoutX();
        beforePanY = moveTarget.getLayoutY();
        currIntersections.clear();
    }
    
    public void move(double sx, double sy) {
        double dx = sx - lastPanX;
        double dy = sy - lastPanY;
        double scale = moveTarget.getScaleX();

        // when moving a container, the translation is applied on the Group but the Layout can only be set on the rectangle
        // That's why we offer different targets for layout and translation.
        layoutTarget.setLayoutX(layoutTarget.getLayoutX() + dx / scale);
        layoutTarget.setLayoutY(layoutTarget.getLayoutY() + dy / scale);
        moveTarget.setTranslateX(moveTarget.getTranslateX() - dx / scale + dx);
        moveTarget.setTranslateY(moveTarget.getTranslateY() - dy / scale + dy);

        lastPanX += dx;
        lastPanY += dy;
        highlightIntersections();
        mapController.getLinker().recomputeLink(moveTarget);
    }
    
    /**
     * Sets the layout coordinates of the element. If the scenery is panned or zoomed, 
     * the translation parameters must also be updated. 
     * @param lx
     * @param ly 
     */
    public void updateLayout(double lx, double ly) {
        double blx = layoutTarget.getLayoutX();
        double bly = layoutTarget.getLayoutY();
        double scale = moveTarget.getScaleX();
        double dx = lx - blx;
        double dy = ly - bly;
        moveTarget.setTranslateX(moveTarget.getTranslateX() - (dx - dx * scale));
        moveTarget.setTranslateY(moveTarget.getTranslateY() - (dy - dy * scale));
        layoutTarget.setLayoutX(lx);
        layoutTarget.setLayoutY(ly);
    }
    
    public void finish() {
        mapController.nodeMoved(beforePanX, beforePanY, moveTarget);
        reset();
    }
    
    public void reset() {
        currIntersections.stream().filter((c) -> (!mapController.isSelected(c))).forEach((cont) -> {
            cont.setSelected(false);
        });
        currIntersections.clear();
    }
    
    
    public void highlightIntersections() {
        Bounds bip = moveTarget.getBoundsInParent();
        List<T> newIntersections = mapController.getIntersections(intersectionType, bip.getMinX(), bip.getMinY(), bip.getWidth(), bip.getHeight(), false);
        currIntersections.stream().filter((c) -> (!mapController.isSelected(c))).forEach((c) -> {
            c.setSelected(false);
        });
        newIntersections.stream().filter((c) -> (!mapController.isSelected(c))).forEach((c) -> {
            c.setSelected(true);
        });
        currIntersections = newIntersections;
    }

    public List<T> getCurrIntersections() {
        return currIntersections;
    }
}
