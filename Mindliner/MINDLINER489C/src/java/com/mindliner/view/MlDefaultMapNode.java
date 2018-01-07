/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.MlcLink;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.view.background.BackgroundPainter;
import com.mindliner.view.connectors.NodeConnection;
import com.mindliner.view.connectors.mlNodeConnector;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.mindliner.clientobjects.MlMapNode;
import java.awt.Dimension;
import java.util.Date;

/**
 * This is a node that takes care of the housekeeping implementations of the
 * interface.
 *
 * @author Marius Messerli Created on 08.09.2012, 11:13:16
 */
public abstract class MlDefaultMapNode implements MlMapNode {

    private static final int HIGHLIGHTING_WINDOW = 60 * 1000;

    private int id = NEW_NODE_ID;
    private Point2D position = new Point(0, 0);
    private Color color = Color.BLACK;
    private Color backgroundColor = new Color(245, 245, 250);
    private List<MlMapNode> children = new ArrayList<>();
    private final List<NodeConnection> childConnections = new ArrayList<>();
    private boolean expanded = false;
    private mlNodeConnector connector = null;
    private mlcObject object = null;
    private Font font = null;
    private MlMapNode parent = null;
    private MlMapNode decorator = null;
    private BackgroundPainter painter = null;
    private boolean customPositioning = false;
    protected int width = 0;
    protected int height = 0;
    int level = -1;

    public MlDefaultMapNode(mlcObject o) {
        object = o;
    }

    @Override
    public void setObject(mlcObject object) {
        this.object = object;
    }

    protected void drawChildConnections(Graphics2D g2) {
        for (NodeConnection c : childConnections) {
            c.draw(g2, NodeDecoratorManager.getOutermostDecorator(this), NodeDecoratorManager.getOutermostDecorator(c.getRelative()));
        }
    }

    @Override
    public mlcObject getObject() {
        return object;
    }

    @Override
    public Font getFont() {
        return font;
    }

    @Override
    public void setFont(Font f) {
        font = f;
    }

    @Override
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    @Override
    public List<MlMapNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public mlNodeConnector getConnector() {
        return connector;
    }

    @Override
    public MlMapNode getDecorator() {
        return decorator;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public MlMapNode getParentNode() {
        return parent;
    }

    @Override
    public Point2D getPosition() {
        return position;
    }

    @Override
    public boolean isExpanded() {
        return expanded;
    }

    @Override
    public void setBackgroundColor(Color c) {
        backgroundColor = c;
    }

    @Override
    public void setChildren(List<MlMapNode> children) {
        this.children = children;
        childConnections.clear();
        for (MlMapNode child : children) {
            addChildConnection(child);
        }
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public void setConnector(mlNodeConnector c) {
        this.connector = c;
        for (NodeConnection conn : childConnections) {
            conn.setConnector(c);
        }
    }

    @Override
    public void setDecorator(MlMapNode node) {
        this.decorator = node;
    }

    @Override
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public void setParentNode(MlMapNode parent) {
        this.parent = parent;
    }

    @Override
    public void setPosition(Point2D p) {
        position = p;
    }

    @Override
    public List<NodeConnection> getChildConnections() {
        return childConnections;
    }

    @Override
    public void addChild(MlMapNode child) {
        if (children != null && !children.contains(child)) {
            children.add(child);
            addChildConnection(child);
        }
    }

    private void addChildConnection(MlMapNode child) {
        int ownerId = this.getObject().getId();
        int relativeId = child.getObject().getId();
        MlcLink link = CacheEngineStatic.getLink(ownerId, relativeId);
        if (link != null) {
            MlMapNode outermost = NodeDecoratorManager.getOutermostDecorator(this);
            NodeConnection childConn = new NodeConnection(link, outermost, child);
            childConn.setConnector(connector);
            childConnections.add(childConn);
        } else {
            Logger.getLogger(MlDefaultMapNode.class.getName()).log(Level.INFO, "There is no link object in the cache for the added child node with object {0}. Cannot draw connection to child.", child.getObject().getId());
        }
    }

    private void removeChildConnection(MlMapNode child) {
        Iterator<NodeConnection> it = childConnections.iterator();
        while (it.hasNext()) {
            NodeConnection c = it.next();
            if (c.getLink().getRelativeId() == child.getObject().getId()) {
                it.remove();
            }
        }
    }

    @Override
    public void removeChild(MlMapNode child) {
        if (children != null && children.contains(child)) {
            children.remove(child);
            removeChildConnection(child);
        }
    }

    @Override
    public void updateChildConnections() {
        childConnections.clear();
        for (MlMapNode child : children) {
            addChildConnection(child);
        }
    }

    @Override
    public void setBackgroundPainter(BackgroundPainter painter) {
        this.painter = painter;
    }

    @Override
    public BackgroundPainter getBackgroundPainter() {
        return painter;
    }

    /**
     * returns true if the object was created less than one minute ago
     *
     * @todo this only works if the server and the client have EXACTLY the same
     * clock because modification time is defined by server
     * @return
     */
    @Override
    public boolean isNew() {
        Date now = new Date();
        return ((now.getTime() - getObject().getCreationDate().getTime()) < HIGHLIGHTING_WINDOW);
    }

    /**
     * returns true if the object was modified less than one minute ago
     *
     * @todo this only works if the server and the client have EXACTLY the same
     * clock because modification time is defined by server
     * @return
     */
    @Override
    public boolean isJustModified() {
        Date now = new Date();
        return ((now.getTime() - getObject().getModificationDate().getTime()) < HIGHLIGHTING_WINDOW);
    }

    @Override
    public boolean isCustomPositioning() {
        return customPositioning;
    }

    @Override
    public void setCustomPositioning(boolean customPositioning) {
        this.customPositioning = customPositioning;
    }

    @Override
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public Dimension getSize() {
        return new Dimension(width, height);
    }

}
