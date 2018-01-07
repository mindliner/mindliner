/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.nodebuilder;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.view.background.BackgroundPainter;
import java.awt.Font;
import java.util.List;
import com.mindliner.clientobjects.MlMapNode;
import com.mindliner.view.connectors.NodeConnection.ConnectorType;

/**
 * The base class for multiple node builders. The task of this class family is
 * to build displayable nodes from a list of objects. Some node builders care
 * about the object's relations, others don't. Some nodes remain visually
 * connected others don't.
 *
 * @author Marius Messerli
 */
public interface NodeBuilder {

    public List<MlMapNode> buildNodes(List<mlcObject> objects);

    /**
     * Wrap the object in (decorated) map nodes.
     *
     * @see setDecoratorStack to define the layers around the core text node
     * @param object
     * @return
     */
    public MlMapNode wrapObject(mlcObject object);

    public void setConnectorType(ConnectorType connectorType);

    /**
     * The maximum number of characters of the headline used on the node
     *
     * @param count The number of characters used on the node
     */
    public void setMaxCharacterCount(int count);

    /**
     * The font used when the node is showing text.
     *
     * @param f The font that will be assigned to the nodes to be built
     */
    public void setFont(Font f);

    /**
     * Assigns a background painter. Required by some nodes so they know how to
     * change the color in case of selection so it stays leggible.
     *
     * @param backgroundPainter
     */
    public void setBackgroundPainter(BackgroundPainter backgroundPainter);

    /**
     * Defines whether or not attributes will be shown
     *
     * @param status
     */
    public void setShowAttributes(boolean status);

    /**
     * Defines whether the description text will be shown
     *
     * @param status
     */
    public void setShowDescription(boolean status);

    /**
     * Defines whether images will be shown
     *
     * @param status
     */
    public void setShowImages(boolean status);
}
