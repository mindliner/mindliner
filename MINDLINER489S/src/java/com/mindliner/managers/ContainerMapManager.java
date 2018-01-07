/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.entities.EntityRefresher;
import com.mindliner.entities.MlsContainer;
import com.mindliner.entities.MlsContainerMap;
import com.mindliner.entities.MlsContainermapObjectLink;
import com.mindliner.entities.MlsContainermapObjectPosition;
import com.mindliner.entities.mlsObject;
import com.mindliner.enums.LinkRelativeType;
import com.mindliner.exceptions.MlLinkException;
import com.mindliner.exceptions.NonExistingObjectException;
import com.mindliner.objects.transfer.MltContainer;
import com.mindliner.objects.transfer.MltContainermapObjectLink;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Dominic Plangger
 */
@Stateless
public class ContainerMapManager implements ContainerMapManagerRemote {

    @PersistenceContext
    private EntityManager em;
    @EJB
    UserManagerLocal userManager;

    @EJB
    LinkManagerRemote linker;

    @EJB
    SolrServerBean solrServer;

    @Override
    public void deleteNode(int mapId, int objId) {
        MlsContainerMap map = em.find(MlsContainerMap.class, mapId);
        if (map == null) {
            throw new IllegalArgumentException("Map " + mapId + " does not exist");
        }

        mlsObject obj = em.find(mlsObject.class, objId);

        if (obj == null) {
            throw new IllegalArgumentException("Object " + objId + " does not exist");
        }

        Logger.getLogger(ContainerMapManager.class.getName()).log(Level.FINE, "Removing object {0} from map {1}", new Object[]{objId, mapId});

        // First remove registered object position
        Iterator<MlsContainermapObjectPosition> it = map.getObjectPositions().iterator();
        while (it.hasNext()) {
            MlsContainermapObjectPosition pos = it.next();
            if (obj.equals(pos.getObject())) {
                it.remove();
                em.remove(pos);
                em.flush();
                Logger.getLogger(ContainerMapManager.class.getName()).log(Level.FINE, "Removed object {0} position from map {1}", new Object[]{objId, mapId});
                break;
            }
        }
        // inform about deletion before unlinking. 
        // otherwise a CM cannot distinguish between delete & update
        MlMessageHandler mh = new MlMessageHandler();
        mh.sendMessage(userManager.getCurrentUser(), map, MlMessageHandler.MessageEventType.OBJECT_UPDATE_EVENT, "Map node deleted");
        mh.closeConnection();

        // Second remove all links. A object is either linked to the map itself or to one or several containers, 
        for (mlsObject rel : map.getRelatives()) {
            if (!(rel instanceof MlsContainer)) {
                continue;
            }
            if (obj.getRelatives().contains(rel)) {
                unlink(objId, rel.getId());
            }
        }
        if (map.getRelatives().contains(obj)) {
            unlink(objId, map.getId());
        }
    }

    @Override
    public MltContainermapObjectLink addLink(int mapId, MltContainermapObjectLink tl) {
        int srcId = tl.getSourceObjId();
        int targetId = tl.getTargetObjId();
        MlsContainerMap map = em.find(MlsContainerMap.class, mapId);
        if (map == null) {
            throw new IllegalArgumentException("Map " + mapId + " does not exist");
        }
        mlsObject src = em.find(mlsObject.class, srcId);
        if (src == null) {
            throw new IllegalArgumentException("Source object " + srcId + " does not exist");
        }

        mlsObject trg = em.find(mlsObject.class, targetId);
        if (trg == null) {
            throw new IllegalArgumentException("Target object " + targetId + " does not exist");
        }

        boolean found = false;
        MltContainermapObjectLink oldLink = null;
        for (MlsContainermapObjectLink link : map.getObjectLinks()) {
            if (link.getSourceObject().equals(src) && link.getTargetObject().equals(trg)) {
                oldLink = new MltContainermapObjectLink(link);
                link.setIsOneWay(tl.isIsOneWay());
                link.setCenter(tl.getCenter());
                link.setSrcOffset(tl.getSrcOffset());
                link.setTargetOffset(tl.getTargetOffset());
                link.setSrcPosition(tl.getSrcPosition());
                link.setTargetPosition(tl.getTargetPosition());
                link.setLabel(tl.getLabel());
                link.setLabelPosition(tl.getLabelPosition());
                em.flush();
                found = true;
                Logger.getLogger(ContainerMapManager.class.getName()).log(Level.FINE, "Containermap link {0} -> {1}  already exists in map {2}, only updating link parameters.", new Object[]{srcId, targetId, mapId});
                break;
            }
        }
        if (!found) {
            MlsContainermapObjectLink link = new MlsContainermapObjectLink();
            link.setIsOneWay(tl.isIsOneWay());
            link.setSourceObject(src);
            link.setTargetObject(trg);
            link.setContainerMap(map);
            link.setCenter(tl.getCenter());
            link.setSrcOffset(tl.getSrcOffset());
            link.setTargetOffset(tl.getTargetOffset());
            link.setSrcPosition(tl.getSrcPosition());
            link.setTargetPosition(tl.getTargetPosition());
            link.setLabel(tl.getLabel());
            link.setLabelPosition(tl.getLabelPosition());
            em.persist(link);
            em.flush();
            EntityRefresher.updateCachedEntity(em, map.getId(), map, true);
            Logger.getLogger(ContainerMapManager.class.getName()).log(Level.FINE, "Created new link {0} -> {1} in container map {2}", new Object[]{srcId, targetId, mapId});
        }

        MlMessageHandler mh = new MlMessageHandler();
        mh.sendMessage(userManager.getCurrentUser(), map, MlMessageHandler.MessageEventType.OBJECT_UPDATE_EVENT, "Map link added");
        mh.closeConnection();
        return oldLink;
    }

    @Override
    public void deleteLink(int mapId, int srcId, int targetId) {
        MlsContainerMap map = em.find(MlsContainerMap.class, mapId);
        if (map == null) {
            throw new IllegalArgumentException("Map " + mapId + " does not exist");
        }
        mlsObject src = em.find(mlsObject.class, srcId);
        if (src == null) {
            throw new IllegalArgumentException("Source object " + srcId + " does not exist");
        }
        mlsObject trg = em.find(mlsObject.class, targetId);
        if (trg == null) {
            throw new IllegalArgumentException("Target object " + targetId + " does not exist");
        }

        Iterator<MlsContainermapObjectLink> it = map.getObjectLinks().iterator();
        while (it.hasNext()) {
            MlsContainermapObjectLink link = it.next();
            if (link.getSourceObject().equals(src) && link.getTargetObject().equals(trg)) {
                it.remove();
                em.remove(link);
                em.flush();
                Logger.getLogger(ContainerMapManager.class.getName()).log(Level.FINE, "Removed Containermap link {0} -> {1} in map {2}.", new Object[]{srcId, targetId, mapId});
                break;
            }
        }

        MlMessageHandler mh = new MlMessageHandler();
        mh.sendMessage(userManager.getCurrentUser(), map, MlMessageHandler.MessageEventType.OBJECT_UPDATE_EVENT, "Map link removed");
        mh.closeConnection();
    }

    @Override
    public void addNode(int mapId, int objId, int x, int y, List<Integer> toAdd) {
        MlsContainerMap map = em.find(MlsContainerMap.class, mapId);
        if (map == null) {
            throw new IllegalArgumentException("Map " + mapId + " does not exist");
        }
        mlsObject obj = em.find(mlsObject.class, objId);
        if (obj == null) {
            throw new IllegalArgumentException("Object " + objId + " does not exist");
        }

        // Set object position in map
        MlsContainermapObjectPosition pos = new MlsContainermapObjectPosition();
        pos.setPositionX(x);
        pos.setPositionY(y);
        pos.setObject(obj);
        pos.setContainerMap(map);
        em.persist(pos);
        em.flush();
        EntityRefresher.updateCachedEntity(em, map.getId(), map, true);
        Logger.getLogger(ContainerMapManager.class.getName()).log(Level.FINE, "Added object {0} to map {1} at ({2},{3})", new Object[]{objId, mapId, x, y});

        // Link object to map itself or one or several containers. If the node is not inside any container, the node will be linked directly
        // to the map. Otherwise it will be linked to the containers.
        if (toAdd != null) {
            for (Integer id : toAdd) {
                link(objId, id);
            }
        }

        List<Integer> changed = new ArrayList<>();
        changed.add(mapId); // important to first send update for map. Then the client can check the updated positions when he receives the object update message
        changed.add(objId);

        MlMessageHandler mh = new MlMessageHandler();
        mh.sendBulkMessage(userManager.getCurrentUser(), changed, MlMessageHandler.MessageEventType.OBJECT_UPDATE_EVENT, "Map node added");
        mh.closeConnection();

    }

    @Override
    public void updateNode(int mapId, int objId, int x, int y, List<Integer> toAdd, List<Integer> toDelete) {
        MlsContainerMap map = em.find(MlsContainerMap.class, mapId);
        if (map == null) {
            throw new IllegalArgumentException("Map " + mapId + " does not exist");
        }

        mlsObject obj = em.find(mlsObject.class, objId);
        if (obj == null) {
            throw new IllegalArgumentException("Object " + objId + " does not exist");
        }

        // update position in map object
        for (MlsContainermapObjectPosition pos : map.getObjectPositions()) {
            if (pos.getObject() == null) {
                System.err.println("map position with null object posId = " + pos.getId());
            } else if (pos.getObject().getId() == objId) {
                pos.setPositionX(x);
                pos.setPositionY(y);
                Logger.getLogger(ContainerMapManager.class.getName()).log(Level.FINE, "Updating object positions for object {0} in map {1} to ({2},{3})", new Object[]{objId, mapId, x, y});

                MlMessageHandler mh = new MlMessageHandler();
                List<Integer> changed = new ArrayList<>();
                changed.add(mapId);
                changed.add(objId);
                mh.sendBulkMessage(userManager.getCurrentUser(), changed, MlMessageHandler.MessageEventType.OBJECT_UPDATE_EVENT, "Map node updated");
                mh.closeConnection();

                // relink to new containers
                // should be done after updating position, otherwise linker will send update message to client with old node positions
                if (toDelete != null) {
                    for (Integer id : toDelete) {
                        unlink(objId, id);
                    }
                }
                if (toAdd != null) {
                    for (Integer id : toAdd) {
                        link(objId, id);
                    }
                }

                return;
            }
        }
        throw new IllegalArgumentException("Specified object " + objId + " has no corresponding object position entry in map " + mapId);
    }

    @Override
    public void updateContainer(int contId, int mapId, MltContainer contDetails, List<Integer> objsInside) {
        MlsContainer cont = em.find(MlsContainer.class, contId);
        if (cont == null) {
            throw new IllegalArgumentException("Container " + contId + " does not exist");
        }
        MlsContainerMap map = em.find(MlsContainerMap.class, mapId);
        if (map == null) {
            throw new IllegalArgumentException("Template " + mapId + " does not exist");
        }
        cont.setHeadline(contDetails.getHeadline());
        cont.setPositionX(contDetails.getPosX());
        cont.setPositionY(contDetails.getPosY());
        cont.setWidth(contDetails.getWidth());
        cont.setHeight(contDetails.getHeight());
        cont.setColor(contDetails.getColor());
        cont.setFill(contDetails.getFill());
        cont.setStrokeWidth(contDetails.getStrokeWidth());
        cont.setStrokeStyle(contDetails.getStrokeStyle());

        if (objsInside != null) {
            // unlink all previous objects
            for (mlsObject relative : cont.getRelatives()) {
                if (!map.equals(relative)) {
                    unlink(relative.getId(), cont.getId());
                }
            }
            // link all objects insde the container
            for (Integer id : objsInside) {
                link(contId, id);
            }
            // unlink all objects that were linked directly to the template but are now inside the container
            for (mlsObject relative : map.getRelatives()) {
                if (objsInside.contains(relative.getId())) {
                    unlink(mapId, relative.getId());
                }
            }
        }

        List<Integer> changed = new ArrayList<>();
        changed.add(mapId);
        changed.add(contId);
        
        // index the possibly new headline
        solrServer.addObject(cont, true);

        MlMessageHandler mh = new MlMessageHandler();
        mh.sendBulkMessage(userManager.getCurrentUser(), changed, MlMessageHandler.MessageEventType.OBJECT_UPDATE_EVENT, "Container updated");
        mh.closeConnection();
    }

    private void unlink(int objId, int objId2) {
        linker.unlink(objId, objId2, false, LinkRelativeType.CONTAINER_MAP);
        Logger.getLogger(ContainerMapManager.class.getName()).log(Level.FINE, "Unlinking template node {0} from map/container {1}", new Object[]{objId, objId2});
    }

    private void link(int objId, int objId2) {
        try {
            linker.link(objId, objId2, false, LinkRelativeType.CONTAINER_MAP);
            Logger.getLogger(ContainerMapManager.class.getName()).log(Level.FINE, "Linking template node {0} to map/container {1}", new Object[]{objId, objId2});
        } catch (MlLinkException | NonExistingObjectException ex) {
            Logger.getLogger(ContainerMapManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
