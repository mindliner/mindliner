/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.view.interaction;

import com.mindliner.view.connectors.NodeConnection;
import java.awt.geom.Point2D;
import java.util.List;
import com.mindliner.clientobjects.MlMapNode;

/**
 *
 * @author marius
 */
public interface PickIdentifyer {
    
    public boolean isSelected(MlMapNode n, Point2D modelPoint);

    /**
     * Finds the node at the specified screen location.
     * @param clickPoint The screen coordinates of the cursor point.
     * @param ignore Nodes to be ignored in the picking process; specify null if no nodes need to be ignored
     * @return The object at the screen location or null if the background was clicked.
     */
    public MlMapNode identifyObject(Point2D clickPoint, List<MlMapNode> ignore);

    /**
     * Finds the connection at the specified screen location. 
     * @param clickPoint
     * @return The connection at the screen location or null if there is no connection there
     */
    public NodeConnection identifyConnection(Point2D clickPoint);
}
