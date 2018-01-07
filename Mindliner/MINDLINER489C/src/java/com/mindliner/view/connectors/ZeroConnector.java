/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.connectors;

import com.mindliner.clientobjects.MlMapNode;
import com.mindliner.clientobjects.MlcLink;
import com.mindliner.view.connectors.NodeConnection.ConnectorType;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author marius
 */
public class ZeroConnector implements mlNodeConnector{

    @Override
    public List<Shape> draw(Graphics g, MlMapNode parent, MlMapNode child, boolean isSelected, MlcLink link, Color color) {
        List<Shape> connection = new ArrayList<>();
        return connection;
    }

    @Override
    public void setDashed(boolean state) {
    }

    @Override
    public float getLineWidth(MlMapNode parent, MlMapNode child) {
        return 1;
    }

    @Override
    public NodeConnection.ConnectorType getConnectorType() {
        return ConnectorType.ZeroConnector;
    }
    
}
