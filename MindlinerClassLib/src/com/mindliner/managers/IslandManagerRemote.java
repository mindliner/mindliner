/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.managers;

import javax.ejb.Remote;

/**
 * 
 * @author Marius Messerli
 */
@Remote
public interface IslandManagerRemote {

    /**
     * This method scans all objects of the specified client and segments
     * the objects into topologically closed networks. The call removes all
     * pre-existing islands for the specified client.
     * @param clientId The client for which the islands are to be (re-)initialized.
     */
    void initializeIslands(final int clientId);

    /**
     * This method is a connected component analysis for all objects of the specified client.
     * Islands are topologically closed networks of objects.
     * @param clientId The client for which the islands are to be (re-)initialized.
     */
    void updateIslands(final int clientId);
    
    /**
     * Determine the size of the island that contains the specified object
     * @param objectId The object with which the island is identified
     * @return The number of objects in the island that contains the specified object or -1 if the object can't be found.
     */
    public int getIslandSize(int objectId);
    
    /**
     * Deletes all islands of the specified client. It uses a native query to set
     * the island_id of all objects to null.
     * @param clientId The client for which the islands are to be deleted
     */
    void deleteIslands(final int clientId);
    
}
