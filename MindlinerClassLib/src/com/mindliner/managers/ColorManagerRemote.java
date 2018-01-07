/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.exceptions.InsufficientAccessRightException;
import com.mindliner.exceptions.NonExistingObjectException;
import com.mindliner.objects.transfer.MlTransferColorScheme;
import java.util.List;
import javax.ejb.Remote;

/**
 *
 * @author Marius Messerli
 */
@Remote
public interface ColorManagerRemote {

    /**
     * Creates a new color scheme with the specified values.
     * @param scheme
     * @return 
     */
    public int createNewCustomScheme(MlTransferColorScheme scheme) throws NonExistingObjectException;

    /**
     * Returns all accessible color scheme, i.e. the system schemes and the
     * custom schemes belonging to the caller's client.
     * @return A list of system and accessible custom schemes.
     */
    public List<MlTransferColorScheme> getAccessibleSchemes();

    public MlTransferColorScheme getScheme(int schemeId) throws NonExistingObjectException, InsufficientAccessRightException;

    /**
     * Deletes the specified scheme.
     * @param schemeId The id of the scheme to be deleted. If the id is that of a system scheme
     */
    public void deleteCustomScheme(int schemeId) throws InsufficientAccessRightException, NonExistingObjectException;
}
