package com.mindliner.json;

import com.mindliner.entities.mlsObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Recursively builds a tree starting from rootTreeNode to be serialized by Gson
 * for the D3.js collapsible tree.
 *
 * @author Ming
 */
public class TreeRelativesJson implements MindlinerObjectJson {

    private TreeNode rootTreeNode = null;
    //keeps track of included nodes
    private final static transient Map<Integer, TreeNode> nodes = new HashMap();

    @Override
    public void addNode(mlsObject parent, mlsObject obj) {

        if (parent == null) { //root node
            rootTreeNode = new TreeNode(obj.getHeadline(), (obj.getRelatives().size() > 0) ? new ArrayList<>() : null, obj.getId());
            nodes.clear();
            nodes.put(obj.getId(), rootTreeNode);
        } else if (!nodes.containsKey(obj.getId())) {
            // obj.getRelatives().contains(parent) query is requried to handle one-way links
            // set children to null to visualize nodes that have no further relatives besides their parent 
            TreeNode n = new TreeNode(obj.getHeadline(),
                    (obj.getRelatives().size() > (obj.getRelatives().contains(parent) ? 1 : 0)) ? new ArrayList<>() : null,
                    obj.getId());
            //add node as child of its parent node
            TreeNode parentNode = nodes.get(parent.getId());
            if (parentNode == null) {
                throw new IllegalStateException("Parent has not been included.");
            }
            parentNode.children.add(n);
            nodes.put(obj.getId(), n);
        }
    }

    static class TreeNode {
        private final String headline;
        private final List<TreeNode> children;
        private final int id;

        public TreeNode(String headline, List<TreeNode> children, int id) {
            this.headline = headline;
            this.id = id;
            this.children = children;
        }
    }
}
