/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.categories.MlsEventType;
import com.mindliner.comparatorsS.LinkPositionComparator;
import com.mindliner.entities.EntityRefresher;
import com.mindliner.entities.MlsImage;
import com.mindliner.entities.MlsImage.ImageType;
import com.mindliner.entities.MlsLink;
import com.mindliner.entities.mlsLog;
import com.mindliner.entities.mlsObject;
import com.mindliner.entities.mlsUser;
import com.mindliner.enums.LinkRelativeType;
import com.mindliner.exceptions.MlLinkException;
import com.mindliner.exceptions.NonExistingObjectException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.annotation.security.DeclareRoles;
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
@DeclareRoles(value = {"Admin", "User"})
@RolesAllowed(value = {"Admin", "User"})
public class LinkManager implements LinkManagerRemote, LinkManagerLocal {

    @PersistenceContext
    private EntityManager em;
    @EJB
    UserManagerLocal userManager;
    @EJB
    LogManagerLocal logManager;

    @Override
    public int[] link(int keyA, int keyB, boolean oneWay, LinkRelativeType type) throws MlLinkException, NonExistingObjectException {
        if (!LinkRelativeType.OBJECT.equals(type) && !LinkRelativeType.ICON_OBJECT.equals(type) && !LinkRelativeType.CONTAINER_MAP.equals(type)) {
            throw new MlLinkException(type + " not supported!");
        }

        boolean updatedA = false;
        boolean updatedB = false;

        mlsObject oA = em.find(mlsObject.class, keyA);
        mlsObject oB = em.find(mlsObject.class, keyB);

        if (oA == null) {
            throw new NonExistingObjectException("Object with specified id " + keyA + " does not exist");
        }
        if (oB == null) {
            throw new NonExistingObjectException("Object with specified id " + keyB + " does not exist");
        }
        mlsUser currentUser = userManager.getCurrentUser();

        assert currentUser != null : "Current user must not be null";

        if (!oA.getRelatives().contains(oB)) {
            MlsLink link = new MlsLink(oA, oB, currentUser);
            link.setRelativeType(type);
            if (oA.isRelativesOrdered()) {
                // note: it must be confusing that we don't take size() -1; however getRelatives() will only be updated after a cache update which happens further down
                link.setRelativeListPosition(oA.getRelatives().size());
            }
            em.persist(link);
            updatedA = true;
            oA.setRelativeCount(oA.getRelativeCount() + 1);
        }
        if (!oneWay) {
            if (!oB.getRelatives().contains(oA)) {
                MlsLink link = new MlsLink(oB, oA, currentUser);
                link.setRelativeType(type);
                if (oB.isRelativesOrdered()) {
                    // node: see above note for oA
                    link.setRelativeListPosition(oB.getRelatives().size());
                }
                em.persist(link);
                updatedB = true;
                oB.setRelativeCount(oB.getRelativeCount() + 1);
            }
        }

        int[] vers = {-1, -1};
        if (updatedA || updatedB) {
            IslandUpdater iu = new IslandUpdater(em);
            iu.reconcileAfterLinking(oA, oB);
            // the cached value of mlsObject.getRelatives() isn't updated automatically with the new relative
            if (updatedA) {
                // adding a new link does not automatically increase lock version of the mlsObject.
                // But as we use the version number to check for changes, we need to
                // increase it too. That can be done through the LockModeType.OPTIMISTIC_FORCE_INCREMENT
                em.lock(oA, javax.persistence.LockModeType.OPTIMISTIC_FORCE_INCREMENT);
                em.flush();
                vers[0] = oA.getVersion();
                EntityRefresher.updateCachedEntity(em, oA.getId(), oA, true);
                logManager.log(oA.getClient(), MlsEventType.EventType.ObjectLinked, oA, keyB, oA.getHeadline(), "link", mlsLog.Type.Link);
            }
            if (updatedB) {
                em.lock(oB, javax.persistence.LockModeType.OPTIMISTIC_FORCE_INCREMENT);
                em.flush();
                vers[1] = oB.getVersion();
                EntityRefresher.updateCachedEntity(em, oB.getId(), oB, true);
                logManager.log(oB.getClient(), MlsEventType.EventType.ObjectLinked, oB, keyA, oB.getHeadline(), "link", mlsLog.Type.Link);
            }
            MlMessageHandler mh = new MlMessageHandler();
            mh.sendMessage(currentUser, oA, MlMessageHandler.MessageEventType.OBJECT_UPDATE_EVENT, "linking");
            mh.sendMessage(currentUser, oB, MlMessageHandler.MessageEventType.OBJECT_UPDATE_EVENT, "linking");
            mh.closeConnection();
        }
        return vers;
    }

    private void compressLinkOrderSequence(mlsObject parent) {
        Query q = em.createNamedQuery("MlsLink.getObjectRelatives");
        q.setParameter("id", parent.getId());
        q.setParameter("relativeType", LinkRelativeType.OBJECT);
        List<MlsLink> links = q.getResultList();
        Collections.sort(links, new LinkPositionComparator());
        for (int i = 0; i < links.size(); i++) {
            MlsLink l = links.get(i);
            l.setRelativeListPosition(i);
        }
    }

    @Override
    public int[] unlink(int keyA, int keyB, boolean oneWay, LinkRelativeType type) {
        boolean updatedA = false;
        boolean updatedB = false;

        mlsObject oA = em.find(mlsObject.class, keyA);
        mlsObject oB = em.find(mlsObject.class, keyB);

        // @todo: fetching relatives only to track non-cell-relatives? 
        // for performance tuning: query link table directly to see whether oA has a link to oB and vice-versa
        if (oA.getRelatives().contains(oB)) {
            updatedA = true;
            oA.setRelativeCount(oA.getRelativeCount() - 1);
        }

        if (!oneWay) {
            if (oB.getRelatives().contains(oA)) {
                updatedB = true;
                oB.setRelativeCount(oB.getRelativeCount() - 1);
            }
        }

        int[] vers = {-1, -1};
        if (updatedA || updatedB) {
            Query q;
            if (oneWay) {
                q = em.createNamedQuery("MlsLink.removeOneWayLink");
            } else {
                q = em.createNamedQuery("MlsLink.removeLink");
            }
            q.setParameter("id1", oA.getId());
            q.setParameter("id2", oB.getId());
            q.setParameter("relativeType", type);
            q.executeUpdate();
            if (updatedA) {
                em.lock(oA, javax.persistence.LockModeType.OPTIMISTIC_FORCE_INCREMENT);
                em.flush();
                vers[0] = oA.getVersion();
                EntityRefresher.updateCachedEntity(em, oA.getId(), oA, false);
                compressLinkOrderSequence(oA);
                logManager.log(oA.getClient(), MlsEventType.EventType.ObjectUnlinked, oA, keyB, oA.getHeadline(), "unlink", mlsLog.Type.Link);
            }
            if (updatedB) {
                em.lock(oB, javax.persistence.LockModeType.OPTIMISTIC_FORCE_INCREMENT);
                em.flush();
                vers[1] = oB.getVersion();
                EntityRefresher.updateCachedEntity(em, oB.getId(), oB, false);
                compressLinkOrderSequence(oB);
                logManager.log(oB.getClient(), MlsEventType.EventType.ObjectUnlinked, oB, keyA, oB.getHeadline(), "unlink", mlsLog.Type.Link);
            }
            MlMessageHandler mh = new MlMessageHandler();
            mh.sendMessage(userManager.getCurrentUser(), oA, MlMessageHandler.MessageEventType.OBJECT_UPDATE_EVENT, "unlinking");
            mh.sendMessage(userManager.getCurrentUser(), oB, MlMessageHandler.MessageEventType.OBJECT_UPDATE_EVENT, "unlinking");
            mh.closeConnection();
        }
        return vers;
    }

    @Override
    public void updateLinkLabel(int linkId, String label) throws NonExistingObjectException {
        MlsLink l = em.find(MlsLink.class, linkId);
        if (l != null) {
            l.setLabel(label);
            l.setModificationDate(new Date());
        } else {
            throw new NonExistingObjectException("No link with the specified id was found");
        }
    }

    @Override
    @RolesAllowed(value = {"User"})
    public int setRelativePosition(int parentId, int relativeId, int newPosition) {
        mlsObject parent = em.find(mlsObject.class, parentId);
        if (parent == null) {
            // parent not found
            return 0;
        }

        Query q = em.createNamedQuery("MlsLink.getObjectRelatives");
        q.setParameter("id", parentId);
        q.setParameter("relativeType", LinkRelativeType.OBJECT);
        List<MlsLink> links = q.getResultList();

        // if the parent's children were not ordered before do it now otherwise sort the list
        if (!parent.isRelativesOrdered()) {
            for (int i = 0; i < links.size(); i++) {
                MlsLink l = links.get(i);
                l.setRelativeListPosition(i);
            }
            parent.setRelativesOrdered(true);
        } else {
            Collections.sort(links, new LinkPositionComparator());
        }

        int oldPosition = -1;
        for (MlsLink l : links) {
            if (l.getRelativeId() == relativeId) {
                oldPosition = l.getRelativeListPosition();
            }
        }
        if (oldPosition < 0) {
            // child not found
            return 0;
        }

        // ensure that new position is inside array bounds
        newPosition = Math.min(newPosition, links.size() - 1);
        newPosition = Math.max(newPosition, 0);

        int swapStart = Math.min(oldPosition, newPosition);
        int swapEnd = Math.max(oldPosition, newPosition);

        for (int i = swapStart; i <= swapEnd; i++) {
            MlsLink l = links.get(i);
            if (l.getRelativeId() == relativeId) {
                l.setRelativeListPosition(newPosition);
            } else if (newPosition < oldPosition) {
                l.setRelativeListPosition(l.getRelativeListPosition() + 1);
            } else {
                l.setRelativeListPosition(l.getRelativeListPosition() - 1);
            }
        }
        logManager.log(parent.getClient(), MlsEventType.EventType.ObjectLinkPositionUpdate, parent, relativeId, "new order position " + newPosition, "setRelativePosition", mlsLog.Type.Modify);
        MlMessageHandler mh = new MlMessageHandler();
        mh.sendMessage(userManager.getCurrentUser(), parent, MlMessageHandler.MessageEventType.OBJECT_UPDATE_EVENT, "update child positions for " + relativeId);
        mh.closeConnection();
        return parent.getVersion();
    }


    @Override
    public void bulkLink(int targetId, List<Integer> partnerIds) {
        mlsObject target = em.find(mlsObject.class, targetId);
        if (target == null) {
            return;
        }
        mlsUser currUser = userManager.getCurrentUser();
        for (int i : partnerIds) {
            mlsObject partner = em.find(mlsObject.class, i);
            if (partner != null) {
                MlsLink link1 = new MlsLink(target, partner, currUser);
                MlsLink link2 = new MlsLink(partner, target, currUser);
                target.setRelativeCount(target.getRelativeCount() + 1);
                partner.setRelativeCount(partner.getRelativeCount() + 1);
                em.persist(link1);
                em.persist(link2);
                em.flush();
                EntityRefresher.updateCachedEntity(em, target.getId(), target, false);
                EntityRefresher.updateCachedEntity(em, partner.getId(), partner, false);
            }
        }
    }

    @Override
    public void bulkUnLink(int targetId, List<Integer> partnerIds) {
        mlsObject target = em.find(mlsObject.class, targetId);
        if (target == null) {
            return;
        }
        for (int i : partnerIds) {
            mlsObject partner = em.find(mlsObject.class, i);
            if (partner != null) {
                Query q = em.createNamedQuery("MlsLink.removeLink");
                q.setParameter("id1", partner.getId());
                q.setParameter("id2", target.getId());
                q.setParameter("relativeType", LinkRelativeType.OBJECT);
                q.executeUpdate();
                target.setRelativeCount(target.getRelativeCount() - 1);
                partner.setRelativeCount(partner.getRelativeCount() - 1);
                em.flush();
                EntityRefresher.updateCachedEntity(em, target.getId(), target, false);
                EntityRefresher.updateCachedEntity(em, partner.getId(), partner, false);
            }
        }
    }

    @Override
    public void unlinkAllIcons(int objectId) {
        mlsObject o = em.find(mlsObject.class, objectId);
        if (o == null) {
            return;
        }
        for (mlsObject relative : o.getRelatives()) {
            if (relative instanceof MlsImage && ((MlsImage) relative).getType() == ImageType.Icon) {
                unlink(o.getId(), relative.getId(), true, LinkRelativeType.ICON_OBJECT);
            }
        }
    }

    @Override
    public void reconcileObjectsRelativeCountField() {
        Query qCount = em.createNamedQuery("mlsObject.getTotalObjectCount");
        Long objectCount = (Long) qCount.getSingleResult();
        long batchSize = 3000L;
        long passes = objectCount / batchSize;
        Query q = em.createQuery("SELECT o FROM mlsObject o");
        q.setMaxResults((int) batchSize);

        for (long i = 0; i < passes; i++) {
            System.out.println("pass no " + i + " of " + passes);
            q.setFirstResult((int) (i * batchSize));
            List<mlsObject> objects = q.getResultList();
            for (mlsObject o : objects) {
                int actualRelativeCount = o.getRelatives().size();
                if (o.getRelativeCount() != actualRelativeCount) {
                    o.setRelativeCount(o.getRelatives().size());
                }
            }
        }
    }

}
