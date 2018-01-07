/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.managers;

import com.mindliner.exceptions.NonExistingObjectException;
import com.mindliner.objects.transfer.mltSyncher;
import java.util.Collection;
import javax.ejb.Remote;

/**
 * This interface provides interaction with the synch subsystem.
 * @author Marius Messerli
 */
@Remote
public interface SynchManagerRemote {

    /**
     * This call returns all the synchers for the caller and the caller's current client.
     * @return The available synchers
     */
    public Collection<mltSyncher> getSynchers();
    
    /**
     * Updates the specified synch units back to the server. Existing units will
     * be updated if changed, units missing in the argument Collection will be deleted 
     * from the server and units that are new will be created on the server
     * @param syncher
     * @return The new syncher with all items having their new ID set.
     * @throws com.mindliner.exceptions.NonExistingObjectException If no syncher with the id of syncher.getId() exists
     */
    public mltSyncher updateSyncher(mltSyncher syncher) throws NonExistingObjectException;
    
    /**
     * This call deletes the specified syncher with all its synchunits. This
     * will delete all traces of any previous synchs and may lead to duplication
     * if the same user is installing a similar syncher again.
     * @param syncher 
     */
    public void deleteSyncher(mltSyncher syncher);    
    
    /**
     * Stores a new syncher with its synch units, if any.
     * @param syncher The new transfer syncher to be stored.
     * @return The syncher with all the of the new elements set correctly.
     */
    public mltSyncher storeNewSyncher(mltSyncher syncher);
        
}
