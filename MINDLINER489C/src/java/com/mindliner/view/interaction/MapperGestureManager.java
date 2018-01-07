/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.interaction;

import java.awt.Point;
import java.awt.event.InputEvent;
import com.mindliner.clientobjects.MlMapNode;

/**
 * This class assigns a meaning to a drag gesture in Mindliner maps.
 *
 * @author Marius Messerli
 */
public class MapperGestureManager {

    public static enum DragMeaning {

        Move,
        Link, // create a link to an additional parent
        Reposition // new relative position among siblings
    }

    private static Point dragStart;
    private static Point dropPoint;
    private MlMapNode dropCandidate;

    public static DragMeaning getMeaning(InputEvent event) {
        if (event.isAltDown()) {
            return DragMeaning.Reposition;
        } else if (event.isShiftDown()) {
            return DragMeaning.Link;
        }
        return DragMeaning.Move;
    }

    public static Point getDragStart() {
        return dragStart;
    }

    public static void setDragStart(Point dragStart) {
        MapperGestureManager.dragStart = dragStart;
    }

    public static Point getDropPoint() {
        return dropPoint;
    }

    public static void setDropPoint(Point dropPoint) {
        MapperGestureManager.dropPoint = dropPoint;
    }

    public MlMapNode getDropCandidate() {
        return dropCandidate;
    }

    public void setDropCandidate(MlMapNode dropCandidate) {
        this.dropCandidate = dropCandidate;
    }

}
