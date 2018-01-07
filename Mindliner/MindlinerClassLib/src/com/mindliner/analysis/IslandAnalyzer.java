/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.analysis;

import com.mindliner.entities.Island;
import com.mindliner.entities.mlsClient;
import java.util.Set;

/**
 * This interface defines how Islands are created and analyzed.
 * @author Marius Messerli
 */
public interface IslandAnalyzer {
    
    /**
     * Identifies the islands for the speicified client.
     * 
     * @param client The client for which the islands are to be found.
     * @return The set of islands that contain all objects for the client in closed topological networks.
     */
    public Set<Island> identifyIslands(mlsClient client);
}
