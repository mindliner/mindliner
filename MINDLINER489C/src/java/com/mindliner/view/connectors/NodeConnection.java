/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.connectors;

import com.mindliner.clientobjects.MlcLink;
import com.mindliner.events.SelectionManager;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import com.mindliner.clientobjects.MlMapNode;

/**
 * NodeConnection is the abstraction of a MlcLink on the Mindmap. It's the same
 * concept as a mlcNode and mlcObject. It's responsible for drawing the
 * connection on the map.
 *
 * @author Dominic Plangger
 */
public class NodeConnection {

    private MlcLink link;
    private mlNodeConnector connector;
    private final MlMapNode holder;
    private final MlMapNode relative;
    private List<Shape> connection; // a connection may consist of several shapes (see CurvedConnectorUnderlined)
    private Color color = Color.red;
    private static final double MARGIN = 3;

    public static enum ConnectorType {
        CurvedTextClear,
        CurvedTextClearRating,
        ZeroConnector
    }

    public NodeConnection(MlcLink link, MlMapNode holder, MlMapNode relative) {
        this.link = link;
        this.holder = holder;
        this.relative = relative;
        color = relative.getColor();
    }

    public boolean isOnConnection(Point2D pt) {
        Double x = pt.getX();
        Double y = pt.getY();
        // Create a square with side length 'MARGIN*2' around the point
        // and then test if the square intersects the connection shapes
        Rectangle2D rect = new Rectangle2D.Double(x - MARGIN, y - MARGIN, MARGIN * 2, MARGIN * 2);
        for (Shape s : connection) {
            if (s.intersects(rect)) {
                return true;
            }
        }
        return false;
    }

    public void draw(Graphics g, MlMapNode parent, MlMapNode child) {
        boolean isSelected = SelectionManager.isConnectionSelected(this);
        connection = connector.draw(g, parent, child, isSelected, link, color);
    }

    public void setConnector(mlNodeConnector connector) {
        this.connector = connector;
    }

    public MlcLink getLink() {
        return link;
    }

    public void setLink(MlcLink link) {
        this.link = link;
    }

    public MlMapNode getHolder() {
        return holder;
    }

    public MlMapNode getRelative() {
        return relative;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NodeConnection)) {
            return false;
        }

        NodeConnection that = (NodeConnection) obj;
        if (link.equals(that.link)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.link != null ? this.link.hashCode() : 0);
        return hash;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

}
