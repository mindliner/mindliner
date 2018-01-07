/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import java.util.Map;
import javax.ejb.Remote;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Marius Messerli
 */
@Remote
public interface ImportManagerRemote {

    /**
     * Imports the specified file. if any of the provided transfer objects has an Id bigger than 0,
     * then the returned map will contain an entry with the old id as key and the new id as value.
     * This is convenient in the case where we want to refer to one of the created objects this method 
     * method returns at client side.
     * 
     * Note: The new root Id is always returned in the map with the entry (-1, newRootId)
     *
     * @param rootObjectPartial
     * @param confidentialityId
     * @param priority
     * @return The transfer object of the root node
     */
    public Map<Integer,Integer> importObjectTree(
            DefaultMutableTreeNode rootObjectPartial, 
            int confidentialityId, 
            int priority);
    
}
