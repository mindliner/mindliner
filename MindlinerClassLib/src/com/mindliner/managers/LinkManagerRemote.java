/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.managers;

import com.mindliner.enums.LinkRelativeType;
import com.mindliner.exceptions.ForeignOwnerException;
import com.mindliner.exceptions.MlLinkException;
import com.mindliner.exceptions.NonExistingObjectException;
import java.util.List;
import javax.ejb.Remote;

/**
 *
 * @author Marius Messerli, Dominic Plangger
 */
@Remote
public interface LinkManagerRemote {
    
    /**
     * 
     * @param keyA
     * @param keyB
     * @param oneWay
     * @param type
     * @return an array containing the new version numbers of both objects (first corresponds to keyA, second to keyB)
     * @throws com.mindliner.exceptions.MlLinkException
     * @throws com.mindliner.exceptions.NonExistingObjectException
     */
    public int[] link(int keyA, int keyB, boolean oneWay, LinkRelativeType type) throws MlLinkException, NonExistingObjectException;
    
    /**
     * 
     * @param keyA
     * @param keyB
     * @param oneWay
     * @param type
     * @return an array containing the new version numbers of both objects (first corresponds to keyA, second to keyB)
     */
    public int[] unlink(int keyA, int keyB, boolean oneWay, LinkRelativeType type);
    
    /**
     * Updates the label which gives additional information about the meaning of the link
     * @param linkId
     * @param label A hint about the meaning of the link
     * @throws com.mindliner.exceptions.NonExistingObjectException if no link exists with the specified id
     */
    public void updateLinkLabel(int linkId, String label) throws NonExistingObjectException;

    /**
     * Setting the specified relative to the specified position. If the parent 
     * does not have the specified relative the request is ignored.
     * @param parentId The parent
     * @param relativeId The relative who's position is to be updated
     * @param position The relative position, if less than 0 then 0 is used, if larger than (number of relatives -1) then nor-1 is used.
     * @return The version of the object after the update
     */
    public int setRelativePosition(int parentId, int relativeId, int position);

    /**
     * Links the target to all of the partners if both the target and the partner could be found.
     * @param targetId The id of the target object.
     * @param partnerIds The IDs of all of the partners.
     * @throws com.mindliner.exceptions.MlLinkException
     */
    public void bulkLink(int targetId, List<Integer> partnerIds) throws MlLinkException;
    public void bulkUnLink(int targetId, List<Integer> partnerIds) throws MlLinkException;
}
