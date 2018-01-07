/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.img.tools;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * This is a simple class to re-size a 2D image.
 *
 * @author Marius Messerli, copied from
 * http://www.mkyong.com/java/how-to-resize-an-image-in-java/
 */
public class ImageResizer {

    public static BufferedImage resize(Image input, int newWidth, int newHeight) {
        if (input == null) {
            return null;
        }
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(input, 0, 0, newWidth, newHeight, null);
        g.dispose();
        g.setComposite(AlphaComposite.Src);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        return resizedImage;
    }
}
