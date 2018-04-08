/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.interaction;

import com.mindliner.main.OSValidator;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;

/**
 * Listener that can be attached to a Component to implement Zoom and Pan
 * functionality.
 *
 * Modifications to serve Mindliner by Marius Messerli
 *
 * @author Sorin Postelnicu
 * @since Jul 14, 2009
 */
public class ZoomPanListener implements MouseListener, MouseMotionListener, MouseWheelListener {
    
    public static final int DEFAULT_MIN_ZOOM_LEVEL = -50;
    public static final int DEFAULT_MAX_ZOOM_LEVEL = 20;
    public static final int NEUTRAL_ZOOM_LEVEL = 0;
    public static final double DEFAULT_ZOOM_MULTIPLICATION_FACTOR = 1.05;
    private final JComponent targetCanvas;
    private int zoomLevel = NEUTRAL_ZOOM_LEVEL;
    private final int minZoomLevel = DEFAULT_MIN_ZOOM_LEVEL;
    private final int maxZoomLevel = DEFAULT_MAX_ZOOM_LEVEL;
    private final double zoomMultiplicationFactor = DEFAULT_ZOOM_MULTIPLICATION_FACTOR;
    private Point dragStartScreen;
    private Point dragEndScreen;
    private boolean dragging = false;
    private AffineTransform coordTransform = new AffineTransform();
    
    public ZoomPanListener(JComponent targetComponent) {
        this.targetCanvas = targetComponent;
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        dragStartScreen = e.getPoint();
        dragEndScreen = null;
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
    }

    /**
     * This is a work-around because the redraw sometimes produces a shift. By
     * redrawing on enter I can (mostly) avoid that users click on the wrong
     * spot.
     *
     * @todo find out why the redraw after zoom change does not work
     */
    @Override
    public void mouseEntered(MouseEvent e) {
//        targetCanvas.repaint();
    }
    
    @Override
    public void mouseExited(MouseEvent e) {
    }
    
    @Override
    public void mouseMoved(MouseEvent e) {
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        if (!dragging) {
            moveCamera(e.getPoint());
        }
    }
    
    public boolean isDragging() {
        return dragging;
    }
    
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        zoomCamera(e);
    }
    
    public void setDragging(boolean state, Point dragOrigin) {
        dragging = state;
        if (dragging) {
            dragStartScreen = dragOrigin;
        }
    }

    /**
     * Returns the point the current drag operation started
     *
     * @return The start of the dragging operation in screen coordinates or null
     * if not in dragging mode
     */
    public Point getDragStartScreen() {
        if (!dragging) {
            return null;
        }
        return dragStartScreen;
    }
    
    public JComponent getTargetComponent() {
        return targetCanvas;
    }
    
    private void moveCamera(Point newLocation) {
        try {
            dragEndScreen = newLocation;
            Point2D.Float dragStart = getInverseTransform(dragStartScreen);
            Point2D.Float dragEnd = getInverseTransform(dragEndScreen);
            double dx = dragEnd.getX() - dragStart.getX();
            double dy = dragEnd.getY() - dragStart.getY();
            coordTransform.translate(dx, dy);
            dragStartScreen = dragEndScreen;
            dragEndScreen = null;
            targetCanvas.repaint();
        } catch (NoninvertibleTransformException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage());
        }
    }
    
    private void zoomCamera(MouseWheelEvent e) {
        try {
            int wheelRotation = e.getWheelRotation();
            Point p = e.getPoint();
            if (wheelRotation > 0) {
                if (zoomLevel > minZoomLevel) {
                    zoomLevel--;
                    Point2D p1 = getInverseTransform(p);
                    coordTransform.scale(1 / zoomMultiplicationFactor, 1 / zoomMultiplicationFactor);
                    Point2D p2 = getInverseTransform(p);
                    coordTransform.translate(p2.getX() - p1.getX(), p2.getY() - p1.getY());
                }
            } else if (zoomLevel < maxZoomLevel) {
                zoomLevel++;
                Point2D p1 = getInverseTransform(p);
                coordTransform.scale(zoomMultiplicationFactor, zoomMultiplicationFactor);
                Point2D p2 = getInverseTransform(p);
                coordTransform.translate(p2.getX() - p1.getX(), p2.getY() - p1.getY());
            }
            targetCanvas.repaint();
        } catch (NoninvertibleTransformException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage());
        }
    }
    
    public Point2D.Float getInverseTransform(Point p1) throws NoninvertibleTransformException {
        AffineTransform inverse = coordTransform.createInverse();
        Point2D.Float p2 = new Point2D.Float();
        inverse.transform(p1, p2);
        return p2;
    }
    
    public int getZoomLevel() {
        return zoomLevel;
    }
    
    public void resetZoomAndPan() {
        zoomLevel = NEUTRAL_ZOOM_LEVEL;
        coordTransform = new AffineTransform();
    }
    
    public AffineTransform getCoordTransform() {
        return coordTransform;
    }
    
    public void setCoordTransform(AffineTransform coordTransform) {
        this.coordTransform = coordTransform;
    }
    
    public void translate(double dx, double dy) {
        coordTransform.translate(dx, dy);
    }
    
    public double getScale() {
        return coordTransform.getScaleX();
    }
    
    public void setScale(double scale) {
        coordTransform.setToScale(scale, scale);
    }
    
}
