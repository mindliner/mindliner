/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.web.backbeans;

import com.mindliner.entities.SoftwareFeature;
import com.mindliner.entities.mlsUser;
import com.mindliner.exceptions.NonExistingObjectException;
import com.mindliner.managers.UserManagerLocal;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class describes a feature for a particular user.
 *
 * @author Marius Messerli
 */
public class UserFeature {

    private SoftwareFeature feature;
    private mlsUser user;
    private boolean active;
    private final UserManagerLocal userManager;

    public UserFeature(SoftwareFeature feature, mlsUser user, boolean active, UserManagerLocal userManager) {
        this.feature = feature;
        this.user = user;
        this.active = active;
        this.userManager = userManager;
    }

    public SoftwareFeature getFeature() {
        return feature;
    }

    public void setFeature(SoftwareFeature feature) {
        this.feature = feature;
    }

    public mlsUser getUser() {
        return user;
    }

    public void setUser(mlsUser user) {
        this.user = user;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        try {
            this.active = active;
            if (active) {
                if (!user.getSoftwareFeatures().contains(feature)) {
                    // update the database
                    userManager.addFeatureAuthorization(user.getId(), feature.getId());
                    // update the local copy without re-loading
                    user.getSoftwareFeatures().add(feature);
                }
            } else {
                if (user.getSoftwareFeatures().contains(feature)) {
                    // update the database
                    userManager.removeFeatureAuthorization(user.getId(), feature.getId());
                    // update the local copy without re-loading
                    user.getSoftwareFeatures().remove(feature);
                }
            }
        } catch (NonExistingObjectException ex) {
            Logger.getLogger(UserFeature.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
