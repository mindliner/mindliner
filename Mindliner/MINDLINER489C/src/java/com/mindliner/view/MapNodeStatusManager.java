/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.events.SelectionManager;
import java.util.ArrayList;
import java.util.List;
import com.mindliner.clientobjects.MlMapNode;

/**
 * This class manages the selection state, the anchor state, and the drop target
 * candidate state of nodes shown in a map. The information is used by the
 * drawing routines to determine how to paint the nodes and also by routines who
 * need to know the current selection.
 *
 * @author Marius Messerli
 */
public class MapNodeStatusManager {

    private static List<MlMapNode> selectedNodes = new ArrayList<>();
    protected static List<MlMapNode> anchorNodes = new ArrayList<>();
    protected static MlMapNode candidateTargetNode = null;

    /**
     * Determins if the specified node is left of its parent node which is taken
     * as the indicator that the sub-branch of the specified node is left of the
     * root node. The result is meaningful only if the map arrangement is
     * MindMap or SymmetricMindMap and meaningless for dynamic maps.
     *
     * @param node
     * @return True if the specified node's x-coordinate is smaller than that of
     * its parent.
     */
    public static boolean isLeft(MlMapNode node) {
        assert (node != null);
        if (node.getParentNode() == null) {
            return false;
        } else {
            return node.getParentNode().getPosition().getX() > node.getPosition().getX();
        }
    }

    /**
     * This function indicates whether the specified node is currnetly one of
     * the selected ones. The outermost node of a possibly decorated node stack
     * is compared to return the same answer for any of the nodes in the
     * decorator stack.
     *
     * @param node The node for which the selection status is sought after.
     * @return True if the node is currently selected, false otherwise.
     */
    public static boolean isSelected(MlMapNode node) {
        for (MlMapNode n : selectedNodes) {
            if (NodeDecoratorManager.getOutermostDecorator(n).equals(NodeDecoratorManager.getOutermostDecorator(node))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determins if the specified node is a clone (i.e. has the same mindliner
     * objects inside) of one of the selected ones.
     *
     * @return True if (a) the specified node points to the same mindliner
     * objects as one of the selcted ones and (b) is not selected itself. False
     * otherwise.
     */
    public static boolean isSelectionClone(MlMapNode node) {
        if (isSelected(node)) {
            return false;
        }
        for (MlMapNode n : selectedNodes) {
            if (node.getObject().equals(n.getObject())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Update the selected nodes only if none of the nodes in the newSelection 
     * is already in the old selection. The expectation is that if the node
     * is already selected then the selection change was probably done in this viewer
     * and so we don't need to do anything.
     *
     * @return True if the newSelection was indeed set, false otherwise.
     */
    public static boolean  maybeSetSelectedNodes(List<MlMapNode> newSelection) {
        for (MlMapNode currentSelection : selectedNodes) {
            if (newSelection.contains(currentSelection)) {
                return false;
            }
        }
        setSelectedNodes(newSelection);
        return true;
    }

    public static void setSelectedNodes(List<MlMapNode> selectedNodes) {
        MapNodeStatusManager.selectedNodes = selectedNodes;
    }

    /**
     * This is a convenience function to get the last item in the selection
     * list.
     *
     * @return The last item in the selection list or null if there are no
     * selected objects.
     */
    public static MlMapNode getCurrentSelection() {
        if (selectedNodes == null || selectedNodes.isEmpty()) {
            return null;
        }
        return (selectedNodes.get(selectedNodes.size() - 1));
    }

    public static List<MlMapNode> getSelectionList() {
        return selectedNodes;
    }

    private static List<mlcObject> getSelectionObjects() {
        List<mlcObject> objectList = new ArrayList<>();
        for (MlMapNode n : selectedNodes) {
            objectList.add(n.getObject());
        }
        return objectList;
    }

    /**
     * Update the current selection and notify the rest of the application
     * through the SelectionManager.
     *
     * @param node
     */
    public static void setCurrentSelection(MlMapNode node) {
        selectedNodes.clear();
        selectedNodes.add(node);
        SelectionManager.setSelection(getSelectionObjects());
    }

    public static void addToCurrentSelection(MlMapNode node) {
        selectedNodes.add(node);
        SelectionManager.setSelection(getSelectionObjects());
    }

    public static void removeFromCurrentSelection(MlMapNode node) {
        selectedNodes.remove(node);
        SelectionManager.setSelection(getSelectionObjects());
    }

    public static void clearSelection() {
        selectedNodes.clear();
    }

    public static MlMapNode getCandidateTargetNode() {
        return candidateTargetNode;
    }

    public static boolean isCandidateTargetNode(MlMapNode node) {
        if (candidateTargetNode == null) {
            return false;
        }
        return candidateTargetNode.equals(node);
    }

    public static void setCandidateTargetNode(MlMapNode candidateTargetNode) {
        MapNodeStatusManager.candidateTargetNode = candidateTargetNode;
    }
}
