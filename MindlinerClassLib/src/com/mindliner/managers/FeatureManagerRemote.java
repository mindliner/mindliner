/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.entities.SoftwareFeature;
import java.util.Collection;
import javax.ejb.Remote;

/**
 *
 * @author Marius Messerli
 */
@Remote
public interface FeatureManagerRemote {

    Collection<SoftwareFeature> getRequiredSoftwareFeatures();
    
}
