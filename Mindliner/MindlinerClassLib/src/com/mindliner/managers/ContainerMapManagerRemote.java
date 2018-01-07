/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.managers;

import com.mindliner.objects.transfer.MltContainer;
import com.mindliner.objects.transfer.MltContainermapObjectLink;
import java.util.List;
import javax.ejb.Remote;

/**
 *
 * @author Dominic Plangger
 */

@Remote
public interface ContainerMapManagerRemote {
        
    /**
     * Registers the node in the specified map at the given position and links itself to the
     * list of containers. toAdd can also contain the map if the node is not inside any container.
     * @param mapId
     * @param objId
     * @param x
     * @param y
     * @param toAdd
     */
    public void addNode(int mapId, int objId, int x, int y, List<Integer> toAdd);
    
    /**
     * Used when a node needs to be updated. For example when a node
     * is dragged out from one container into a new one. Then we remove the link to the old one,
     * add a link to the new one and update the positions.
     * @param mapId
     * @param objId
     * @param x
     * @param y
     * @param toAdd
     * @param toDel
     */
    public void updateNode(int mapId, int objId, int x, int y, List<Integer> toAdd, List<Integer> toDel);
    
    /**
     * Updates the shape/color/position/label of a container.
     * @param contId
     * @param tmplId
     * @param contDetails
     * @param objsInside
     */
    public void updateContainer(int contId, int tmplId, MltContainer contDetails, List<Integer> objsInside);
    
    /**
     * Removes a node from a template, thereby removing all links to the containers (or the map itself).
     * @param mapId
     * @param objId
     */
    public void deleteNode(int mapId, int objId);
    
    /**
     * Adds a container map link. May also be used to update an existing link.
     * @param mapId
     * @param link
     * @return The link object BEFORE updating the link. Null if the link did not exist before
     */
    public MltContainermapObjectLink addLink(int mapId, MltContainermapObjectLink link);
    
    /**
     * Removes a link in a container map.
     * @param mapId
     * @param srcId
     * @param targetId 
     */
    public void deleteLink(int mapId, int srcId, int targetId);
    
}
