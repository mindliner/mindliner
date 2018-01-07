/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.objects.transfer;

import com.mindliner.entities.MlsImage;
import java.io.Serializable;
import javax.swing.ImageIcon;

/**
 * The transfer class for an image
 *
 * @author Marius Messerli
 */
public class MltImage extends MltObject implements Serializable {

    private MlsImage.ImageType type;
    private int pixelSizeX;
    private int pixelSizeY;
    private ImageIcon image;
    private static final long serialVersionUID = 19640205L;
    private String url;

    public MltImage(MlsImage img) {
        super(img);
        type = img.getType();
        pixelSizeX = img.getPixelSizeX();
        pixelSizeY = img.getPixelSizeY();
        image = img.getImage();
        url = img.getUrl();
    }
    
    public MltImage() {
    }

    public MlsImage.ImageType getType() {
        return type;
    }

    public int getPixelSizeX() {
        return pixelSizeX;
    }

    public int getPixelSizeY() {
        return pixelSizeY;
    }

    public ImageIcon getImage() {
        return image;
    }

    public String getUrl() {
        return url;
    }
    
}
