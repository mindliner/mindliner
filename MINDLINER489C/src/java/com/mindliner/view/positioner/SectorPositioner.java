package com.mindliner.view.positioner;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.mindliner.clientobjects.MlMapNode;
import com.mindliner.view.NodeChildCountComparator;
import java.util.Collections;

/**
 *
 * Level one nodes are arranged around the center node at equally spaced angles.
 * If there are more than MAX_FULL_SIZE_CHILD_COUNT child nodes their size and
 * shape is drawn as if they were of a higher level (demotion). The child nodes
 * are place in the sector given by the initial first node angle but if there is
 * not space then a space is scaned for in the entire view.
 *
 * This class was previously called BrizwalkPositioner.
 *
 * @author Marius Messerli
 */
public class SectorPositioner extends ObjectPositioner {

    protected static final double GRID_BREAKER_ANGLE = 5; // this is to avoid a perfect grid if number of children is 1,2,3, or 4
    protected static final double ANGLE_SCAN_INCREMENT = 5D; // degrees
    protected static final double MAX_CHILD_DISTANCE = 300d;
    protected static final double INITIAL_CHILD_SECTOR = 100D;

    // the steps that the scanner takes to find free space (i.e. the max gap between two cells in the scan direction)
    protected final double STEP_SIZE = 10D;
    protected final List<Rectangle2D> occupied = new ArrayList<>();

    protected boolean isInView(Rectangle2D probe) {
        return !(probe.getX() < 0 || probe.getY() < 0
                || probe.getX() + probe.getWidth() > getDrawingArea().getWidth()
                || probe.getY() + probe.getHeight() > getDrawingArea().getWidth());
    }

    protected boolean isAvailable(Rectangle2D probe) {
        for (Rectangle2D r : occupied) {
            if (r.intersects(probe)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the location of the first free slot using an algorithm that
     * probes the preferred direction until the end of the view is reached, then
     * changes the direction and probes again; problem is that the tiles are
     * quite badly scattered.
     *
     * @param preferredAngle The angle at which the object is ideally positioned
     * @param scanStartX Where the space search should start in x ...
     * @param scanStartY ... and y
     * @return
     */
    protected Rectangle2D findSpace(int width, int height, double preferredAngle, double scanStartX, double scanStartY) {
        Rectangle2D probe = new Rectangle2D.Double(scanStartX, scanStartY, width, height);
        ForceVector direction = new ForceVector(preferredAngle / 180 * Math.PI);
        double scanAngle = preferredAngle;
        int iteration = 0;
        while (!isAvailable(probe)) {
            if (isInView(probe)) {
                probe = new Rectangle2D.Double(
                        probe.getX() + direction.getX() * STEP_SIZE,
                        probe.getY() + direction.getY() * STEP_SIZE,
                        width, height);
            } else {
                // restart scan with new angle
                double angleOffset = ANGLE_SCAN_INCREMENT * iteration++ / 2;
                if (iteration % 2 == 0) {
                    scanAngle += angleOffset;
                } else {
                    scanAngle -= angleOffset;
                }
                // stop if we don't find space in a 120 degree angle along the preferred direction
                double distX = probe.getCenterX() < scanStartX ? scanStartX - probe.getCenterX() : probe.getCenterX() - scanStartX;
                double distY = probe.getCenterY() < scanStartY ? scanStartY - probe.getCenterY() : probe.getCenterY() - scanStartY;
                double distance = Math.sqrt(distX * distX + distY * distY);
                if (angleOffset > 60 || distance > MAX_CHILD_DISTANCE) {
                    probe = null;
                    break;
                }
                direction = new ForceVector(scanAngle);
                probe = new Rectangle2D.Double(scanStartX, scanStartY, width, height);
            }
        }
        return probe;
    }

    /**
     * Arranges the parent's children around the direction specified
     *
     * @param parent The parent
     * @param angle The overall direction in which free space is
     * @param angleRoaming The range around the free space direction where the
     * children can be available
     */
    private void arrangeChildren(MlMapNode parent, Double angle, Double angleRoaming, int level) {
        if (parent.getChildren().isEmpty() || !parent.isExpanded()) {
            return;
        }
        boolean fullHouse = false;
        Map<MlMapNode, Double> childAngles = new HashMap<>();
        double startAngle = angle - angleRoaming / 2;
        double angleStep = angleRoaming / parent.getChildren().size();
        int childCount = parent.getChildren().size();
        List<MlMapNode> sorted = new ArrayList<>(parent.getChildren());
        Collections.sort(sorted, Collections.reverseOrder(new NodeChildCountComparator()));
        int topIndex = 0;
        int bottomIndex = childCount - 1;
        for (int i = 0; !fullHouse && i < childCount; i++) {
            MlMapNode child;
            if (i % 2 == 0) {
                child = sorted.get(bottomIndex--);
            } else {
                child = sorted.get(topIndex++);
            }
            double childAngle = startAngle + i * angleStep;
            childAngles.put(child, childAngle);

            Rectangle2D space = findSpace(child.getSize().width, child.getSize().height, childAngle, parent.getPosition().getX(), parent.getPosition().getY());
            if (space == null) {
                fullHouse = true;
            } else {
                child.setPosition(new Point2D.Double(space.getX(), space.getY()));
                child.setSize((int) space.getWidth(), (int) space.getHeight());
                occupied.add(space);
            }
        }
        if (!fullHouse) {
            // only add grandchildren after all children were place (breadth first addition)
            for (int i = 0; i < parent.getChildren().size(); i++) {
                MlMapNode child = parent.getChildren().get(i);
                Double ca = childAngles.get(child);
                if (ca == null) {
                    System.err.println("child angle not defined for node " + child.getObject().getHeadline());
                } else {
                    arrangeChildren(child, childAngles.get(child), INITIAL_CHILD_SECTOR, ++level);
                }
            }
        }
    }

    @Override
    public double arrangePositions() {
            occupied.clear();
        if (getNodes().isEmpty()) {
            return 0D;
        }

        MlMapNode root = getNodes().get(0);
        Rectangle2D firstSpace = findSpace(root.getSize().width, root.getSize().height, 0,
                (getDrawingArea().getWidth() - root.getSize().width) / 2,
                (getDrawingArea().getHeight() - root.getSize().height / 2));
        if (firstSpace != null) {
            root.setPosition(new Point2D.Double(firstSpace.getX(), firstSpace.getY()));
            root.setSize((int) firstSpace.getWidth(), (int) firstSpace.getHeight());
        }
        occupied.add(firstSpace);
        arrangeChildren(root, GRID_BREAKER_ANGLE, 360D, 0);
        return 0D;
    }

    @Override
    public double arrangeNodeFamilyPosition(MlMapNode branchHeadNode, boolean left) {
        arrangeChildren(branchHeadNode, GRID_BREAKER_ANGLE, 360D, 0);
        return 0.0;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName().concat(": ").concat("keeps children close to parents");
    }

}
