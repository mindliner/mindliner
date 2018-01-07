/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.entities.SoftwareFeature;
import java.util.Collection;
import javax.ejb.Local;

/**
 * Manages Mindliner's configurable features.
 *
 * @author Marius Messerli
 */
@Local
public interface FeatureManagerLocal {

    /**
     * This call ensures that all the available features of this version are
     * also registered in the database. It add missing features to the database.
     */
    void verifyAndComplementRequiredSoftwareFeatures();

    /**
     * This call checks if there are any software features listed in the
     * database which are no longer existing in the current version of the
     * software.
     *
     * @return True if no extra features are found (good case), false if there
     * are features in the database which are no longer existing in the current
     * version (bad case)
     */
    boolean checkForSuperfluousFeatures();

    Collection<SoftwareFeature> getRequiredSoftwareFeatures();

}
