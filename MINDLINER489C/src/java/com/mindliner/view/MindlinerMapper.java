/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.mindliner.view;

import com.mindliner.commands.CommandRecorder;
import com.mindliner.commands.ObjectDeletionCommand;
import com.mindliner.commands.UnlinkCommand;
import com.mindliner.events.ObjectChangeManager;
import com.mindliner.events.ObjectChangeObserver;
import com.mindliner.exceptions.ForeignOwnerException;
import com.mindliner.view.background.BackgroundPainter;
import com.mindliner.view.colorizers.HierarchicalColorizer;
import com.mindliner.view.colorizers.NodeColorizerBase;
import com.mindliner.view.interaction.PickIdentifyer;
import com.mindliner.view.interaction.ZoomPanListener;
import com.mindliner.view.nodebuilder.NodeBuilder;
import com.mindliner.view.positioner.MindmapPositioner;
import com.mindliner.view.positioner.ObjectPositioner;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.apache.poi.ss.formula.eval.NotImplementedException;
import com.mindliner.clientobjects.MlMapNode;

/**
 * This interface describes the interaction with the mindliner graphical object
 * view.
 *
 * @author Marius Messerli
 */
public abstract class MindlinerMapper extends JPanel implements GraphView, ObjectChangeObserver {

    private NodeBuilder nodeBuilder = null;
    private JPanel parentPanel = null;
    private List<MlMapNode> nodes = new ArrayList<>();
    private final Stack<List<MlMapNode>> nodeHistory = new Stack<>();
    private ObjectPositioner positioner = new MindmapPositioner();
    private boolean shortestPathEnabled;
    private int maxShortestPath = 3;
    private NodeColorizerBase colorizer = new HierarchicalColorizer();
    protected final float alpha = 0.85F;
    protected BackgroundPainter backgroundPainter;
    // in order to allow for smooth zooming we need to cache some node dimensions
    // however, we want to compute them accurately if the user is not interacting

    @Override
    public abstract ZoomPanListener getZoomAndPanListener();

    public abstract PickIdentifyer getPickIdentifyer();

    @Override
    public abstract void repaintSingleNode(MlMapNode n);

    @Override
    public abstract void ensureNodeInView(boolean onlyWhenOffscreen);

    public void exportToPdf() {
        throw new NotImplementedException("PDF Export");
    }

    public void exportToSvg() {
        throw new NotImplementedException("SVG Export");
    }

    /**
     * Returns the object under the cursor.
     *
     * @param p The untransformed screen coordinates.
     * @return The object under the cursor or null if none found.
     */
    @Override
    public abstract MlMapNode getNodeAtScreenLocation(Point p);

    /**
     * This function takes a node's object and create a new node for eatch
     * object relative that is not yet present, except for the grandparent.
     *
     * @param parent The node to which missing object node's are to be added
     */
    public abstract void addMissingNodes(MlMapNode parent);

    public int getMaxShortestPath() {
        return maxShortestPath;
    }

    public void setMaxShortestPath(int maxShortestPath) {
        this.maxShortestPath = maxShortestPath;
    }

    public boolean isShortestPathEnabled() {
        return shortestPathEnabled;
    }

    public void setShortestPathEnabled(boolean shortestPathEnabled) {
        this.shortestPathEnabled = shortestPathEnabled;
    }

    public void setPositioner(ObjectPositioner p) {
        positioner = p;
    }

    public ObjectPositioner getPositioner() {
        return positioner;
    }

    /**
     * @todo Think about taking a list of objects as arguments. This call is
     * mostly used after another call to buildNodes, one of the two is not
     * required.
     *
     * @param nodes
     */
    @Override
    public void setNodes(List<MlMapNode> nodes) {
        this.nodes = nodes;
        if (positioner != null) {
            positioner.setNodes(nodes);
        }
    }

    /**
     * Returns a list of root nodes for this map
     *
     * @return A list of root nodes
     */
    @Override
    public List<MlMapNode> getNodes() {
        return nodes;
    }

    @Override
    public void pushCurrentNodes() {
        if (!nodes.isEmpty()) {
            nodeHistory.push(getNodes());
        }
    }

    @Override
    public void popNodes() {
        if (!nodeHistory.isEmpty()) {
            nodes = nodeHistory.pop();
            repaint();
        }
    }

    @Override
    public boolean hasHistory() {
        return (nodeHistory.size() > 0);
    }

    @Override
    public void clearViewHistory() {
        nodeHistory.clear();
    }

    public void setParentFrame(JPanel p) {
        parentPanel = p;
    }

    public JPanel getParentPanel() {
        return parentPanel;
    }

    private void drawNodeAndExpandedChildren(MlMapNode node, Graphics2D g2, boolean layoutOnly) {
        node.draw(g2, layoutOnly);
        if (node.isExpanded()) {
            for (MlMapNode n : node.getChildren()) {
                drawNodeAndExpandedChildren(n, g2, layoutOnly);
            }
        }
    }

    protected void render(Graphics g1, MlMapNode node, boolean layoutOnly) {
        Graphics2D g2 = (Graphics2D) g1;
        positioner.setFontRenderContext(g2.getFontRenderContext());
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        if (node != null) {
            drawNodeAndExpandedChildren(node, g2, layoutOnly);
        } else {
            for (MlMapNode n : getNodes()) {
                drawNodeAndExpandedChildren(n, g2, layoutOnly);
            }
        }
    }

    /**
     * Deletes the specified node from the node list.
     *
     * @param n The node to be removed
     * @param udpateSelection
     */
    @Override
    public void deleteNode(MlMapNode n, boolean udpateSelection) {
        nodes.remove(n);
    }

    @Override
    public void setBackground(Color c) {
        super.setBackground(c);
        if (backgroundPainter != null) {
            backgroundPainter.setBackground(c);
            backgroundPainter.initialize();
        }
        if (positioner != null) {
            colorizer.setBackgroundColor(c);
            colorizer.assignNodeColors(nodes);
        }
    }

    @Override
    public void setBackgroundPainter(BackgroundPainter bp) {
        backgroundPainter = bp;
        // provision for startup configuration
        if (getNodeBuilder() != null) {
            getNodeBuilder().setBackgroundPainter(backgroundPainter);
        }
    }

    @Override
    public BackgroundPainter getBackgroundPainter() {
        return backgroundPainter;
    }

    /**
     * This function assigns new node colors based on the node's content and the
     * selected color driver.
     *
     */
    @Override
    public void reAssignNodeColors() {
        if (getPositioner() == null) {
            System.err.println("developer note: cannot re-assign colors - positioner is null.");
        } else {
            colorizer.assignNodeColors(nodes);
        }
    }

    @Override
    public NodeBuilder getNodeBuilder() {
        return nodeBuilder;
    }

    @Override
    public void setNodeBuilder(NodeBuilder nodeBuilder) {
        this.nodeBuilder = nodeBuilder;
    }

    @Override
    public void unlinkNodes(MlMapNode child, MlMapNode parent) {
        try {
            unlinkObjectsAndNodes(child, parent);
        } catch (ForeignOwnerException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Link created by another user", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(MindlinerMapper.class.getName()).log(Level.INFO, null, ex);
            return;
        }
        deleteNode(child, true);
    }

    @Override
    public void unlinkNodesOneWay(MlMapNode parent, MlMapNode child) {
        if (child == null || parent == null) {
            return;
        }
        CommandRecorder cr = CommandRecorder.getInstance();
        cr.scheduleCommand(new UnlinkCommand(parent.getObject(), child.getObject(), true));
        parent.removeChild(child);
        if (child.getParentNode() != null) {
            if (child.getParentNode().equals(parent)) {
                deleteNode(child, true);
            }
        }
    }

    @Override
    public void deleteNodeAndObject(List<MlMapNode> nodes) {
        CommandRecorder cr = CommandRecorder.getInstance();
        ObjectDeletionCommand delCmd = new ObjectDeletionCommand(TreeLinearizer.getObjects(nodes));
        cr.scheduleCommand(delCmd);
        // I need to call this directly even in online mode as the above command will remove the object from the cache and the message listener will not find it anymore
        ObjectChangeManager.objectsDeleted(delCmd.getDeletedObjects());
    }

    private void unlinkObjectsAndNodes(MlMapNode child, MlMapNode parent) throws ForeignOwnerException {
        if (child == null || parent == null) {
            return;
        }
        CommandRecorder cr = CommandRecorder.getInstance();
        UnlinkCommand uc = new UnlinkCommand(parent.getObject(), child.getObject(), false);
        cr.scheduleCommand(uc);
        if (uc.isProceed()) {
            child.removeChild(parent);
            parent.removeChild(child);
        }
    }

    @Override
    public void setColorizer(NodeColorizerBase c) {
        colorizer = c;
    }

    @Override
    public NodeColorizerBase getColorizer() {
        return colorizer;
    }

}
