/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.categories.MlsEventType;
import com.mindliner.entities.MlsNews;
import com.mindliner.entities.mlsClient;
import com.mindliner.entities.mlsLog;
import com.mindliner.entities.mlsObject;
import com.mindliner.entities.mlsUser;
import com.mindliner.exceptions.ForeignClientException;
import com.mindliner.objects.transfer.MltLog;
import java.util.ArrayList;
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
@DeclareRoles(value = {"MasterAdmin", "Admin", "User"})
@RolesAllowed(value = {"Admin", "User"})
public class LogManagerBean implements LogManagerRemote, LogManagerLocal {

    @PersistenceContext
    private EntityManager em;
    @EJB
    private UserManagerLocal userManager;

    private List<mlsLog> getLog(mlsUser user, int maxRecords) {
        Query q = em.createNamedQuery("mlsLog.getRecentRecords");
        q.setParameter("user", user);
        q.setMaxResults(maxRecords);
        return q.getResultList();
    }

    @Override
    @RolesAllowed(value = {"MasterAdmin"})
    public List<mlsLog> getLog(int userId, int numberOfRecords) {
        mlsUser user = em.find(mlsUser.class, userId);
        if (user != null) {
            return getLog(user, numberOfRecords);
        }
        return new ArrayList<>();
    }

    @Override
    public List<MltLog> getLogTransferObjects(int userId, int numberOfRecords) {
        List<mlsLog> logSection = getLog(userId, numberOfRecords);
        List<MltLog> tlog = new ArrayList<>();
        for (mlsLog l : logSection) {
            tlog.add(new MltLog(l));
        }
        return tlog;
    }

    @Override
    public List<MltLog> getLogRecords(List<Integer> ids) {
        List<MltLog> result = new ArrayList<>();
        for (Integer i : ids) {
            mlsLog l = em.find(mlsLog.class, i);
            if (l != null) {
                result.add(new MltLog(l));
            }
        }
        return result;
    }

    @Override
    @RolesAllowed(value = {"Admin", "MasterAdmin"})
    public List<mlsLog> clientAdminGetUserLog(int userId, int numberOfRecords) throws ForeignClientException {

        // here implement a check whether the calling user indeed has 
        mlsUser specifiedUser = em.find(mlsUser.class, userId);
        if (specifiedUser != null) {
            // only return log for users to callers with shared client
            for (mlsClient c : userManager.getCurrentUser().getClients()) {
                if (specifiedUser.getClients().contains(c)) {
                    return getLog(specifiedUser, numberOfRecords);
                }
            }
            throw new ForeignClientException("User does not belong to caller's client");
        }
        return new ArrayList<>();
    }

    @Override
    public List<mlsLog> getLogForObject(int objectId, int recordCount) {
        Query q = em.createNamedQuery("mlsLog.getObjectLog");
        q.setParameter("objectId", objectId);
        q.setParameter("userId", userManager.getCurrentUser().getId());
        return q.getResultList();
    }

    @Override
    public List<Integer> getChangeList() {
        List<Integer> changeIds = new ArrayList<>();
        mlsUser currentUser = userManager.getCurrentUser();
        Date start = currentUser.getLastLogout();
        Date end = currentUser.getLastLogin();
        if (start == null || end == null || start.compareTo(end) >= 0) {
            // either never logged in before or something is wrong with the logout/login records
            return changeIds;
        }
        Query q = em.createNamedQuery("mlsLog.findChangedObjects");
        q.setParameter("startTime", start);
        q.setParameter("endTime", end);
        List<mlsLog> changeList = q.getResultList();
        for (mlsLog l : changeList) {
            if (l.isHasObject()) {
                changeIds.add(l.getObjectId());
            }
        }
        return changeIds;
    }

    @Override
    public void log(mlsClient client, MlsEventType.EventType eventType, mlsObject object, int linkObjectId, String headline, String method, mlsLog.Type type, Date timestamp) {
        // we don't log news events as this would create a feedback loop with a user subscribing to the creation event
        if (object instanceof MlsNews) {
            return;
        }
        if (timestamp == null) {
            timestamp = new Date();
        }
        mlsLog l = new mlsLog();
        mlsUser u = userManager.getCurrentUser();
        l.setDataPool(client);
        l.setEventType(eventType);
        l.setHasObject(object != null);
        l.setHeadline(headline);
        if (linkObjectId < 0) {
            linkObjectId = 0;
        }
        l.setLinkObjectId(linkObjectId);
        l.setMethod(method);
        l.setTime(new Date());
        l.setType(type);
        l.setUser(u);
        l.setUserObject(object);
        l.setTime(timestamp);
        em.persist(l);
        em.flush();
    }

    @Override
    public void log(mlsClient dataPool, MlsEventType.EventType eventType, mlsObject object, int linkObjectId, String headline, String method, mlsLog.Type type) {
        log(dataPool, eventType, object, linkObjectId, headline, method, type, new Date());
    }

    @Override
    public void remoteLog(int dataPoolId, MlsEventType.EventType eventType, int objectId, int linkObjectId, String headline, mlsLog.Type type, Date timestamp) {
        mlsClient c = em.find(mlsClient.class, dataPoolId);
        mlsObject o = em.find(mlsObject.class, objectId);
        if (c != null && o != null) {
            // because we don't have the method parameter we pass the eventType which is more precise
            log(c, eventType, o, linkObjectId, headline, eventType.name(), type, timestamp);
        }
    }

}
