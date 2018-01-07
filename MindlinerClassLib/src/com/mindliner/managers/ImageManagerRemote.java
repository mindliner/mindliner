/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.entities.MlsImage.ImageType;
import com.mindliner.exceptions.NonExistingObjectException;
import java.util.List;
import javax.ejb.Remote;
import javax.swing.ImageIcon;

/**
 * The manager for images.
 *
 * @author Marius Messerli
 */
@Remote
public interface ImageManagerRemote {

    /**
     * Updates the headline, the description, and the image content for the
     * specified image
     *
     * @param imageId The id of the image to be updated
     * @param image The pixel content (the description is also updated from
     * image.getDescription())
     * @param type The image type
     * @param headline The new headline
     * @param URL The url
     * @return
     * @throws NonExistingObjectException
     */
    int updateImage(int imageId, ImageIcon image, ImageType type, String headline, String URL) throws NonExistingObjectException;

    /**
     * Adds the specified icons to the specified client.
     *
     * @param clientId
     * @param icons
     */
    void addIconImages(int clientId, List<ImageIcon> icons);

    void deleteAllIcons(int clientId);

    /**
     * Obtain the IDs of all icons available for the calling user. This function
     * is necessary because icons are blocked by the normal search routines so
     * they don't become available for normal user operations such as
     * edit/delete/link, etc.
     *
     * @return The ids of all the icons for the calling user, use getObjects()
     * to convert these ids to icons.
     */
    public List<Integer> getAllAccessibleIconIds();

    /**
     * Returns the icon images for the specified client.
     *
     * @param clientId The id of the client for which the icons are requested
     * @return A list of IDs of the client's icons
     * @throws com.mindliner.exceptions.NonExistingObjectException If the
     * specified client does not exist.
     */
    public List<Integer> getIconIds(int clientId) throws NonExistingObjectException;

    /**
     * Checks if the specified client has all the icons needed for the
     * Worksphere Map and generates any missing icons.
     *
     * @param clientId
     */
    void ensureDefaultWSMIconSet(int clientId);

}
