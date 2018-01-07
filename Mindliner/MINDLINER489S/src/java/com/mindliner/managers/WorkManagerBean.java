/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mindliner.analysis.WeekPlanSignature;
import com.mindliner.cal.WeekNumbering;
import com.mindliner.categories.MlsEventType;
import com.mindliner.common.ClientIdsStringBuilder;
import com.mindliner.entities.mlsLog;
import com.mindliner.entities.mlsObject;
import com.mindliner.entities.mlsTask;
import com.mindliner.entities.mlsUser;
import com.mindliner.entities.mlsWeekPlan;
import com.mindliner.entities.mlsWorkUnit;
import com.mindliner.json.WorkUnitJson;
import com.mindliner.objects.transfer.mltWeekPlan;
import com.mindliner.objects.transfer.mltWorkUnit;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
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
 *
 * @author Marius Messerli
 */
@Stateless
@DeclareRoles(value = {"Admin", "User"})
@RolesAllowed(value = {"Admin", "User"})
public class WorkManagerBean implements WorkManagerRemote, WorkManagerLocal {

    @PersistenceContext
    private EntityManager em;
    @EJB
    UserManagerLocal userManager;
    @EJB
    CategoryManagerRemote catman;
    @Resource
    EJBContext ctx;
    @EJB
    LogManagerLocal logManager;

    @Override
    public mltWorkUnit createWorkUnit(int objectId, Date startTime, Date endTime, String timeZoneId, boolean plan) {
        if (startTime == null || endTime == null || startTime.getTime() > endTime.getTime()) {
            System.err.println("WorkUnit: Illegal start or end time. Work unit not registered");
            return null;
        } else {
            mlsUser u = userManager.getCurrentUser();
            mlsObject o = em.find(mlsObject.class, objectId);
            if (u != null && o != null && o instanceof mlsTask) {
                mlsWorkUnit w = new mlsWorkUnit();

                // full wiring of new tasks to both weekplans and workunits in memory
                mlsTask task = (mlsTask) o;
                w.setTask(task);
                task.getWorkUnits().add(w);
                // force a task update so that the new workunit is propagated to the clients
                task.setModificationDate(new Date());
                w.setStart(startTime);
                w.setEnd(endTime);
                w.setTimeZoneId(timeZoneId);
                w.setPlan(plan);
                w.setUser(u);
                u.getWorkUnits().add(w);
                em.persist(w);
                em.flush();
                logManager.log(o.getClient(), MlsEventType.EventType.ObjectWorkUnitAdded, o, 0, o.getHeadline(), "createWorkUnit", mlsLog.Type.Modify);

                // broadcast the task update but not the week plan update (as nobody is listening)
                MlMessageHandler mh = new MlMessageHandler();
                mh.sendMessage(userManager.getCurrentUser(), task, MlMessageHandler.MessageEventType.OBJECT_UPDATE_EVENT, "work unit addition");
                mh.closeConnection();
                return new mltWorkUnit(w);
            }
        }
        return null;
    }

    @Override
    public mltWeekPlan getWeekPlan(int year, int week) {
        mlsUser u = userManager.getCurrentUser();
        if (u != null) {
            mlsWeekPlan w = getWeekPlanForUser(u, year, week);
            if (w != null) {
                return new mltWeekPlan(w);
            } else {
                int i = createWeekPlan(year, week);
                mlsWeekPlan w2 = em.find(mlsWeekPlan.class, i);
                if (w2 != null) {
                    return new mltWeekPlan(w2);
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    @Override
    public mltWeekPlan getCurrentWeekPlan() {
        Date now = new Date();
        int activeWeek = WeekNumbering.getWeek(now);
        int activeYear = WeekNumbering.getYear(now);
        return getWeekPlan(activeYear, activeWeek);
    }

    private mlsWeekPlan getWeekPlanForUser(mlsUser user, int year, int week) {
        Query q = em.createQuery("SELECT w FROM mlsWeekPlan w WHERE w.year=" + year + " AND w.weekInYear=" + week + " AND w.user.id=" + user.getId());
        List<mlsWeekPlan> wList = q.getResultList();
        if (wList.size() == 1) {
            return wList.get(0);
        } else {
            return null;
        }
    }

    @Override
    public List<mltWeekPlan> getForeignWeekPlans(int year, int week) {
        mlsUser currentUser = userManager.getCurrentUser();

        Query q = em.createQuery("SELECT w FROM mlsWeekPlan w WHERE w.year = " + year
                + " AND w.weekInYear = " + week
                + " AND w.user.id IN (" + ClientIdsStringBuilder.buildColleaguesIdsString(currentUser) + ")");
        List<mlsWeekPlan> plans = q.getResultList();
        if (plans.isEmpty()) {
            return null;
        } else {
            mlsUser me = userManager.getCurrentUser();
            List<mltWeekPlan> transferPlans = new ArrayList<>();
            for (mlsWeekPlan w : plans) {
                if (!w.getUser().equals(me)) {
                    transferPlans.add(new mltWeekPlan(w));
                }
            }
            return transferPlans;
        }
    }

    @Override
    public void addToWeekPlan(int planId, int taskId) {
        mlsWeekPlan w = em.find(mlsWeekPlan.class, planId);
        if (w != null) {
            mlsObject o = em.find(mlsObject.class, taskId);
            if (o != null && o instanceof mlsTask && !w.getObjects().contains(o)) {
                w.getObjects().add(o);
                logManager.log(o.getClient(), MlsEventType.EventType.ObjectAddedToWeekplan, o, 0, "year=" + w.getYear() + ", week=" + w.getWeekInYear(), "addToWeekPlan", mlsLog.Type.Info);
            }
        }
    }

    @Override
    public void removeFromWorkPlan(int planId, int taskId) {
        mlsWeekPlan w = em.find(mlsWeekPlan.class, planId);
        if (w != null) {
            mlsObject o = em.find(mlsObject.class, taskId);
            if (o != null) {
                w.getObjects().remove(o);
                logManager.log(o.getClient(), MlsEventType.EventType.ObjectRemovedFromWeekplan, o, 0, "year=" + w.getYear() + ", week=" + w.getWeekInYear(), "removeFromWorkPlan", mlsLog.Type.Info);
            }
        }
    }

    @Override
    public int createWeekPlan(int year, int week) {
        mlsUser u = userManager.getCurrentUser();

        // check if that plan already exists and return it if positive
        Query q = em.createQuery("SELECT w FROM mlsWeekPlan w WHERE w.year=" + year + " AND w.weekInYear=" + week + " AND w.user.id=" + u.getId());
        List<mlsWeekPlan> resultList = q.getResultList();
        for (mlsWeekPlan w : resultList) {
            if (w.getWeekInYear() == week && w.getYear() == year) {
                return w.getId();
            }
        }

        mlsWeekPlan w = new mlsWeekPlan();
        w.setUser(u);
        w.setWeekInYear(week);
        w.setYear(year);
        em.persist(w);
        em.flush();
        return w.getId();
    }

    @Override
    public mltWeekPlan getWeekPlanById(int id) {
        mlsWeekPlan w = em.find(mlsWeekPlan.class, id);
        if (w != null) {
            return new mltWeekPlan(w);
        } else {
            return null;
        }
    }

    @Override
    public void removeWorkUnit(int id) {
        mlsWorkUnit w = em.find(mlsWorkUnit.class, id);
        if (w != null) {
            mlsTask task = w.getTask();
            task.getWorkUnits().remove(w);
            mlsUser u = em.find(mlsUser.class, w.getUser().getId());
            if (u != null) {
                u.getWorkUnits().remove(w);
            }
            em.remove(w);
            em.flush();
            logManager.log(task.getClient(), MlsEventType.EventType.ObjectWorkUnitRemoved, task, 0, "", "removeWorkUnit", mlsLog.Type.Info);
            // broadcast task update
            MlMessageHandler mh = new MlMessageHandler();
            mh.sendMessage(userManager.getCurrentUser(), task, MlMessageHandler.MessageEventType.OBJECT_UPDATE_EVENT, "work unit removal");
            mh.closeConnection();
        }
    }

    @Override
    public List<Integer> getOutdatedWeekplanIds(List<WeekPlanSignature> clientWeekplanSignatures) {
        List<Integer> outdated = new ArrayList<>();
        for (WeekPlanSignature sig : clientWeekplanSignatures) {
            mlsWeekPlan w = em.find(mlsWeekPlan.class, sig.getId());
            if (w == null) {
                outdated.add(sig.getId());
            } else if (sig.getVersion() != w.getVersion() || sig.getTaskCount() != w.getObjects().size()) {
                outdated.add(sig.getId());
            }
        }
        return outdated;
    }

    @Override
    public String getWorkUnitsJSON(int year, int month) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        Date start = c.getTime();
        c.add(Calendar.MONTH, 1);
        Date end = c.getTime();
        System.out.println("running query for start = " + start + " and end = " + end);
        Query q = em.createNamedQuery("mlsWorkUnit.getMonthReport");
        q.setParameter("periodStart", start);
        q.setParameter("periodEnd", end);
        q.setParameter("userId", userManager.getCurrentUser().getId());
        List<mlsWorkUnit> workUnits = q.getResultList();

        // trying to re-copy as gson did not return
        List<WorkUnitJson> workUnitsJson = new ArrayList<>();
        for (mlsWorkUnit w : workUnits) {
            workUnitsJson.add(new WorkUnitJson(w));
        }
            Gson gson = new Gson();        
        Type workUnitType = new TypeToken<List<WorkUnitJson>>() {}.getType();
        String outputJson = gson.toJson(workUnitsJson, workUnitType);
        return outputJson;
    }

}
