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

import com.mindliner.entities.Island;
import com.mindliner.entities.mlsObject;
import java.util.Iterator;
import javax.persistence.EntityManager;

/**
 * This is a helper class for session beans that need to update the island
 * assignments of objects.
 *
 * @author Marius Messerli
 */
public class IslandUpdater {

    private final EntityManager em;

    public IslandUpdater(EntityManager em) {
        this.em = em;
    }

    /**
     * This call ensures that the island assignments of the two objects are
 updated after o1 link operation.
     *
     * @param o1 The first object that was linked, o1 must be a managed entity
     * @param o2 The second object that was linked, o2 must be a managed entity
     */
    public void reconcileAfterLinking(mlsObject o1, mlsObject o2) {
//        if (o1.getIsland() == null) {
//            if (o2.getIsland() == null) {
//                createNewIsland(o1, o2);
//            } else {
//                // add o1 to o2's island 
//                o1.setIsland(o2.getIsland());
//                o2.getIsland().getObjects().add(o1); try leaving out to save memory
//            }
//        } else {
//            if (o2.getIsland() == null) {
//                o2.setIsland(o1.getIsland());
//                o1.getIsland().getObjects().add(o2); // try leaving out to save memory
//            } else {
//                if (!o1.getIsland().equals(o2.getIsland())) {
//                    if (o1.getIsland().getObjects().size() > o2.getIsland().getObjects().size()) {
//                        reassignIsland(o1.getIsland(), o2.getIsland());
//                    } else {
//                        reassignIsland(o2.getIsland(), o1.getIsland());
//                    }
//                }
//            }
//        }
    }

    private void createNewIsland(mlsObject a, mlsObject b) {
        Island i = new Island();
        i.setClient(a.getClient());
        a.setIsland(i);
        b.setIsland(i);
        i.getObjects().add(a);
        i.getObjects().add(b);
        em.persist(i);
    }

    /**
     * Reassigns the island associations of all objects in the smaller island
     *
     * @param targetIsland The island that will have all the objects after the
     * reassignment
     * @param sourceIsland The island that will be deleted after all objects
     * were re-assigned to targetIsland
     */
    private void reassignIsland(Island targetIsland, Island sourceIsland) {
        for (Iterator it = sourceIsland.getObjects().iterator(); it.hasNext();) {
            mlsObject o = (mlsObject) it.next();
            o.setIsland(targetIsland);
            targetIsland.getObjects().add(o);
        }
        em.remove(sourceIsland);
    }

}
