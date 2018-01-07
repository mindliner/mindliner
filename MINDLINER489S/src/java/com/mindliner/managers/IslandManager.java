package com.mindliner.managers;

import com.mindliner.analysis.DisjointSet;
import com.mindliner.entities.Island;
import com.mindliner.entities.mlsClient;
import com.mindliner.entities.mlsObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * This class finds, updates, and manages islands, i.e. connected components in
 * the object network.
 *
 * @author Marius Messerli
 */
@Stateless
public class IslandManager implements IslandManagerRemote {

    @PersistenceContext
    EntityManager em;

    @Override
    public void initializeIslands(final int clientId) {
        mlsClient client = em.find(mlsClient.class, clientId);
        if (client == null) {
            return;
        }
        deleteIslands(clientId);
        updateIslands(clientId);
    }

    @Override
    public int getIslandSize(int objectId) {
        mlsObject o = em.find(mlsObject.class, objectId);
        if (o == null) {
            return -1;
        }
        if (o.getIsland() == null) {
            return 0;
        }
        return o.getIsland().getObjects().size();
    }

    @Override
    public void updateIslands(final int clientId) {
        mlsClient client = em.find(mlsClient.class, clientId);
        if (client == null) {
            return;
        }
        em.flush();

        // loads objects who are not associated with any island
        Query q = em.createNamedQuery("mlsObject.getStandaloneObjectsForClient");
        q.setParameter("clientId", client.getId());
        List<mlsObject> resultList;

        resultList = q.getResultList();

        // In the first pass we put each object into its own set
        DisjointSet ds = new DisjointSet();
        for (mlsObject o : resultList) {
            ds.makeSet(o.getId());
        }
        // In the second path we merge objects into sets according to the links (edges)
        for (mlsObject o : resultList) {
            for (mlsObject r : o.getRelatives()) {
                ds.union(o.getId(), r.getId());
            }
        }

        // now we create the islands
        Map<Long, List<Integer>> tmpIslands = new HashMap<>();
        for (mlsObject o : resultList) {
            long setId = ds.findSet(o.getId());
            List<Integer> islandElements = tmpIslands.get(setId);
            if (islandElements == null){
                // create a new island and put this object as its first member
                List<Integer> newlist = new ArrayList<>();
                newlist.add(o.getId());
                tmpIslands.put(setId, newlist);
            }
            else{
                islandElements.add(o.getId());
            }
        }
        
        // output result
        System.out.println("Total of " + tmpIslands.size() + " islands determined for client " + client);
        
        // create islands
        for (Long id : tmpIslands.keySet()){
            List<Integer> islandMemberIds = tmpIslands.get(id);
            Island persistentIsland = new Island();
            persistentIsland.setClient(client);
            client.getIslands().add(persistentIsland);
            for (Integer i : islandMemberIds){
                mlsObject o = em.find(mlsObject.class, i);
                persistentIsland.getObjects().add(o);
                o.setIsland(persistentIsland);
            }
            em.persist(persistentIsland);
        }
    }

    @Override
    public void deleteIslands(final int clientId) {
        // set all island references to null (must be native, takes too much time through relations
        Query q = em.createNativeQuery("UPDATE objects SET ISLAND_ID = null WHERE CLIENT_ID = " + clientId);
        q.executeUpdate();

        // delete all Island records
        q = em.createNamedQuery("Island.deleteForClient");
        q.setParameter("clientId", clientId);
        q.executeUpdate();
    }

}
