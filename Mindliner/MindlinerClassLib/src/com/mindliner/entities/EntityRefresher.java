/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.entities;

import javax.persistence.EntityManager;

/**
 * This class is used to force the entity manager to update.
 *
 * @author Dominic Plangger, Marius Messerli
 */
public class EntityRefresher {

    /**
     * Refreshes the entity in the l1 cache and removes it from l2 cache. use
     * case: mlsUser.getClients in the cache is not automatically updated when
     * persisting a new UserClientLink object.
     *
     * @param em The entity manager
     * @param id The entity id
     * @param entity The entity reference
     */
    public static void updateCachedEntity(EntityManager em, int id, Object entity) {
        updateCachedEntity(em, id, entity, true);
    }

    public static void updateCachedEntity(EntityManager em, int id, Object entity, boolean refresh) {
        // remove entity from l2 session cache (application-scoped)
        em.getEntityManagerFactory().getCache().evict(entity.getClass(), id);
        // refresh l1 cache (transaction-scoped) if desired (can take some time)
        if (em.contains(entity) && refresh) {
            em.refresh(entity);
        } else if (!refresh) {
            em.detach(entity);
        }
    }

}
