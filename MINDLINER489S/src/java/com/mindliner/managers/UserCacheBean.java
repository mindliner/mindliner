/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.analysis.CurrentWorkTask;
import com.mindliner.entities.mlsTask;
import com.mindliner.entities.mlsUser;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * Caches the ID for a username and the lastSeen attribute of a user. Is used by
 * UserManagerBean to get the corresponding ID of a username
 *
 *
 * @author Dominic Plangger
 */
@Singleton
public class UserCacheBean {

    @PersistenceContext
    private EntityManager em;

    private Map<String, Integer> userIdMap;
    private Map<Integer, Date> lastSeenMap;
    private Set<Integer> updatedUsers;
    private Map<Integer, Integer> currentTask;

    @PostConstruct
    public void setup() {
        userIdMap = new HashMap<>();
        lastSeenMap = new HashMap<>();
        updatedUsers = new HashSet<>();
        currentTask = new HashMap<>();
    }

    public Integer getUserId(String username) {
        if (userIdMap.containsKey(username)) {
            return userIdMap.get(username);
        } else {
            Logger.getLogger(UserCacheBean.class.getName()).log(Level.INFO, "User {0} not in cache, fetching from DB", username);
            Query nq = em.createNamedQuery("mlsUser.getUserByUserName");
            nq.setParameter("userName", username);
            mlsUser u = (mlsUser) nq.getSingleResult();
            if (u == null) {
                return null;
            }
            userIdMap.put(username, u.getId());
            return u.getId();
        }
    }

    public void setLastSeen(Integer userId, Date d) {
        lastSeenMap.put(userId, d);
        updatedUsers.add(userId);
    }

    public Date getLastSeen(Integer userId) {
        return lastSeenMap.get(userId);
    }

    public List<mlsUser> getLoggedInUsers() {
        List<mlsUser> liu = new ArrayList<>();
        for (Integer i : updatedUsers) {
            mlsUser u = em.find(mlsUser.class, i);
            if (u != null) {
                liu.add(u);
            }
        }
        return liu;
    }

    // persist lastSeen attribute every 5 minutes
    @Schedule(minute = "*/10", hour = "*")
    public void persistLastSeen() {
        for (Integer id : updatedUsers) {
            mlsUser u = em.find(mlsUser.class, id);
            if (u != null) {
                u.setLastSeen(lastSeenMap.get(id));
            }
        }
        updatedUsers.clear();
    }

    /**
     * Specifies the current work object for the specified user.
     *
     * @param u The user for which the current work task is set.
     * @param t The task or null to clear the current task for the specified
     * user.
     */
    public void setCurrentWorkObject(mlsUser u, mlsTask t) {
        if (t == null) {
            currentTask.remove(u.getId());
            return;
        }
        currentTask.put(u.getId(), t.getId());
    }
    
    public List<CurrentWorkTask> getCurrentWorkTasks(){
        List<CurrentWorkTask> cwt = new ArrayList<>();
        for (int userId : currentTask.keySet()){
            Integer taskId = currentTask.get(userId);
            if (taskId != null){
                cwt.add(new CurrentWorkTask(userId, taskId));
            }
        }
        return cwt;
    }

}
