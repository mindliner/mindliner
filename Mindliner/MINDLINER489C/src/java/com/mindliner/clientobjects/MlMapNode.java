/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.clientobjects;

import com.mindliner.view.background.BackgroundPainter;
import com.mindliner.view.connectors.NodeConnection;
import com.mindliner.view.connectors.mlNodeConnector;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * This is the core definition of a mindliner node. These nodes are used in
 * mindmaps but also for plots. When in mind maps they typically are related to
 * their parents and children. When used in plots they are typically stand-alone
 * and their connector is the ZeroConnector.
 *
 * The philosophy is that each of the decorators is adding some information to
 * the view. Typically the information is derived from the object that is
 * wrapped up in decorators. So when the object changes the next redraw will
 * pull the updated information without further ado.
 *
 *
 * @author Marius Messerli
 */
public interface MlMapNode {

    public static final int NEW_NODE_ID = -1;

    public void setObject(mlcObject object);

    public mlcObject getObject();

    public int getId();

    public void setId(int id);

    // The parent node in the tree.
    public MlMapNode getParentNode();

    public void setParentNode(MlMapNode parent);

    /**
     * Returns the innermost node's origin.
     *
     * @see pullPosition
     * @return
     */
    public Point2D getPosition();

    /**
     * Sets the innermost node's position.
     *
     * @param p The new position
     * @see pushPosition
     */
    public void setPosition(Point2D p);

    /**
     * Obtain the hegiht of this node alone
     *
     * @param frc
     * @return
     */
    public boolean isExpanded();

    public void setExpanded(boolean status);

    public Font getFont();

    public void setFont(Font f);

    public Color getColor();

    public void setColor(Color c);

    public Color getBackgroundColor();

    public void setBackgroundColor(Color c);

    /**
     * The nodes relationships are managed even though all objects already have
     * relationships. The nodes's relationships are likely just a subset of the
     * node's relationshops.
     *
     * @return A list of nodes that are directly linked to this one.
     */
    public List<MlMapNode> getChildren();

    public void addChild(MlMapNode child);

    public void removeChild(MlMapNode child);

    public void setChildren(List<MlMapNode> children);

    /**
     * Draws this node with any encapslated decorators.
     *
     * @param g The grpahics context
     * @param layoutOnlyPass if true nothing is drawn and the nodes are only layed out and sizes are computed
     */
    public void draw(Graphics g, boolean layoutOnlyPass);

    /**
     * Determines if the specified point is within the node.
     *
     * @param p The location to probe for location within/outside.
     * @param g A reference to the graphics context
     * @return True if the specified location is within the node, false if it is
     * outside.
     */
    public boolean isWithin(Point2D p, Graphics g);

    public void setConnector(mlNodeConnector c);

    public mlNodeConnector getConnector();

    // The decorator node to this one.
    public MlMapNode getDecorator();

    public void setDecorator(MlMapNode node);

    public List<NodeConnection> getChildConnections();

    /**
     * updates all NodeConnections to the child nodes. Used when the link
     * objects changed
     */
    public void updateChildConnections();

    /**
     * Assigns the depth of a node where the root node is level 0, its children
     * are level 1 its grandchildren level 2, etc.
     *
     * @param level
     */
    public void setLevel(int level);

    /**
     * Returns the depth of a node in a tree.
     *
     * @return The depth of the node in the tree where the root node is level 0.
     */
    public int getLevel();

    public void setBackgroundPainter(BackgroundPainter painter);

    public BackgroundPainter getBackgroundPainter();

    public boolean isNew();

    public boolean isJustModified();

    /**
     * Reveals whether this node has a custom position.
     *
     * @return True if this node has a custom position, false otherwise. Nodes
     * with a custom position flag set to true will not changed by the
     * positioners.
     */
    public boolean isCustomPositioning();

    public void setCustomPositioning(boolean state);

    public void setSize(int width, int height);

    public Dimension getSize();

}
