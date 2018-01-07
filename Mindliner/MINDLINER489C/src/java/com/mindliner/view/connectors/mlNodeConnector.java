/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.connectors;

import com.mindliner.clientobjects.MlcLink;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Shape;
import java.util.List;
import com.mindliner.clientobjects.MlMapNode;
import com.mindliner.view.connectors.NodeConnection.ConnectorType;

/**
 *
 * @author Marius Messerli
 */
public interface mlNodeConnector {

    List<Shape> draw(Graphics g, MlMapNode parent, MlMapNode child, boolean isSelected, MlcLink link, Color color);

    /**
     * Sets the connector line dashed or solid.
     *
     * @param state If true the connector line will be drawn dashed.
     */
    void setDashed(boolean state);

    float getLineWidth(MlMapNode parent, MlMapNode child);

    ConnectorType getConnectorType();

}
