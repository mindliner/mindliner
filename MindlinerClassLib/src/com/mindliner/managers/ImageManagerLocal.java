/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.entities.MlsImage;
import com.mindliner.entities.mlsClient;
import com.mindliner.exceptions.InsufficientAccessRightException;
import com.mindliner.exceptions.NonExistingObjectException;
import java.util.List;
import javax.ejb.Local;
import javax.swing.ImageIcon;

/**
 * The local interface to image managing functions.
 *
 * @author Marius Messerli
 */
@Local
public interface ImageManagerLocal {

    /**
     * Returns all icons for the specified data pool.
     *
     * @param client The client for which the icons are needed
     * @return A list of icon type images belonging to the data pool
     */
    public List<MlsImage> getIcons(mlsClient client);

    void deleteAllIcons(mlsClient client);

    void addIconImages(mlsClient client, List<ImageIcon> icons);

    /**
     * Retrieves an image by id.
     *
     * @param imageID The image id.
     * @return The image
     * @throws NonExistingObjectException If no image with the specified id
     * exists.
     * @throws InsufficientAccessRightException if the calling user does not
     * have access rights to the image
     */
    MlsImage getImage(int imageID) throws NonExistingObjectException, InsufficientAccessRightException;

    /**
     * Deletes the specified icons unless they are in use
     *
     * @param icons The icons that need to be deleted
     */
    public void deleteUnusedIcons(List<MlsImage> icons);

    /**
     * Checks if the specified client has all the icons needed for the
     * Worksphere Map and generates any missing icons.
     *
     * @param clientId
     */
    void ensureDefaultWSMIconSet(int clientId);

}
