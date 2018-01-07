/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.view;

import com.mindliner.view.connectors.ConnectorFactory;
import com.mindliner.clientobjects.MlMapNode;
import com.mindliner.view.connectors.NodeConnection.ConnectorType;

/**
 * This class recursively updates the connection type of the tree
 * @author Marius Messerli
 */
public class NodeConnectorUpdater {
    
    public static void updateConnectorsForTree(MlMapNode n, ConnectorType type){
        n.setConnector(ConnectorFactory.getConnector(type));
        for (MlMapNode child : n.getChildren()){
            updateConnectorsForTree(child, type);
        }
    }
}
