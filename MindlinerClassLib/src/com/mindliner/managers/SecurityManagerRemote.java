/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.exceptions.ForeignClientException;
import com.mindliner.exceptions.NonExistingObjectException;
import java.util.List;
import javax.ejb.Remote;

/**
 *
 * @author Marius Messerli
 */
@Remote
public interface SecurityManagerRemote {

    public void updateConfidentialityName(int id, String name);

    /**
     * Updates the specified confidentialities on the server and creates any new confidentialities in the list (i.e. id < 0)
     * @param confidentialities The confidentialities for which name and level are to be merged. If a confidentiality cannot be found by its id the values for that conf are ignored.
     */
    public void updateConfidentialities(List<mlsConfidentiality> confidentialities);

    /**
     * The call allows the master admin to create confidentiality levels for any
     * client.
     *
     * @param clientId The client's id.
     * @param level The numeric confidentiality strength (larger means more
     * confidential)
     * @param name The name of that conf level
     * @return The id of the new level or -1 if the specified client could not
     * be found
     */
    public int createConfidentiality(int clientId, int level, String name);

    /**
     * Removes the specified ID after having mapped all objects wtih that
     * confidentiality to the new confidentiality. If the caller is not in role
     * MasterAdmin an exception is thrown in case the confidentiality does not
     * belong to the caller's client.
     *
     * @param id The id of the confidentiality to be deleted.
     * @param replacementConfidentialityId The id of the confidentiality to
     * which any and all objects with the confidentiality to be deleted will be
     * mapped
     * @exception NonExistingObjectException is thrown if the new
     * confidentiality does not exist
     * @exception ForeignClientException is thrown if the confidentiality to be
     * deleted belongs to anotehr client and the caller ist not in role
     * MasterAdmin
     */
    public void removeConfidentiality(int id, int replacementConfidentialityId) throws ForeignClientException, NonExistingObjectException;

    /**
     * Returns the specified confidentiality.
     *
     * @param key
     * @return
     */
    public mlsConfidentiality getConfidentiality(int key);

    /**
     * Returns all confidentialities for the specified client.
     *
     * @param clientId The client for which the confidentialities are requested
     * @return
     */
    public List<mlsConfidentiality> getConfidentialities(int clientId);

    /**
     * The function returns all confidentialities for the caller's client up to
     * the caller's max confidentiality level.
     *
     * @return
     */
    public List<mlsConfidentiality> getAllAllowedConfidentialities();
    
    /**
     * Returns allowed confidentialities (equal and below clevel) for the specified client.
     * 
     * @param clientId The client for which the confidentialities are requested
     * @param cLevel
     * @return 
     */
    public List<mlsConfidentiality> getAllowedConfidentialities(int clientId, int cLevel);

}
