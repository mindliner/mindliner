/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.cache;

import com.mindliner.analysis.CurrentWorkTask;
import com.mindliner.categories.MlsNewsType;
import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.categories.mlsPriority;
import com.mindliner.clientobjects.MlcImage;
import com.mindliner.clientobjects.MlcLink;
import com.mindliner.clientobjects.mlcClient;
import com.mindliner.clientobjects.mlcNews;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcObjectCollection;
import com.mindliner.clientobjects.mlcTask;
import com.mindliner.clientobjects.mlcUser;
import com.mindliner.clientobjects.mlcWeekPlan;
import com.mindliner.clientobjects.mlcWorkUnit;
import com.mindliner.contentfilter.TimeFilter.TimePeriod;
import com.mindliner.contentfilter.mlFilterTO;
import com.mindliner.entities.SoftwareFeature;
import com.mindliner.exceptions.ForeignOwnerException;
import com.mindliner.exceptions.IsOwnerException;
import com.mindliner.exceptions.SubCollectionExtractionException;
import com.mindliner.image.LazyImage;
import com.mindliner.main.MindlinerMain;
import com.mindliner.objects.transfer.MltLog;
import com.mindliner.serveraccess.OnlineManager;
import com.mindliner.serveraccess.OnlineService;
import com.mindliner.serveraccess.StatusReporter;
import com.mindliner.system.MlSessionClientParams;
import java.awt.HeadlessException;
import java.awt.Image;
import java.io.File;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.apache.poi.ss.util.WorkbookUtil;

/**
 *
 * This class provides the main access to the Mindliner cache. Many of the cache
 * subsystem's functiosn are mapped to a static function to that it can be
 * called from anywhere in the application.
 *
 * @author Marius Messerli
 */
public class CacheEngineStatic {

    private static MainCache INSTANCE = null;

    public static MainCache createMainCache(String login) {
        synchronized (CacheEngineStatic.class) {
            if (INSTANCE == null) {
                INSTANCE = new MainCache();
                MlSessionClientParams.setCurrentLoginName(login);
                INSTANCE.setConnectionPriority(OnlineService.HIGH_PRIORITY);
                OnlineManager.getInstance().registerService(INSTANCE);
            }
        }
        return INSTANCE;
    }

    public static MainCache getMainCache() {
        if (INSTANCE == null) {
            throw new IllegalStateException("Attempted main cache used before initialization.");
        }
        return INSTANCE;
    }

    private static boolean ensureUserDirectoryExists(String path) {
        File candidateUserDirectory = new File(path);
        if (!candidateUserDirectory.isDirectory()) {
            if (candidateUserDirectory.exists()) {
                return false;
            }
            return candidateUserDirectory.mkdir();
        }
        return true;
    }

    /**
     * Returns a Mindliner cache file path.
     *
     * @param nameExtension A specific string to be chosen uniquely among the
     * Mindliner cache files.
     * @return
     */
    public static String getDataCacheFilePath(String nameExtension) {
        return getCacheFilePath(MlSessionClientParams.getDataCacheFileName(), nameExtension);
    }

    private static String getCacheFilePath(String baseName, String nameExtension) throws HeadlessException, IllegalStateException {
        String fileSeparator = System.getProperty("file.separator");
        StringBuilder sb = new StringBuilder(getCacheDirectoryForCurrentUser());
        if (ensureUserDirectoryExists(sb.toString())) {
            sb.append(fileSeparator).append(nameExtension);
            sb.append(
                    "-").append(baseName);
            return sb.toString();
        } else {
            JOptionPane.showMessageDialog(MindlinerMain.getInstance(), "Could not create user directory for cache file. Ensure " + sb.toString() + " is a directory you can write to or delete it");
            throw new IllegalStateException();
        }
    }

    public static String getColorCacheFilePath(String nameExtension) {
        return getCacheFilePath(MlSessionClientParams.getColorCacheFileName(), nameExtension);
    }

    public static String getCacheDirectoryForCurrentUser() {
        StringBuilder sb = new StringBuilder();
        // replaces characters in the user name that are illegal in a file path
        String safeUserName = WorkbookUtil.createSafeSheetName(MlSessionClientParams.getCurrentLoginName());
        String fileSeparator = System.getProperty("file.separator");
        sb.append(MlSessionClientParams.getMindlinerLocalDocsPath()).
                append(fileSeparator).
                append(safeUserName);
        return sb.toString();
    }

    public static synchronized List<mlcObject> getPrimarySearchHits(String searchString, mlFilterTO fto) {
        try {
            return INSTANCE.getObjectCache().getPrimarySearchHitsP(searchString, fto);
        } catch (MlCacheException ex) {
            JOptionPane.showMessageDialog(MindlinerMain.getInstance(), ex.getMessage(), "Failed to perform search", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public static mlcObject forceFetchServerObject(int id) throws MlCacheException {
        INSTANCE.getObjectCache().fetchObject(id);
        return INSTANCE.getObjectCache().getObject(id);
    }

    /**
     * This function returns a list of objects that are related to the specified
     * object.
     *
     * @param o The object for which related objects are to be found.
     * @return
     */
    public static List<mlcObject> getLinkedObjects(mlcObject o) {
        try {
            return INSTANCE.getObjectCache().getLinkedObjects(o);
        } catch (MlCacheException ex) {
            JOptionPane.showMessageDialog(MindlinerMain.getInstance(), ex.getMessage(), "Failed to get related objects", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public static mlcClient getClient(int clientId) {
        return INSTANCE.getObjectCache().getClient(clientId);
    }

    public static void loadLinkedObjects(List<mlcObject> holders) {
        try {
            INSTANCE.getObjectCache().loadLinkedObjects(holders);
        } catch (MlCacheException ex) {
            JOptionPane.showMessageDialog(MindlinerMain.getInstance(), ex.getMessage(), "Failed to get linked objects", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static int getLinksCount() {
        return INSTANCE.getObjectCache().getLinkCount();
    }

    public static void addRelative(mlcObject o, mlcObject relative, boolean isOneWay) {
        INSTANCE.getObjectCache().addRelative(o, relative, isOneWay);
    }

    public static void removeRelative(mlcObject o, mlcObject relative, boolean isOneWay) {
        INSTANCE.getObjectCache().removeRelative(o, relative, isOneWay);
    }

    public static void setStatusReporter(StatusReporter sr) {
        INSTANCE.getObjectCache().setStatusReporter(sr);
    }

    /**
     * Adds the specified object to the cache.
     *
     * @param o The object to be added to the cache
     */
    public static void addToCache(mlcObject o) {
        INSTANCE.getObjectCache().addToCache(o);
    }

    /**
     * Returns the object with the specified ID. If no object can be found in
     * the cache and the system is in online mode then a server lookup is
     * performed. Don't use this call in a loop for many objects as it is
     * inefficient. Use getObjects() instead.
     *
     * @param key
     * @return
     */
    public static mlcObject getObject(int key) {
        try {
            return INSTANCE.getObjectCache().getObject(key);
        } catch (MlCacheException ex) {
            // treat this exception silently - the synch subsystem may be checking for object that have been deleted from the server in the meantime
//            JOptionPane.showMessageDialog(null, ex.getMessage(), "Failed to get server object", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public static List<mlcObject> getObjects(List<Integer> ids) {
        try {
            return INSTANCE.getObjectCache().getObjects(ids);
        } catch (MlCacheException ex) {
            JOptionPane.showMessageDialog(MindlinerMain.getInstance(), ex.getMessage(), "Failed to reconstruct client objects", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public static mlcObject getFamilyRatingPeak(mlcObject start) {
        try {
            return INSTANCE.getFamilyRatingPeak(start);
        } catch (MlCacheException ex) {
            JOptionPane.showMessageDialog(MindlinerMain.getInstance(), ex.getMessage(), "Failed to get rating root", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public static double[] getRatingMinAndMax() {
        return INSTANCE.getObjectCache().getRatingMinMax();
    }

    /**
     * Removes an object from the from the cache and, if online, from the
     * server. This function should only be called from the
     * ObjectDeletionCommand who deals properly with online/offline state of the
     * application.
     *
     * @param o The object to be removed.
     */
    public static void removeObject(mlcObject o) {
        try {
            INSTANCE.getObjectCache().removeObject(o);
        } catch (ForeignOwnerException ex) {
            JOptionPane.showMessageDialog(MindlinerMain.getInstance(), ex.getMessage(), "Failed to remove object", JOptionPane.ERROR_MESSAGE);
        } catch (IsOwnerException ex) {
            JOptionPane.showMessageDialog(MindlinerMain.getInstance(), ex.getMessage(), "Failed to remove contact", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void removeObjects(List<mlcObject> o) {
        try {
            INSTANCE.getObjectCache().removeObjects(o);
        } catch (ForeignOwnerException ex) {
            JOptionPane.showMessageDialog(MindlinerMain.getInstance(), ex.getMessage(), "Failed to remove object", JOptionPane.ERROR_MESSAGE);
        } catch (IsOwnerException ex) {
            JOptionPane.showMessageDialog(MindlinerMain.getInstance(), ex.getMessage(), "Failed to remove contact", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void removeObjectFromCache(mlcObject o) {
        INSTANCE.getObjectCache().removeObjectFromCache(o);
    }

    public static void forceCacheReload(int objectId) {
        try {
            INSTANCE.getObjectCache().fetchObject(objectId);
        } catch (MlCacheException ex) {
            JOptionPane.showMessageDialog(MindlinerMain.getInstance(), ex.getMessage(), "Failed to get server object", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static mlsConfidentiality getConfidentiality(int id) {
        return INSTANCE.getCategoryCache().getConfidentiality(id);
    }

    public static mlsConfidentiality getClosestConfidentialityForLevel(int level) {
        return INSTANCE.getCategoryCache().getClosestConfidentialityForLevel(level);
    }

    public static MlsNewsType getActionItemType(int id) {
        return INSTANCE.getCategoryCache().getActionItemType(id);
    }

    public static mlsPriority getPriority(int id) {
        return INSTANCE.getCategoryCache().getPriority(id);
    }

    public static mlsPriority getPriority(String name) {
        return INSTANCE.getCategoryCache().getPriority(name);
    }

    public static mlcWeekPlan getWeekPlan(int id) {
        return INSTANCE.getWorkPlanManager().getWeekPlan(id);
    }

    public static mlcWeekPlan getWeekPlan(int year, int week) {
        return INSTANCE.getWorkPlanManager().getWeekplan(year, week);
    }

    public static List<mlcWeekPlan> getForeignWeekPlans(int year, int week) {
        return INSTANCE.getWorkPlanManager().getForeignWeekPlans(year, week);
    }

    public static void removeWorkUnit(mlcWorkUnit workUnit) {
        INSTANCE.getWorkPlanManager().removeWorkUnit(workUnit);
    }

    public static int createWorkUnit(mlcTask task, Date start, Date end, String timeZoneId, boolean plan) {
        return INSTANCE.getWorkPlanManager().createWorkUnit(task, start, end, timeZoneId, plan);
    }

    public static Date getLastWorkUnitEnd() {
        return INSTANCE.getWorkPlanManager().getEndOfLastWorkUnit();
    }

    public static void ensureMyTasksDueInWeekAreOnPlan(int year, int week) {
        INSTANCE.getWorkPlanManager().ensureMyTasksDueInWeekAreOnPlan(year, week);
    }

    /**
     * Returns the number of minutes that have been accumulated in the specified
     * week.
     *
     * @param wp The weekplan.
     * @return The number of minutes worked on the specified week
     */
    public static int getWorkForWeek(mlcWeekPlan wp) {
        assert (wp != null);
        return INSTANCE.getWorkPlanManager().getIntegratedWorkMinutes(wp);
    }

    public static int getDailyWorkMinutes(mlcUser user, mlcTask task, mlcWeekPlan weekPlan, int dayOfWeek, boolean isPlan) {
        return INSTANCE.getWorkPlanManager().getWorkMinutesForDay(user, task, weekPlan, dayOfWeek, isPlan);
    }

    public static boolean hasWorkInWeek(mlcUser user, mlcTask task, mlcWeekPlan weekPlan) {
        return INSTANCE.getWorkPlanManager().hasWorkInWeek(user, task, weekPlan);
    }

    public static int getActualPastWeekAverages(int taskId, int weekPlanId, int pastWeeks) {
        return INSTANCE.getWorkPlanManager().getActualPastWeekAveragesForObject(taskId, weekPlanId, pastWeeks);
    }

    /**
     * @todo replace this function and use INSTANCE.getSingleUser(userId) to
     * speed up
     * @param userId
     * @return
     * @throws MlCacheException
     */
    public static mlcUser getUser(int userId) throws MlCacheException {
        return INSTANCE.getUser(userId);
    }

    public static int getObjectCount() {
        return INSTANCE.getObjectCount();
    }

    public static int getWorkUnitCount() {
        return INSTANCE.getWorkUnitCount();
    }

    public static void storeCacheData() {
        try {
            INSTANCE.storeCache();
        } catch (MlCacheException ex) {
            JOptionPane.showMessageDialog(MindlinerMain.getInstance(), ex.getMessage(), "Cache Storatge", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void updateLinks(int key, boolean force) throws MlCacheException {
        INSTANCE.getObjectCache().updateLinks(key, force);
    }

    public static List<MlcImage> getIcons(mlcClient dataPool) {
        return INSTANCE.getObjectCache().getIcons(dataPool);
    }

    public static List<MlcImage> getIcons(List<Integer> iconIds) {
        return INSTANCE.getObjectCache().getIcons(iconIds);
    }

    public static List<mlsConfidentiality> getConfidentialities() {
        return INSTANCE.getCategoryCache().getConfidentialities();
    }

    public static List<mlsConfidentiality> getConfidentialities(int clientId) {
        return INSTANCE.getCategoryCache().getConfidentialities(clientId);
    }

    public static List<MlsNewsType> getActionItemTypes() {
        return INSTANCE.getCategoryCache().getActionItemTypes();
    }

    public static List<mlsPriority> getPriorities() {
        return INSTANCE.getCategoryCache().getPriorities();
    }

    public static Collection<SoftwareFeature> getFeatures() {
        return INSTANCE.getFeatures();
    }

    /**
     * This is _the_ call to obtain the current user. It is returning the cached
     * version whenever possible so this is a fast operation. The current user
     * is fetched from the server once per application session with the call
     * updateUsers().
     *
     * @return The current user.
     */
    public static mlcUser getCurrentUser() {
        try {
            return INSTANCE.getCurrentUser();
        } catch (MlCacheException ex) {
            JOptionPane.showMessageDialog(MindlinerMain.getInstance(), ex.getMessage(), "Current User", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public static List<mlcUser> getUsers() {
        return INSTANCE.getUsers();
    }

    public static List<mlcUser> getDataPoolMates() {
        return INSTANCE.getDataPoolMates();
    }

    public static void performMaintenance(boolean forced) throws MlCacheException {
        INSTANCE.performOnlineCacheMaintenance(forced);
    }

    public static void updateCellValue(int rowIndex, int columnIndex, Object value) {
    }

    public static MlcLink getLink(int ownerId, int relativeId) {
        return INSTANCE.getObjectCache().getLink(ownerId, relativeId);
    }

    public static List<MlcLink> getLinks(int ownerId) {
        return INSTANCE.getObjectCache().getLinks(ownerId);
    }

    public static void replaceObject(int oldId, mlcObject newObj) {
        INSTANCE.getObjectCache().replaceObject(oldId, newObj);
    }

    public static LazyImage getImageAsync(MlcImage image) {
        return INSTANCE.getObjectCache().getImageAsync(image);
    }

    public static void invalidateImage(int imageId) {
        INSTANCE.getObjectCache().invalidateImage(imageId);
    }

    public static void putImage(int imageId, Image img) {
        INSTANCE.getObjectCache().putImage(imageId, img);
    }

    public static Image getImageSync(int imageId) {
        return INSTANCE.getObjectCache().getImageSync(imageId);
    }

    public static List<mlcNews> getNews() {
        return INSTANCE.getObjectCache().getNews();
    }

    public static List<MltLog> getLog(List<Integer> keys) {
        return INSTANCE.getObjectCache().getLog(keys);
    }

    public static void clearObjectAndLinkCache() {
        INSTANCE.getObjectCache().clearObjectAndLinkCache();
    }

    public static List<mlcTask> getMyOverdueTasks() {
        return INSTANCE.getWorkPlanManager().getMyOverdueTasks();
    }

    public static List<mlcTask> getMyUpcomingTasks(TimePeriod period) {
        return INSTANCE.getWorkPlanManager().getMyUpcomingTasks(period);
    }

    public static List<mlcTask> getMyPriorityTasks() {
        return INSTANCE.getWorkPlanManager().getMyPriorityTasks();
    }

    public static List<mlcObject> getStandAloneObjects() {
        return INSTANCE.getWorkPlanManager().getStandAloneObjects();
    }

    public static void setCurrentWorkTask(mlcTask t) {
        INSTANCE.getWorkPlanManager().setCurrentWorkObject(t);
    }

    public static List<CurrentWorkTask> getCurrentWorkTasks() {
        return INSTANCE.getWorkPlanManager().getCurrentWorkTasks();
    }

    public static List<mlcUser> getCurrentWorkers(mlcTask t) {
        return INSTANCE.getWorkPlanManager().getCurrentWorkers(t);
    }

    /**
     * Returns the weekplan for the specified date and the current caller. If no
     * plan exists a new one is created.
     *
     * @param date The date for which the weekplan is needed.
     * @return
     */
    public static mlcWeekPlan getWeekPlan(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int week = c.get(Calendar.WEEK_OF_YEAR);
        int year = c.get(Calendar.YEAR);
        return CacheEngineStatic.getWeekPlan(year, week);
    }

    public static List<mlcObject> getIslandPeaks(int minimumIslandSize, int maximumResultCount) {
        return INSTANCE.getObjectCache().getIslandPeaks(minimumIslandSize, maximumResultCount);
    }
    
    public static void createSubCollections(mlcObjectCollection oc, int maxChildCount){
        try {
            INSTANCE.getObjectCache().createSubCollections(oc, maxChildCount);
        } catch (SubCollectionExtractionException ex) {
            Logger.getLogger(CacheEngineStatic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
