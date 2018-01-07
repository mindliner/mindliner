/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view;

import com.mindliner.clientobjects.MlMapNode;
import com.mindliner.clientobjects.mlcObject;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marius Messerli
 */
public class MlMapNodeUtils {

    public static List<mlcObject> getObjects(List<MlMapNode> nodes) {
        List<mlcObject> objects = new ArrayList<>();
        for (MlMapNode n : nodes) {
            objects.add(n.getObject());
        }
        return objects;
    }

    /**
     * Returns the first node that represents the specified object
     * @param nodes A linearized list of nodes to search through (use Treelinearizer)
     * @param objectId The id of the object to be found
     * @return The node or null
     */
    public static MlMapNode findNodeForObject(List<MlMapNode> nodes, Integer objectId) {
        MlMapNode startNode = null;
        for (MlMapNode n : nodes) {
            if (n.getObject().getId() == objectId) {
                startNode = n;
                break;
            }
        }
        return startNode;
    }

}
