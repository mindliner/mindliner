/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mindliner.cal.ReportingPeriod;
import com.mindliner.cal.ReportingPeriod.Period;
import com.mindliner.entities.MlsUserReport;
import com.mindliner.entities.mlsClient;
import com.mindliner.entities.mlsUser;
import com.mindliner.enums.LinkRelativeType;
import com.mindliner.json.UserActivityJson;
import com.mindliner.json.UserLinksJson;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * This bean prepares reports that serve as KPIs for data pool managers (client
 * admin).
 *
 * @author Marius Messerli
 */
@Stateless
@DeclareRoles(value = {"Admin", "User", "MasterAdmin"})
public class ReportManager implements ReportManagerRemote, ReportManagerLocal {

    @PersistenceContext
    private EntityManager em;
    @EJB
    private UserManagerLocal userManager;
    @EJB
    private ReportCacheBean reportCache;

    private enum MetricColumn {

        Creation,
        Modification,
        Deletion,
        LinksToOwn,
        LinksToForeign
    }


    @Override
    @RolesAllowed(value = {"User", "Admin", "MasterAdmin"})
    public String getUserActivityReport(int usr, ReportingPeriod.Period period) {
        Query q = em.createNamedQuery("MlsUserReport.getReports");
        List<Integer> cIds = new ArrayList<>();
        for (mlsClient c : userManager.getCurrentUser().getClients()) {
            cIds.add(c.getId());
        }
        Calendar c = Calendar.getInstance();
        c.setTime(period.getStartDate());
        // the values of the day preceeding the starting day of the period are used to calculate the
        // activity increases of the first day of the period.
        // --> The db contains the total acumulated amount for each activity since the start of measuring up until each day.
        c.add(Calendar.DAY_OF_YEAR, -1);
        q.setParameter("clientIds", cIds);
        q.setParameter("startDate", c.getTime());
        q.setParameter("endDate", period.getEndDate());
        q.setParameter("userId", usr);

        List<MlsUserReport> reports = q.getResultList();
        if (reports.size() < 2) {
            return "";
        }
        List<UserActivityJson> activity = new ArrayList<>();
        UserActivityJson creCnt = new UserActivityJson("Creations");
        UserActivityJson delCnt = new UserActivityJson("Deletions");
        UserActivityJson modCnt = new UserActivityJson("Modifications");
        UserActivityJson selfCnt = new UserActivityJson("Self links");
        UserActivityJson foreignCnt = new UserActivityJson("Foreign links");
        activity.add(creCnt);
        activity.add(delCnt);
        activity.add(modCnt);
        activity.add(selfCnt);
        activity.add(foreignCnt);
        for (MlsUserReport report : reports) {
            // There is a count per day and per client. addEntry will sum up the counts as of per day
            creCnt.addEntry(report.getCreateCount(), report.getCreationDate());
            delCnt.addEntry(report.getRemoveCount(), report.getCreationDate());
            modCnt.addEntry(report.getModifyCount(), report.getCreationDate());
            selfCnt.addEntry(report.getSelfLinksCount(), report.getCreationDate());
            foreignCnt.addEntry(report.getForeignLinksCount(), report.getCreationDate());
        }
        // Until now we acumulated the number of creations for each day from the begin of measuring.
        // These values we can transform into a list indicating only the per day increments.
        // To this end, the entry for the first day is ignored and only used to compute the increase of the next day.
        creCnt.computeDailyIncreases();
        delCnt.computeDailyIncreases();
        modCnt.computeDailyIncreases();
        selfCnt.computeDailyIncreases();
        foreignCnt.computeDailyIncreases();

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

        TypeToken typeToken = new TypeToken<List<UserActivityJson>>() {};
        String json = gson.toJson(activity, typeToken.getType());
        return json;
    }

    @Override
    public List<mlsUser> getVisibleUsers() {
        mlsUser currentUser = userManager.getCurrentUser();
        Query q = em.createNamedQuery("mlsUser.getUsersWithSharedDataPools");
        q.setParameter("userId", currentUser.getId());
        List<mlsUser> users = q.getResultList();
        if (users == null) {
            users = new ArrayList<>();
        }
        return users;
    }

    @Override
    public String getUserLinksReport(Period period) {
        Query q = em.createNamedQuery("MlsLink.getUserConnectionsCount");
        List<Integer> cIds = new ArrayList<>();
        for (mlsClient c : userManager.getCurrentUser().getClients()) {
            cIds.add(c.getId());
        }
        q.setParameter("clientId", cIds);
        q.setParameter("relativeType", LinkRelativeType.OBJECT);
        q.setParameter("endTime", period.getEndDate());
        q.setParameter("startTime", period.getStartDate());
        // query groups all links by link_owner, obj1_owner, obj2_owner and returns the count for each group
        List<Object[]> rows = q.getResultList();
        if (rows == null || rows.isEmpty()) {
            return "";
        }
        List<UserLinksJson> links = new ArrayList<>();
        Map<mlsUser, Map<mlsUser, Long>> map = new HashMap<>();
        for (Object[] row : rows) {
            mlsUser lOwner = (mlsUser) row[0];
            mlsUser o1Owner = (mlsUser) row[1];
            mlsUser o2Owner = (mlsUser) row[2];
            long count = (long) row[3];

            if (!map.containsKey(lOwner)) {
                map.put(lOwner, new HashMap<mlsUser, Long>());
            }
            Map<mlsUser, Long> ml = map.get(lOwner);
            // Links where both objects belong to the same user as the link are not counted (can't be displayed by d3js)
            if (!lOwner.equals(o1Owner)) {
                addToMap(ml, o1Owner, count);
            }
            if (!lOwner.equals(o2Owner) && !o1Owner.equals(o2Owner)) {
                addToMap(ml, o2Owner, count);
            }
        }

        Set<String> targets = new HashSet<>();
        Set<String> owners = new HashSet<>();
        for (Map.Entry<mlsUser, Map<mlsUser, Long>> entry : map.entrySet()) {
            UserLinksJson currUser = new UserLinksJson(entry.getKey().getUserName());
            if (entry.getValue().isEmpty()) {
                // If a user has no links or just self links, his map will be empty. We only want to display links to foreign objects
                continue;
            }
            for (Map.Entry<mlsUser, Long> sentry : entry.getValue().entrySet()) {
                currUser.addLink(sentry.getKey().getUserName(), sentry.getValue());
                targets.add(sentry.getKey().getUserName());
            }
            links.add(currUser);
            owners.add(entry.getKey().getUserName());
        }
        // All link targets must be mentioned in the json, even in the case they did not link anything
        targets.removeAll(owners);
        for (String t : targets) {
            links.add(new UserLinksJson(t));
        }
        if (links.isEmpty()) {
            return "";
        }

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

        TypeToken typeToken = new TypeToken<List<UserLinksJson>>() {
        };
        String json = gson.toJson(links, typeToken.getType());
        return json;
    }

    private void addToMap(Map<mlsUser, Long> ml, mlsUser o1Owner, long count) {
        if (ml.containsKey(o1Owner)) {
            ml.put(o1Owner, ml.get(o1Owner) + count);
        } else {
            ml.put(o1Owner, count);
        }
    }

    @Override
    public int getObjectCount(int userId) {
        Query q = em.createNamedQuery("mlsObject.getCountByOwner");
        q.setParameter("ownerId", userId);
        Long count = (Long) q.getSingleResult();
        return count.intValue();
    }

    @Override
    public int getObjectCount(mlsClient c) {
        Query q = em.createNamedQuery("mlsObject.getCountByClient");
        q.setParameter("clientId", c.getId());
        Long count = (Long) q.getSingleResult();
        return count.intValue();
    }

    @Override
    public int getObjectCount() {
        return reportCache.getTotalObjectCount();
    }

    @Override
    public Date getLastObjectModification() {
        return reportCache.getLastObjectModification();
    }

    @Override
    public int getLinkCount() {
        return reportCache.getTotalLinkCount();
    }

    @Override
    public int getLoggedInUserCount() {
        return reportCache.getOnlineUsers();
    }

}
