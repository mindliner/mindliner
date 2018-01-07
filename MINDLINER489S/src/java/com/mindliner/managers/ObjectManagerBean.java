/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.categories.MlsEventType;
import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.categories.mlsPriority;
import com.mindliner.common.SubCollectionFilter;
import com.mindliner.contentfilter.Completable;
import com.mindliner.entities.EntityRefresher;
import com.mindliner.entities.MlAuthorization;
import com.mindliner.entities.MlsContainer;
import com.mindliner.entities.MlsContainerMap;
import com.mindliner.entities.MlsContainermapObjectLink;
import com.mindliner.entities.MlsContainermapObjectPosition;
import com.mindliner.entities.MlsImage;
import com.mindliner.entities.MlsLink;
import com.mindliner.entities.MlsNews;
import com.mindliner.entities.UserClientLink;
import com.mindliner.entities.mlsClient;
import com.mindliner.entities.mlsContact;
import com.mindliner.entities.mlsLog;
import com.mindliner.entities.mlsObject;
import com.mindliner.entities.mlsObjectCollection;
import com.mindliner.entities.mlsRatingDetail;
import com.mindliner.entities.mlsTask;
import com.mindliner.entities.mlsUser;
import com.mindliner.enums.LinkRelativeType;
import com.mindliner.enums.ObjectCollectionType;
import com.mindliner.enums.ObjectReviewStatus;
import com.mindliner.exceptions.ForeignOwnerException;
import com.mindliner.exceptions.IsOwnerException;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.MlLinkException;
import com.mindliner.exceptions.NonExistingObjectException;
import com.mindliner.exceptions.SubCollectionExtractionException;
import com.mindliner.managers.MlMessageHandler.MessageEventType;
import com.mindliner.objects.transfer.MltObject;
import com.mindliner.objects.transfer.ObjectSignature;
import com.mindliner.objects.transfer.mlTransferObjectFactory;
import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 * This class handles request for object retreival, storage, and attribute
 * changes.
 *
 * @author Marius Messerli
 */
@Stateless
@DeclareRoles(value = {"Admin", "User", "MasterAdmin"})
public class ObjectManagerBean implements ObjectManagerRemote, ObjectManagerLocal {

    @PersistenceContext
    private EntityManager em;
    @EJB
    CategoryManagerRemote catman;
    @EJB
    UserManagerLocal userManager;
    @EJB
    CategoryManagerRemote categoryManager;
    @EJB
    SolrServerBean solrServer;
    @EJB
    ObjectFactoryLocal objectFactory;
    @EJB
    LogManagerLocal logManager;
    @EJB
    LinkManagerLocal linkManagerLocal;
    @EJB
    LinkManagerRemote linkManagerRemote;

    @Asynchronous
    @Override
    public void addSolrFile(byte[] bytes, int attachedObjId) {
        Logger.getLogger(ObjectManagerBean.class.getName()).log(Level.INFO, "Indexing file of size {0}", bytes.length);
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        solrServer.addFile(is, attachedObjId);
    }

    @Override
    @RolesAllowed(value = {"User", "Admin", "MasterAdmin"})
    public mlsObject find(int key) {
        mlsObject o = em.find(mlsObject.class, key);
        return o;
    }

    @Override
    @RolesAllowed(value = {"User", "Admin", "MasterAdmin"})
    public mlsObject findLocal(int key) {
        return find(key);
    }

    @Override
    @RolesAllowed(value = {"User"})
    public mlsObject merge(mlsObject o) throws OptimisticLockException {
        solrServer.addObject(o, true);
        return em.merge(o);
    }

    @Override
    public int getNumberOfAccessibleRelatives(int parentId) {
        List<mlsObject> accessible = new ArrayList<>();
        mlsObject o = em.find(mlsObject.class, parentId);
        if (o == null) {
            return 0;
        }
        mlsUser currentUser = userManager.getCurrentUser();
        for (mlsObject r : o.getRelatives()) {
            if (r.getConfidentiality().compareTo(currentUser.getMaxConfidentiality(r.getClient())) <= 0) {
                accessible.add(r);
            }
        }
        return accessible.size();
    }

    @Override
    @RolesAllowed(value = {"User", "Admin", "MasterAdmin"})
    public int getVersion(int key) {
        mlsObject o = em.find(mlsObject.class, key);
        if (o != null) {
            return o.getVersion();
        } else {
            return -1;
        }
    }

    @Override
    @RolesAllowed(value = {"User", "Admin", "MasterAdmin"})
    public Map<Integer, Integer> getVersions(Integer[] objectIds) {
        Map<Integer, Integer> result = new HashMap<>(objectIds.length);
        for (Integer objectId : objectIds) {
            mlsObject o = em.find(mlsObject.class, objectId);
            if (o != null) {
                result.put(o.getId(), o.getVersion());
            }
        }
        return result;
    }

    @Override
    @RolesAllowed(value = {"User"})
    public void remove(mlsObject o) throws ForeignOwnerException, IsOwnerException {
        if (!userManager.getCurrentUser().equals(o.getOwner())) {
            throw new ForeignOwnerException("Object belongs to someone else and was not deleted.");
        }
        o = em.merge(o);
        if (o != null) {
            o.beforeDeletion();
            List<Integer> removed = removeWithoutMessage(o);
            logManager.log(o.getClient(), MlsEventType.EventType.ObjectDeleted, o, 0, o.getHeadline(), "remove", mlsLog.Type.Remove);
            MlMessageHandler mh = new MlMessageHandler();
            mh.sendBulkMessage(userManager.getCurrentUser(), removed, MessageEventType.OBJECT_DELETION_EVENT, "");
            mh.closeConnection();
        }
    }

    @Override
    @RolesAllowed(value = {"User"})
    public void remove(int key) throws ForeignOwnerException, IsOwnerException {
        mlsObject object = em.find(mlsObject.class, key);
        remove(object);
    }

    @Override
    @RolesAllowed(value = {"User"})
    public void removeObjects(List<Integer> keys) {
        List<Integer> deletedObjectIds = new ArrayList<>();
        for (int key : keys) {
            // re-implement removal here; calling remove(int key) would cause one message per object and quickly exhaust available message connections and throw and error
            mlsObject o = em.find(mlsObject.class, key);
            if (o != null) {
                List<Integer> removed = removeWithoutMessage(o);
                deletedObjectIds.addAll(removed);
                logManager.log(o.getClient(), MlsEventType.EventType.ObjectDeleted, o, 0, o.getHeadline(), "remove", mlsLog.Type.Remove);
            }
        }
        MlMessageHandler mh = new MlMessageHandler();
        mh.sendBulkMessage(userManager.getCurrentUser(), deletedObjectIds, MessageEventType.OBJECT_DELETION_EVENT, "");
        mh.closeConnection();
    }

    public List<Integer> removeWithoutMessage(mlsObject managedObject) {
        List<Integer> removed = new ArrayList<>();
        removed.add(managedObject.getId());

        // In case of a container map, we also need to delete any associated containers and standalone objects
        if (managedObject instanceof MlsContainerMap) {
            List<Integer> r = removeContainerMapContent((MlsContainerMap) managedObject);
            removed.addAll(r);
        }

        List<mlsObject> relatives = managedObject.getRelatives();
        Query q = em.createNamedQuery("MlsLink.removeObjectLinks");
        q.setParameter("id", managedObject.getId());
        q.executeUpdate();
        // refresh all relatives otherwise they wont notice the deletion
        for (mlsObject rel : relatives) {
            EntityRefresher.updateCachedEntity(em, rel.getId(), rel);
        }
        if (managedObject.getIsland() != null) {
            managedObject.getIsland().getObjects().remove(managedObject);
            managedObject.setIsland(null);
        }
        em.remove(managedObject);
        solrServer.removeObject(managedObject, true);

        return removed;
    }

    private List<Integer> removeContainerMapContent(MlsContainerMap map) {
        List<Integer> removed = new ArrayList<>();

        // gather all associated containers
        Query q = em.createNamedQuery("MlsLink.getObjectLinks", MlsLink.class);
        q.setParameter("id", map.getId());
        List<MlsLink> links = q.getResultList();
        for (MlsLink link : links) {
            if (map.getId() == link.getHolderId() && LinkRelativeType.CONTAINER_MAP.equals(link.getRelativeType())) {
                mlsObject rel = em.find(mlsObject.class, link.getRelativeId());
                if (rel instanceof MlsContainer) {
                    removed.add(rel.getId());
                }
            }
        }

        // gather all associated objects that are only connected to the container map
        List<MlsContainermapObjectPosition> objsPos = map.getObjectPositions();
        for (MlsContainermapObjectPosition objPos : objsPos) {
            mlsObject obj = objPos.getObject();

            boolean single = obj.getRelatives().stream().allMatch((o) -> {
                return removed.contains(o.getId()) || o.getId() == map.getId();
            });

            if (single) {
                removed.add(obj.getId());
            }
        }

        // remove gathered objects
        for (Integer o : removed) {
            mlsObject obj = em.find(mlsObject.class, o);
            removeWithoutMessage(obj);
        }

        return removed;
    }

    private int setDataPool(mlsObject o, mlsClient dataPool, mlsConfidentiality conf, MlMessageHandler mh) {
        o.setClient(dataPool);
        o.setConfidentiality(conf);
        o.setModificationDate(new Date());
        em.flush();
        linkManagerLocal.unlinkAllIcons(o.getId());
        mh.sendMessage(userManager.getCurrentUser(), o, MessageEventType.OBJECT_UPDATE_EVENT, "data pool");
        solrServer.addObject(o, false);
        return o.getVersion();
    }

    @Override
    public Map<Integer, Integer> bulkSetDataPool(List<Integer> objectIds, int dataPoolId, int confidentialityId) {
        Map<Integer, Integer> versions = new HashMap<>();
        mlsClient dataPool = em.find(mlsClient.class, dataPoolId);
        mlsConfidentiality conf = em.find(mlsConfidentiality.class, confidentialityId);
        if (conf != null && dataPool != null) {
            if (conf.getClient().getId() != dataPool.getId()) {
                throw new IllegalArgumentException("The specified confidentiality must belong to the specified data pool");
            }
            MlMessageHandler mh = new MlMessageHandler();

            for (Integer id : objectIds) {
                mlsObject o = em.find(mlsObject.class, id);
                if (o != null) {
                    versions.put(id, setDataPool(o, dataPool, conf, mh));
                }
            }
            solrServer.commit();
            mh.closeConnection();
        }
        return versions;
    }

    private int setConfidentiality(mlsObject o, mlsConfidentiality conf, MlMessageHandler mh) {
        o.setConfidentiality(conf);
        if (o instanceof mlsContact) {
            // ensure that the profile picture of the contact has the same confidentiality
            mlsContact c = (mlsContact) o;
            MlsImage pp = c.getProfilePicture();
            if (pp != null) {
                pp.setConfidentiality(conf);
            }
        }
        o.setModificationDate(new Date());
        if (o instanceof mlsContact) {
            // ensure that the profile picture of the contact has the same confidentiality
            ((mlsContact) o).getProfilePicture().setConfidentiality(conf);
        }
        logManager.log(o.getClient(), MlsEventType.EventType.ObjectUpdated, o, 0, "confidentiality update" + conf.getName(), "bulkSetConfidentiality", mlsLog.Type.Modify);
        em.flush();
        mh.sendMessage(userManager.getCurrentUser(), o, MessageEventType.OBJECT_UPDATE_EVENT, "confidentiality");
        solrServer.addObject(o, false);
        return o.getVersion();
    }

    @Override
    @RolesAllowed(value = {"User"})
    public Map<Integer, Integer> bulkSetConfidentiality(List<Integer> objectIds, int confId) {
        Map<Integer, Integer> versions = new HashMap<>(objectIds.size());
        mlsConfidentiality conf = em.find(mlsConfidentiality.class, confId);
        if (conf != null) {
            MlMessageHandler mh = new MlMessageHandler();
            for (Integer id : objectIds) {
                mlsObject o = em.find(mlsObject.class, id);
                if (o != null) {
                    versions.put(o.getId(), setConfidentiality(o, conf, mh));
                }
            }
            solrServer.commit();
            mh.closeConnection();
        }
        return versions;
    }

    private int setPriority(mlsTask task, mlsPriority prio, MlMessageHandler mh) {
        task.setPriority(prio);
        task.setModificationDate(new Date());
        logManager.log(task.getClient(), MlsEventType.EventType.ObjectUpdated, task, 0, "priority update to " + prio.getName(), "setPriority", mlsLog.Type.Modify);
        em.flush();
        mh.sendMessage(userManager.getCurrentUser(), task, MessageEventType.OBJECT_UPDATE_EVENT, "priority");
        solrServer.addObject(task, true);
        return task.getVersion();
    }

    @Override
    @RolesAllowed(value = {"User"})
    public Map<Integer, Integer> bulkSetPriority(List<Integer> objectIds, int priorityId) {
        Map<Integer, Integer> versions = new HashMap<>(objectIds.size());
        MlMessageHandler mh = new MlMessageHandler();
        for (Integer id : objectIds) {
            mlsTask t = em.find(mlsTask.class, id);
            mlsPriority p = em.find(mlsPriority.class, priorityId);
            if (t != null && p != null) {
                int version = setPriority(t, p, mh);
                versions.put(id, version);
            }
        }
        mh.closeConnection();
        return versions;
    }

    @Override
    @RolesAllowed(value = {"User"})
    public Map<Integer, Integer> bulkSetStatus(List<Integer> objectIds, ObjectReviewStatus status) {
        Map<Integer, Integer> versions = new HashMap<>(objectIds.size());
        for (Integer id : objectIds) {
            int version = setStatus(id, status);
            versions.put(id, version);
        }
        return versions;
    }

    @Override
    @RolesAllowed(value = {"User"})
    public Map<Integer, Integer> bulkSetPrivacy(List<Integer> objectIds, boolean privacy) {
        Map<Integer, Integer> versions = new HashMap<>(objectIds.size());
        MlMessageHandler mh = new MlMessageHandler();
        for (Integer id : objectIds) {
            mlsObject o = em.find(mlsObject.class, id);
            if (o != null) {
                versions.put(id, setPrivacy(o, privacy, mh));
            }
        }
        mh.closeConnection();
        return versions;
    }

    @Override
    @RolesAllowed(value = {"User"})
    public Map<Integer, Integer> bulkSetCompletion(List<Integer> objectIds, boolean completed) {
        Map<Integer, Integer> versions = new HashMap<>(objectIds.size());
        MlMessageHandler mh = new MlMessageHandler();
        for (Integer id : objectIds) {
            mlsObject o = em.find(mlsObject.class, id);
            if (o != null) {
                versions.put(id, setCompletion(o, completed, mh));
            }
        }
        mh.closeConnection();
        return versions;
    }

    @Override
    public Map<Integer, Integer> bulkSetArchived(List<Integer> objectIds, boolean state) {
        Map<Integer, Integer> versions = new HashMap<>(objectIds.size());
        int messages = 0;
        MlMessageHandler mh = new MlMessageHandler();
        for (Integer id : objectIds) {
            mlsObject mbo = em.find(mlsObject.class, id);
            if (mbo != null) {
                if (mbo.isArchived() != state) {
                    // cannot process more than 1000 messages in a single transaction
                    if (messages > 990) {
                        mh.closeConnection();
                        mh = new MlMessageHandler();
                        messages = 0;
                    }
                    versions.put(id, setArchived(mbo, state, mh));
                    messages++;
                }
            }
        }
        mh.closeConnection();
        return versions;
    }

    @Override
    @RolesAllowed(value = {"User"})
    public Map<Integer, Integer> bulkSetOwner(List<Integer> objectIds, int newOwnerId) throws MlAuthorizationException {
        Map<Integer, Integer> versions = new HashMap<>(objectIds.size());
        mlsUser owner = em.find(mlsUser.class, newOwnerId);
        if (owner != null) {
            MlMessageHandler mh = new MlMessageHandler();
            boolean authorizationException = false;
            for (Integer id : objectIds) {
                mlsObject o = em.find(mlsObject.class, id);
                if (o != null) {
                    try {
                        versions.put(id, setOwner(o, owner, mh));
                    } catch (MlAuthorizationException ex) {
                        authorizationException = true;
                    }
                }
            }
            if (authorizationException) {
                throw new MlAuthorizationException("Either the object is private or the new owner does not have clearance for its confidentiality. Rectify and re-try.");
            }
            mh.closeConnection();
        }
        return versions;
    }

    @Override
    @RolesAllowed(value = {"User"})
    public Map<Integer, Integer> bulkSetDueDate(List<Integer> objectIds, Date dueDate) {
        Map<Integer, Integer> versions = new HashMap<>(objectIds.size());
        MlMessageHandler mh = new MlMessageHandler();
        for (Integer id : objectIds) {
            mlsObject o = em.find(mlsObject.class, id);
            if (o != null && o instanceof mlsTask) {
                versions.put(id, setDueDate((mlsTask) o, dueDate, mh));
            }
        }
        mh.closeConnection();
        return versions;
    }

    private int setOwner(mlsObject o, mlsUser owner, MlMessageHandler mh) throws MlAuthorizationException {
        if (!o.getOwner().equals(owner)) {
            if (o.getPrivateAccess()) {
                throw new MlAuthorizationException("Object has private access - make it non-private before re-assigning");
            }
            if (owner.getClients().contains(o.getClient())) {
                if (owner.getMaxConfidentiality(o.getClient()).compareTo(o.getConfidentiality()) < 0) {
                    throw new MlAuthorizationException("New owner does not have clearance for object's confidentiality level - downgrade object before re-assigning");
                }
                o.setOwner(owner);
                o.setModificationDate(new Date());
                logManager.log(o.getClient(), MlsEventType.EventType.ObjectUpdated, o, 0, "new owner: " + owner.getUserName(), "setOwner", mlsLog.Type.Modify);
                em.flush();

                mh.sendMessage(userManager.getCurrentUser(), o, MessageEventType.OBJECT_UPDATE_EVENT, "owner");

                solrServer.addObject(o, true);
            }
        }
        return o.getVersion();
    }

    @Override
    @RolesAllowed(value = {"User"})
    public int setOwner(int objectKey, int newOwnerId) throws MlAuthorizationException {
        mlsObject object = em.find(mlsObject.class, objectKey);
        mlsUser newOwner = em.find(mlsUser.class, newOwnerId);
        if (object != null && newOwner != null) {
            MlMessageHandler mh = new MlMessageHandler();
            int version = setOwner(object, newOwner, mh);
            mh.closeConnection();
            return version;
        } else {
            throw new MlAuthorizationException("The new owner is not in the object's data pool");
        }
    }

    @Override
    @RolesAllowed(value = {"User"})
    public int setConfidentiality(int key, int confidentialityId
    ) {
        mlsObject o = em.find(mlsObject.class, key);
        mlsConfidentiality conf = em.find(mlsConfidentiality.class, confidentialityId);
        if (o == null || conf == null) {
            return -1;
        }
        if (o.getConfidentiality().equals(conf)) {
            return o.getVersion();
        }
        if (!conf.getClient().equals(o.getClient())) {
            System.err.println("ObjectManagerBean.setConfidentiality(): Ignoring request as the specified confidentiality does not belong to the object's data pool");
        } else {
            MlMessageHandler mh = new MlMessageHandler();
            setConfidentiality(o, conf, mh);
            mh.closeConnection();
            return o.getVersion();
        }
        return -1;
    }

    @Override
    @RolesAllowed(value = {"User"})
    public int setDataPool(int key, int dataPoolId, int confidentialityId
    ) {
        mlsObject o = em.find(mlsObject.class, key);
        if (o == null || o.getClient().getId() == dataPoolId) {
            return -1;
        }
        if (o.getClient().getId() == dataPoolId) {
            return o.getVersion();

        }
        mlsClient pool = em.find(mlsClient.class, dataPoolId);
        mlsConfidentiality conf = em.find(mlsConfidentiality.class, confidentialityId);
        if (conf == null) {
            System.err.println("Requested confidentiality does not exist, ignoring request to change data pool");
            return -1;
        }
        if (!conf.getClient().equals(pool)) {
            System.err.println("ObjectManagerBean.setDataPool(): Ignoring request because the specified confidentiality does not belong to the specified data pool");
        } else if (pool != null) {
            MlMessageHandler mh = new MlMessageHandler();
            setDataPool(o, pool, conf, mh);
            mh.closeConnection();
            return o.getVersion();
        }
        return -1;
    }

    @Override
    @RolesAllowed(value = {"User"})
    public int setModificationDate(int key, Date newDate) {
        mlsObject mbo = em.find(mlsObject.class, key);
        if (!mbo.getModificationDate().equals(newDate)) {
            mbo.setModificationDate(newDate);
            logManager.log(mbo.getClient(), MlsEventType.EventType.ObjectUpdated, mbo, 0, "modification update", "setModificationDate", mlsLog.Type.Modify);
            em.flush();
            solrServer.addObject(mbo, true);
        }
        return mbo.getVersion();
    }

    @Override
    @RolesAllowed(value = {"User"})
    public int setPriority(int key, int priorityId) {
        mlsObject mbo = em.find(mlsObject.class, key);
        mlsPriority newPriority = em.find(mlsPriority.class, priorityId);
        if (mbo != null && mbo instanceof mlsTask && newPriority != null) {
            mlsTask task = (mlsTask) mbo;
            if (!task.getPriority().equals(newPriority)) {
                MlMessageHandler mh = new MlMessageHandler();
                int version = setPriority(task, newPriority, mh);
                mh.closeConnection();
                return version;
            }
            return task.getVersion();
        }
        return -1;
    }

    @Override
    @RolesAllowed(value = {"User"})
    public int setCollectionType(int key, ObjectCollectionType type) {
        mlsObject o = em.find(mlsObject.class, key);
        if (o != null && o instanceof mlsObjectCollection) {
            mlsObjectCollection coll = (mlsObjectCollection) o;
            if (!coll.getType().equals(type)) {
                coll.setType(type);
                o.setModificationDate(new Date());
                logManager.log(o.getClient(), MlsEventType.EventType.ObjectUpdated, o, 0, "colletion type updated to " + type, "setCollectionType", mlsLog.Type.Modify);
                MlMessageHandler mh = new MlMessageHandler();
                mh.sendMessage(userManager.getCurrentUser(), o, MessageEventType.OBJECT_UPDATE_EVENT, "object collection type");
                mh.closeConnection();
                solrServer.addObject(o, true);
            }
            return o.getVersion();
        }
        return -1;
    }

    @Override
    @RolesAllowed(value = {"User"})
    public int setStatus(int key, ObjectReviewStatus status
    ) {
        mlsObject mbo = em.find(mlsObject.class, key);
        if (mbo != null) {
            if (!mbo.getStatus().equals(status)) {
                mbo.setStatus(status);
                logManager.log(mbo.getClient(), MlsEventType.EventType.ObjectUpdated, mbo, 0, "status updated to " + status, "setStatus", mlsLog.Type.Modify);
                em.flush();
                MlMessageHandler mh = new MlMessageHandler();
                mh.sendMessage(userManager.getCurrentUser(), mbo, MessageEventType.OBJECT_UPDATE_EVENT, "review status");
                mh.closeConnection();
            }
            return mbo.getVersion();
        }
        return -1;
    }

    @Override
    @RolesAllowed(value = {"User"})
    public int setRelativesOrdered(int key, boolean state
    ) {
        mlsObject o = em.find(mlsObject.class, key);
        if (o != null) {
            if (o.isRelativesOrdered() != state) {
                o.setRelativesOrdered(state);
                logManager.log(o.getClient(), MlsEventType.EventType.ObjectUpdated, o, 0, "set ordering state to " + state, "setRelativesOrdered", mlsLog.Type.Modify);
                em.flush();
                MlMessageHandler mh = new MlMessageHandler();
                mh.sendMessage(userManager.getCurrentUser(), o, MessageEventType.OBJECT_UPDATE_EVENT, "relative ordering set to " + state);
                mh.closeConnection();
            }
            return o.getVersion();
        }
        return -1;
    }

    private int setCompletion(mlsObject o, boolean completed, MlMessageHandler mh) {
        Completable cpl = (Completable) o;
        if (cpl.isCompleted() != completed) {
            cpl.setCompleted(completed);
            o.setModificationDate(new Date());
            logManager.log(o.getClient(), MlsEventType.EventType.ObjectUpdated, o, 0, "set completion to " + completed, "setCompletionState", mlsLog.Type.Modify);
            em.flush();
            mh.sendMessage(userManager.getCurrentUser(), o, MessageEventType.OBJECT_UPDATE_EVENT, "completion");
            solrServer.addObject(o, true);
        }
        return o.getVersion();
    }

    @Override
    @RolesAllowed(value = {"User"})
    public int setCompletionState(int key, boolean completed) {
        int version = -1;
        mlsObject mbo = em.find(mlsObject.class, key);
        if (mbo != null && mbo instanceof Completable) {
            MlMessageHandler mh = new MlMessageHandler();
            version = setCompletion(mbo, completed, mh);
            mh.closeConnection();
        }
        return version;
    }

    private int setArchived(mlsObject o, boolean state, MlMessageHandler mh) {
        o.setArchived(state);
        o.setModificationDate(new Date());
        if (!(o instanceof MlsNews)) {
            // avoid feedback loop through subscriptions to ObjectUpdated
            logManager.log(o.getClient(), MlsEventType.EventType.ObjectUpdated, o, 0, "set archive state to " + state, "setArchived", mlsLog.Type.Modify);
            solrServer.addObject(o, true);
        }
        em.flush();
        mh.sendMessage(userManager.getCurrentUser(), o, MessageEventType.OBJECT_UPDATE_EVENT, "archived");
        return o.getVersion();
    }

    @Override
    public int setArchived(int key, boolean state) {
        int version = -1;
        mlsObject mbo = em.find(mlsObject.class, key);
        if (mbo != null) {
            if (mbo.isArchived() != state) {
                MlMessageHandler mh = new MlMessageHandler();
                version = setArchived(mbo, state, mh);
                mh.closeConnection();
            }
        }
        return version;
    }

    @Override
    @RolesAllowed(value = {"User"})
    public int setHeadline(int key, String headline
    ) {
        assert headline != null : "Null headline not allowed";
        mlsObject mbo = em.find(mlsObject.class, key);
        if (mbo != null) {
            if (!mbo.getHeadline().equals(headline)) {
                if (!(mbo instanceof mlsContact)) {
                    mbo.setHeadline(headline);
                } else {
                    System.err.println("Cannot update the headline of a contact; its automatically built from the name");
                }
                mbo.setModificationDate(new Date());
                logManager.log(mbo.getClient(), MlsEventType.EventType.ObjectUpdated, mbo, 0, "headline updated", "setHeadline", mlsLog.Type.Modify);
                em.flush();
                MlMessageHandler mh = new MlMessageHandler();
                mh.sendMessage(userManager.getCurrentUser(), mbo, MessageEventType.OBJECT_UPDATE_EVENT, "headline or description");
                mh.closeConnection();
                solrServer.addObject(mbo, true);
            }
            return mbo.getVersion();
        }
        return -1;
    }

    @Override
    @RolesAllowed(value = {"User"})
    public int setDescription(int key, String description) {
        assert description != null : "Null description not allowed";
        mlsObject o = em.find(mlsObject.class, key);
        if (o != null) {
            if (!o.getDescription().equals(description)) {
                o.setDescription(description);
                o.setModificationDate(new Date());
                logManager.log(o.getClient(), MlsEventType.EventType.ObjectUpdated, o, 0, "description to " + description, "setDescription", mlsLog.Type.Modify);
                em.flush();
                MlMessageHandler mh = new MlMessageHandler();
                mh.sendMessage(userManager.getCurrentUser(), o, MessageEventType.OBJECT_UPDATE_EVENT, "headline or description");
                mh.closeConnection();
                solrServer.addObject(o, true);
            }
            return o.getVersion();
        }
        return -1;
    }

    private void checkObjectAuthorization(mlsObject object, mlsUser currentUser) throws NonExistingObjectException {
        if (!currentUser.getClients().contains(object.getClient())) {
            throw new NonExistingObjectException("Rejected request for object id=" + object.getId() + " belonging to another client");
        }
        if (currentUser.getMaxConfidentiality(object.getClient()) == null) {
            throw new NonExistingObjectException("Rejected request for object id = " + object.getId() + " due to a configuration problem: missing MaxConfidentiality for caller");
        }
        if (object.getConfidentiality() == null) {
            throw new NonExistingObjectException("Rejected request for object id = " + object.getId() + ": missing object confidentiality attribute");
        }
        if (object.getConfidentiality().compareTo(currentUser.getMaxConfidentiality(object.getClient())) > 0) {
            throw new NonExistingObjectException("Rejected request for object id=" + object.getId() + " for which the caller has not authorization");
        }
        if (object.getPrivateAccess() && !object.getOwner().equals(currentUser)) {
            throw new NonExistingObjectException("Rejected request for foreign private object id=" + object.getId());
        }
    }

    @Override
    public boolean isAuthorizedForCurrentUser(mlsObject object) {
        mlsUser currentUser = userManager.getCurrentUser();
        return currentUser.getClients().contains(object.getClient())
                && currentUser.getMaxConfidentiality(object.getClient()) != null
                && object.getConfidentiality() != null
                && object.getConfidentiality().compareTo(currentUser.getMaxConfidentiality(object.getClient())) <= 0
                && (!object.getPrivateAccess() || object.getOwner().equals(currentUser));
    }

    @Override
    @RolesAllowed(value = {"User"})
    public List<MltObject> getTransferObjects(List<Integer> keyList) {
        mlsUser u = userManager.getCurrentUser();
        List<MltObject> transferList = new ArrayList<>();

        for (Integer key : keyList) {
            mlsObject o = em.find(mlsObject.class, key);
            if (o != null) {
                try {
                    checkObjectAuthorization(o, u);
                    if (u.getClients().contains(o.getClient())) {
                        MltObject t = mlTransferObjectFactory.getTransferObject(o);
                        transferList.add(t);
                    }
                } catch (NonExistingObjectException ex) {
                    // no further action required
                }
            }
        }
        return transferList;
    }

    @Override
    @RolesAllowed(value = {"User"})
    public MltObject getTransferObject(int key) throws NonExistingObjectException {
        mlsUser u = userManager.getCurrentUser();
        mlsObject o = em.find(mlsObject.class, key);
        if (o == null) {
            throw new NonExistingObjectException("No object exists with the specified id " + key);
        }
        checkObjectAuthorization(o, u);
        return mlTransferObjectFactory.getTransferObject(o);
    }

    @RolesAllowed(value = {"MasterAdmin"})
    @Override
    public MltObject
            masterAdminGetTransferObject(int key) {
        mlsObject o = em.find(mlsObject.class, key);
        if (o == null) {
            return null;
        }
        MltObject t = mlTransferObjectFactory.getTransferObject(o);
        return t;
    }

    private int setDueDate(mlsTask task, Date d, MlMessageHandler mh) {
        task.setDueDate(d);
        task.setModificationDate(new Date());
        logManager.log(task.getClient(), MlsEventType.EventType.ObjectUpdated, task, 0,
                d == null ? "clear due date" : "due date to " + d.toString(),
                "setDueDate", mlsLog.Type.Modify);
        em.flush();
        mh.sendMessage(userManager.getCurrentUser(), task, MessageEventType.OBJECT_UPDATE_EVENT, "dueDate");
        solrServer.addObject(task, true);
        return task.getVersion();
    }

    /**
     * This function sets the due date of the specified object. If the specified
     * object does not have a due date the function does nothing.
     *
     * @param key
     * @param d
     * @return
     */
    @Override
    @RolesAllowed(value = {"User"})
    public int setDueDate(int key, Date d) {
        mlsObject mo = (mlsObject) em.find(mlsObject.class, key);
        if (mo != null && mo instanceof mlsTask) {
            mlsTask task = ((mlsTask) mo);
            if (task.getDueDate() == null || !task.getDueDate().equals(d)) {
                MlMessageHandler mh = new MlMessageHandler();
                int version = setDueDate(task, d, mh);
                mh.closeConnection();
                return version;
            }
            return mo.getVersion();
        } else {
            return -1;
        }
    }

    private int setPrivacy(mlsObject o, boolean state, MlMessageHandler mh) {
        o.setPrivateAccess(state);
        o.setModificationDate(new Date());
        logManager.log(o.getClient(), MlsEventType.EventType.ObjectUpdated, o, 0, "set privacy to " + state, "setPrivacyFlag", mlsLog.Type.Modify);
        em.flush();
        mh.sendMessage(userManager.getCurrentUser(), o, MessageEventType.OBJECT_UPDATE_EVENT, "privacy");
        solrServer.addObject(o, true);
        return o.getVersion();
    }

    @Override
    @RolesAllowed(value = {"User"})
    public int setPrivacyFlag(int key, boolean b) throws ForeignOwnerException {
        mlsObject o = em.find(mlsObject.class, key);
        if (o != null) {
            mlsUser currentUser = userManager.getCurrentUser();
            if (!currentUser.equals(o.getOwner())) {
                throw new ForeignOwnerException("Only the owner of an object can make this private.");
            }
            if (o.getPrivateAccess() != b) {
                MlMessageHandler mh = new MlMessageHandler();
                int version = setPrivacy(o, b, mh);
                mh.closeConnection();
                return version;
            }
            return o.getVersion();
        }
        return -1;
    }

    @Override
    @RolesAllowed(value = {"User"})
    public List getOutdatedObjectsIds(List<ObjectSignature> inlist) {
        List<Integer> results = new ArrayList<>();

        for (ObjectSignature cachedObject : inlist) {
            mlsObject liveObject = em.find(mlsObject.class, cachedObject.getId());
            // I leave the non-existing objects in so that the cache reconstruction fails and removes the cache record
            if (liveObject == null || liveObject.getVersion() != cachedObject.getVersion()) {
                results.add(cachedObject.getId());
            }
        }
        return results;
    }

    @Override
    @RolesAllowed(value = {"User"})
    public List<mlsUser> getUsersByFirstAndLAstnameSubstrings(String firstname, String lastname, int clientId) {
        Query q;
        if (lastname.isEmpty()) {
            q = em.createNamedQuery("mlsUser.getUserByFirstnameSubstring");
            q.setParameter("clientId", clientId);
            q.setParameter("firstNameFragment", "%" + firstname + "%");
        } else {
            q = em.createNamedQuery("mlsUser.getUserByFirstAndLastnameSubstring");
            q.setParameter("clientId", clientId);
            q.setParameter("firstNameFragment", "%" + firstname + "%");
            q.setParameter("lastNameFragment", "%" + lastname + "%");
        }
        List<mlsUser> results = q.getResultList();
        // force instantiation
        for (mlsUser u : results) {
            u.getClients().size();
        }
        return results;
    }

    @Override
    @RolesAllowed(value = {"User"})
    public int updateContactDetails(int contactId, String first, String middle, String last, String email, String phoneNumber, String mobileNumber, String description) {
        mlsObject o = em.find(mlsObject.class, contactId);
        if (o != null && o instanceof mlsContact) {
            mlsContact c = (mlsContact) o;
            c.setFirstName(first);
            c.setMiddleName(middle);
            c.setLastName(last);
            c.setEmail(email);
            c.setDescription(description);
            c.setMobileNumber(mobileNumber);
            c.setPhoneNumber(phoneNumber);
            logManager.log(c.getClient(), MlsEventType.EventType.ObjectUpdated, c, 0, first + " " + middle + " " + last + " " + email, "updateContactDetails", mlsLog.Type.Modify);
            em.flush();
            MlMessageHandler mh = new MlMessageHandler();
            mh.sendMessage(userManager.getCurrentUser(), c, MessageEventType.OBJECT_UPDATE_EVENT, "contact");
            mh.closeConnection();
            return c.getVersion();
        }
        return -1;
    }

    @Override
    public int updateContactProfilePicture(int contactId, int profilePictureId) {
        mlsObject o = em.find(mlsObject.class, contactId);
        if (o != null && o instanceof mlsContact) {
            mlsContact c = (mlsContact) o;
            MlsImage profilePic = em.find(MlsImage.class, profilePictureId);
            if (profilePic != null && profilePic.getId() != profilePictureId) {
                c.setProfilePicture(profilePic);
                em.flush();
                MlMessageHandler mh = new MlMessageHandler();
                mh.sendMessage(userManager.getCurrentUser(), c, MessageEventType.OBJECT_UPDATE_EVENT, "contact profile picture");
                mh.closeConnection();
                logManager.log(c.getClient(), MlsEventType.EventType.ObjectUpdated, c, 0, profilePic.getHeadline(), "updateContactProfilePicture", mlsLog.Type.Modify);
            }
            return c.getVersion();
        }
        return -1;
    }

    @Override
    @RolesAllowed(value = {"User"})
    public int setEffortEstimation(int key, int minutes) {
        mlsObject o = em.find(mlsObject.class, key);
        if (o != null && o instanceof mlsTask) {
            mlsTask task = (mlsTask) o;
            if (task.getEffortEstimation() != minutes) {
                task.setEffortEstimation(minutes);
                o.setModificationDate(new Date());
                logManager.log(o.getClient(), MlsEventType.EventType.ObjectUpdated, o, 0, "effort set to " + minutes, "setEffortEstimation", mlsLog.Type.Modify);
                MlMessageHandler mh = new MlMessageHandler();
                mh.sendMessage(userManager.getCurrentUser(), o, MessageEventType.OBJECT_UPDATE_EVENT, "effort estimate");
                mh.closeConnection();

                solrServer.addObject(o, true);
            }
            return o.getVersion();
        }
        return -1;
    }

    @Override
    @RolesAllowed(value = {"User"})
    public MltObject
            changeObjectType(int id, Class<? extends mlsObject> newType) throws Exception {
        mlsObject o = em.find(mlsObject.class, id);
        if (o != null) {
            try {
                mlsObject newObj = (mlsObject) newType.newInstance();
                setCommonAttributes(newObj, o);
                setSpecificAttributes(newObj);
                // it is not possible to keep the id of the old object as the id's are auto generated
                // it would be possible using native SQL queries, but that is not recommended as it
                // bypasses JPA (i.e. it runs outside the transaction, versioning would have to be implemented by hand)
                em.persist(newObj);

                // replace the object in containermaps (position and link entries)
                List<MlsContainerMap> changedMaps = new ArrayList<>();

                Query q = em.createNamedQuery("MlsContainermapObjectPosition.getObjectPositions", MlsContainermapObjectPosition.class
                );
                q.setParameter("oId", id);
                List<MlsContainermapObjectPosition> mapPositions = q.getResultList();
                mapPositions.stream().forEach((pos) -> {
                    pos.setObject(newObj);
                    changedMaps.add(pos.getContainerMap());
                });

                q = em.createNamedQuery("MlsContainermapObjectLink.getObjectLinks", MlsContainermapObjectLink.class);
                q.setParameter("oId", id);
                List<MlsContainermapObjectLink> mapLinks = q.getResultList();
                mapLinks.stream().forEach((l) -> {
                    changedMaps.add(l.getContainerMap());
                    if (l.getSourceObject().getId() == id) {
                        l.setSourceObject(newObj);
                    } else if (l.getTargetObject().getId() == id) {
                        l.setTargetObject(newObj);

                    } else {
                        Logger.getLogger(ObjectManagerBean.class
                                .getName()).log(Level.SEVERE, "Unexpected query result");
                    }
                });

                em.flush();
                for (mlsObject map : changedMaps) {
                    EntityRefresher.updateCachedEntity(em, map.getId(), map);
                }

                // replace the object in all links
                List<Integer> ids = new ArrayList<>();
                changedMaps.stream().forEach((map) -> {
                    ids.add(map.getId());
                });
                MlMessageHandler mh = new MlMessageHandler();
                mh.sendBulkMessage(userManager.getCurrentUser(), ids, MessageEventType.OBJECT_UPDATE_EVENT, "object type change");
                mh.closeConnection();

                q = em.createNamedQuery("MlsLink.getObjectLinks", MlsLink.class);
                q.setParameter("id", id);
                List<MlsLink> relatives = q.getResultList();
                Set<mlsObject> changedRelatives = new HashSet<>();
                if (relatives != null) {
                    for (MlsLink l : relatives) {
                        boolean isHolder = true;
                        if (l.getRelativeId() == o.getId()) {
                            l.setRelativeId(newObj.getId());
                            isHolder = false;
                        } else {
                            l.setHolderId(newObj.getId());

                        }
                        mlsObject other = em.find(mlsObject.class, isHolder ? l.getRelativeId() : l.getHolderId());
                        if (other != null) {
                            // TODO add only if (!(is-one-way && isHolder))
                            changedRelatives.add(other);

                        } else {
                            Logger.getLogger(ObjectManagerBean.class
                                    .getName()).log(Level.WARNING, "Link {0} has a reference to an object that doesn't exist: {1}", new Object[]{l.getId(), isHolder ? l.getRelativeId() : l.getHolderId()});
                        }
                    }
                    em.flush();
                    for (mlsObject rel : changedRelatives) {
                        EntityRefresher.updateCachedEntity(em, rel.getId(), rel);
                    }
                    EntityRefresher.updateCachedEntity(em, newObj.getId(), newObj);
                    em.remove(o);

                    mh = new MlMessageHandler();
                    mh.sendMessage(userManager.getCurrentUser(), o, newObj.getId(), MessageEventType.OBJECT_REPLACE_EVENT, "object type change");
                    mh.closeConnection();

                    solrServer.removeObject(o, false); // need only to commit once in addObject
                    solrServer.addObject(newObj, true);

                    MltObject t = mlTransferObjectFactory.getTransferObject(newObj);
                    logManager.log(newObj.getClient(), MlsEventType.EventType.ObjectTypeChanged, newObj, 0, "new type is " + newObj.getClass().getSimpleName(), "changeObjectType", mlsLog.Type.Modify);
                    return t;

                }
            } catch (IllegalAccessException | InstantiationException ex) {
                Logger.getLogger(ObjectManagerBean.class
                        .getName()).log(Level.SEVERE, null, ex);
                throw ex;
            }
        }
        throw new Exception("Object " + id + " does not exist.");
    }

    @Override
    public mlsObject changeObjectType(mlsObject o, Class<? extends mlsObject> newType) throws Exception {
        MltObject ot = changeObjectType(o.getId(), newType);

        return em.find(mlsObject.class, ot.getId());
    }

    private void setSpecificAttributes(mlsObject o) {
        if (o instanceof mlsTask) {
            mlsTask t = (mlsTask) o;
            mlsPriority p = categoryManager.getAllPriorities().get(0);
            t.setPriority(p);
        }
    }

    private void setCommonAttributes(mlsObject newObj, mlsObject template) {
        newObj.setClient(template.getClient());
        newObj.setConfidentiality(template.getConfidentiality());
        newObj.setCreationDate(template.getCreationDate());
        newObj.setDescription(template.getDescription());
        newObj.setHeadline(template.getHeadline());
        newObj.setModificationDate(new Date());
        newObj.setOwner(template.getOwner());
        newObj.setPrivateAccess(template.getPrivateAccess());
        newObj.setRating(template.getRating());
        mlsRatingDetail rd = new mlsRatingDetail();
        rd.setGeneration(template.getRatingDetail().getGeneration());
        rd.setRating(template.getRatingDetail().getRating());
        em.persist(rd);
        newObj.setRatingDetail(rd);
        newObj.setSynchUnits(template.getSynchUnits());
        newObj.setVersion(template.getVersion());

    }

    private List<Integer> getObjectIcons(int key) {
        TypedQuery<MlsLink> q = em.createNamedQuery("MlsLink.getObjectRelatives", MlsLink.class
        );
        q.setParameter("id", key);
        q.setParameter("relativeType", LinkRelativeType.ICON_OBJECT);
        List<MlsLink> result = q.getResultList();

        List<Integer> icons = new ArrayList<>();
        if (result != null) {
            for (MlsLink l : result) {
                icons.add(l.getRelativeId());
            }
        }

        return icons;
    }

    @Override
    public Map<Integer, List<Integer>> getObjectIcons(Set<Integer> keys) {
        HashMap<Integer, List<Integer>> result = new HashMap<>();
        for (Integer key : keys) {
            List<Integer> list = getObjectIcons(key);
            result.put(key, list);
        }
        return result;
    }

    @Override
    public String
            enrollUser(String authToken) throws MlAuthorizationException {
        MlAuthorization auth = em.find(MlAuthorization.class, authToken);

        if (auth == null) {
            throw new MlAuthorizationException("We couldn't find this token.");
        }
        if (auth.isCompleted()) {
            throw new MlAuthorizationException("This authorization token has already been used.");
        }
        if (auth.getExpiration().compareTo(new Date()) < 0) {
            throw new MlAuthorizationException("This authorization token has expired.");
        }
        if (!auth.getAuthorizationType().equals(MlAuthorization.AuthorizationType.DataPoolEnrollmentRequest)) {
            throw new MlAuthorizationException("This token is not supposed to be used with a data pool inviation.");
        }
        /*
         Case when a request was created without a known user (e.g. only by using the email address)
         Assume user created account later on and set to current user.
         --> Potential security issue!
         */
        mlsUser userToBeEnrolled = auth.getUser() == null ? userManager.getCurrentUser() : auth.getUser();

        if (userToBeEnrolled != userManager.getCurrentUser()) {
            throw new MlAuthorizationException("This token is not meant for you.");
        }
        mlsClient dataPool = auth.getDataPool();
        if (dataPool == null) {
            throw new MlAuthorizationException("No data pool specified.");
        }
        mlsConfidentiality maxConfidentiality = auth.getMaxConfidentiality();
        if (maxConfidentiality == null) {
            throw new MlAuthorizationException("No maximum confidentiality specified.");
        }
        //Should never happen, but you never know...
        if (dataPool.getUsers().contains(userToBeEnrolled)) {
            auth.setCompleted(true);
            throw new MlAuthorizationException("You are already enrolled in the specified data pool.");
        }

        // We need the attached dataPool such that EntityRefresher.updateCachedEntity can update the cached version
        dataPool = em.merge(dataPool);

        UserClientLink ucl = new UserClientLink(userToBeEnrolled.getId(), dataPool.getId());
        ucl.setMaxConfidentiality(maxConfidentiality);
        em.persist(ucl);
        // always flush before trying to update cached entities. (l1 cache refreshes of an entity do not take unflushed changes into account)
        em.flush();
        EntityRefresher.updateCachedEntity(em, userToBeEnrolled.getId(), userToBeEnrolled);
        EntityRefresher.updateCachedEntity(em, dataPool.getId(), dataPool);
        EntityRefresher.updateCachedEntity(em, maxConfidentiality.getId(), maxConfidentiality);
        auth.setCompleted(true);
        return dataPool.getName();
    }

    @Override
    public String createEnrollmentAuthorization(mlsUser user, mlsClient dataPool, mlsConfidentiality maxConfidentiality, String email) {
        MlAuthorization auth = new MlAuthorization();
        auth.setUser(user);
        auth.setDataPool(dataPool);
        auth.setMaxConfidentiality(maxConfidentiality);
        auth.setEmail(email);
        auth.setAuthorizationType(MlAuthorization.AuthorizationType.DataPoolEnrollmentRequest);
        // create random token
        // create 128 bit alpha-numeric string, 130 is next multiple of 5 to comply with 32bit-base
        // source: http://stackoverflow.com/questions/41107
        try {
            String token = new BigInteger(130, SecureRandom.getInstance("SHA1PRNG", "SUN")).toString(32);
            auth.setToken(token);

        } catch (NoSuchAlgorithmException | NoSuchProviderException ex) {
            Logger.getLogger(ObjectManagerBean.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        em.persist(auth);
        return auth.getToken();
    }

    @Override
    public List<MlAuthorization> findPendingRequests(mlsClient dataPool, mlsUser user, MlAuthorization.AuthorizationType type) {
        Query q = em.createNamedQuery("MlAuthorization.findPendingRequests");
        q.setParameter("dataPool", dataPool);
        q.setParameter("user", user);
        q.setParameter("type", type);
        return q.getResultList();
    }

    @Override
    public mlsClient
            findDataPool(int dataPoolId) {
        return em.find(mlsClient.class, dataPoolId);
    }

    public void buildSubCollections(int rootId, int maxChildCount) throws SubCollectionExtractionException {
        mlsObject root = em.find(mlsObject.class, rootId);
        if (root == null) {
            throw new SubCollectionExtractionException("The input object does not exist");
        }
        if (!(root instanceof mlsObjectCollection)) {
            throw new SubCollectionExtractionException("The input must be of the type collection");
        }

        SubCollectionFilter filter = new SubCollectionFilter(root.getRelatives(), maxChildCount);
        int postFix = 0;
        while (filter.hasMore()) {
            try {

                // the broadcast channel
                MlMessageHandler mh = new MlMessageHandler();

                // create new collection
                mlsObject subCollection = (mlsObject) mlsObjectCollection.class.newInstance();
                setCommonAttributes(subCollection, root);
                setSpecificAttributes(subCollection);
                subCollection.setHeadline(root.getHeadline().concat("-").concat(Integer.toString(postFix++)));
                em.persist(subCollection);
                em.flush();
                solrServer.addObject(subCollection, true);
                logManager.log(subCollection.getClient(), MlsEventType.EventType.ObjectCreated,
                        subCollection, 0, subCollection.getHeadline(), "", mlsLog.Type.Create);

                // link it to root
                linkManagerRemote.link(rootId, subCollection.getId(), false, LinkRelativeType.OBJECT);

                List<mlsObject> childSet = filter.next();
                for (mlsObject child : childSet) {
                    linkManagerRemote.unlink(rootId, child.getId(), false, LinkRelativeType.OBJECT);
                    linkManagerRemote.link(subCollection.getId(), child.getId(), false, LinkRelativeType.OBJECT);
                }

            } catch (MlLinkException | NonExistingObjectException | InstantiationException | IllegalAccessException ex) {
                Logger.getLogger(ObjectManagerBean.class.getName()).log(Level.SEVERE, null, ex);
                throw new SubCollectionExtractionException(ex.getMessage());
            }
        }
    }

}
