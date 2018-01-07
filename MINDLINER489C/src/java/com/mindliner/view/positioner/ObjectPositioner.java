/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.positioner;

import java.awt.Dimension;
import java.awt.font.FontRenderContext;
import java.util.ArrayList;
import java.util.List;
import com.mindliner.clientobjects.MlMapNode;

/**
 *
 * @author Marius Messerli
 */
public abstract class ObjectPositioner {

    private FontRenderContext fontRenderContext = null;

    private List<MlMapNode> nodes = new ArrayList<>();
    private Dimension drawingArea = new Dimension(1024, 1024);
    private PositionLayout layout;

    public enum PositionLayout {

        MindMap,
        SymmetricMindMap,
        Brizwalk,
        Path
    }

    /**
     * Arranges all nodes in the map and returns the sum of all node
     * displacements executed by this call.
     *
     * @return The sum of all displacements.
     */
    public abstract double arrangePositions();

    /**
     * This call specifically only arranges the specified head node and its
     * decendants and leaves the other nodes untouched.
     *
     * @param branchHeadNode
     * @param left For mindmaps defines whether this node and its family is on
     * the left or right side of the root node; for non-maps this parameter is
     * ignored.
     * @return
     */
    public abstract double arrangeNodeFamilyPosition(MlMapNode branchHeadNode, boolean left);

    protected List<MlMapNode> getNodes() {
        return nodes;
    }

    /**
     * Sets multiple top level nodes. This is used for scatter plots while for
     * maps the call is typically to setRootNode
     *
     * @param nodes The top level nodes
     * @see setRootNode
     */
    public void setNodes(List<MlMapNode> nodes) {
        assert (nodes != null);
        this.nodes = nodes;
    }

    public void setRootNode(MlMapNode node) {
        List<MlMapNode> tmpnodes = new ArrayList<>();
        tmpnodes.add(node);
        setNodes(tmpnodes);
    }

    public void setDrawingArea(Dimension drawingArea) {
        this.drawingArea = drawingArea;
    }

    protected Dimension getDrawingArea() {
        return drawingArea;
    }

    public void setFontRenderContext(FontRenderContext fcr) {
        fontRenderContext = fcr;
    }

    public FontRenderContext getFontRenderContext() {
        return fontRenderContext;
    }

    public PositionLayout getLayout() {
        return layout;
    }

    public void setLayout(PositionLayout layout) {
        this.layout = layout;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

}
