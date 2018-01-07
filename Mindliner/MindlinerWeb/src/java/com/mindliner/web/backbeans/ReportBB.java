/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.web.backbeans;

import com.mindliner.cal.ReportingPeriod.Period;
import com.mindliner.entities.mlsUser;
import com.mindliner.managers.IslandManagerRemote;
import com.mindliner.managers.ReportCacheBean;
import com.mindliner.managers.ReportManagerRemote;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
 * This class delivers data to generate visual reports on the state of the data
 * pool and its users.
 *
 * @author Marius Messerli
 */
@ManagedBean
@ViewScoped
public class ReportBB implements Serializable {

    private int count = 0;

    @EJB
    private ReportManagerRemote reportManager;
    @EJB
    private IslandManagerRemote islandManager;
    @EJB
    private ReportCacheBean reportCache;

    private String userActivity = null;
    // the period attribute that is set by the jsf page
    private Period period = Period.LastMonth;
    // the period that is used in the current reports
    private Period reportPeriod = period;
    // the user links in JSON format
    // in contrast to interlinkReport, the userLinks contain the target name and date of each link (not only the link count)
    private String userLinks = null;

    private int selectedUser;
    private int reportUser;
    private List<mlsUser> users = null;

    @PostConstruct
    public void init() {
        users = reportManager.getVisibleUsers();
        // might be an issue when the account is freshly created
        if(users.size() > 0) {
            selectedUser = users.get(0).getId();
            reportUser = selectedUser;
        }
    }

    public void clearReport() {
        userActivity = null;
    }

    public int getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(int selectedUser) {
        this.selectedUser = selectedUser;
    }

    public void createIslands() {
        islandManager.initializeIslands(19);
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public String getUserLinksJson(String varName) {
        if (userLinks == null || !reportPeriod.equals(period)) {
            reportPeriod = period;
            userLinks = reportManager.getUserLinksReport(this.period);
        }
        String res = varName + " = '" + userLinks + "';";
        return res;
    }

    public String getUserActivityJson(String varName) {
        if (userActivity == null || reportUser != selectedUser) {
            reportUser = selectedUser;
            userActivity = reportManager.getUserActivityReport(selectedUser, period);
        }
        String res = varName + " = '" + userActivity + "';";
        return res;
    }

    public List<Period> getReportingPeriods() {
        return Arrays.asList(Period.values());
    }

    /**
     * Returns the users with whom the caller shares at least one data pool
     *
     * @return The list of users
     */
    public List<mlsUser> getUsers() {
        return users;
    }

    public int getTotalObjectCount() {
        return reportCache.getTotalObjectCount();
    }

    public int getTotalLinkCount() {
        return reportCache.getTotalLinkCount();
    }

    public int getLoggedInUserCount() {
        return reportCache.getOnlineUsers();
    }

    public Date getLastObjectModificationTime() {
        return reportCache.getLastObjectModification();
    }

}
