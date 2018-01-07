/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.mindliner.cache;

import com.mindliner.analysis.CurrentWorkTask;
import com.mindliner.analysis.WeekPlanSignature;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcTask;
import com.mindliner.clientobjects.mlcUser;
import com.mindliner.clientobjects.mlcWeekPlan;
import com.mindliner.clientobjects.mlcWorkUnit;
import com.mindliner.commands.CommandRecorder;
import com.mindliner.commands.WorkUnitCreationCommand;
import com.mindliner.commands.WorkUnitRemovalCommand;
import com.mindliner.events.ObjectChangeObserver;
import com.mindliner.managers.SearchManagerRemote;
import com.mindliner.managers.WorkManagerRemote;
import com.mindliner.objects.transfer.mltWeekPlan;
import com.mindliner.serveraccess.OnlineService;
import com.mindliner.serveraccess.OnlineService.OnlineStatus;
import com.mindliner.serveraccess.RemoteLookupAgent;
import com.mindliner.common.WeekUtil;
import com.mindliner.contentfilter.TimeFilter.TimePeriod;
import com.mindliner.main.MindlinerMain;
import com.mindliner.managers.UserManagerRemote;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.swing.JOptionPane;

/**
 * This cacher deals with week plans, work objects, work units, and some task
 * functions
 *
 * @author Marius Messerli
 */
public class WorkPlanCacheImpl implements CacheAgent, WorkPlanCache, OnlineService, ObjectChangeObserver {

    private static final long CURRENT_WORKER_UPDATE_INTERVALL = 10000;
    private WorkManagerRemote workManagerRemote = null;
    private UserManagerRemote userManager = null;
    private Map<Integer, mlcWeekPlan> weekPlanMap = new HashMap<>();
    private OnlineStatus onlineStatus = OnlineStatus.offline;
    private final static String WORK_CACHE_NAME_EXTENSION = "work";
    private int connectionPriority = 0;
    private SearchManagerRemote searchManager = null;
    private final MainCache mainCache;
    private final Map<mlcTask, Date> currentWorkeksUpdateMap = new HashMap<>();
    private final Map<mlcTask, List<mlcUser>> taskWorkersMap = new HashMap<>();

    public WorkPlanCacheImpl(MainCache mainCache) {
        this.mainCache = mainCache;
    }

    @Override
    public void objectChanged(mlcObject o) {
    }

    @Override
    public void objectReplaced(int oldId, mlcObject o) {
    }

    @Override
    public void objectDeleted(mlcObject o) {
        if (o == null) {
            throw new IllegalArgumentException("Object argument is zero.");
        }
        if (!(o instanceof mlcTask)) {
            return;
        }
        // remove the object id from the local weekplan cache
        for (mlcWeekPlan wp : weekPlanMap.values()) {
            Iterator it = wp.getTasksIds().iterator();
            for (; it.hasNext();) {
                Integer id = (Integer) it.next();
                if (id == null || id == o.getId()) {
                    it.remove();
                }
            }
        }
    }

    /**
     * Mindliner does not need any data in the work plan cache so we don't
     * complain hard if the cache loading does not work.
     *
     * @throws MlCacheException
     */
    @Override
    public void initialize() throws MlCacheException {
        try {
            File f = new File(CacheEngineStatic.getDataCacheFilePath(WORK_CACHE_NAME_EXTENSION));
            FileInputStream fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);
            loadCache(ois);
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(WorkPlanCacheImpl.class.getName()).warning("Could not read cache file, but can continue normally.");
        }

    }

    @Override
    public void objectCreated(mlcObject o) {
    }

    private mlcWeekPlan fetchWeekPlan(int id) {
        mltWeekPlan wt = workManagerRemote.getWeekPlanById(id);
        if (wt != null) {
            mlcWeekPlan w = new mlcWeekPlan(wt);
            weekPlanMap.put(w.getId(), w);
            return w;
        }
        return null;
    }

    @Override
    public mlcWeekPlan getWeekPlan(int id) {
        mlcWeekPlan wp = weekPlanMap.get(id);
        if (wp != null) {
            return wp;
        } else if (onlineStatus.equals(OnlineStatus.online)) {
            return fetchWeekPlan(id);
        } else {
            return null;
        }
    }

    @Override
    public mlcWeekPlan getWeekplan(int year, int week) {
        for (mlcWeekPlan w : weekPlanMap.values()) {
            if (w.getWeek() == week && w.getYear() == year && w.getUser().equals(CacheEngineStatic.getCurrentUser())) {
                return w;
            }
        }
        if (onlineStatus.equals(OnlineStatus.online)) {
            mlcWeekPlan wp = new mlcWeekPlan(workManagerRemote.getWeekPlan(year, week));
            weekPlanMap.put(wp.getId(), wp);
            return wp;
        }
        return null;
    }

    private void removeAllForeignCachedPlansForWeek(int year, int week) {
        Iterator<mlcWeekPlan> it = weekPlanMap.values().iterator();
        for (; it.hasNext();) {
            mlcWeekPlan w = it.next();
            if (w.getYear() == year && w.getWeek() == week && w.getUser().getId() != CacheEngineStatic.getCurrentUser().getId()) {
                it.remove();
            }
        }
    }

    private int getForeignWorkUnitCount(List<mlcWeekPlan> plans) {
        int count = 0;
        for (mlcWeekPlan w : plans) {
            for (Integer id : w.getTasksIds()) {
                mlcTask t = (mlcTask) CacheEngineStatic.getObject(id);
                if (t != null) {
                    count += t.getWorkUnits().size();
                }
            }
        }
        return count;
    }

    @Override
    public List<mlcWeekPlan> getForeignWeekPlans(int year, int week) {
        List<mlcWeekPlan> foreignPlans = new ArrayList<>();
        for (mlcWeekPlan w : weekPlanMap.values()) {
            if (w.getYear() == year 
                    && w.getWeek() == week 
                    && !w.getUser().equals(CacheEngineStatic.getCurrentUser())) {
                foreignPlans.add(w);
            }
        }
        if (foreignPlans.isEmpty() && onlineStatus.equals(OnlineStatus.online)) {

            List<mltWeekPlan> tplans = workManagerRemote.getForeignWeekPlans(year, week);
            if (tplans != null) {
                for (mltWeekPlan twp : tplans) {
                    mlcWeekPlan plan = new mlcWeekPlan(twp);
                    weekPlanMap.put(plan.getId(), plan);
                    foreignPlans.add(plan);
                }
            }
        }
        return foreignPlans;
    }

    @Override
    public mlcWeekPlan createWeekplan(int year, int week) {
        if (onlineStatus.equals(OnlineStatus.online)) {
            int planId = workManagerRemote.createWeekPlan(year, week);
            weekPlanMap.remove(planId);
            mlcWeekPlan wp = new mlcWeekPlan(workManagerRemote.getWeekPlanById(planId));
            weekPlanMap.put(wp.getId(), wp);
            return wp;
        } else {
            JOptionPane.showMessageDialog(MindlinerMain.getInstance(), "Cannot create new weekplans in offline mode.", "Weekplan Creation", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    @Override
    public void goOffline() {
        workManagerRemote = null;
        userManager = null;
        searchManager = null;
        onlineStatus = OnlineStatus.offline;
    }

    @Override
    public void goOnline() throws MlCacheException {
        if (!onlineStatus.equals(OnlineStatus.online)) {
            try {
                workManagerRemote = (WorkManagerRemote) RemoteLookupAgent.getManagerForClass(WorkManagerRemote.class);
                userManager = (UserManagerRemote) RemoteLookupAgent.getManagerForClass(UserManagerRemote.class);
                searchManager = (SearchManagerRemote) RemoteLookupAgent.getManagerForClass(SearchManagerRemote.class);
                onlineStatus = OnlineStatus.online;
            } catch (NamingException ex) {
                throw new MlCacheException(ex.getMessage());
            }
        }
    }

    @Override
    public OnlineStatus getStatus() {
        return onlineStatus;
    }

    @Override
    public String getServiceName() {
        return "Workplan Manager";
    }

    @Override
    public void storeCache() throws MlCacheException {
        FileOutputStream fos = null;
        try {
            File f = new File(CacheEngineStatic.getDataCacheFilePath(WORK_CACHE_NAME_EXTENSION));
            fos = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(weekPlanMap);
            fos.close();
        } catch (IOException ex) {
            throw new MlCacheException("Could not store cache: " + ex.getMessage());
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ex) {
                throw new MlCacheException("Could not store cache: " + ex.getMessage());
            }
        }
    }

    private void loadCache(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        weekPlanMap = (HashMap<Integer, mlcWeekPlan>) ois.readObject();
    }

    private long getActualMillisForTask(mlcUser user, mlcTask task, Date start, Date end, boolean plan) {
        long totalMillis = 0;
        for (mlcWorkUnit wu : task.getWorkUnits()) {
            if (start.compareTo(wu.getStart()) <= 0
                    && end.compareTo(wu.getStart()) > 0
                    && wu.isPlan() == plan
                    && wu.getUserId() == user.getId()) {
                totalMillis += (wu.getEnd().getTime() - wu.getStart().getTime());
            }
        }
        return totalMillis;
    }
    

    @Override
    public int getWorkMinutesForDay(mlcUser user, mlcTask task, mlcWeekPlan weekPlan, int dayOfWeek, boolean plan) {
        Calendar cal = getDayCalendar(weekPlan, dayOfWeek);
        Date start = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date end = cal.getTime();
        return (int) getActualMillisForTask(user, task, start, end, plan) / 1000 / 60;
    }

    private Calendar getDayCalendar(mlcWeekPlan weekPlan, int dayOfWeek) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, weekPlan.getYear());
        cal.set(Calendar.WEEK_OF_YEAR, weekPlan.getWeek());
        cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal;
    }
    
    

    @Override
    public boolean hasWorkInWeek(mlcUser user, mlcTask task, mlcWeekPlan plan) {
        Date start = WeekUtil.getWeekStart(plan.getYear(), plan.getWeek());
        Date end = WeekUtil.getWeekEnd(plan.getYear(), plan.getWeek());
        int totalMins = (int) getActualMillisForTask(user, task, start, end, false) / 1000 / 60;
        return totalMins > 0;

    }

    @Override
    public int getActualPastWeekAveragesForObject(int taskId, int weekPlanId, int numberOfPastWeeks) {
        int sum = 0;
        int nonZeroWeekCount = 0;
        mlcTask task = (mlcTask) CacheEngineStatic.getObject(taskId);
        mlcWeekPlan weekPlan = CacheEngineStatic.getWeekPlan(weekPlanId);
        if (task == null || weekPlan == null) {
            return 0;
        }
        for (int i = 1; i <= numberOfPastWeeks; i++) {
            mlcWeekPlan wp = getWeekplan(weekPlan.getYear(), weekPlan.getWeek() - i);
            long weekSum = getWeekMinutes(wp, false, task);
            if (weekSum > 0) {
                nonZeroWeekCount++;
                sum += weekSum;
            }
        }
        if (nonZeroWeekCount == 0) {
            return 0;
        }
        return sum / nonZeroWeekCount;
    }

    /**
     * Returns the number of minutes worked or planned this week.
     *
     * @param weekPlan The weekplan.
     * @param isPlan If true the functions adds up plan units, otherwise work
     * @param taskSelector The task for which the week minutes are requested; if
     * this is null then all tasks of the specified weekPlan are included units.
     * @return The number of minutes.
     */
    private int getWeekMinutes(mlcWeekPlan weekPlan, boolean isPlan, mlcTask taskSelector) {
        int minutes = 0;
        mlcUser user = weekPlan.getUser();
        for (Integer id : weekPlan.getTasksIds()) {
            mlcTask task = (mlcTask) CacheEngineStatic.getObject(id);
            if (task == null) {
                return 0;
            }
            if (taskSelector == null || task.equals(taskSelector)) {
                minutes
                        += getWorkMinutesForDay(user, task, weekPlan, Calendar.MONDAY, isPlan)
                        + getWorkMinutesForDay(user, task, weekPlan, Calendar.TUESDAY, isPlan)
                        + getWorkMinutesForDay(user, task, weekPlan, Calendar.WEDNESDAY, isPlan)
                        + getWorkMinutesForDay(user, task, weekPlan, Calendar.THURSDAY, isPlan)
                        + getWorkMinutesForDay(user, task, weekPlan, Calendar.FRIDAY, isPlan)
                        + getWorkMinutesForDay(user, task, weekPlan, Calendar.SATURDAY, isPlan)
                        + getWorkMinutesForDay(user, task, weekPlan, Calendar.SUNDAY, isPlan);
            }
        }
        return minutes;
    }

    @Override
    public int createWorkUnit(mlcTask task, Date start, Date end, String timeZoneId, boolean plan) {
        CommandRecorder cr = CommandRecorder.getInstance();
        WorkUnitCreationCommand wucc = new WorkUnitCreationCommand(task, start, end, timeZoneId, plan);
        cr.scheduleCommand(wucc);
        return wucc.getWorkUnit().getId();
    }

    @Override
    public void removeWorkUnit(mlcWorkUnit workUnit) {
        CommandRecorder cr = CommandRecorder.getInstance();
        // the first argument of the WorkUnitRemovalCommand is ignored
        cr.scheduleCommand(new WorkUnitRemovalCommand(null, workUnit));
    }

    @Override
    public Date getEndOfLastWorkUnit() {
        Calendar cal = Calendar.getInstance();
        cal.set(2005, 12, 31); // the start of the Mindliner development ;-)
        Date lastEnd = cal.getTime();
        for (mlcWeekPlan wp : weekPlanMap.values()) {
            for (Integer id : wp.getTasksIds()) {
                mlcTask task = (mlcTask) CacheEngineStatic.getObject(id);
                if (task != null) {
                    for (mlcWorkUnit w : task.getWorkUnits()) {
                        if (w.isPlan() == false && w.getEnd().compareTo(lastEnd) > 0) {
                            lastEnd = w.getEnd();
                        }
                    }
                }
            }
        }
        return lastEnd;
    }

    @Override
    public int getIntegratedWorkMinutes(mlcWeekPlan weekPlan) {
        if (weekPlan == null) {
            throw new IllegalArgumentException("The specified weekplan is null; cannot continue.");
        }
        int total = 0;
        for (Integer id : weekPlan.getTasksIds()) {
            mlcTask task = (mlcTask) CacheEngineStatic.getObject(id);
            if (task != null) {
                for (mlcWorkUnit w : task.getWorkUnits()) {
                    if (w.isPlan() == false) {
                        long duration = w.getEnd().getTime() - w.getStart().getTime();
                        total += duration / 1000 / 60;
                    }
                }
            }
        }
        return total;
    }

    @Override
    public int getCount() {
        int count = 0;
        Collection<mlcWeekPlan> weekPlans = weekPlanMap.values();
        for (mlcWeekPlan wp : weekPlans) {
            count += countWorkUnits(wp);
        }
        return count;
    }

    @Override
    public boolean isCacheUpToDate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private int countWorkUnits(mlcWeekPlan w) {
        int units = 0;
        for (Integer id : w.getTasksIds()) {
            if (!(CacheEngineStatic.getObject(id) instanceof mlcTask)) {
                return 0;
            }
            mlcTask t = (mlcTask) CacheEngineStatic.getObject(id);
            if (t != null) {
                units += t.getWorkUnits().size();
            }
        }
        return units;
    }

    @Override
    public void performOnlineCacheMaintenance(boolean forced) {
        if (!weekPlanMap.isEmpty()) {
            List<WeekPlanSignature> signatures = new ArrayList<>();
            for (mlcWeekPlan wp : weekPlanMap.values()) {
                signatures.add(new WeekPlanSignature(wp.getId(), wp.getVersion(), countWorkUnits(wp)));
            }
            List<Integer> outdatedWeekplanIds = workManagerRemote.getOutdatedWeekplanIds(signatures);
            for (Integer id : outdatedWeekplanIds) {
                weekPlanMap.remove(id);
                // cache re-fill is done on demand only
            }
        }
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
    public void ensureMyTasksDueInWeekAreOnPlan(int year, int week) {
        if (onlineStatus.equals(OnlineStatus.online)) {
            searchManager.ensureMyDueTasksAreOnWeekPlan(year, week);
        }
    }

    @Override
    public List<mlcTask> getMyOverdueTasks() {
        if (onlineStatus.equals(OnlineStatus.online)) {
            List<Integer> taskIds = searchManager.getOverdueTasksIds();
            return (List<mlcTask>) (List) CacheEngineStatic.getObjects(taskIds);
        } else {
            JOptionPane.showMessageDialog(MindlinerMain.getInstance(), "Sorry: Works only in onine mode right now", "List Overdue Tasks", JOptionPane.ERROR_MESSAGE);
            return new ArrayList<>();
        }
    }

    @Override
    public List<mlcTask> getMyUpcomingTasks(TimePeriod lookAhead) {
        if (onlineStatus.equals(OnlineStatus.online)) {
            try {
                List<Integer> taskIds = searchManager.getUpcomingTasksIds(lookAhead);
                return (List<mlcTask>) (List) mainCache.getObjectCache().getObjects(taskIds);
            } catch (MlCacheException ex) {
                Logger.getLogger(WorkPlanCacheImpl.class.getName()).log(Level.SEVERE, null, ex);
                return new ArrayList<>();
            }
        } else {
            JOptionPane.showMessageDialog(MindlinerMain.getInstance(), "Sorry: Works only in onine mode right now", "List Upcoming Tasks", JOptionPane.ERROR_MESSAGE);
            return new ArrayList<>();
        }
    }

    @Override
    public List<mlcTask> getMyPriorityTasks() {
        if (onlineStatus.equals(OnlineStatus.online)) {
            try {
                List<Integer> taskIds = searchManager.getOpenPriorityTasksIds();
                return (List<mlcTask>) (List) mainCache.getObjectCache().getObjects(taskIds);
            } catch (MlCacheException ex) {
                Logger.getLogger(WorkPlanCacheImpl.class.getName()).log(Level.SEVERE, null, ex);
                return new ArrayList<>();
            }
        } else {
            JOptionPane.showMessageDialog(MindlinerMain.getInstance(), "Sorry: Works only in onine mode right now", "List Priority Tasks", JOptionPane.ERROR_MESSAGE);
            return new ArrayList<>();
        }
    }

    @Override
    public List<mlcObject> getStandAloneObjects() {
        if (onlineStatus.equals(OnlineStatus.online)) {
            try {
                List<Integer> ids = searchManager.getActiveStandAloneObjectIds();
                return (List<mlcObject>) (List) mainCache.getObjectCache().getObjects(ids);
            } catch (MlCacheException ex) {
                Logger.getLogger(WorkPlanCacheImpl.class.getName()).log(Level.SEVERE, null, ex);
                return new ArrayList<>();
            }
        } else {
            JOptionPane.showMessageDialog(MindlinerMain.getInstance(), "Sorry: Works only in onine mode right now", "List Priority Tasks", JOptionPane.ERROR_MESSAGE);
            return new ArrayList<>();
        }
    }

    @Override
    public void setCurrentWorkObject(mlcTask t) {
        // this call only makes sense in online mode
        if (userManager != null) {
            userManager.setCurrentWorkObject(t == null ? null : t.getId());
        }
    }

    @Override
    public List<CurrentWorkTask> getCurrentWorkTasks() {
        if (userManager != null) {
            return userManager.getCurrentWorkTasks();
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Returns the users who are currently working on the specified tasks. Since
     * this operation is quite expensive and may be called on every redraw of
     * the screen it is updated at intervalls.
     *
     * @param task
     * @return
     */
    @Override
    public List<mlcUser> getCurrentWorkers(mlcTask task) {
        if (task == null || userManager == null) {
            return new ArrayList<>();
        }
        
        Date lastCheckDate = currentWorkeksUpdateMap.get(task);
        if (lastCheckDate == null // task was not checked before
                || (new Date()).getTime() - lastCheckDate.getTime() > CURRENT_WORKER_UPDATE_INTERVALL) { // last check has lapsed
            List<mlcUser> currentTaskWorkers = new ArrayList<>();
            List<CurrentWorkTask> currentWorkTasks = userManager.getCurrentWorkTasks();
            
            for (CurrentWorkTask cwt : currentWorkTasks) {
                if (cwt.getTaskId() == task.getId()) {
                    try {
                        mlcUser u = mainCache.getUser(cwt.getUserId());
                        if (u != null) {
                            currentTaskWorkers.add(u);
                        }
                    } catch (MlCacheException ex) {
                        Logger.getLogger(WorkPlanCacheImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            taskWorkersMap.put(task, currentTaskWorkers);
            currentWorkeksUpdateMap.put(task, new Date());
        }
        return taskWorkersMap.get(task);
    }
}
