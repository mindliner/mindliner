/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.mindliner.cache;

import com.mindliner.analysis.MlClassHandler;
import com.mindliner.clientobjects.MlClientClassHandler;
import com.mindliner.clientobjects.MlcContainer;
import com.mindliner.clientobjects.MlcImage;
import com.mindliner.clientobjects.MlcLink;
import com.mindliner.clientobjects.MlcContainerMap;
import com.mindliner.clientobjects.mlcNews;
import com.mindliner.clientobjects.mlcClient;
import com.mindliner.clientobjects.mlcContact;
import com.mindliner.clientobjects.mlcKnowlet;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcObjectCollection;
import com.mindliner.clientobjects.mlcTask;
import com.mindliner.clientobjects.mlcUser;
import com.mindliner.clientobjects.mlcWorkUnit;
import com.mindliner.commands.CommandRecorder;
import com.mindliner.commands.LinkCommand;
import com.mindliner.contentfilter.mlFilterTO;
import com.mindliner.entities.MlsImage;
import com.mindliner.entities.SoftwareFeature;
import com.mindliner.events.ObjectChangeManager;
import com.mindliner.exceptions.ForeignOwnerException;
import com.mindliner.exceptions.IsOwnerException;
import com.mindliner.exceptions.NonExistingObjectException;
import com.mindliner.exceptions.SubCollectionExtractionException;
import com.mindliner.image.LazyImage;
import com.mindliner.main.MindlinerMain;
import com.mindliner.main.SearchPanel;
import com.mindliner.managers.ImageManagerRemote;
import com.mindliner.managers.LogManagerRemote;
import com.mindliner.managers.ObjectManagerRemote;
import com.mindliner.managers.SearchManagerRemote;
import com.mindliner.managers.SubscriptionManagerRemote;
import com.mindliner.managers.UserManagerRemote;
import com.mindliner.objects.transfer.MltContainer;
import com.mindliner.objects.transfer.MltImage;
import com.mindliner.objects.transfer.MltLink;
import com.mindliner.objects.transfer.MltObject;
import com.mindliner.objects.transfer.MltContainerMap;
import com.mindliner.objects.transfer.MltLog;
import com.mindliner.objects.transfer.ObjectSignature;
import com.mindliner.objects.transfer.MltNews;
import com.mindliner.objects.transfer.mltClient;
import com.mindliner.objects.transfer.mltContact;
import com.mindliner.objects.transfer.mltKnowlet;
import com.mindliner.objects.transfer.mltObjectCollection;
import com.mindliner.objects.transfer.mltTask;
import com.mindliner.objects.transfer.mltWorkUnit;
import com.mindliner.serveraccess.OnlineManager;
import com.mindliner.serveraccess.OnlineService;
import com.mindliner.serveraccess.RemoteLookupAgent;
import com.mindliner.serveraccess.StatusReporter;
import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.swing.JOptionPane;

/**
 *
 * @author Marius Messerli
 */
public class ObjectCacheImpl implements CacheAgent, ObjectCache, OnlineService {

    private final int CACHE_BLOCK_SIZE = 500;
    private final String OBJECT_CACHE_NAME_EXTENSION = "object";
    private OnlineStatus onlineStatus = OnlineStatus.offline;
    private Map<Integer, mlcObject> objects = new HashMap<>();
    // although MlcImage is also an mlcObject I handle MlcImage-Icons separately as they should not be available to normal user search/edit ops
    private Map<Integer, MlcImage> images = new HashMap<>();
    private Map<Integer, mlcContact> owners = new HashMap<>();
    private Map<Integer, mlcClient> clients = new HashMap<>();
    private Map<Integer, MltLog> logRecords = new HashMap<>();
    private ConcurrentHashMap<Integer, List<MlcLink>> relationMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, Boolean> relationsUpdated = new ConcurrentHashMap<>();
    private StatusReporter statusReporter = null;
    private CategoryCache categoryCache = null;
    // Remote Manager Interfaces
    private SearchManagerRemote searchManager = null;
    private ObjectManagerRemote objectManager = null;
    private LogManagerRemote logManager = null;
    private ImageManagerRemote imageManager = null;
    private SubscriptionManagerRemote subscriptionManager = null;
    UserManagerRemote userManager = null;
    private int connectionPriority = 0;
    private final ImageCache imgCache = new ImageCache();

    @Override
    public void initialize() throws MlCacheException {
        imgCache.initialize();
        try {
            File f = new File(CacheEngineStatic.getDataCacheFilePath(OBJECT_CACHE_NAME_EXTENSION));
            FileInputStream fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);
            loadCache(ois);
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(ObjectCacheImpl.class.getName()).log(Level.WARNING, "Could not load object cache: {0}", ex.getMessage());
            goOnline();
        }
    }

    @Override
    public mlcObject getObject(int key) throws MlCacheException {
        mlcObject o = objects.get(key);
        if (o == null && key > 0 && onlineStatus.equals(OnlineStatus.online)) {
            fetchObject(key);
            o = objects.get(key);
        }
        return o;
    }

    public List<MltLog> getLog(List<Integer> keys) {
        List<MltLog> result = new ArrayList<>();
        List<Integer> missing = new ArrayList<>();
        for (Integer key : keys) {
            MltLog l = logRecords.get(key);
            if (l != null) {
                result.add(l);
            } else if (key > 0) {
                missing.add(key);
            }
        }
        if (!missing.isEmpty() && onlineStatus.equals(OnlineStatus.online)) {
            List<MltLog> fetched = logManager.getLogRecords(missing);
            result.addAll(fetched);
            // add to cache
            for (MltLog l : fetched) {
                logRecords.put(l.getId(), l);
            }
        }
        return result;
    }

    private List<mlcObject> expandTransferObjects(List<MltObject> tlist) throws MlCacheException {
        int progressUpdateLotSize = 20;
        Iterator it = tlist.iterator();
        List<mlcObject> olist = new ArrayList<>();
        if (statusReporter != null) {
            statusReporter.startTask(0, tlist.size() / progressUpdateLotSize, true, true);
            statusReporter.setMessage("rebuilding objects...");
        }
        int i = 0;
        while (it.hasNext() && (statusReporter == null || !statusReporter.isCancelled())) {
            MltObject transfer = (MltObject) it.next();
            mlcObject o = buildObjectFromTransfer(transfer);
            if (o != null) {
                if (i % progressUpdateLotSize == 0) {
                    if (statusReporter != null) {
                        statusReporter.setProgress(i / progressUpdateLotSize);
                    }
                }
                olist.add(o);
                i++;
            }
        }
        if (statusReporter != null) {
            statusReporter.done();
            if (statusReporter.isCancelled() == true) {
                return new ArrayList<>();
            }
        }
        return olist;
    }

    private List<MltObject> fetchTransferObjectsFromServer(List<Integer> ids) {
        List<MltObject> transferList = new ArrayList<>();
        if (onlineStatus.equals(OnlineStatus.offline)) {
            return transferList;
        }
        int listCursorPosition = 0;
        if (statusReporter != null) {
            statusReporter.startTask(0, ids.size(), true, true);
        }
        if (statusReporter != null) {
//            statusReporter.setMessage("fetching updated objects from server...");
        }
        while (listCursorPosition < ids.size() && (statusReporter == null || !statusReporter.isCancelled())) {
            ArrayList<Integer> keyBlock = new ArrayList<>();
            int i = 0;
            while (i < CACHE_BLOCK_SIZE && listCursorPosition < ids.size()) {
                keyBlock.add(ids.get(listCursorPosition));
                i++;
                listCursorPosition++;
            }
            if (statusReporter != null) {
                statusReporter.setProgress(listCursorPosition);
            }
            List<MltObject> transferBlock = objectManager.getTransferObjects(keyBlock);
            transferList.addAll(transferBlock);
        }
        if (statusReporter != null && statusReporter.isCancelled()) {
            List<MltObject> truncatedTransferList = new ArrayList<>();
            for (int i = 0; i < listCursorPosition; i++) {
                truncatedTransferList.add(transferList.get(i));
            }
            transferList = truncatedTransferList;
        }
        if (statusReporter != null) {
            statusReporter.done();
        }
        return transferList;
    }

    @Override
    public List<mlcObject> getObjects(List<Integer> ids) throws MlCacheException {
        List<mlcObject> alreadInCache = new ArrayList<>();
        ArrayList<Integer> missingFromCache = new ArrayList<>();
        if (ids == null || ids.isEmpty()) {
            return alreadInCache;
        }
        /**
         * split the signatures into those found in the cache and those who need
         * to be fetch from the server make sure that the original sequence
         * remains unchanged
         */
        for (Integer id : ids) {
            mlcObject cachedObject = objects.get(id);
            if (cachedObject != null) {
                alreadInCache.add(cachedObject);
            } else {
                missingFromCache.add(id);
            }
        }
        int reconstructionMaxCount = 0;

        if (missingFromCache.isEmpty() || !OnlineManager.isOnline()) {
            return alreadInCache;
        } else {
            if (missingFromCache.size() > 1000) {
                int lot1 = missingFromCache.size() / 2;
                int lot2 = missingFromCache.size() / 4;
                final String GET_ALL = "Get all hits";
                final String GET_TOP = "Get top ";
                final String USE_CACHED_ONLY = "Use cached objects only";

                Object answer = JOptionPane.showInputDialog(
                        MindlinerMain.getInstance(),
                        "Download Preference.",
                        missingFromCache.size() + " objects to download.",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        new Object[]{
                            GET_ALL,
                            GET_TOP + lot1,
                            GET_TOP + lot2,
                            USE_CACHED_ONLY},
                        GET_ALL);

                // cancel was clicked
                if (answer == null) {
                    reconstructionMaxCount = 0;
                }
                if (answer instanceof String) {
                    String s = (String) answer;
                    if (s.compareTo(GET_TOP + lot1) == 0) {
                        reconstructionMaxCount = lot1;
                    } else if (s.compareTo(GET_TOP + lot2) == 0) {
                        reconstructionMaxCount = lot2;
                    } else if (s.compareTo(GET_ALL) == 0) {
                        reconstructionMaxCount = missingFromCache.size();
                    } else if (s.compareTo(USE_CACHED_ONLY) == 0) {
                        reconstructionMaxCount = 0;
                    }
                } else {
                    reconstructionMaxCount = 0;
                }
            } else {
                reconstructionMaxCount = missingFromCache.size();
            }

            if (reconstructionMaxCount < missingFromCache.size()) {
                ArrayList<Integer> truncatedCacheMisses = new ArrayList<>();
                for (int j = 0; j < reconstructionMaxCount; j++) {
                    truncatedCacheMisses.add(missingFromCache.get(j));
                }
                missingFromCache = truncatedCacheMisses;
            }

            // force loading of missing objects into objects cache
            if (missingFromCache.size() > 0) {
                List<MltObject> transferList = fetchTransferObjectsFromServer(missingFromCache);
                expandTransferObjects(transferList);
            }

            /**
             * a second pass is required now that all the items requested by
             * user are guaranteed to be in the cache; the original cacheHits
             * are overwritten
             */
            alreadInCache = new ArrayList<>();
            for (Integer id : ids) {
                mlcObject mbo = objects.get(id);
                if (mbo != null) {
                    alreadInCache.add(mbo);
                }
            }
            return alreadInCache;
        }
    }

    @Override
    public synchronized List<mlcObject> getPrimarySearchHitsP(String searchString, mlFilterTO fto) throws MlCacheException {
        if (onlineStatus.equals(OnlineStatus.online)) {
            if (statusReporter != null) {
                statusReporter.setMessage("receiving object signatures ...", 1);
            }
            List<Integer> ids = searchManager.getTextSearchResultsIds(searchString, fto);
            if (statusReporter != null) {
                statusReporter.done();
            }
            return getObjects(ids);
        } else {
            List<mlcObject> objectList = new ArrayList<>();
            Collection<mlcObject> cachedObjects = objects.values();
            String[] searchTerms = searchString.split(" ");

            for (mlcObject o : cachedObjects) {
                if (fto.getObjectType().equals(MlClassHandler.MindlinerObjectType.Any) || o.getClass().equals(MlClientClassHandler.getClassByType(fto.getObjectType()))) {
                    boolean passed = true;
                    for (String word : searchTerms) {
                        if (!(o.getHeadline().toLowerCase().contains(word.toLowerCase())
                                || o.getDescription().toLowerCase().contains(word.toLowerCase()))) {
                            passed = false;
                        }
                    }
                    if (passed) {
                        objectList.add(o);
                    }
                }
            }
            return SearchPanel.filterObjects(objectList);
        }
    }

    /**
     * Removes the specified object from the cache and, if in online mode, the
     * server.
     *
     * @param object
     * @throws com.mindliner.exceptions.ForeignOwnerException
     * @throws com.mindliner.exceptions.UserContactDeletionException
     * @throws com.mindliner.exceptions.IsOwnerException
     */
    @Override
    public void removeObject(mlcObject object) throws ForeignOwnerException, IsOwnerException {
        removeObjectFromCache(object);
        if (onlineStatus.equals(OnlineStatus.online)) {
            objectManager.remove(object.getId());
        }
    }

    /**
     * Removes the specified objects from the cache and, if in online mode, the
     * server.
     *
     * @param objects
     * @throws com.mindliner.exceptions.ForeignOwnerException
     * @throws com.mindliner.exceptions.UserContactDeletionException
     * @throws com.mindliner.exceptions.IsOwnerException
     */
    @Override
    public void removeObjects(List<mlcObject> objects) throws ForeignOwnerException, IsOwnerException {
        if (onlineStatus.equals(OnlineStatus.online)) {
            List<Integer> keys = new ArrayList<>();
            objects.stream().forEach((obj) -> {
                keys.add(obj.getId());
            });
            objectManager.removeObjects(keys);
        }
    }

    @Override
    public void removeObjectFromCache(mlcObject object) {
        if (object != null) {
            List<Integer> relativeIds = getRelativeIds(relationMap.get(object.getId()));
            // keep track of the non-cell relatives of the relatives of the object to delete
            if (relativeIds != null) {
                relativeIds.stream().map((id) -> objects.get(id)).filter((relative) -> (relative != null)).forEach((relative) -> {
                    LinkCommand.updateNonCellRelatives(object, relative, false);
                });
            }

            removeObjectFromLinkSets(object.getId());
            object.beforeDeletion();
            objects.remove(object.getId());
            // try the following just in case it is a contact
            owners.remove(object.getId());
        }
    }

    private void removeObjectFromLinkSets(int objectId) {
        Collection<List<MlcLink>> linkSets = relationMap.values();
        linkSets.stream().map((relatives) -> relatives.iterator()).forEach((iter) -> {
            for (; iter.hasNext();) {
                MlcLink link = (MlcLink) iter.next();
                if (link.getRelativeId() == objectId) {
                    iter.remove();
                }
            }
        });
    }

    @Override
    public void performOnlineCacheMaintenance(boolean forced) throws MlCacheException {

        // remove any temporary cache items if no offline commands are pending synchronization
        if (!CommandRecorder.getInstance().hasCommands()) {
            Iterator it = objects.values().iterator();
            for (; it.hasNext();) {
                mlcObject o = (mlcObject) it.next();
                if (o.getId() < 0) {
                    it.remove();
                }
            }
        }
        List<ObjectSignature> cacheSigs = new ArrayList<>();
        objects.values().stream().forEach((o) -> {
            cacheSigs.add(new ObjectSignature(o.getId(), o.getVersion()));
        });
        List<Integer> outdatedObjectsIds = objectManager.getOutdatedObjectsIds(cacheSigs);
        System.out.println("Cache maintenance: " + outdatedObjectsIds.size() + " out of " + cacheSigs.size() + " were out of date and therefore removed from cache.");
        expireLinksandObjects(outdatedObjectsIds, true);
        mlcUser u = CacheEngineStatic.getCurrentUser();
        if (CacheEngineStatic.getCurrentUser().isAuthorizedForFeature(SoftwareFeature.CurrentFeatures.WORKSPHEREMAP)) {
            CacheEngineStatic.getCurrentUser().getClientIds().forEach(i -> {
                imageManager.ensureDefaultWSMIconSet(i);
            });
        }
        reloadIcons();
    }

    @Override
    public void removeRelative(mlcObject o, mlcObject relative, boolean isOneWay) {
        List<MlcLink> relatives = relationMap.get(o.getId());
        if (relatives != null) {
            removeLink(relatives, relative);
        }

        relatives = relationMap.get(relative.getId());
        if (!isOneWay) {
            // also remove the reverse link
            if (relatives != null) {
                removeLink(relatives, o);
            }
        } else if (relatives != null) {
            for (MlcLink l : relatives) {
                if (l.getRelativeId() == o.getId()) {
                    l.setIsOneWay(true);
                }
            }
        }
    }

    private void removeLink(List<MlcLink> relatives, mlcObject relative) {
        Iterator it = relatives.iterator();
        while (it.hasNext()) {
            MlcLink l = (MlcLink) it.next();
            if (l.getRelativeId() == relative.getId()) {
                it.remove();
            }
        }
    }

    @Override
    public void addRelative(mlcObject o, mlcObject relative, boolean isOneWay) {

        if (relative == null) {
            return;
        }
        mlcUser currUser = CacheEngineStatic.getCurrentUser();
        MlcLink forward = new MlcLink(o, relative, currUser, o.getClient());
        // CREATE THE FORWARD LINK
        List<MlcLink> relatives = relationMap.get(o.getId());
        if (relatives == null) {
            relatives = new ArrayList<>();
        }
        if (!relatives.contains(forward)) {
            relatives.add(forward);
            o.setRelativeCount(o.getRelativeCount() + 1);
        }
        relationMap.put(o.getId(), relatives);

        // CREATE THE BACK LINK
        if (!isOneWay) {
            MlcLink backward = new MlcLink(relative, o, currUser, o.getClient());
            List<MlcLink> reverse = relationMap.get(relative.getId());
            if (reverse == null) {
                reverse = new ArrayList<>();
            }
            if (reverse.contains(backward) == false) {
                reverse.add(backward);
                relative.setRelativeCount(relative.getRelativeCount() + 1);
                relationMap.put(relative.getId(), reverse);
            }
        } else {
            // when removing a link only in one way, then we have to set the other link to isOneWay = true
            forward.setIsOneWay(true);
            List<MlcLink> reverse = relationMap.get(relative.getId());
            if (reverse != null) {
                reverse.stream().filter((l) -> (l.getRelativeId() == o.getId())).map((l) -> {
                    l.setIsOneWay(false);
                    return l;
                }).forEach((_item) -> {
                    forward.setIsOneWay(false);
                });
            }
        }
    }

    private mlcClient reconstructClient(int clientId) {
        assert (onlineStatus.equals(OnlineStatus.online));
        mltClient tc = userManager.getClient(clientId);
        if (tc == null) {
            return null;
        }
        mlcClient c = new mlcClient();
        c.setActive(tc.isActive());
        c.setId(tc.getId());
        c.setName(tc.getName());
        c.setVersion(tc.getVersion());
        return c;
    }

    private void setCommonFields(mlcObject co, MltObject transfer) throws MlCacheException {
        co.setId(transfer.getId());
        co.setConfidentiality(categoryCache.getConfidentiality(transfer.getConfidentialityId()));
        co.setDescription(transfer.getDescription());
        mlcUser owner;
        try {
            owner = CacheEngineStatic.getUser(transfer.getOwnerId());
        } catch (MlCacheException ex) {
            throw new MlCacheException("Could not find owner for transfer object " + transfer.getId());
        }
        co.setOwner(owner);
        co.setHeadline(transfer.getHeadline());
        co.setModificationDate(transfer.getModificationDate());
        co.setCreationDate(transfer.getCreationDate());
        co.setVersion(transfer.getVersion());
        co.setRating(transfer.getRating());
        co.setPrivateAccess(transfer.isPrivateAccess());
        co.setArchived(transfer.isArchived());
        co.setStatus(transfer.getStatus());
        co.setRelativesOrdered(transfer.isRelativesOrdered());
        co.setIslandId(transfer.getIsland_id());
        mlcClient client = getClient(transfer.getClientId());
        if (client == null) {
            throw new MlCacheException("Cannot reconstruct client for new object");
        }
        co.setSynchUnits(transfer.getSynchUnits());
        co.setClient(client);
        co.setRelativeCount(transfer.getRelativeCount());
    }

    private mlcObject buildObjectFromTransfer(MltObject transfer) throws MlCacheException {

        mlcObject newObject = null;
        if (transfer == null) {
            throw new MlCacheException("Cannot reconstruct client object for a transfer object that is null.");
        }
        if (transfer instanceof mltTask) {
            mlcTask tsk = new mlcTask();
            newObject = tsk;
            mltTask tto = (mltTask) transfer;
            tsk.setPriority(categoryCache.getPriority(tto.getPriorityOrdinal()));
            tsk.setEffortEstimation(tto.getEffortEstimation());
            tsk.setCompleted(tto.getCompleted());
            tsk.setDescription(tto.getDescription());
            tsk.setDueDate(tto.getDueDate());
            tsk.setId(transfer.getId()); // will be set again with common fields but I need it here already
            for (mltWorkUnit tw : tto.getWorkUnits()) {
                mlcWorkUnit nwu = new mlcWorkUnit(tw);
                nwu.setTaskId(tsk.getId());
                tsk.getWorkUnits().add(nwu);
            }
        } else if (transfer instanceof mltContact) {
            mlcContact c = new mlcContact();
            newObject = c;
            mltContact cto = (mltContact) transfer;
            c.setFirstName(cto.getFirstName());
            c.setMiddleName(cto.getMiddleName());
            c.setLastName(cto.getLastName());
            c.setEmail(cto.getEmail());
            c.setPhoneNumber(cto.getPhoneNumber());
            c.setMobileNumber(cto.getMobileNumber());
            c.setHeadline(cto.getHeadline());
            if (cto.getProfilePictureId() != -1) {
                MlcImage img = (MlcImage) getObject(cto.getProfilePictureId());
                c.setProfilePicture(img);
            }
        } else if (transfer instanceof MltNews) {
            mlcNews news = new mlcNews();
            newObject = news;
            MltNews newsT = (MltNews) transfer;
            news.setActionItemType(categoryCache.getActionItemType(newsT.getTypeId()));
            news.setUserObjectId(newsT.getUserObjectId());
            news.setModificationDate(newsT.getModificationDate());
            news.setLog(newsT.getLog());
        } else if (transfer instanceof mltObjectCollection) {
            mlcObjectCollection oc = new mlcObjectCollection();
            newObject = oc;
            mltObjectCollection octo = (mltObjectCollection) transfer;
            oc.setDescription(octo.getDescription());
        } else if (transfer instanceof mltKnowlet) {
            newObject = new mlcKnowlet();
        } else if (transfer instanceof MltImage) {
            MltImage ti = (MltImage) transfer;
            MlcImage ci = new MlcImage();
            // iconImages are not cached in  the special ImageCache as they are very small
            if (ti.getType() == null) {
                System.err.println("type is null for image head=" + ti.getHeadline() + " url=" + ti.getUrl());
            } else if (ti.getType().equals(MlsImage.ImageType.Icon)) {
                ci.setIcon(ti.getImage());
            } // url images are loaded into cache when needed
            else if (!ti.getType().equals(MlsImage.ImageType.URL)) {
                imgCache.putImage(ti.getId(), ti.getImage().getImage());
            }
            ci.setPixelSizeX(ti.getPixelSizeX());
            ci.setPixelSizeY(ti.getPixelSizeY());
            ci.setType(ti.getType());
            ci.setUrl(ti.getUrl());
            newObject = ci;
        } else if (transfer instanceof MltContainerMap) {
            MltContainerMap tt = (MltContainerMap) transfer;
            MlcContainerMap ct = new MlcContainerMap();
            ct.setObjectPositions(tt.getObjPositions());
            ct.setObjLinks(tt.getObjLinks());
            newObject = ct;
        } else if (transfer instanceof MltContainer) {
            MltContainer tc = (MltContainer) transfer;
            MlcContainer cc = new MlcContainer();
            cc.setPosX(tc.getPosX());
            cc.setPosY(tc.getPosY());
            cc.setWidth(tc.getWidth());
            cc.setHeight(tc.getHeight());
            cc.setColor(tc.getColor());
            cc.setOpacity(tc.getFill());
            cc.setStrokeWidth(tc.getStrokeWidth());
            cc.setStrokeStyle(tc.getStrokeStyle());
            newObject = cc;
        }
        setCommonFields(newObject, transfer);
        addToCache(newObject);

        return newObject;
    }

    @Override
    public void addToCache(mlcObject o) {
        if (o instanceof mlcContact) {
            owners.put(o.getId(), (mlcContact) o);
        }
        objects.put(o.getId(), o);
    }

    @Override
    public void fetchObject(int key) throws MlCacheException {
        if (key < 0) {
            throw new IllegalArgumentException("Key must not be negative.");
        }
        // ensure we delete it so we are sure not to work with an outdated object no matter what
        objects.remove(key);
        if (onlineStatus.equals(OnlineStatus.online)) {
            try {
                MltObject transferObject;
                transferObject = objectManager.getTransferObject(key);
                if (transferObject != null) {
                    mlcObject mo = buildObjectFromTransfer(transferObject);
                    if (!(mo instanceof mlcNews)) {
                        addToCache(mo);
                    }
                } else {
                    objects.remove(key);
                }
            } catch (NonExistingObjectException ex) {
                objects.remove(key);
                throw new MlCacheException(ex.getMessage());
            }
        }
    }

    /**
     * Function returns all objects related to the specified one. If in online
     * mode the relations are loaded from the server and the cache is updated
     * locally. If in offline mode the relations are loaded from cache.
     *
     * @param origin
     * @todo For the offline case - why don't we take the expand signature
     * function?
     */
    @Override
    public List<mlcObject> getLinkedObjects(mlcObject origin) throws MlCacheException {
        List<mlcObject> results = new ArrayList<>();
        if (origin instanceof mlcNews) {
            mlcNews ai = (mlcNews) origin;
            if (ai.getUserObjectId() != -1) {
                mlcObject userObject = getObject(ai.getUserObjectId());
                if (userObject != null && !results.contains(userObject)) {
                    results.add(userObject);
                }
            }
            return results;
        }
        updateLinks(origin.getId(), false);
        List<MlcLink> links = relationMap.get(origin.getId());

        if (links != null) {
            links = SearchPanel.filterLinks(links); // maybe there is for example a link owner restriction
            List<Integer> missingRelativesIds = new ArrayList<>();
            for (MlcLink link : links) {
                mlcObject relative = objects.get(link.getRelativeId());
                if (relative == null) {
                    missingRelativesIds.add(link.getRelativeId());
                }
            }
            getObjects(missingRelativesIds);
            for (MlcLink link : links) {
                mlcObject relative = objects.get(link.getRelativeId());
                if (relative != null && !results.contains(relative)) {
                    results.add(relative);
                }
            }
            return SearchPanel.filterObjects(results);
        }
        return results;
    }

    @Override
    public void loadLinkedObjects(List<mlcObject> holders) throws MlCacheException {
        for (mlcObject o : holders) {
            getLinkedObjects(o);
        }
    }

    @Override
    public void updateLinks(int key, boolean force) throws MlCacheException {
        List<Integer> wrapper = new ArrayList<>(1);
        wrapper.add(key);
        updateLinks(wrapper, force);
    }

    @Override
    public void updateLinks(List<Integer> keys, boolean force) throws MlCacheException {
        if (!onlineStatus.equals(OnlineStatus.online)) {
            return;
        }
        Iterator<Integer> it = keys.iterator();
        while (it.hasNext()) {
            int curr = it.next();
            // don't update the relations if not forced by caller and and we already updated them once
            if (curr < 0 || (!force && relationsUpdated.get(curr) != null)) {
                it.remove();
            }
        }
        if (keys.isEmpty()) {
            return;
        }
        Map<Integer, List<MltLink>> updatedRelatives = searchManager.fetchRelativesMap(keys);

        for (Integer key : updatedRelatives.keySet()) {
            List<MlcLink> links = convertLinks(updatedRelatives.get(key));
            relationMap.put(key, links);
            relationsUpdated.put(key, true);
        }
    }

    private List<MlcLink> convertLinks(List<MltLink> tlinks) {
        List<MlcLink> result = new ArrayList<>();
        for (MltLink tlink : tlinks) {
            mlcUser owner;
            try {
                owner = CacheEngineStatic.getUser(tlink.getOwnerId());
            } catch (MlCacheException ex) {
                Logger.getLogger(ObjectCacheImpl.class.getName()).log(Level.SEVERE, null, ex);
                continue;
            }
            mlcClient client = getClient(tlink.getClientId());
            MlcLink clink = new MlcLink(tlink, owner, client);
            result.add(clink);
        }
        return result;
    }

    @Override
    public void storeCache() throws MlCacheException {
        imgCache.storeCache();

        FileOutputStream fos = null;
        try {
            File f = new File(CacheEngineStatic.getDataCacheFilePath(OBJECT_CACHE_NAME_EXTENSION));
            fos = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(clients);
            oos.writeObject(objects);
            oos.writeObject(owners);
            oos.writeObject(relationMap);
            oos.writeObject(images);
            oos.writeObject(logRecords);
            fos.close();
        } catch (IOException ex) {
            throw new MlCacheException("Could not store object cache: " + ex.getMessage());
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ex) {
                throw new MlCacheException("Could not properly store object cache: " + ex.getMessage());
            }
        }
    }

    private void loadCache(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        clients = (Map<Integer, mlcClient>) ois.readObject();
        objects = (HashMap<Integer, mlcObject>) ois.readObject();
        owners = (HashMap<Integer, mlcContact>) ois.readObject();
        relationMap = (ConcurrentHashMap<Integer, List<MlcLink>>) ois.readObject();
        images = (Map<Integer, MlcImage>) ois.readObject();
        logRecords = (Map<Integer, MltLog>) ois.readObject();
    }

    @Override
    public void setCategoryCache(CategoryCache categoryCache) {
        this.categoryCache = categoryCache;
    }

    @Override
    public int getObjectCount() {
        return objects.size();
    }

    @Override
    public void goOffline() {
        if (!onlineStatus.equals(OnlineStatus.offline)) {
            searchManager = null;
            objectManager = null;
            userManager = null;
            imageManager = null;
            logManager = null;
            subscriptionManager = null;
            onlineStatus = OnlineStatus.offline;
        }
    }

    private void expireRelationsAndObjects() {
        if (relationMap.isEmpty() && objects.isEmpty()) {
            return;
        }
        expireLinksandObjects(logManager.getChangeList(), false);
    }

    private void expireLinksandObjects(List<Integer> changedObjectsIds, boolean fetch) {
        for (Integer cid : changedObjectsIds) {
            relationMap.remove(cid);
            if (objects.get(cid) != null) {
                objects.remove(cid);
            }
        }
        /* 
         * Flag remaining link sets as current except if link count == 1 in which case the link 
         * may have been added as a back-link without checking for additional links of the target object
         */
        relationsUpdated = new ConcurrentHashMap<>();
        for (Integer rid : relationMap.keySet()) {
            relationsUpdated.put(rid, Boolean.TRUE);
        }

        /*
         We need to fetch the expired objects and replace them at all locations where
         a reference is hold to them (e.g. in the map or the MlObjectTables).
        
         As there is no clean and proper way to find out to which expired objects a reference
         is hold somewhere else, we rather replace all expired objects.
        
         However we only need to do this for objects that are already in the cache (-> fetch).
         */
        if (fetch) {
            try {
                System.out.println("Fetching objects..........");
                List<mlcObject> objs = getObjects(changedObjectsIds);
                for (mlcObject obj : objs) {
                    ObjectChangeManager.objectChanged(obj);
                }
            } catch (MlCacheException ex) {
                Logger.getLogger(ObjectCacheImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void goOnline() throws MlCacheException {
        if (!onlineStatus.equals(OnlineStatus.online)) {
            try {
                objectManager = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
                searchManager = (SearchManagerRemote) RemoteLookupAgent.getManagerForClass(SearchManagerRemote.class);
                userManager = (UserManagerRemote) RemoteLookupAgent.getManagerForClass(UserManagerRemote.class);
                imageManager = (ImageManagerRemote) RemoteLookupAgent.getManagerForClass(ImageManagerRemote.class);
                logManager = (LogManagerRemote) RemoteLookupAgent.getManagerForClass(LogManagerRemote.class);
                subscriptionManager = (SubscriptionManagerRemote) RemoteLookupAgent.getManagerForClass(SubscriptionManagerRemote.class);
                onlineStatus = OnlineStatus.online;
            } catch (NamingException ex) {
                throw new MlCacheException(ex.getMessage());
            }
            expireRelationsAndObjects();
        }
    }

    @Override
    public String getServiceName() {
        return "Object Cache";
    }

    @Override
    public OnlineStatus getStatus() {
        return onlineStatus;
    }

    @Override
    public int getConnectionPriority() {
        return connectionPriority;
    }

    @Override
    public void setConnectionPriority(int priority) {
        connectionPriority = priority;
    }

    @Override
    public void linkObjects(mlcObject o1, mlcObject o2) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getLinkCount() {
        int count = 0;
        for (List<MlcLink> linkSet : relationMap.values()) {
            count += linkSet.size();
        }
        return count;
    }

    @Override
    public mlcClient getClient(int clientId) {
        mlcClient c = clients.get(clientId);
        if (c != null) {
            return c;
        }
        if (onlineStatus.equals(OnlineStatus.online)) {
            c = reconstructClient(clientId);
        }
        if (c != null) {
            clients.put(c.getId(), c);
        }
        return c;
    }

    private void reloadIcons() {
        if (!OnlineManager.isOnline()) {
            return;
        }
        // remove all icons from cache
        Iterator<MlcImage> imageIterator = images.values().iterator();
        while (imageIterator.hasNext()) {
            MlcImage img = imageIterator.next();
            if (img.getType() == MlsImage.ImageType.Icon) {
                imageIterator.remove();
            }
        }

        // reload form server
        List<Integer> ids = imageManager.getAllAccessibleIconIds();

        if (!ids.isEmpty()) {
            try {
                List<MlcImage> icons = (List<MlcImage>) (List<?>) getObjects(ids);
                for (MlcImage i : icons) {
                    images.put(i.getId(), i);
                }
            } catch (MlCacheException ex) {
                JOptionPane.showMessageDialog(MindlinerMain.getInstance(), ex.getMessage(), "Icon Download", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private List<MlcImage> compileIcons(mlcClient dataPool) {
        List<MlcImage> icons = new ArrayList<>();
        for (MlcImage img : images.values()) {
            if (img.getType() == MlsImage.ImageType.Icon && img.getClient().equals(dataPool)) {
                if (img.getIcon() == null) {
                    System.err.println("getIcon() is null for MlcImage " + img.getName() + ", ignored in compilation");
                } else {
                    icons.add(img);
                }
            }
        }
        return icons;
    }

    @Override
    public List<MlcImage> getIcons(mlcClient dataPool) {
        if (OnlineManager.isOnline() && compileIcons(dataPool).isEmpty()) {
            reloadIcons();
        }
        return compileIcons(dataPool);
    }

    @Override
    public List<MlcImage> getIcons(List<Integer> iconIds) {
        try {
            if (iconIds.isEmpty()) {
                return new ArrayList<>();
            }
            List<MlcImage> icons = new ArrayList<>();

            // determine data pool
            MlcImage firstIcon = (MlcImage) getObject(iconIds.get(0));
            if (firstIcon == null) {
                return new ArrayList<>();
            }
            for (MlcImage icon : getIcons(firstIcon.getClient())) {
                if (iconIds.contains(icon.getId())) {
                    icons.add(icon);
                }
            }
            return icons;
        } catch (MlCacheException ex) {
            Logger.getLogger(ObjectCacheImpl.class.getName()).log(Level.SEVERE, null, ex);
            return new ArrayList<>();
        }
    }

    private List<Integer> getRelativeIds(List<MlcLink> links) {
        List<Integer> result = new ArrayList<>();
        if (links == null) {
            return result;
        }
        for (MlcLink l : links) {
            result.add(l.getRelativeId());
        }
        return result;
    }

    @Override
    public MlcLink getLink(int ownerId, int relativeId) {
        List<MlcLink> links = relationMap.get(ownerId);
        if (links == null || links.isEmpty()) {
            return null;
        }
        for (MlcLink link : links) {
            if (link.getRelativeId() == relativeId) {
                return link;
            }
        }
        return null;
    }

    @Override
    public List<MlcLink> getLinks(int ownerId) {
        List<MlcLink> links = relationMap.get(ownerId);
        if (links == null || links.isEmpty()) {
            return new ArrayList<>();
        }
        return links;
    }

    @Override
    public void replaceObject(int oldId, mlcObject newObj) {
        // go through all links and check if there is one having
        // the old object as relative. Then replace it with the new object.
        for (List<MlcLink> links : relationMap.values()) {
            Iterator<MlcLink> it = links.iterator();
            while (it.hasNext()) {
                MlcLink l = it.next();
                if (l.getRelativeId() == oldId) {
                    l.setRelativeId(newObj.getId());
                }
            }
        }
    }

    @Override
    public LazyImage getImageAsync(MlcImage image) {
        if (image != null) {
            return imgCache.getImageAsync(image.getId(), image.getUrl());
        }
        return null;
    }

    @Override
    public void invalidateImage(int imageId) {
        imgCache.invalidateImage(imageId);
    }

    @Override
    public void putImage(int imageId, Image img) {
        if (img != null) {
            imgCache.putImage(imageId, img);
        }
    }

    @Override
    public Image getImageSync(int imageId) {
        return imgCache.getImageSync(imageId);
    }

    @Override
    public void setStatusReporter(StatusReporter sr) {
        this.statusReporter = sr;
    }

    @Override
    public double[] getRatingMinMax() {
        double min = 100000;
        double max = -1;
        for (mlcObject o : objects.values()) {
            if (o.getRating() < min) {
                min = o.getRating();
            }
            if (o.getRating() > max) {
                max = o.getRating();
            }
        }
        double[] result = new double[2];
        result[0] = min;
        result[1] = max;
        return result;
    }

    @Override
    public List<mlcTask> getMyTasks() {
        List<mlcTask> myTasks = new ArrayList<>();
        for (mlcObject o : objects.values()) {
            if (o instanceof mlcTask && o.getOwner().equals(CacheEngineStatic.getCurrentUser())) {
                myTasks.add((mlcTask) o);
            }
        }
        return myTasks;
    }

    private void purgeArchivedNewsFromCache() {
        Iterator it = objects.values().iterator();
        for (; it.hasNext();) {
            mlcObject o = (mlcObject) it.next();
            if (o instanceof mlcNews && o.isArchived()) {
                it.remove();
            }
        }
    }

    @Override
    public List<mlcNews> getNews() {
        purgeArchivedNewsFromCache();
        try {
            if (OnlineManager.isOnline() && subscriptionManager != null) {
                return (List) getObjects(subscriptionManager.getNewsIds());
            } else {
                List<mlcNews> news = new ArrayList<>();
                for (mlcObject o : objects.values()) {
                    if (o instanceof mlcNews) {
                        news.add((mlcNews) o);
                    }
                }
                return news;
            }
        } catch (MlCacheException ex) {
            Logger.getLogger(ObjectCacheImpl.class.getName()).log(Level.SEVERE, null, ex);
            return new ArrayList<>();
        }
    }

    @Override
    public void clearObjectAndLinkCache() {
        objects.clear();
        relationMap.clear();
        relationsUpdated.clear();
    }

    @Override
    public List<mlcObject> getIslandPeaks(int minimumIslandSize, int maximumResultcount) {
        try {
            List<Integer> islandPeakIds = searchManager.getIslandPeaks(minimumIslandSize, maximumResultcount);
            return getObjects(islandPeakIds);
        } catch (MlCacheException ex) {
            Logger.getLogger(ObjectCacheImpl.class.getName()).log(Level.SEVERE, null, ex);
            return new ArrayList<>();
        }
    }

    @Override
    public void createSubCollections(mlcObjectCollection rootCollection, int maxChildCount) throws SubCollectionExtractionException {
        int id = rootCollection.getId();
        // purge objects from cache
        relationMap.remove(id);
        relationsUpdated.remove(id);

        // build sub-collections
        objectManager.buildSubCollections(rootCollection.getId(), maxChildCount);
        try {
            // re-load cache
            fetchObject(id);
        } catch (MlCacheException ex) {
            Logger.getLogger(ObjectCacheImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
