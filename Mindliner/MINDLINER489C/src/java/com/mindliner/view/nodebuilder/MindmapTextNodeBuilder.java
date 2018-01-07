/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.nodebuilder;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.mlcNews;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.image.IconLoader;
import com.mindliner.main.SearchPanel;
import com.mindliner.view.background.BackgroundPainter;
import java.util.ArrayList;
import java.util.List;
import com.mindliner.clientobjects.MlMapNode;

/**
 *
 * @author Marius Messerli
 */
public class MindmapTextNodeBuilder extends NodeBuilderImpl {

    public MindmapTextNodeBuilder(BackgroundPainter backgroundPainter) {
        super(backgroundPainter);
    }

    private int maxLevels = 2;

    private MlMapNode addNodeAndChildren(mlcObject object, MlMapNode parent, int level) {
        if (level < maxLevels) { // decide whether to show the object
            MlMapNode decoratedNode = wrapObject(object);
            IconLoader.getInstance().loadIcons(object);
            decoratedNode.setParentNode(parent);
            decoratedNode.setLevel(level);
            decoratedNode.setExpanded(true);
            if (parent != null) {
                parent.addChild(decoratedNode);
            }
            if (level < maxLevels - 1) { // decide whether to show children
                List<mlcObject> relatedObjects = CacheEngineStatic.getLinkedObjects(object);
                if (level < maxLevels - 2) { // pre-fetch grandchildren in single call
                    CacheEngineStatic.loadLinkedObjects(relatedObjects);
                }
                if (!object.isRelativesOrdered()) {
                    SearchPanel.applySelectedSorting(relatedObjects);
                }
                for (mlcObject o : relatedObjects) {
                    if (parent == null || parent.getObject().getId() != o.getId() && !(o instanceof mlcNews)) {
                        addNodeAndChildren(o, decoratedNode, level + 1);
                    }
                }
            }
            return decoratedNode;
        }
        return null;
    }

    /**
     * This function build multiple maps, one for each object passed as
     * parameter.
     *
     * @param objects One or multiple objects that each will server as center of
     * a map.
     * @return
     */
    @Override
    public List<MlMapNode> buildNodes(List<mlcObject> objects) {
        List<MlMapNode> nodes = new ArrayList<>();
        for (mlcObject children : objects) {
            if (!(children instanceof mlcNews)) {
                MlMapNode rootNode = addNodeAndChildren(children, null, 0);
                rootNode.setExpanded(true);
                nodes.add(rootNode);
            }
        }
        return nodes;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevels = maxLevel;
    }

}
