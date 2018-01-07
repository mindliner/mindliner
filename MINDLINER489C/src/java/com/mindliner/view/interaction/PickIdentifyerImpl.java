/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.mindliner.view.interaction;

import com.mindliner.view.MindlinerMapper;
import com.mindliner.view.NodeDecoratorManager;
import com.mindliner.view.TreeLinearizer;
import com.mindliner.view.connectors.NodeConnection;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.mindliner.clientobjects.MlMapNode;

/**
 * Class selects the innermost text component of a (possibly) decorated node
 * cascade.
 *
 * @author Marius Messerli
 */
public class PickIdentifyerImpl implements PickIdentifyer {

    private static ZoomPanListener zoomAndPanListener = null;
    private static MindlinerMapper viewerPanel = null;

    public PickIdentifyerImpl(MindlinerMapper v, ZoomPanListener l) {
        viewerPanel = v;
        zoomAndPanListener = l;
    }

    @Override
    public NodeConnection identifyConnection(Point2D clickPoint) {
        try {
            Point2D dst = zoomAndPanListener.getInverseTransform((Point) clickPoint);
            List<MlMapNode> rootNodes = viewerPanel.getNodes();
            List<MlMapNode> nodes = TreeLinearizer.linearizeAllTrees(rootNodes, false);
            for (MlMapNode n : nodes) {
                List<NodeConnection> childConns = n.getChildConnections();
                for (NodeConnection c : childConns) {
                    if (c.isOnConnection(dst)) {
                        return c;
                    }
                }
            }
            return null;
        } catch (NoninvertibleTransformException ex) {
            Logger.getLogger(PickIdentifyerImpl.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public MlMapNode identifyObject(Point2D clickPoint, List<MlMapNode> ignore) {
        try {
            Point2D dst = zoomAndPanListener.getInverseTransform((Point) clickPoint);
            List<MlMapNode> nodes = viewerPanel.getNodes();
            for (MlMapNode n : nodes) {
                if (ignore == null || !ignore.contains(n)) {
                    if (isSelected(n, dst)) {
                        return n;
                    } else {
                        MlMapNode selectedChild = getSelectedChild(n, dst, ignore);
                        if (selectedChild != null) {
                            return selectedChild;
                        }
                    }
                }
            }
            return null;
        } catch (NoninvertibleTransformException ex) {
            Logger.getLogger(PickIdentifyerImpl.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public boolean isSelected(MlMapNode node, Point2D transformClickPoint) {
        Graphics2D g = (Graphics2D) viewerPanel.getGraphics();
        return NodeDecoratorManager.getOutermostDecorator(node).isWithin(transformClickPoint, g);
    }

    public MlMapNode getSelectedChild(MlMapNode parent, Point2D tcp, List<MlMapNode> ignore) {
        for (MlMapNode n : parent.getChildren()) {
            if (ignore == null || !ignore.contains(n)) {
                if (isSelected(n, tcp)) {
                    return n;
                } else {
                    if (n.isExpanded()) {
                        MlMapNode selectedChild = getSelectedChild(n, tcp, ignore);
                        if (selectedChild != null) {
                            return selectedChild;
                        }
                    }
                }
            }
        }
        return null;
    }

}
