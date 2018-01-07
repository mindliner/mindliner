/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.containermap;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.enums.Position;
import com.mindliner.objects.transfer.MltContainermapObjectLink;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

/**
 *
 * @author Dominic Plangger
 */
public class ContainerMapUtils {

    public static javafx.scene.image.Image awtToFxImage(Image img) {
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();
        javafx.scene.image.Image fxImg = SwingFXUtils.toFXImage(bimage, null);
        return fxImg;
    }

    /**
     * Computes the offset (ratio) from the point x/y on the given edge p.
     *
     * @param b
     * @param p
     * @param x
     * @param y
     * @return
     */
    public static double computeEdgeOffset(Bounds b, Position p, double x, double y) {
        double startp;
        if (Position.isHorizontal(p)) {
            startp = (x - b.getMinX()) / b.getWidth();
        } else {
            startp = (y - b.getMinY()) / b.getHeight();
        }
        return startp;
    }

    /**
     * Returns the nearest edge of the bounds relative to the point x/y
     *
     * @param b
     * @param x
     * @param y
     * @return
     */
    public static Position computeNearestEdge(Bounds b, double x, double y) {
        Position p;
        Point2D e = new Point2D(x, y);
        double minx = b.getMinX();
        double miny = b.getMinY();
        double w = b.getWidth();
        double h = b.getHeight();
        double left = e.distance(minx, e.getY());
        double right = e.distance(minx + w, e.getY());
        double top = e.distance(e.getX(), miny);
        double bottom = e.distance(e.getX(), miny + h);
        if (top < left && top < right) {
            p = Position.TOP;
        } else if (left <= top && left <= bottom) {
            p = Position.LEFT;
        } else if (bottom < left && bottom < right) {
            p = Position.BOTTOM;
        } else {
            p = Position.RIGHT;
        }
        return p;
    }

    public static Node findElement(double x, double y) {
        FXMLController controller = FXMLController.getInstance();
        Group nodeGroup = controller.getNodeGroup();
        Point2D p = new Point2D(x, y);
        for (Node child : nodeGroup.getChildren()) {
            if (child instanceof MapNode) {
                if (child.getBoundsInParent().contains(p)) {
                    return child;
                }
            } else if (child instanceof MapContainer) {
                MapContainer mc = (MapContainer) child;
                if (mc.isOnBoundary(p)) {
                    return child;
                }
            }
        }
        return null;
    }

    public static MltContainermapObjectLink convertLink(MapLink link) {
        Node start = link.getStartNode();
        Node end = link.getEndNode();
        mlcObject startObj = ((ContainerMapElement) start).getObject();
        mlcObject endObj = ((ContainerMapElement) end).getObject();
        MltContainermapObjectLink tl = new MltContainermapObjectLink();
        tl.setCenter(link.getCenter() == -1 ? 0.5 : link.getCenter());
        tl.setIsOneWay(link.isOneWay());
        tl.setSourceObjId(startObj.getId());
        tl.setTargetObjId(endObj.getId());
        tl.setSrcOffset(link.getStartOffset());
        tl.setTargetOffset(link.getEndOffset());
        tl.setSrcPosition(link.getStartPosition());
        tl.setTargetPosition(link.getEndPosition());
        tl.setLabel(link.getLabel());
        tl.setLabelPosition(link.getLabelPosition());
        return tl;
    }

    public static String colorToString(Color c) {
        String hex = String.format("#%02X%02X%02X",
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255));
        return hex;
    }

    /**
     * Takes a awt color and converts it to a JavaFX color
     * @param awtColor The awt color to be converted
     * @return A JavaFx color
     */
    public static Color convertAwtToFx(java.awt.Color awtColor) {
        return new Color(
                (float) awtColor.getRed() / 255,
                (float) awtColor.getGreen() / 255,
                (float) awtColor.getBlue() / 255,
                1);
    }

    public static void setButtonIcon(Labeled b, String icon, int size) {
        javafx.scene.image.Image img = new javafx.scene.image.Image(ContainerMapUtils.class.getResourceAsStream(icon));
        ImageView iv = new ImageView(img);
        iv.setFitHeight(size);
        iv.setFitWidth(size);
        b.setGraphic(iv);
    }

}
