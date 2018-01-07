/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.containermap;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;

/**
 * This class provides utility functions to deal with coordinate systems.
 *
 * @author Dominic Plangger & Marius Messerli
 */
public class CoordUtil {

    /**
     * important function; uses the known layoutBounds & parentBounds of an
     * existing node to compute the layoutBounds for a new node. Is used for
     * example when adding a new node after the scenery has been zoomed and
     * panned.
     * @param sceneX The scene x coords (i.e. bounds in parent)
     * @param sceneY The scene y coords (i.e. bounds in parent)
     * @param nodeGroup All nodes of the map
     * @param globalScale The scaling to be applied to all objects in the map
     * @return 
     */
    public static Point2D inferLayoutCoords(double sceneX, double sceneY, Group nodeGroup, double globalScale) {
        for (Node node : nodeGroup.getChildren()) {
            double referenceNodeLayoutX = 0;
            double referenceNodeLayoutY = 0;

            // To get the layout coordinates we have to distinguish between nodes and containers (groups). Because:
            // The layoutBounds of a node are the boundsInLocal without transformations, meaning that minX and minY of the bounds are always 0. 
            // Thats why we have to use getLayoutX/Y.
            // --
            // A group however has no layoutX and layoutY coordinates, meaning that getLayoutX and getLayoutY are always 0. To get the layout start coordinates of a group,
            // we call getLayoutBounds.getMinX/Y which will trigger a layout to get the most up-to-date coordinates.
            // see https://blogs.oracle.com/jfxprg/entry/the_peculiarities_of_javafx_layout
            
            if (node instanceof MapNode) {
                // the layout bounds of the new node are the layout bounds of the reference node plus the de-scaled difference of their parent bounds.
                referenceNodeLayoutX = node.getLayoutX();
                referenceNodeLayoutY = node.getLayoutY();
            } else if (node instanceof MapContainer) {
                referenceNodeLayoutX = node.getLayoutBounds().getMinX();
                referenceNodeLayoutY = node.getLayoutBounds().getMinY();
            }
            double x = referenceNodeLayoutX + (sceneX - node.getBoundsInParent().getMinX()) / globalScale;
            double y = referenceNodeLayoutY + (sceneY - node.getBoundsInParent().getMinY()) / globalScale;
            return new Point2D(x, y);
        }
        return new Point2D(sceneX, sceneY);
    }

    /**
     * Returns the screen translation (with zoom/pan) for an object for which
     * the layout coords are known
     *
     * See Mindliner object 180398
     *
     * @param newLayoutX The layout x coord of the new object
     * @param newLayoutY
     * @param nodeGroup The existing nodes
     * @param globalScale The scale in use
     * @return
     */
    public static Point2D inferTranslate(double newLayoutX, double newLayoutY, Group nodeGroup, double globalScale) {
        for (Node referenceNode : nodeGroup.getChildren()) {
            double referenceNodeLayoutX = 0;
            double referenceNodeLayoutY = 0;
            if (referenceNode instanceof MapNode) {
                referenceNodeLayoutX = referenceNode.getLayoutX();
                referenceNodeLayoutY = referenceNode.getLayoutY();
            } else if (referenceNode instanceof MapContainer) {
                referenceNodeLayoutX = referenceNode.getLayoutBounds().getMinX();
                referenceNodeLayoutY = referenceNode.getLayoutBounds().getMinY();
            }
            double x = referenceNode.getBoundsInParent().getMinX() + (newLayoutX - referenceNodeLayoutX) * globalScale;
            double y = referenceNode.getBoundsInParent().getMinY() + (newLayoutY - referenceNodeLayoutY) * globalScale;
            return new Point2D(x, y);
        }
        // if this is the first object just return its layout coords
        return new Point2D(newLayoutX, newLayoutY);
    }
}
