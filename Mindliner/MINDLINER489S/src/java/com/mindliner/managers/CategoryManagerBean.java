/*
 * TaskManagerBean.java
 *
 * Created on 09.08.2007, 12:12:00
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.categories.*;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author Marius Messerli
 */
@Stateless
public class CategoryManagerBean implements CategoryManagerRemote {

    @PersistenceContext
    private EntityManager em;
    @EJB
    UserManagerLocal userManager;

    @Override
    public mlsPriority storeNewPriority(mlsPriority p) {
        em.persist(p);
        return p;
    }

    @Override
    public void removePriority(Object priority) {
        em.remove(em.merge(priority));
    }

    @Override
    public mlsPriority findPriority(int key) {
        return em.find(mlsPriority.class, key);
    }

    @Override
    public List<mlsPriority> getAllPriorities() {
        Query q = em.createQuery("SELECT p FROM mlsPriority p");
        return q.getResultList();
    }

    @RolesAllowed(value = "Admin")
    @Override
    public mlsMindlinerCategory update(mlsMindlinerCategory mc) {
        return em.merge(mc);
    }

    @RolesAllowed(value = "Admin")
    @Override
    public mlsMindlinerCategory storeNew(mlsMindlinerCategory mc) {
        em.persist(mc);
        return mc;
    }

    @RolesAllowed(value = "Admin")
    @Override
    public void remove(mlsMindlinerCategory mc) {
        em.remove(em.merge(mc));
    }

    @Override
    public mlsMindlinerCategory find(Class c, int key) {
        return (mlsMindlinerCategory) em.find(c, key);
    }

    @Override
    public void storeNewActionItemType(MlsNewsType type) {
        em.persist(type);
    }

    @Override
    public MlsNewsType updateActionItemType(MlsNewsType type) {
        return em.merge(type);
    }

    @Override
    public MlsNewsType findActionItemType(int key) {
        return em.find(MlsNewsType.class, key);
    }

    @Override
    public MlsNewsType findActionItemType(String tag) {
        Query q = em.createNamedQuery("MlsNewsType.findByTag");
        q.setParameter("tag", tag);
        List<MlsNewsType> list = q.getResultList();
        if (list.isEmpty()) {
            throw new IllegalStateException("found no news type for tag " + tag);
        } else if (list.size() > 1) {
            throw new IllegalStateException("found more than one news type for tag " + tag);
        } else {
            return list.get(0);
        }
    }

    @Override
    public List<MlsNewsType> getAllActionItemTypes() {
        Query q = em.createNamedQuery("MlsNewsType.findAll");
        return q.getResultList();
    }

}
