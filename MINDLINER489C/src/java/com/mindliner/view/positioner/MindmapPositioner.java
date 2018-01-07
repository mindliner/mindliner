/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.positioner;

import com.mindliner.commands.CommandRecorder;
import com.mindliner.commands.RelativeOrderUpdateCommand;
import com.mindliner.view.NodeDecoratorManager;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import com.mindliner.clientobjects.MlMapNode;

/**
 * This class arranges a family of nodes so that the child tree grows to the
 * right of the root node and the parent is centered on the y axis on its direct
 * children.
 *
 * @author Marius Messerli
 */
public class MindmapPositioner extends ObjectPositioner {

    public static void moveSibling(MlMapNode parent, MlMapNode sibling, int position) {
        List<MlMapNode> newChildList = new ArrayList<>(parent.getChildren());
        newChildList.remove(sibling);
        newChildList.add(position, sibling);
        parent.setChildren(newChildList);
        CommandRecorder cr = CommandRecorder.getInstance();
        cr.scheduleCommand(new RelativeOrderUpdateCommand(parent.getObject(), sibling.getObject(), position));
    }

    private final int verticalSpace = 5;

    @Override
    protected Dimension getDrawingArea() {
        return super.getDrawingArea();
    }

    protected int getVerticalSpace() {
        return verticalSpace;
    }

    protected int getHorizontalSpace(MlMapNode node) {
        int size = node.getFont().getSize();
        return 5 * size;
    }

    // this function calculates the height of the specified node and its expanded children
    protected double getFamilyHeight(MlMapNode node) {
        double childrenHeight = 0;
        if (node.isExpanded()) {
            for (MlMapNode child : node.getChildren()) {
                childrenHeight += getFamilyHeight(child);
            }
            childrenHeight += (node.getChildren().isEmpty()) ? 0 : getVerticalSpace() * (node.getChildren().size() - 1);
        }
        return Math.max(node.getSize().height, childrenHeight) + getVerticalSpace();
    }

    public void translateFamily(MlMapNode parent, double dx, double dy) {
        if (dx == 0 && dy == 0) {
            return;
        }
        Point2D position = parent.getPosition();
        double currentX = position.getX();
        double currentY = position.getY();
        parent.setPosition(new Point2D.Double(currentX + dx, currentY + dy));
        if (parent.isExpanded()) {
            for (MlMapNode child : parent.getChildren()) {
                translateFamily(child, dx, dy);
            }
        }
    }

    @Override
    public double arrangePositions() {
        int previousYExtent = 0;
        for (MlMapNode center : getNodes()) {
            double familyHeight = getFamilyHeight(center);
            center.setPosition(new Point2D.Double(0.0, previousYExtent + familyHeight / 2));
            arrangeNodeFamilyPosition(center, false);
            previousYExtent += familyHeight;
        }
        return 0;
    }

    /**
     * Arranges, i.e. sets the position, of the specified node and all its
     * expanded children.
     *
     * @param parent The root node of the family tree to arrange
     * @param left True if the node should appear on the left of the maps' root
     * node, false if it should appear on the right
     * @return The total displacement of all nodes that was required to push
     * them into position. Unused for this class - always returns 0.
     */
    @Override
    public double arrangeNodeFamilyPosition(MlMapNode parent, boolean left) {
        Point2D parentPosition = parent.getPosition();
        double parentYCenter = parentPosition.getY() + parent.getSize().height / 2;
        double parentFamilyHeight = getFamilyHeight(parent);
        double nextNodeBottom = parentYCenter + parentFamilyHeight / 2;
        int childCount = parent.getChildren().size();
        for (int i = childCount - 1; i >= 0; i--) { // counting down to have the children arranged top to bottom
            MlMapNode child = parent.getChildren().get(i);
            MlMapNode childDecorator = NodeDecoratorManager.getOutermostDecorator(child);
            double childFamilyHeight = getFamilyHeight(childDecorator);
            double childPosX;
            if (left) {
                childPosX = parentPosition.getX() - getHorizontalSpace(childDecorator) - childDecorator.getSize().width;
            } else {
                childPosX = parentPosition.getX() + getHorizontalSpace(childDecorator) + parent.getSize().width;
            }
            double childPosY;
            if (childCount == 1 && childFamilyHeight < parent.getSize().height) {
                childPosY = nextNodeBottom - (parent.getSize().height + childDecorator.getSize().height) / 2;
            } else {
                childPosY = nextNodeBottom - (childDecorator.getSize().height + childFamilyHeight) / 2;
            }
            nextNodeBottom -= childFamilyHeight;
            if (Math.abs(parentPosition.getY() - childPosY) < 1) {
                childPosY = parentPosition.getY();
            }
            if (!childDecorator.isCustomPositioning()) childDecorator.setPosition(new Point2D.Double(childPosX, childPosY));
            arrangeNodeFamilyPosition(child, left);
        }
        return 0;
    }
}
