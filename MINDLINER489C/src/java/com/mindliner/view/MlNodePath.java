/*
 * Manages an array of nodes that are linked to eath other in sequence.
 */
package com.mindliner.view;

import com.mindliner.clientobjects.MlMapNode;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marius Messerli
 */
public class MlNodePath {

    private ArrayList<MlMapNode> path = new ArrayList<>();

    /**
     * Constructor takes a start and end and tries to find a path no longer
     * than the specified length.
     * @param start The node from which to start
     * @param end The node to end the path
     * @param maxPathSearchLength The maximum allowed path search length
     */
    public MlNodePath(MlMapNode start, MlMapNode end, int maxPathSearchLength) {
        List<Integer> shortestPath = MlPathManager.findShortestPath(start, end, maxPathSearchLength);
        if (shortestPath != null && !shortestPath.isEmpty()){
            System.out.println("continue here...");
        }
    }
    
    

}
