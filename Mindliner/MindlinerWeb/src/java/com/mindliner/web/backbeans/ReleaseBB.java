/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.web.backbeans;

import com.mindliner.entities.Release;
import com.mindliner.managers.ReleaseManagerLocal;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
 *
 * @author Marius Messerli
 */
@ManagedBean
@ViewScoped
public class ReleaseBB {

    @EJB
    ReleaseManagerLocal releaseManager;

    private int releaseId;
    private String releaseVersionString;
    private String releaseNotesUrl;
    private String distributionUrl;
    private int latestCompatibleDesktopVersion;

    /**
     * Creates a new instance of ReleaseBB
     */
    public ReleaseBB() {
    }

    @PostConstruct
    public void initialize() {
        Release r = releaseManager.getCurrentServerRelease();
        if (r != null) {
            releaseNotesUrl = r.getReleaseNotesUrl();
            distributionUrl = r.getDistributionUrl();
            latestCompatibleDesktopVersion = r.getLatestDesktopVersion();
            releaseId = r.getId();
            releaseVersionString = r.getVersionString();
        }
    }

    public String getReleaseNotesUrl() {
        return releaseNotesUrl;
    }

    public void setReleaseNotesUrl(String releaseNotesUrl) {
        this.releaseNotesUrl = releaseNotesUrl;
    }

    public String getDistributionUrl() {
        return distributionUrl;
    }

    public void setDistributionUrl(String distributionUrl) {
        this.distributionUrl = distributionUrl;
    }

    public int getLatestCompatibleDesktopVersion() {
        return latestCompatibleDesktopVersion;
    }

    public void setLatestCompatibleDesktopVersion(int latestCompatibleDesktopVersion) {
        this.latestCompatibleDesktopVersion = latestCompatibleDesktopVersion;
    }

    public void saveChanges() {
        releaseManager.updateReleaseDetails(releaseId, releaseNotesUrl, distributionUrl, latestCompatibleDesktopVersion);
    }

    public int getReleaseId() {
        return releaseId;
    }

    public String getReleaseVersionString() {
        return releaseVersionString;
    }

}
