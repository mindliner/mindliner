/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.entities.mlsClient;
import com.mindliner.entities.mlsObject;
import com.mindliner.entities.mlsUser;
import com.mindliner.exceptions.ForeignClientException;
import com.mindliner.exceptions.NonExistingObjectException;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author Marius Messerli
 */
@Stateless
@DeclareRoles(value = {"MasterAdmin", "Admin", "User"})
public class SecurityManager implements SecurityManagerRemote {

    @PersistenceContext
    private EntityManager em;
    @EJB
    UserManagerLocal userManager;
    @Resource
    private SessionContext sc;

    @Override
    @RolesAllowed(value = "MasterAdmin")
    public int createConfidentiality(int clientId, int level, String name) {
        mlsClient client = em.find(mlsClient.class, clientId);
        if (client != null) {
            mlsConfidentiality c = new mlsConfidentiality();
            c.setClevel(level);
            c.setName(name);
            c.setClient(client);
            em.persist(c);
            em.flush();
            return c.getId();
        }
        return -1;

    }

    @Override
    @RolesAllowed(value = {"Admin", "MasterAdmin"})
    public void updateConfidentialityName(int id, String name) {
        mlsConfidentiality c = em.find(mlsConfidentiality.class, id);
        if (c != null) {
            c.setName(name);
        }
    }

    @Override
    @RolesAllowed(value = {"Admin", "MasterAdmin"})
    public void removeConfidentiality(int confidentialityId, int newConfidentialityId) throws ForeignClientException, NonExistingObjectException {
        mlsConfidentiality newConfidentiality = em.find(mlsConfidentiality.class, newConfidentialityId);
        if (newConfidentiality == null) {
            throw new NonExistingObjectException("The new confidentiality does not exist.");
        }
        mlsUser currentUser = userManager.getCurrentUser();
        mlsConfidentiality confidentialityToBeRemoved = em.find(mlsConfidentiality.class, confidentialityId);

        if (!sc.isCallerInRole("MasterAdmin")) {
            if (currentUser.getClients().contains(confidentialityToBeRemoved.getClient())) {
                throw new ForeignClientException("Confidentiality does not belong to one of caller's data pool. Removal denied.");
            }
        }
        mlsConfidentiality confToBeRemoved = em.find(mlsConfidentiality.class, confidentialityId);
        if (confToBeRemoved == null) {
            throw new NonExistingObjectException("The confidentiality to be removed does not exist on the server.");
        }
        if (!confToBeRemoved.getClient().equals(newConfidentiality.getClient())) {
            throw new ForeignClientException("The replacement confidentiality must belong to the same data pool as the one to be removed");
        }
        Query q = em.createNamedQuery("mlsObject.getObjectsForConfidentiality");
        q.setParameter("confidentialityId", confidentialityId);
        List results = q.getResultList();
        for (Object o : results) {
            mlsObject mo = (mlsObject) o;
            mo.setConfidentiality(newConfidentiality);
        }
        em.remove(confidentialityToBeRemoved);
    }

    @Override
    @RolesAllowed({"Admin", "MasterAdmin", "User"})
    public mlsConfidentiality getConfidentiality(int key) {
        return em.find(mlsConfidentiality.class, key);
    }
    
    @Override
    @RolesAllowed({"Admin", "MasterAdmin", "User"})
    public List<mlsConfidentiality> getAllowedConfidentialities(int clientId, int cLevel) {
        Query q = em.createNamedQuery("mlsConfidentiality.getAllowedConfidentialities");
        q.setParameter("cId", clientId);
        q.setParameter("cLevel", cLevel);
        return q.getResultList();
    }

    @Override
    @RolesAllowed(value = {"User"})
    public List<mlsConfidentiality> getAllAllowedConfidentialities() {
        mlsUser u = userManager.getCurrentUser();
        Query q = em.createNamedQuery("mlsConfidentiality.getAll");
        List<mlsConfidentiality> confis = q.getResultList();

        // now remove the one's that the caller is not authorized to
        Iterator it = confis.iterator();
        while (it.hasNext()) {
            mlsConfidentiality c = (mlsConfidentiality) it.next();
            if (!u.getClients().contains(c.getClient()) || u.getMaxConfidentiality(c.getClient()).compareTo(c) < 0) {
                it.remove();
            }
        }
        return confis;
    }

    @Override
    public List<mlsConfidentiality> getConfidentialities(int clientId) {
        Query q = em.createNamedQuery("mlsConfidentiality.getAllConfidentialities");
        q.setParameter("cId", clientId);
        return q.getResultList();
    }

    @Override
    public void updateConfidentialities(List<mlsConfidentiality> confidentialities) {
        for (mlsConfidentiality c : confidentialities){
            mlsConfidentiality cServer = em.find(mlsConfidentiality.class, c.getId());
            if (cServer != null){
                em.merge(c);
            }
            else{
                mlsClient dataPool = em.find(mlsClient.class, c.getClient().getId());
                if (dataPool != null){
                    c.setClient(dataPool); // ensure we have the managed entity to avoid creation of a new one through cascading
                    dataPool.getConfidentialities().add(c); // double-wire in case ownership of relationship will ever change
                    em.persist(c);
                }
            }
        }
    }

    
}
