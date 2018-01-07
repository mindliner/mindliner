/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.background;

import com.mindliner.gui.color.ColorManager;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 *
 * @author Marius Messerli Created on 07.08.2012, 15:45:26
 */
public class ImageBackgroundPainter extends DefaultBackgroundPainter {

    private boolean dark = false;
    private Color averageColor;

    public static enum ImageSample {

        Jeans,
        Bamboo,
        Dunes,
        CobbleStone,
        KahnDhaka,
        KahnFDRPark,
        BlackBoard,
        ParkInRome
    }

    public static enum Layout {

        stretch, center
    }
    private Image image = null;
    private Layout layout = Layout.stretch;

    public ImageBackgroundPainter() {
        try {
            image = ImageIO.read(getClass().getResource("/com/mindliner/img/background/JeansBWBG.jpg"));
            determineCharacteristics();
        } catch (IOException ex) {
            image = null;
            Logger.getLogger(ImageBackgroundPainter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ImageBackgroundPainter(Image image) {
        assert image != null : "A null image is not allowed here";
        this.image = image;
        determineCharacteristics();
    }

    public ImageBackgroundPainter(URL location) {
        try {
            image = ImageIO.read(location);
            determineCharacteristics();
        } catch (IOException ex) {
            Logger.getLogger(ImageBackgroundPainter.class.getName()).log(Level.SEVERE, null, ex);
            image = null;
        }
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public void paint(Graphics2D g2, JPanel panel) {
        if (image == null) {
            super.paint(g2, panel);
        } else {
            switch (layout) {
                case center:
                    int x = (int) ((panel.getSize().getWidth() - image.getWidth(null)) / 2);
                    int y = (int) ((panel.getSize().getHeight() - image.getHeight(null)) / 2);
                    g2.drawImage(image, x, y, getBackground(), null);
                    break;

                case stretch:
                    g2.drawImage(image, 0, 0, (int) panel.getSize().getWidth(), (int) panel.getSize().getHeight(), null);
                    break;

                default:
                    throw new AssertionError();
            }
        }
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    @Override
    public boolean isDark() {
        return dark;
    }

    private void determineCharacteristics() {
        if (image == null || image.getWidth(null) == 0 || image.getHeight(null) == 0) {
            return;
        }
        // Create a buffered image with transparency
        BufferedImage bi = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
        // Draw the image on to the buffered image
        Graphics2D bGr = bi.createGraphics();
        bGr.drawImage(image, 0, 0, null);
        bGr.dispose();

        int[] rgbValues = bi.getRGB(0, 0, bi.getWidth(), bi.getHeight(), null, 0, bi.getWidth());

        // the average intensity is used to determine whether the image is rather bright or rather dark
        double Intensity = 0;
        for (int i = 0; i < rgbValues.length; i++) {
            Intensity += ColorManager.getBrightness(rgbValues[i]);
        }
        double avg = Intensity / rgbValues.length;
        dark = avg <= ColorManager.DARKNESS_THRESHOLD;

        // the average color is used to draw adjacent panels in approximately the same background color
        // note: the red and blue component seem mixed up in the code below (at least to me) but that seems to be the correct layout of the values
        double redAverage = 0;
        double greenAverage = 0;
        double blueAverage = 0;
        int width = image.getWidth(null);
        for (int i = 0; i < width; i++) {
            int pixel = rgbValues[i];
            blueAverage += pixel & 0xff;
            greenAverage += (pixel & 0xff00) >> 8;
            redAverage += (pixel & 0xff0000) >> 16;
        }
        redAverage = redAverage / (double) width;
        greenAverage = greenAverage / (double) width;
        blueAverage = blueAverage / (double) width;
        averageColor = new Color((int) redAverage, (int) greenAverage, (int) blueAverage);
    }

    @Override
    public Color getBackground() {
        return averageColor;
    }

}
