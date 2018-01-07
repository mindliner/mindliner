/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.entities.Release;
import javax.ejb.Local;

/**
 * Class handles the release table.
 * @author Marius Messerli
 */
@Local
public interface ReleaseManagerLocal {
    
    /**
     * Obtain the release object for the current (server side) release.
     * @return 
     */
    public Release getCurrentServerRelease();
    /**
     * This call updates the mutable release fields, i.e. those that may need to be updated after the initial version deployment.
     * @param releaseId The id
     * @param releaseNoteURL The URL where the release notes can be found
     * @param distributionURL The URL where the desktop app can be downloaded
     * @param highestCompatibleClientVersion The version number of the most recent client build that is still compatible this this server version
     */
    public void updateReleaseDetails(int releaseId, String releaseNoteURL, String distributionURL, int highestCompatibleClientVersion);
}
