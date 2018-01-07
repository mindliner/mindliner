/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.categories.mlsPriority;
import com.mindliner.entities.MlsImage;
import com.mindliner.entities.MlsLink;
import com.mindliner.entities.mlsClient;
import com.mindliner.entities.mlsKnowlet;
import com.mindliner.entities.mlsObject;
import com.mindliner.entities.mlsObjectCollection;
import com.mindliner.entities.mlsRatingDetail;
import com.mindliner.entities.mlsTask;
import com.mindliner.entities.mlsUser;
import com.mindliner.objects.transfer.MltImage;
import com.mindliner.objects.transfer.MltObject;
import com.mindliner.objects.transfer.mltKnowlet;
import com.mindliner.objects.transfer.mltObjectCollection;
import com.mindliner.objects.transfer.mltTask;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Marius Messerli
 */
@Stateless
@DeclareRoles(value = {"Admin", "User", "MasterAdmin"})
public class ImportManager implements ImportManagerRemote {

    @PersistenceContext
    EntityManager em;
    @EJB
    UserManagerLocal userManager;
    @EJB
    SolrServerBean solrServer;

    private mlsConfidentiality confidentiality;
    private mlsClient client;
    private mlsUser owner;
    private mlsPriority priority;
    private int counter;
    private static final int MAX_ATTACHED_ENTITIES = 500;
    private IslandUpdater iu;
    private Map<Integer, Integer> result;

    @Override
    @RolesAllowed(value = {"Admin", "User"})
    public Map<Integer, Integer> importObjectTree(DefaultMutableTreeNode transferRoot, int confidentialityId, int priority) {
        confidentiality = em.find(mlsConfidentiality.class, confidentialityId);
        this.priority = em.find(mlsPriority.class, priority);
        mlsUser currentUser = userManager.getCurrentUser();
        MltObject ot = (MltObject) transferRoot.getUserObject();
        client = em.find(mlsClient.class, ot.getClientId());
        if (client == null) {
            client = currentUser.getClients().get(0);
        }
        owner = currentUser;
        if (confidentiality == null) {
            return null;
        }
        counter = 1;
        iu = new IslandUpdater(em);
        result = new HashMap<>();
        mlsObject newMapRoot = initializeAndAddParentAndChildren(null, transferRoot, null);
        if (newMapRoot instanceof mlsObjectCollection) {
            em.flush();
            solrServer.commit();
            result.put(-1, newMapRoot.getId());
            return result;
        }
        return null;
    }

    /**
     * This import uses the specified mlsObject and its children only as
     * templates and creates a new set of objects. Perhaps it would be more
     * efficient to persist the whole tree at once??
     *
     * @param persistedParent The persisted parent object.
     * @param transferObject The partial object to be added
     * @param partialParent The parent of the partialObject
     * @return
     */
    private mlsObject initializeAndAddParentAndChildren(mlsObject persistedParent, DefaultMutableTreeNode transferRoot, DefaultMutableTreeNode transferParent) {
        if (counter % MAX_ATTACHED_ENTITIES == 0) {
            // when importing a large number of objects, EM operations eventually slow down (up to 300ms per operation). 
            // The reason for this is apparently the persistence context that cannot cope well with many attached entities.
            // Therefore we clear it after a defined number of added entities.
            em.clear();
            Logger.getLogger(ImportManager.class.getName()).log(Level.INFO, "Cleared persistence context after {0} entities have been added.", MAX_ATTACHED_ENTITIES);
        }
        MltObject transferObject = (MltObject) transferRoot.getUserObject();
        mlsObject o;
        if (transferObject instanceof mltKnowlet) {
            o = new mlsKnowlet();
        } else if (transferObject instanceof mltObjectCollection) {
            o = new mlsObjectCollection();
        } else if (transferObject instanceof mltTask) {
            o = new mlsTask();
        } else if (transferObject instanceof MltImage) {
            o = new MlsImage();
        } else {
            throw new IllegalStateException("imported object type is not supported");
        }
        o.setConfidentiality(confidentiality);
        o.setHeadline(transferObject.getHeadline());
        o.setDescription(transferObject.getDescription());
        o.setClient(client);
        o.setOwner(owner);
        o.setCreationDate(transferObject.getCreationDate());
        o.setModificationDate(transferObject.getModificationDate());
        if (o instanceof mlsTask) {
            mlsTask t = (mlsTask) o;
            t.setPriority(priority);
        }

        mlsRatingDetail rd = new mlsRatingDetail();
        o.setRatingDetail(rd);
        em.persist(rd);
        em.persist(o);
        em.flush();

        solrServer.addObject(o, false);
        // If the transfer object has an assigned Id, we will return the (old id -> new id) mapping
        if (transferObject.getId() > 0) {
            result.put(transferObject.getId(), o.getId());
        }
        if (persistedParent != null) {
            MlsLink link1 = new MlsLink(persistedParent.getId(), o.getId(), owner, client);
            MlsLink link2 = new MlsLink(o.getId(), persistedParent.getId(), owner, client);
            em.persist(link1);
            em.persist(link2);
            iu.reconcileAfterLinking(persistedParent, o);
            em.flush();
        }

        Enumeration e = transferRoot.children();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) e.nextElement();
            // becuase Mindliner is not hierarchical I need to exclude the parent to avoid circular loops
            // &todo this check should not be necessary?
            if (child != transferParent) {
                initializeAndAddParentAndChildren(o, child, transferRoot);
            }
        }
        return o;
    }
}
