/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.mindliner.cache;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcUser;
import com.mindliner.entities.SoftwareFeature;
import com.mindliner.events.ObjectChangeManager;
import com.mindliner.events.ObjectChangeObserver;
import com.mindliner.exceptions.NonExistingObjectException;
import com.mindliner.managers.FeatureManagerRemote;
import com.mindliner.managers.RatingAgentRemote;
import com.mindliner.managers.UserManagerRemote;
import com.mindliner.objects.transfer.mltUser;
import com.mindliner.prefs.MlPreferenceManager;
import com.mindliner.serveraccess.OnlineManager;
import com.mindliner.serveraccess.OnlineService;
import com.mindliner.serveraccess.RemoteLookupAgent;
import com.mindliner.system.MlSessionClientParams;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.naming.NamingException;

/**
 * This is the top class that represents the caching subsystem. It holds a
 * reference to all the cache subclasses and routes calls to these. Besides it
 * also acts as a cache subclass for things like the NickNames that should
 * actually be in their own caching subsystem.
 *
 * @author Marius Messerli
 */
public class MainCache implements CacheAgent, OnlineService {

    private Map<Integer, mlcUser> users = new HashMap<>();
    private Map<Integer, SoftwareFeature> softwareFeatures = new HashMap<>();
    private OnlineStatus onlineStatus = OnlineStatus.offline;
    // cache subsystems
    private CategoryCache categoryCache = null;
    private WorkPlanCache workPlanCache = null;
    private ObjectCache objectCache = null;
    public static final String MAIN_CACHE_FILE_EXTENSION = "main";
    private mlcUser currentUser = null;
    private UserManagerRemote userManager = null;
    private FeatureManagerRemote featureManager = null;
    private RatingAgentRemote ratingManager = null;
    private int connectionPriority = 0;
    private static final long serialVersionUID = 19640205L;
    private static final String LAST_MAINTENANCE_48H_KEY = "LastCacheMaintenance48";
    private static final String LAST_MAINTENANCE_1W_KEY = "LastCacheMaintenance168";

    /**
     * This function and each of its sub-systems first stays offline and
     * attempts to laod the cache file. Only if this fails the corresponding
     * sub-system is put into online mode and the required information is loaded
     * from the server. If this fails as well false is returned.
     *
     * @throws com.mindliner.cache.MlCacheException
     */
    @Override
    public void initialize() throws MlCacheException {
        OnlineManager onlineManager = OnlineManager.getInstance();
        // The sequence these calls appear below must match that of storeCache()

        categoryCache = new CategoryCacheImpl();
        ((CacheAgent) categoryCache).initialize();
        ((OnlineService) categoryCache).setConnectionPriority(OnlineService.HIGH_PRIORITY);
        onlineManager.registerService((OnlineService) categoryCache);

        objectCache = new ObjectCacheImpl();
        ((CacheAgent) objectCache).initialize();
        objectCache.setCategoryCache(categoryCache);
        ((OnlineService) objectCache).setConnectionPriority(OnlineService.HIGH_PRIORITY);
        onlineManager.registerService((OnlineService) objectCache);

        LinksChangeObserver lco = new LinksChangeObserver(objectCache);
        ObjectChangeManager.registerObserver(lco);

        workPlanCache = new WorkPlanCacheImpl(this);
        ((CacheAgent) workPlanCache).initialize();
        onlineManager.registerService((OnlineService) workPlanCache);
        ((OnlineService) workPlanCache).setConnectionPriority(OnlineService.MEDIUM_PRIORITY);
        ObjectChangeManager.registerObserver((ObjectChangeObserver) workPlanCache);

        loadCache();
    }

    /**
     * Terminates the statefull remote managers (stateful session beans) and
     * sets all remote managers and the online flag to null.
     */
    @Override
    public void goOffline() {
        if (onlineStatus != OnlineStatus.offline) {
            userManager = null;
            featureManager = null;
            ratingManager = null;
            onlineStatus = OnlineStatus.offline;
        }
    }

    /**
     * Initiate the session bean references.
     *
     * @throws com.mindliner.cache.MlCacheException if the remote service could
     * not be found (re-thrown NamingException)
     */
    @Override
    public void goOnline() throws MlCacheException {
        if (onlineStatus != OnlineStatus.online) {
            try {
                userManager = (UserManagerRemote) RemoteLookupAgent.getManagerForClass(UserManagerRemote.class);
                featureManager = (FeatureManagerRemote) RemoteLookupAgent.getManagerForClass(FeatureManagerRemote.class);
                ratingManager = (RatingAgentRemote) RemoteLookupAgent.getManagerForClass(RatingAgentRemote.class);
                onlineStatus = OnlineStatus.online;
            } catch (NamingException ex) {
                throw new MlCacheException(ex.getMessage());
            }
        }
    }

    @Override
    public String getServiceName() {
        return "Main Cache";
    }

    @Override
    public OnlineStatus getStatus() {
        return onlineStatus;
    }

    public Map<Integer, SoftwareFeature> getSoftwareFeatures() {
        return softwareFeatures;
    }

    public void updateRequiredSoftwareFeatures() {
        if (onlineStatus.equals(OnlineStatus.online)) {
            softwareFeatures.clear();
            Collection<SoftwareFeature> requiredSoftwareFeatures = featureManager.getRequiredSoftwareFeatures();
            for (SoftwareFeature sf : requiredSoftwareFeatures) {
                softwareFeatures.put(sf.getId(), sf);
            }
        } else {
            throw new IllegalStateException("Must not use this function in offline mode.");
        }
    }

    /**
     * In online mode the user is fetched from the server otherwise returned
     * from the cache if it exists. This is not very efficient but needs to be
     * like this for bootstrap.
     *
     * @todo this could be optimized so that the user is cached
     * @param userId
     * @return
     */
    private void fetchUser(int userId) throws MlCacheException {
        if (onlineStatus.equals(OnlineStatus.online) || onlineStatus.equals(OnlineStatus.goingOnline)) {
            mlcUser user = reconstructUser(userId);
            if (user != null) {
                users.put(userId, user);
            }
        } else {
            throw new IllegalStateException("Don't call fetch() in offline mode");
        }
    }

    private List<SoftwareFeature> convertFeaturesFromIds(List<Integer> ids) throws MlCacheException {
        if (softwareFeatures.isEmpty()) {
            goOnline();
            updateRequiredSoftwareFeatures();
        } else {
            // check if all the features are in the cache
            boolean featureMissing = false;
            for (Integer i : ids) {
                SoftwareFeature sf = softwareFeatures.get(i);
                if (sf == null) {
                    featureMissing = true;
                }
            }
            if (featureMissing) {
                goOnline();
                updateRequiredSoftwareFeatures();
            }
        }
        List<SoftwareFeature> flist = new ArrayList<>();
        for (Integer i : ids) {
            SoftwareFeature sf = softwareFeatures.get(i);
            if (sf != null) {
                flist.add(sf);
            }
        }
        return flist;
    }

    private mlcUser reconstructUser(int userId) throws MlCacheException {
        try {
            assert (onlineStatus.equals(OnlineStatus.online));
            mlcUser u;
            mltUser tu = userManager.getUser(userId);
            u = new mlcUser();
            u.setId(tu.getId());
            u.setMaxConfidentialityIds(tu.getMaxConfidentialityIds());
            u.setLoginName(tu.getLoginName());
            u.setFirstName(tu.getFirstName());
            u.setLastName(tu.getLastName());
            u.setEmail(tu.getEmail());
            u.setLastLogin(tu.getLastLogin());
            u.setLastLogout(tu.getLastLogout());
            u.setLastSeen(tu.getLastSeen());
            u.setSoftwareFeatures(convertFeaturesFromIds(tu.getSoftwareFeatureIds()));
            u.setClientIds(tu.getClientIds());
            u.setActive(tu.isActive());
            u.setLoginCount(tu.getLoginCount());
            u.setVersion(tu.getVersion());
            return u;
        } catch (NonExistingObjectException ex) {
            throw new MlCacheException(ex.getMessage());
        }
    }

    /**
     * This function attempts to return the most updated users. In offline mode
     * this is just what is currently in the cache. In online mode outdated
     * cache records and cache misses are re-loaded from the server.
     *
     * @throws com.mindliner.cache.MlCacheException
     */
    public void updateUsers() throws MlCacheException {
        if (onlineStatus.equals(OnlineStatus.online)) {
            List<Integer> userIds = userManager.getSignaturesOfActiveUsers();
            for (Integer id : userIds) {
                mlcUser u = users.get(id);
                if (u == null) {
                    fetchUser(id);
                }
            }
        }
    }

    public mlcUser getUser(int key) throws MlCacheException {
        mlcUser u = users.get(key);
        if (u == null) {
            fetchUser(key);
            u = users.get(key);
        }
        return u;
    }

    public List<mlcUser> getUsers() {
        ArrayList<mlcUser> userList = new ArrayList<>(users.values());
        return userList;
    }

    public int getObjectCount() {
        return objectCache.getObjectCount();
    }

    public int getWorkUnitCount() {
        return workPlanCache.getCount();
    }

    @Override
    public void storeCache() throws MlCacheException {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            File f = new File(CacheEngineStatic.getDataCacheFilePath(MAIN_CACHE_FILE_EXTENSION));
            fos = new FileOutputStream(f);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(MlSessionClientParams.DATA_CACHE_FILE_VERSION);
            oos.writeObject(getCurrentUser());
            oos.writeObject(users);
            oos.writeObject(softwareFeatures);
            oos.close();
            fos.close();
            ((CacheAgent) categoryCache).storeCache();
            ((CacheAgent) objectCache).storeCache();
            ((CacheAgent) workPlanCache).storeCache();
        } catch (IOException ex) {
            throw new MlCacheException(ex.getMessage());
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ex) {
                throw new MlCacheException(ex.getMessage());
            }
            try {
                if (oos != null) {
                    oos.close();
                }
            } catch (IOException ex) {
                throw new MlCacheException(ex.getMessage());
            }
        }
    }

    /**
     * This function is loading the specific information for this class only,
     * its sub-systems have been initialized earlier.
     */
    private void loadCache() throws MlCacheException {
        ObjectInputStream ois;
        try {
            File f = new File(CacheEngineStatic.getDataCacheFilePath(MAIN_CACHE_FILE_EXTENSION));
            FileInputStream fis = new FileInputStream(f);
            ois = new ObjectInputStream(fis);
            // just skipping version here - this has been checked before
            Integer version = (Integer) ois.readObject();

            mlcUser cacheUser = (mlcUser) ois.readObject();
            // the following comparison must be done via ID and not via equals because at this
            // time during startup the current user data structure is not fully filled
            MlSessionClientParams.setCurrentUserId(cacheUser.getId());
            users = (HashMap<Integer, mlcUser>) ois.readObject();
            if (users == null || users.isEmpty()) {
                goOnline();
                updateUsers();
            }
            softwareFeatures = (HashMap<Integer, SoftwareFeature>) ois.readObject();
            if (softwareFeatures == null) {
                goOffline();
                updateRequiredSoftwareFeatures();
            }
        } catch (IOException | ClassNotFoundException ex) {
            attemptOnlineConnection();
        }
    }

    private void attemptOnlineConnection() throws MlCacheException {
        // cache file not found, not beloging to caller, or structure out of date
        goOnline();
        updateCurrentUser(userManager.getCurrentUserId());

        // I also need the object cache online
        OnlineService os = (OnlineService) objectCache;
        os.goOnline();
        MlSessionClientParams.setCurrentUserId(getCurrentUserId());
        updateUsers();
    }

    public Collection<SoftwareFeature> getFeatures() {
        return softwareFeatures.values();
    }

    private void run48HourCycleIfNeeded(boolean forcedMaintenance, Preferences userPrefs) throws MlCacheException {
        String last48HRunKey = userPrefs.get(MlPreferenceManager.getFullyQualifiedPreferenceKey(LAST_MAINTENANCE_48H_KEY), "");
        Date lastRun = null;
        SimpleDateFormat sdf = new SimpleDateFormat();
        int hoursSinceLastRun;
        if (!last48HRunKey.isEmpty()) {
            try {
                lastRun = sdf.parse(last48HRunKey);
            } catch (ParseException ex) {
                Logger.getLogger(MainCache.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (lastRun == null) {
            hoursSinceLastRun = 49;
        } else {
            hoursSinceLastRun = (int) (((new Date()).getTime() - lastRun.getTime()) / 1000 / 60 / 60);
        }
        if (forcedMaintenance || hoursSinceLastRun > 2 * 24) {
            System.out.println("Performing 48 hour cache maintenace....");
            updateRequiredSoftwareFeatures();
            updateUsers();
            userPrefs.put(MlPreferenceManager.getFullyQualifiedPreferenceKey(LAST_MAINTENANCE_48H_KEY), sdf.format(new Date()));
        }
    }

    private void runWeekCycleIfNeeded(boolean forcedMaintenance, Preferences userPrefs) throws MlCacheException {
        String last1WRunKey = userPrefs.get(MlPreferenceManager.getFullyQualifiedPreferenceKey(LAST_MAINTENANCE_1W_KEY), "");
        Date lastRun = null;
        int hoursSinceLastRun;
        SimpleDateFormat sdf = new SimpleDateFormat();
        if (!last1WRunKey.isEmpty()) {
            try {
                lastRun = sdf.parse(last1WRunKey);
            } catch (ParseException ex) {
                // if format is wrong then treat case like it is not present
            }
        }
        int refractionWindow = 7 * 24;
        if (lastRun == null) {
            hoursSinceLastRun = refractionWindow + 1;
        } else {
            hoursSinceLastRun = (int) (((new Date()).getTime() - lastRun.getTime()) / 1000 / 60 / 60);
        }

        if (forcedMaintenance || hoursSinceLastRun > refractionWindow) {
            System.out.println("Performing weekly cache maintenace....");
            ((CacheAgent) categoryCache).performOnlineCacheMaintenance(forcedMaintenance);
            userPrefs.put(MlPreferenceManager.getFullyQualifiedPreferenceKey(LAST_MAINTENANCE_1W_KEY), sdf.format(new Date()));
        }
    }

    @Override
    public void performOnlineCacheMaintenance(boolean forcedMaintenance) throws MlCacheException {
        if (!onlineStatus.equals(OnlineStatus.online)) {
            System.err.println("Cannot perform cache maintenance in offline mode; ignoring request");
            return;
        }
        Preferences userPrefs = Preferences.userNodeForPackage(MainCache.class);

        // always update the object cache
        updateCurrentUser(userManager.getCurrentUserId());
        ((CacheAgent) objectCache).performOnlineCacheMaintenance(forcedMaintenance);
        ((CacheAgent) workPlanCache).performOnlineCacheMaintenance(forcedMaintenance);

        run48HourCycleIfNeeded(forcedMaintenance, userPrefs);
        runWeekCycleIfNeeded(forcedMaintenance, userPrefs);
    }

    /**
     * This function reconstructs the current user from the server.
     *
     * @param currentUserId
     * @throws MlCacheException If the system is in offline mode or the call to
     * reconstruct the user fails otherwise.
     */
    public void updateCurrentUser(int currentUserId) throws MlCacheException {
        if (onlineStatus.equals(OnlineStatus.goingOnline) || onlineStatus.equals(OnlineStatus.online)) {
            mlcUser cu = reconstructUser(currentUserId);
            users.put(cu.getId(), cu);
            currentUser = cu;
            MlSessionClientParams.setCurrentUserId(currentUserId);
        } else {
            throw new MlCacheException("Failed to reconstruct current user as system is in offline mode");
        }
    }

    public mlcUser getCurrentUser() throws MlCacheException {
        if (currentUser == null) {
            // is it in cache?
            currentUser = users.get(MlSessionClientParams.getCurrentUserId());
            if (currentUser == null) {
                // can I fetch it?
                fetchUser(MlSessionClientParams.getCurrentUserId());
                currentUser = users.get(MlSessionClientParams.getCurrentUserId());
            }
            if (currentUser == null) {
                throw new MlCacheException("Failed to get current user (id=" + MlSessionClientParams.getCurrentUserId() + ")");
            }
        }
        return currentUser;
    }

    public int getCurrentUserId() {
        int userId = userManager.getCurrentUserId();
        return userId;
    }

    public List<mlcUser> getDataPoolMates() {
        List<mlcUser> mates = new ArrayList<>();
        for (mlcUser u : users.values()) {
            if (!Collections.disjoint(mates, u.getClientIds())) {
                mates.add(u);
            }
        }
        return mates;
    }

    public ObjectCache getObjectCache() {
        return objectCache;
    }

    public CategoryCache getCategoryCache() {
        return categoryCache;
    }

    public WorkPlanCache getWorkPlanManager() {
        return workPlanCache;
    }

    @Override
    public int getConnectionPriority() {
        return connectionPriority;
    }

    @Override
    public void setConnectionPriority(int priority) {
        connectionPriority = priority;
    }

    public mlcObject getFamilyRatingPeak(mlcObject start) throws MlCacheException {
        int peakId = ratingManager.getFamilyRatingPeak(start.getId(), 5);
        return objectCache.getObject(peakId);
    }

}
