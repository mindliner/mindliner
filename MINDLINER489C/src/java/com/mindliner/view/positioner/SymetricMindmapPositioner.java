/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.positioner;

import com.mindliner.view.NodeDecoratorManager;
import java.awt.Point;
import java.awt.geom.Point2D;
import com.mindliner.clientobjects.MlMapNode;

/**
 *
 * @author Marius Messerli 
 * Created on 12.09.2012, 16:33:42
 */
public class SymetricMindmapPositioner extends MindmapPositioner {

    /**
     * There are obviously many ways to do this. The most symetric tree would
     * have the least height difference between left and right. The problem with
     * that is that each time I expand/collapse a node or make additions or
     * deletions that parent may actually have to be moved to the other side
     * which causes a lot of confusion while editing.
     *
     * Therefore I choose the simple scheme to place half of the nodes (evtl +1)
     * on the right side and half of the nodes on the left, no matter how large
     * they actually are.
     *
     */
    private double arrangeFirstLevelNodes(MlMapNode root) {
        MlMapNode rootDecorator = NodeDecoratorManager.getOutermostDecorator(root);
        double totalLeftHeight = 0;
        double totalRightHeight = 0;
        for (int i = 0; i < root.getChildren().size(); i++) {
            if (i > root.getChildren().size() / 2) {
                totalLeftHeight += getFamilyHeight(root.getChildren().get(i));
            } else {
                totalRightHeight += getFamilyHeight(root.getChildren().get(i));
            }
        }
        double nextNodeRightY = root.getPosition().getY() - totalRightHeight / 2;
        double nextNodeLeftY = root.getPosition().getY() - totalLeftHeight / 2;
        for (int i = 0; i < root.getChildren().size(); i++) {
            MlMapNode child = root.getChildren().get(i);
            double levelOneFamilyHeight = getFamilyHeight(child);
            boolean addLeft = i > root.getChildren().size() / 2;
            if (addLeft) {
                double yPosition = nextNodeLeftY + levelOneFamilyHeight / 2;
                child.setPosition(
                        new Point2D.Double(
                        -getHorizontalSpace(rootDecorator) + child.getSize().width,
                        yPosition));
                nextNodeLeftY += levelOneFamilyHeight;
            } else {
                double yPosition = nextNodeRightY + levelOneFamilyHeight / 2;
                child.setPosition(
                        new Point2D.Double(
                        getHorizontalSpace(rootDecorator) + rootDecorator.getSize().height,
                        yPosition));
                nextNodeRightY += levelOneFamilyHeight;
            }
            arrangeNodeFamilyPosition(child, addLeft);
        }
        return Math.max(totalRightHeight, totalLeftHeight);
    }

    @Override
    public double arrangePositions() {
        int previousYExtent = 0;
        for (MlMapNode center : getNodes()) {
            double familyHeight = getFamilyHeight(center);
            center.setPosition(new Point(0, previousYExtent));
            arrangeFirstLevelNodes(center);
            previousYExtent += familyHeight / 2;
        }
        return 0;
    }
}
