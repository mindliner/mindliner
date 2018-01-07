/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.clientobjects;

import com.mindliner.entities.MlsImage;
import com.mindliner.entities.ObjectAttributes;
import java.io.Serializable;
import java.util.List;
import javax.swing.ImageIcon;

/**
 * The client version of an image used for icons and profile pictures.
 *
 * @author Marius Messerli
 */
public class MlcImage extends mlcObject implements Serializable {

    private MlsImage.ImageType type;
    private int pixelSizeX;
    private int pixelSizeY;
    private int numberOfChannels;
    private String name = "";
    private String url = "";
    private ImageIcon icon = null; // only used for ImageType.Icon, all other images reside in the ImageCache
    private static final long serialVersionUID = 19640205L;

    public MlsImage.ImageType getType() {
        return type;
    }

    public void setType(MlsImage.ImageType type) {
        this.type = type;
    }

    public int getPixelSizeX() {
        return pixelSizeX;
    }

    public void setPixelSizeX(int pixelSizeX) {
        this.pixelSizeX = pixelSizeX;
    }

    public int getPixelSizeY() {
        return pixelSizeY;
    }

    public void setPixelSizeY(int pixelSizeY) {
        this.pixelSizeY = pixelSizeY;
    }

    public int getNumberOfChannels() {
        return numberOfChannels;
    }

    public void setNumberOfChannels(int numberOfChannels) {
        this.numberOfChannels = numberOfChannels;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Image is only used for ImageType.Icon In all other types, the image is
     * uncoupled from this object and resides in the ImageCache
     *
     * @return
     */
    public ImageIcon getIcon() {
        return icon;
    }

    public void setIcon(ImageIcon image) {
        this.icon = image;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        MlcImage clone = (MlcImage) super.clone();
        clone.setType(type);
        clone.setPixelSizeX(pixelSizeX);
        clone.setPixelSizeY(pixelSizeY);
        clone.setNumberOfChannels(numberOfChannels);
        clone.setName(name);
        clone.setUrl(url);
        clone.setIcon(icon);
        return clone;
    }

    @Override
    public List<ObjectAttributes> getChanges(mlcObject previousState) {
        List<ObjectAttributes> changes = super.getChanges(previousState);
        if (!(previousState instanceof MlcImage)) {
            return changes;
        }
        MlcImage previousImage = (MlcImage) previousState;

        if (url == null) {
            if (previousImage.getUrl() == null) {
                return changes;
            } else {
                changes.add(ObjectAttributes.URL);
            }
        } else if (!url.equals(previousImage.getUrl())) {
            changes.add(ObjectAttributes.URL);
        }

        return changes;
    }

}
