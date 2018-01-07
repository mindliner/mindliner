/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.entities;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.swing.ImageIcon;

/**
 * This class holds a "small" image, say no larger than 512x512 or 1kx1k max
 *
 * @author Marius Messerli
 */
@Entity
@Table(name = "images")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorValue(value = "SIMG")
@NamedQueries({
    @NamedQuery(name = "MlsImage.getIconsForUser", query = "SELECT i FROM MlsImage i, IN(i.client.users) u where i.type = :imageType AND u = :user"),
    @NamedQuery(name = "MlsImage.getIconsForDataPool", query = "SELECT i FROM MlsImage i where i.type = :imageType AND i.client.id = :clientId")
})
public class MlsImage extends mlsObject implements Serializable {

    public static enum ImageType {

        Icon,
        ProfilePicture,
        URL,
        Custom
    }
    private static final long serialVersionUID = 19640205L;
    private ImageType type = ImageType.URL;
    private int pixelSizeX = -1;
    private int pixelSizeY = -1;
    private ImageIcon image = null;
    private String url = null;

    @Column(name = "URL")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Column(name = "IMAGE_TYPE")
    @Enumerated(value = EnumType.STRING)
    public ImageType getType() {
        return type;
    }

    public void setType(ImageType type) {
        this.type = type;
    }

    @Column(name = "SIZE_PIXELS_X")
    public int getPixelSizeX() {
        return pixelSizeX;
    }

    public void setPixelSizeX(int pixelSizeX) {
        this.pixelSizeX = pixelSizeX;
    }

    @Column(name = "SIZE_PIXELS_Y")
    public int getPixelSizeY() {
        return pixelSizeY;
    }

    public void setPixelSizeY(int pixelSizeY) {
        this.pixelSizeY = pixelSizeY;
    }

    @Lob
    @Column(name = "IMAGE_DATA")
    public ImageIcon getImage() {
        return image;
    }

    public void setImage(ImageIcon image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return getHeadline() + " (x=" + pixelSizeX + ", y=" + pixelSizeY + ")";
    }

}
