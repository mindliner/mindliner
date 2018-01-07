/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.entities.SoftwareFeature;
import com.mindliner.entities.SoftwareFeature.CurrentFeatures;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class FeatureManager implements FeatureManagerRemote, FeatureManagerLocal {

    @PersistenceContext
    EntityManager em;
    @EJB
    UserManagerLocal userManager;

    @Override
    public void verifyAndComplementRequiredSoftwareFeatures() {
        Query nq = em.createNamedQuery("SoftwareFeature.findAll");
        List<SoftwareFeature> persistedFeatures = nq.getResultList();
        for (CurrentFeatures cf : CurrentFeatures.values()) {
            boolean existing = false;
            for (SoftwareFeature pf : persistedFeatures) {
                if (cf.name().equals(pf.getName())) {
                    existing = true;
                }
            }
            if (!existing) {
                SoftwareFeature newFeature = new SoftwareFeature();
                newFeature.setName(cf.name());
                newFeature.setDescription("automatically added, please edit");
                em.persist(newFeature);
                Logger.getLogger(FeatureManager.class.getName()).log(Level.INFO, null, "Added new software feature to database: " + cf.name());
            }
        }
    }

    @Override
    public boolean checkForSuperfluousFeatures() {
        Query nq = em.createNamedQuery("SoftwareFeature.findAll");
        List<SoftwareFeature> persistedFeatures = nq.getResultList();
        boolean superfluousFeatures = false;
        for (SoftwareFeature pf : persistedFeatures) {
            CurrentFeatures fg = CurrentFeatures.valueOf(pf.getName());
            if (fg == null) {
                superfluousFeatures = true;
            }
        }
        return superfluousFeatures;
    }

    @Override
    @RolesAllowed(value = {"Admin", "User", "MasterAdmin"})
    public Collection<SoftwareFeature> getRequiredSoftwareFeatures() {
        Query nq = em.createNamedQuery("SoftwareFeature.findAll");
        List<SoftwareFeature> persistedFeatures = nq.getResultList();
        List<SoftwareFeature> requiredFeatures = new ArrayList<>();
        for (SoftwareFeature sf : persistedFeatures) {
            if (SoftwareFeature.isStillExisting(sf.getName())) {
                requiredFeatures.add(sf);
            }
        }
        return requiredFeatures;
    }

}
