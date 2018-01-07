/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.image;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.MlcImage;
import com.mindliner.view.interaction.ZoomPanListener;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.JLabel;

/**
 * This implements a picture label that scales to its size.
 *
 * @author Marius Messerli
 */
public class ScaledImageLabel extends JLabel {

    public enum InitialZoomPrefs {

        Fill, // scale image so that the entire available space is filled
        Fit, // scale so that the entire image is shown
        Original // use the original image resolution
    }

    private final int availableWidth;
    private final int availableHeight;
    private Image image;
    private final ZoomPanListener zoomer;
    private InitialZoomPrefs initialZoom = InitialZoomPrefs.Original;

    public ScaledImageLabel(int availableWidth, int availableHeight, InitialZoomPrefs zoomPrefs) {
        this.availableWidth = availableWidth;
        this.availableHeight = availableHeight;
        this.initialZoom = zoomPrefs;
        zoomer = new ZoomPanListener(this);
        setupListeners();
    }

    private void setupListeners() {
        addMouseListener(zoomer);
        addMouseMotionListener(zoomer);
        addMouseWheelListener(zoomer);
    }
    

    public void setImage(Image img) {
        this.image = img;
        setInitialZoom();
        repaint();
    }

    public void setImage(MlcImage img) {
        if (img != null) {
            image = CacheEngineStatic.getImageSync(img.getId());
            setInitialZoom();
            repaint();

        } else {
            image = null;
        }
    }

    public void setImage(LazyImage limg) {
        image = limg.getImage();
        setInitialZoom();
        repaint();
    }

    public void clear() {
        image = null;
    }

    @Override
    public int getWidth() {
        return availableWidth;
    }

    @Override
    public int getHeight() {
        return availableHeight;
    }

    private void setInitialZoom() {
        double overWidth = (double) image.getWidth(null) / availableWidth;
        double overHeight = (double) image.getHeight(null) / availableHeight;

        switch (initialZoom) {
            case Fill:
                zoomer.setScale(Math.max(1 / overWidth, 1 / overHeight));
                break;

            case Original:
                zoomer.setScale(1D);
                break;

            case Fit:
                zoomer.setScale(Math.min(1 / overWidth, 1 / overHeight));
                break;
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(getWidth(), getHeight());
    }

    @Override
    public void paint(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            Graphics2D g2 = (Graphics2D) g;
            g2.transform(zoomer.getCoordTransform());
            g2.drawImage(image, 0, 0, image.getWidth(null), image.getHeight(null), null);
        } else {
            g.clearRect(0, 0, getWidth(), getHeight());
        }
    }
}
