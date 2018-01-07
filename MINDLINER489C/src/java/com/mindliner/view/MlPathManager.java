/*
 * Handles various functions with respect to a path of nodes that are connected
 * by links.
 */
package com.mindliner.view;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.MlMapNode;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.managers.SearchManagerRemote;
import com.mindliner.serveraccess.RemoteLookupAgent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.swing.JOptionPane;
import org.jboss.weld.util.collections.ArraySet;

/**
 *
 * @author Marius Messerli
 */
public class MlPathManager {
    
    public static List<Integer> findShortestPath(MlMapNode start, MlMapNode end, int maximumPathLength) {
        List<Integer> results = new ArrayList<>();
        if (start == null || end == null || maximumPathLength == 0) {
            return results;
        }
        SearchManagerRemote searchManager;
        try {
            searchManager = (SearchManagerRemote) RemoteLookupAgent.getManagerForClass(SearchManagerRemote.class);
        } catch (NamingException ex) {
            Logger.getLogger(MapTransferHandler.class.getName()).log(Level.SEVERE, "Failed to access SearchManager for finding shortest path", ex);
            return results;
        }
        // Get shortest path from server
        Set<Integer> leaves = new ArraySet<>();
        leaves.add(start.getObject().getId());
        List<Integer> shortestPath = searchManager.getShortestPath(leaves, end.getObject().getId(), maximumPathLength);
        if (shortestPath == null || shortestPath.isEmpty()) {
            return results;
        }

        // Go through shortest Path withouth displaying anything. This is to make sure the 
        // shortest path does exist. Maybe there are search restrictions that prevent objects
        // on the shortest path to be displayed. @todo: in this case, ask server for another shortestPath and try again
        boolean unreachablePath = false;
        mlcObject next = start.getObject();
        for (int i = 1; i < shortestPath.size(); i++) {
            List<mlcObject> relatives = CacheEngineStatic.getLinkedObjects(next);
            Integer id = shortestPath.get(i);
            next = null;
            for (mlcObject child : relatives) {
                if (child.getId() == id) {
                    next = child;
                    break;
                }
            }
            if (next == null) {
                unreachablePath = true;
                break;
            }
        }
        if (!unreachablePath) {
            System.out.println("Shortest Path:");
            for (Integer i : shortestPath) {
                System.out.println("\nshortest path id = " + i);
                results.add(i);
            }
            return results;
        } else {
            JOptionPane.showMessageDialog(null, "Server suggested shortest path that cannot be reconstructed. Most possibly search restrictions prevent the display.", "Shortest Path", JOptionPane.INFORMATION_MESSAGE);
        }
        return results;
    }

    
}
