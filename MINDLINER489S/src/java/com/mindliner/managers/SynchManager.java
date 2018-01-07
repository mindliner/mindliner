/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.entities.Syncher;
import com.mindliner.entities.Synchunit;
import com.mindliner.entities.mlsClient;
import com.mindliner.entities.mlsObject;
import com.mindliner.entities.mlsUser;
import com.mindliner.exceptions.NonExistingObjectException;
import com.mindliner.objects.transfer.mltSyncher;
import com.mindliner.objects.transfer.mltSynchunit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * This bean supports synchronization operations between ML and a foreign source
 *
 * @author Marius Messerli
 */
@DeclareRoles(value = {"Admin", "User"})
@RolesAllowed(value = {"Admin", "User"})
@Stateless
public class SynchManager implements SynchManagerRemote {

    @PersistenceContext(name = "MindlinerEE-ejbPU")
    private EntityManager em;
    @EJB
    UserManagerLocal userManager;
    @Resource
    EJBContext ctx;

    private void updateSynchUnits(Syncher syncher, Collection<mltSynchunit> transferUnits) {
        List<mltSynchunit> newUnits = new ArrayList<>();
        mlsUser currentUser = userManager.getCurrentUser();

        // make a server map for fast lookup
        Map<Integer, Synchunit> sMap = new HashMap<>();
        for (Synchunit s : syncher.getSynchUnits()) {
            sMap.put(s.getId(), s);
        }

        // make a transfer map for fast lookup
        Map<Integer, mltSynchunit> tMap = new HashMap<>();
        for (mltSynchunit t : transferUnits) {
            tMap.put(t.getId(), t);
        }

        // now loop through the transfer units and update or create accordingly
        for (mltSynchunit t : transferUnits) {
            if (t.getId() == Synchunit.UNPERSISTED_ID) {
                newUnits.add(t);
            } else {
                Synchunit s = sMap.get(t.getId());
                if (s == null) {
                    newUnits.add(t);
                } else {
                    s.setLastSynched(t.getLastSynched());
                }
            }
        }
        for (mltSynchunit t : newUnits) {
            mlsObject so = em.find(mlsObject.class, t.getMindlinerObjectId());
            if (so != null) {
                Synchunit newUnit = new Synchunit();
                newUnit.setForeignObjectId(t.getForeignObjectId());
                newUnit.setLastSynched(t.getLastSynched());
                newUnit.setMindlinerObject(so);
                so.getSynchUnits().add(newUnit);
                newUnit.setSyncher(syncher);
                syncher.getSynchUnits().add(newUnit);
                em.persist(newUnit);
                MlMessageHandler mh = new MlMessageHandler();
                mh.sendMessage(currentUser, so, MlMessageHandler.MessageEventType.OBJECT_UPDATE_EVENT, "synch units update");
                mh.closeConnection();
            }
        }

        // now loop through the server units and delete accordingly
        for (Iterator<Synchunit> serverUnitsIterator = syncher.getSynchUnits().iterator(); serverUnitsIterator.hasNext();) {
            Synchunit su = serverUnitsIterator.next();
            if (su.getId() != Synchunit.UNPERSISTED_ID) {
                mltSynchunit t = tMap.get(su.getId());
                if (t == null) {
                    // the server object is missing from the transfer list, hence it was deleted
                    su.getMindlinerObject().getSynchUnits().remove(su);
                    serverUnitsIterator.remove();
                    em.remove(su);
                    MlMessageHandler mh = new MlMessageHandler();
                    mh.sendMessage(currentUser, su.getMindlinerObject(), MlMessageHandler.MessageEventType.OBJECT_UPDATE_EVENT, "synch units update");
                    mh.closeConnection();
                }
            }
        }
    }

    @Override
    public Collection<mltSyncher> getSynchers() {
        mlsUser currentUser = userManager.getCurrentUser();
        Query nq = em.createNamedQuery("Syncher.findByUserId");
        nq.setParameter("userId", currentUser.getId());
        Collection<Syncher> synchers = nq.getResultList();
        List<mltSyncher> tSynchers = new ArrayList<>();
        for (Syncher s : synchers) {
            tSynchers.add(new mltSyncher(s));
        }
        return tSynchers;
    }

    private void updateSyncherData(Syncher s, mltSyncher ts) {
        s.setConflictResolution(ts.getConflictResolution());
        s.setDeleteOnMissingCounterpart(ts.isDeleteOnMissingCounterpart());
        s.setIgnoreCompleted(ts.isIgnoreCompleted());
        s.setImmediateForeignUpdate(ts.isImmediateForeignUpdate());
        s.setInitialDirection(ts.getInitialDirection());
        s.setContentCheck(ts.isContentCheck());
        s.setSourceFolder(ts.getSourceFolder());
        s.setCategoryName(ts.getCategoryName());
        updateSynchUnits(s, ts.getSynchUnits());
    }

    @Override
    public mltSyncher updateSyncher(mltSyncher tSyncher) throws NonExistingObjectException {
        Syncher syncher;
        if (tSyncher.getId() == mltSyncher.UNPERSISTED_SYNCHER_ID) {
            throw new IllegalArgumentException("Use storeNewSyncher() for previously unpresisted synchers");
        }
        syncher = em.find(Syncher.class, tSyncher.getId());
        if (syncher == null) {
            throw new NonExistingObjectException("No syncher was found with specified id of " + tSyncher.getId());
        }
        updateSyncherData(syncher, tSyncher);
        em.flush();
        em.refresh(syncher);
        return new mltSyncher(syncher);
    }

    @Override
    public mltSyncher storeNewSyncher(mltSyncher tSyncher) {
        if (tSyncher.getId() != mltSyncher.UNPERSISTED_SYNCHER_ID) {
            throw new IllegalArgumentException("Use updateSyncher() for previously persisted synchers.");
        }
        mlsUser currentUser = userManager.getCurrentUser();
        Syncher newSyncher = new Syncher();
        mlsClient client = em.find(mlsClient.class, tSyncher.getClientId());
        assert client != null : "The specified client does not exist";
        newSyncher.setClient(client);
        newSyncher.setUser(currentUser);
        newSyncher.setSourceBrand(tSyncher.getBrand());
        newSyncher.setSourceType(tSyncher.getType());
        em.persist(newSyncher);
        updateSyncherData(newSyncher, tSyncher);
        em.flush();
        em.refresh(newSyncher);
        return new mltSyncher(newSyncher);
    }

    @Override
    public void deleteSyncher(mltSyncher transferSyncher) {
        Syncher syncher = em.find(Syncher.class, transferSyncher.getId());
        if (syncher != null) {
            for (Synchunit su : syncher.getSynchUnits()) {
                em.remove(su);
            }
            em.remove(syncher);
        }
    }
}
