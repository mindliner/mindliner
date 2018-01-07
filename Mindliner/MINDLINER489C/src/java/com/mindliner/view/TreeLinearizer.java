/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view;

import com.mindliner.clientobjects.mlcObject;
import java.util.ArrayList;
import java.util.List;
import com.mindliner.clientobjects.MlMapNode;

/**
 *
 * @author M.Messerli Created on 05.08.2012, 18:09:40
 */
public class TreeLinearizer {

    /**
     * This function flattens the tree hierarchy of all specified root nodes
     * into a single list.
     *
     * @param nodes A list of nodes, possibly each being the root of a tree
     * @param includeCollapsedBranches If true the entire sub-tree starting at root is added, otherwise only expanded nodes are added
     * @return
     */
    public static List<MlMapNode> linearizeAllTrees(List<MlMapNode> nodes, boolean includeCollapsedBranches) {
        List<MlMapNode> allNodes = new ArrayList<>();
        for (MlMapNode n : nodes) {
            allNodes.addAll(linearizeTree(n, includeCollapsedBranches));
        }
        return allNodes;
    }

    /**
     * Returns all nodes of a tree in a list.
     *
     * @param root The root node of the tree to be linearize
     * @param includeCollapsedBranches If true the entire sub-tree starting at root is added, otherwise only expanded nodes are added
     * @return A list of all nodes of the tree.
     */
    public static List<MlMapNode> linearizeTree(MlMapNode root, boolean includeCollapsedBranches) {
        List<MlMapNode> nodeList = new ArrayList<>();
        linearizeNode(root, nodeList, includeCollapsedBranches);
        return nodeList;
    }

    private static void linearizeNode(MlMapNode parent, List<MlMapNode> linearList, boolean includeCollapsedBranches) {
        linearList.add(parent);
        if (includeCollapsedBranches || parent.isExpanded()) {
            for (MlMapNode child : parent.getChildren()) {
                linearizeNode(child, linearList, includeCollapsedBranches);
            }
        }
    }

    public static List<mlcObject> getObjects(List<MlMapNode> nodes) {
        List<mlcObject> objects = new ArrayList<>();
        for (MlMapNode n : nodes) {
            if (!objects.contains(n.getObject())) {
                objects.add(n.getObject());
            }
        }
        return objects;
    }
}
