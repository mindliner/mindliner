/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.entities.mlsLog;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * This singleton caches some frequently used stats about Mindliner.
 *
 * @author Marius Messerli
 */
@Singleton
@RunAs("MasterAdmin")
@RolesAllowed(value = {"Admin", "User", "MasterAdmin"})
public class ReportCacheBean {

    @PersistenceContext
    private EntityManager em;

    @EJB
    UserCacheBean userBean;

    private int totalObjectCount;
    private int totalLinkCount;
    private Date lastObjectModification;
    private int dataPoolCount;
    private int onlineUserCount;

    @PostConstruct
    public void setup() {
        update();
    }

    /**
     * Updating the stats every 10 seconds
     */
    @Schedule(second = "*/10", minute = "*", hour = "*")
    public void update() {
        Query q = em.createNamedQuery("mlsObject.getTotalObjectCount");
        Long count = (Long) q.getSingleResult();
        totalObjectCount = count.intValue();

        q = em.createNamedQuery("MlsLink.getTotalLinkCount");
        count = (Long) q.getSingleResult();
        totalLinkCount = count.intValue();

        q = em.createQuery("SELECT l from mlsLog l WHERE l.objectId > 0 ORDER by l.time DESC");
        q.setMaxResults(1);
        List<mlsLog> lastLog = q.getResultList();
        if (!lastLog.isEmpty()) {
            lastObjectModification = lastLog.get(0).getTime();
        }

        q = em.createQuery("SELECT COUNT(c) FROM mlsClient c WHERE c.active = 1");
        count = (Long) q.getSingleResult();
        dataPoolCount = count.intValue();
        onlineUserCount = userBean.getLoggedInUsers().size();
    }

    public int getTotalObjectCount() {
        return totalObjectCount;
    }

    public int getTotalLinkCount() {
        return totalLinkCount;
    }

    public Date getLastObjectModification() {
        return lastObjectModification;
    }

    public int getDataPoolCount() {
        return dataPoolCount;
    }

    public int getOnlineUsers() {
        return onlineUserCount;
    }

}
