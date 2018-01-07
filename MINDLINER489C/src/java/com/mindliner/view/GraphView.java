/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view;

import com.mindliner.view.interaction.ZoomPanListener;
import com.mindliner.view.background.BackgroundPainter;
import com.mindliner.view.nodebuilder.NodeBuilder;
import com.mindliner.view.colorizers.NodeColorizerBase;
import java.awt.Point;
import java.util.List;
import com.mindliner.clientobjects.MlMapNode;

/**
 * The interface to Mindliner's graphics object and link view.
 * 
 * @author Marius Mesesrli
 */
public interface GraphView {

    public ZoomPanListener getZoomAndPanListener();

    public abstract void repaintSingleNode(MlMapNode n);

    /**
     * Centers the current view
     * @param onlyWhenOffscreen If true then centering only occurs if there is no selection of if the selected node is offscreen
     * 
     */
    public abstract void ensureNodeInView(boolean onlyWhenOffscreen);

    /**
     * Returns the object under the cursor.
     *
     * @param p The untransformed screen coordinates.
     * @return The object under the cursor or null if none found.
     */
    public MlMapNode getNodeAtScreenLocation(Point p);

    /**
     * @todo Think about taking a list of objects as arguments. This call is
     * mostly used after another call to buildNodes, one of the two is not
     * required.
     *
     * @param nodes
     */
    public void setNodes(List<MlMapNode> nodes);

    /**
     * Returns a list of root nodes for this map
     *
     * @return A list of root nodes
     */
    public List<MlMapNode> getNodes();

    public void pushCurrentNodes();

    public void popNodes();

    public boolean hasHistory();

    public void clearViewHistory();

    /**
     * Deletes the specified node from the node list.
     *
     * @param n The node to be removed
     * @param udpateSelection
     */
    public void deleteNode(MlMapNode n, boolean udpateSelection);

    public void setBackgroundPainter(BackgroundPainter bp);

    public BackgroundPainter getBackgroundPainter();

    /**
     * This function assigns new node colors based on the node's content and the
     * selected color driver.
     */
    public void reAssignNodeColors();

    public NodeBuilder getNodeBuilder();

    public void setNodeBuilder(NodeBuilder nodeBuilder);

    public void unlinkNodes(MlMapNode child, MlMapNode parent);

    public void unlinkNodesOneWay(MlMapNode parent, MlMapNode child);

    public void deleteNodeAndObject(List<MlMapNode> nodes);

    public void setColorizer(NodeColorizerBase c);

    public NodeColorizerBase getColorizer();

}
