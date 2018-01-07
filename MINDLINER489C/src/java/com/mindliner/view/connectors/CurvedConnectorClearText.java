/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.connectors;

import com.mindliner.clientobjects.MlcLink;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import com.mindliner.clientobjects.MlMapNode;
import com.mindliner.view.connectors.NodeConnection.ConnectorType;

/**
 *
 * @author Marius Messerli
 */
public class CurvedConnectorClearText implements mlNodeConnector, Serializable {

    private final int horizontalLead = 20;
    private final int verticalLead = 20;
    private final float alpha = 0.8f;
    private boolean dashed = false;
    private static final long serialVersionUID = 19640205L;
    private final float selectedFactor = 2.5f;

    @Override
    public void setDashed(boolean state) {
        dashed = state;
    }

    @Override
    public float getLineWidth(MlMapNode parent, MlMapNode child) {
        return 2.0f;
    }

    @Override
    public java.util.List<Shape> draw(Graphics g, MlMapNode parent, MlMapNode child, boolean isSelected, MlcLink link, Color color) {

        Graphics2D g2 = (Graphics2D) g;
        Stroke previousStroke = g2.getStroke();

        float lineWidth = getLineWidth(parent, child);
        if (isSelected) {
            lineWidth *= selectedFactor;
        }

        if (dashed) {
            g2.setStroke(DashedStroke.getStroke(lineWidth));
        } else {
            g2.setStroke(new BasicStroke(lineWidth));
        }

        // curved connector
        Point2D parentPosition = parent.getPosition();
        Point2D childPosition = child.getPosition();

        double p1x;
        double p1y;
        double p2x;
        double p2y;
        double p3x;
        double p3y;
        double p4x;
        double p4y;

        double d = 0.5f; // damping
        double angle = 0;
        // parent completely to the right of the child
        if (parentPosition.getX() > childPosition.getX() + child.getSize().width) {
            p1x = parentPosition.getX();
            p1y = parentPosition.getY() + parent.getSize().height / 2;
            p2x = p1x - horizontalLead;
            p2y = p1y;
            p3x = childPosition.getX() + child.getSize().width + horizontalLead;
            p3y = childPosition.getY() + child.getSize().height / 2;
            p4x = p3x - horizontalLead;
            p4y = p3y;
            double dx = p4x - p1x;
            double dy = p4y - p1y;
            // the angle of the arrow depends on the position of child & parent node
            angle = Math.atan(Math.abs(dy) / Math.abs(dx));
            if (dx < 0 && dy > 0) {
                angle = (180 - Math.toDegrees(angle) * d);
                angle = Math.toRadians(angle);
            } else {
                angle = (180 + Math.toDegrees(angle) * d);
                angle = Math.toRadians(angle);
            }
        } else // parent completely left of child
         if (parentPosition.getX() + parent.getSize().width < childPosition.getX()) {
                p1x = parentPosition.getX() + (int) parent.getSize().width;
                p1y = parentPosition.getY() + parent.getSize().height / 2;
                p2x = p1x + horizontalLead;
                p2y = p1y;
                p3x = childPosition.getX() - horizontalLead;
                p3y = childPosition.getY() + child.getSize().height / 2;
                p4x = childPosition.getX();
                p4y = p3y;

                double dx = p4x - p1x;
                double dy = p4y - p1y;
                angle = Math.atan(dy / dx) * d;
            } else // parent above child
             if (parentPosition.getY() < childPosition.getY()) {
                    p1x = parentPosition.getX() + parent.getSize().width / 2;
                    p1y = parentPosition.getY() + parent.getSize().height;
                    p2x = p1x;
                    p2y = p1y + verticalLead;
                    p3x = childPosition.getX() + child.getSize().width / 2;
                    p3y = childPosition.getY() - verticalLead;
                    p4x = p3x;
                    p4y = childPosition.getY();
                    double dx = p4x - p1x;
                    double dy = p4y - p1y;
                    angle = Math.atan(dy / Math.abs(dx));
                    if (dx < 0) {
                        angle = 90 + ((90 - Math.toDegrees(angle)) * d);
                        angle = Math.toRadians(angle);
                    } else {
                        angle = 90 - ((90 - Math.toDegrees(angle)) * d);
                        angle = Math.toRadians(angle);
                    }
                } else // parent below child
                {
                    p1x = parentPosition.getX() + parent.getSize().width / 2;
                    p1y = parentPosition.getY();
                    p2x = p1x;
                    p2y = p1y - verticalLead;
                    p3x = childPosition.getX() + child.getSize().width / 2;
                    p3y = childPosition.getY() + child.getSize().height + verticalLead;
                    p4x = p3x;
                    p4y = p3y - verticalLead;
                    double dx = p4x - p1x;
                    double dy = p4y - p1y;
                    angle = Math.atan(Math.abs(dy) / Math.abs(dx));
                    if (dx < 0) {
                        angle = 270 - ((90 - Math.toDegrees(angle)) * d);
                        angle = Math.toRadians(angle);
                    } else {
                        angle = 270 + ((90 - Math.toDegrees(angle)) * d);
                        angle = Math.toRadians(angle);
                    }
                }

        // set color and draw the line
        Color previousColor = g2.getColor();
        Color lineColor = new Color(
                (float) color.getRed() / 255,
                (float) color.getGreen() / 255,
                (float) color.getBlue() / 255,
                alpha);

        lineColor = parent.getBackgroundPainter().isDark() ? lineColor.brighter() : lineColor.darker();
        g2.setColor(lineColor);
        CubicCurve2D.Double curve = drawConnector(p1x, p1y, p2x, p2y, p3x, p3y, p4x, p4y, g2, link, angle);

        // restore default settings
        g2.setColor(previousColor);
        g2.setStroke(previousStroke);
        java.util.List<Shape> connection = new ArrayList<>();
        connection.add(curve);
        return connection;
    }

    private CubicCurve2D.Double drawConnector(double p1x, double p1y, double p2x, double p2y, double p3x, double p3y, double p4x, double p4y, Graphics2D g2, MlcLink link, double angle) {
        CubicCurve2D.Double curve = new CubicCurve2D.Double(p1x, p1y, p2x, p2y, p3x, p3y, p4x, p4y);
        g2.draw(curve);
        if (link.isIsOneWay()) {
            drawArrow(g2, (float) p4x, (float) p4y, angle);
        }
        if (link.getLabel() != null && !link.getLabel().isEmpty()) {
            double x = (p3x + p2x) / 2 - 2;
            double y = (p3y + p2y) / 2 - 2;
            g2.fillOval((int) x, (int) y, 4, 4);
        }
        return curve;
    }

    private void drawArrow(Graphics2D g, float xx, float yy, double angle) {
        float arrowRatio = 0.7f;
        float arrowLength = 5.0f;

        Path2D.Float path = new Path2D.Float();

        path.moveTo(xx - arrowLength, yy - arrowRatio * arrowLength);
        path.lineTo(xx, yy);
        path.lineTo(xx - arrowLength, yy + arrowRatio * arrowLength);

        AffineTransform at = AffineTransform.getTranslateInstance(0d, 0d);
        at.rotate(angle, xx, yy);
        Shape shape = at.createTransformedShape(path);
        g.draw(shape);
    }

    @Override
    public ConnectorType getConnectorType() {
        return ConnectorType.CurvedTextClear;
    }

}
