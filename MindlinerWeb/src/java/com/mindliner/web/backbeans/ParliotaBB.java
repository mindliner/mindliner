/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.web.backbeans;

import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.entities.mlsClient;
import com.mindliner.entities.mlsObject;
import com.mindliner.managers.ParliotaLoaderLocal;
import com.mindliner.managers.SecurityManagerRemote;
import com.mindliner.managers.UserManagerLocal;
import com.mindliner.parliota.objects.ParMeeting;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;

/**
 * This back bean is supporting the parliota import page.
 *
 * @author Marius Messerli (marius@mindliner.com)
 */
@ManagedBean(name = "parliota")
@ViewScoped
public class ParliotaBB {

    private String seed;
    private ParMeeting meeting;

    // default attributes for the new objects
    private mlsClient datapool;

    // this map hold the allowed (i.e. all up to her/his level) confidentialities for the current user
    private Map<mlsClient, List<mlsConfidentiality>> allowedConfidentialitiesCache = new HashMap<>();
    private mlsConfidentiality confidentiality;

    private boolean privateAccess = false;
    private mlsObject relative;

    // we need to map parliota people (authors, participants) to Mindliner contacts
    // make sure peopleName.get(n) corresponds to contactIDs.get(n)
    private final Map<Integer, String> peopleName = new HashMap<>();
    private final Map<Integer, Integer> contactIDs = new HashMap<>();

    @EJB
    ParliotaLoaderLocal parliotaLoader;
    @EJB
    UserManagerLocal userManager;
    @EJB
    SecurityManagerRemote securityManager;

    /**
     * Creates a new instance of ParliotaBB
     */
    public ParliotaBB() {
    }

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        this.seed = seed;
    }

    public void readMeeting() {
        if (!seed.isEmpty()) {
            meeting = parliotaLoader.load(seed, datapool, confidentiality, relative);
        }
    }

    public String getTitle() {
        return meeting.getHeadline();
    }

    public String getDescription() {
        return meeting.getDescription();
    }

    public ParMeeting getMeeting() {
        return meeting;
    }

    public void setMeeting(ParMeeting meeting) {
        this.meeting = meeting;
    }

    public mlsClient getDatapool() {
        return datapool;
    }

    public void setDatapool(mlsClient datapool) {
        this.datapool = datapool;
    }

    public mlsConfidentiality getConfidentiality() {
        return confidentiality;
    }

    public void setConfidentiality(mlsConfidentiality confidentiality) {
        this.confidentiality = confidentiality;
    }

    public boolean isPrivateAccess() {
        return privateAccess;
    }

    public void setPrivateAccess(boolean privateAccess) {
        this.privateAccess = privateAccess;
    }

    public ParliotaLoaderLocal getParliotaLoader() {
        return parliotaLoader;
    }

    public void setParliotaLoader(ParliotaLoaderLocal parliotaLoader) {
        this.parliotaLoader = parliotaLoader;
    }

    public mlsObject getRelative() {
        return relative;
    }

    public void setRelative(mlsObject relative) {
        this.relative = relative;
    }

    public List<mlsClient> getDataPools() {
        return userManager.getCurrentUser().getClients();
    }

    // Use datapool in object creation and object.getClient() in object editor
    public List<mlsConfidentiality> getConfidentialities() {
        if (datapool == null){
            FacesContext fc = FacesContext.getCurrentInstance();
            Flash flash = fc.getExternalContext().getFlash();
            flash.put("Configuration Error", "Datapool not initialized");
        }
        if ((allowedConfidentialitiesCache.get(datapool)) == null) {
            mlsConfidentiality maxConf = userManager.getCurrentUser().getMaxConfidentiality(datapool);
            if (maxConf != null) {
                allowedConfidentialitiesCache.put(datapool,
                        securityManager.getAllowedConfidentialities(datapool.getId(), maxConf.getClevel()));
            }
        }
        return allowedConfidentialitiesCache.get(datapool);
    }
}
