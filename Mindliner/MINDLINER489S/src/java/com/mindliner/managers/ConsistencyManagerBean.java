/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.entities.mlsClient;
import com.mindliner.entities.mlsObject;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * This class performs consistency tests on the data base.
 *
 * @author Marius Messerli
 */
@Stateless
@DeclareRoles(value = {"Admin", "MasterAdmin"})
public class ConsistencyManagerBean implements ConsistencyManagerRemote {

    @PersistenceContext
    private EntityManager em;
    @EJB
    ObjectManagerRemote objectManager;

    private void checkAttributes(mlsObject o) {
        if (!o.getClient().equals(o.getConfidentiality().getClient())) {
            Logger.getLogger(getClass().getName()).log(
                    Level.WARNING,
                    "consistency failure : object with id {0} has confidentiality belonging to foreign client with id {1}",
                    new Object[]{o.getId(), o.getConfidentiality().getId()});
        }
    }

    @Override
    @RolesAllowed(value = {"MasterAdmin"})
    public void checkClientIntegrity(int clientId) {
        mlsClient client = em.find(mlsClient.class, clientId);
        if (client != null) {
            Logger.getLogger(getClass().getName()).info("Started object consistency check for client " + client.getName());
            Query nq = em.createNamedQuery("mlsObject.getObjectsForClient");
            nq.setParameter("clientId", clientId);
            List<mlsObject> objects = nq.getResultList();
            for (mlsObject o : objects) {
                checkAttributes(o);
                for (mlsObject relative : o.getRelatives()) {
                    if (!(o.getClient().equals(relative.getClient()))) {
                        Logger.getLogger(getClass().getName()).log(
                                Level.WARNING,
                                "consistency failure : object with id {0} has a link to object of foreign client with id {1}",
                                new Object[]{o.getId(), relative.getId()});
                    }
                }
            }
        }
    }
}
