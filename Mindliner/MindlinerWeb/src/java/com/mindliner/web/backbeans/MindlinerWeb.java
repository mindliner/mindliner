/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.web.backbeans;

import com.mindliner.entities.Release;
import com.mindliner.entities.SoftwareFeature;
import com.mindliner.entities.SoftwareFeature.CurrentFeatures;
import com.mindliner.entities.mlsClient;
import com.mindliner.entities.mlsUser;
import com.mindliner.managers.ReportManagerLocal;
import com.mindliner.managers.UserManagerLocal;
import com.mindliner.managers.UserManagerRemote;
import java.io.IOException;
import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

/**
 * This class hold application data for the Mindliner Web application.
 *
 * @author Marius Messerli
 */
@ManagedBean
@SessionScoped
public class MindlinerWeb implements Serializable {

    private static final int HEARTBEAT_TIMEOUT = 20 * 1000; // ms
    private Timer heartBeatTimer = null;

    @EJB
    private UserManagerLocal userManager;
        @EJB
    private UserManagerRemote userManagerRemote;
    @EJB
    private ReportManagerLocal reportManager;

    private String userName;

    @PostConstruct
    public void startHeartBeatTimer() {
        userName = userManager.getCurrentUser().toString();
        userManagerRemote.login();
        System.out.println("MindlinerWeb: starting hearbeat timer for " + userName);
        heartBeatTimer = new Timer("Heart Beat Timer");
        heartBeatTimer.schedule(new HeartBeatTask(), 1000, HEARTBEAT_TIMEOUT);
    }

    @PreDestroy
    public void stopTimer() {
        if (heartBeatTimer != null) {
            System.out.println("MindlinerWeb: stopping hearbeat timer for " + userName);
            heartBeatTimer.cancel();
        }
    }

    public mlsUser getCurrentUser() {
        return userManager.getCurrentUser();
    }

    public String getVersionString() {
        return Release.VERSION_STRING;
    }

    public boolean isAuthorizedForWeekPlan() {
        return userManager.isAuthorized(CurrentFeatures.TIME_MANAGEMENT);
    }

    public boolean isAuthorizedForSubscription() {
        return userManager.isAuthorized(SoftwareFeature.CurrentFeatures.SUBSCRIPTION);
    }

    public boolean isAuthorizedForConfidentiality() {
        return userManager.isAuthorized(SoftwareFeature.CurrentFeatures.CONFIDENTIALITY_LEVELS);
    }
    
    public void logout() {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.invalidateSession();
        try {
            ec.redirect("goodbye.xhtml");
        } catch (IOException ex) {
            Logger.getLogger(MindlinerWeb.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public class HeartBeatTask extends TimerTask {

        @Override
        public void run() {
            try {
                userManager.heartBeat();
            } catch (Exception ex) {
                Logger.getLogger(MindlinerWeb.class.getName()).log(Level.INFO, null, ex);
            }
        }
    }

    public int getObjectCount(mlsUser u) {
        return reportManager.getObjectCount(u.getId());
    }

    public boolean datapoolAvailable() {
        mlsUser cu = userManager.getCurrentUser();
        return cu.getClients().size() > 0;
    }
    
}
