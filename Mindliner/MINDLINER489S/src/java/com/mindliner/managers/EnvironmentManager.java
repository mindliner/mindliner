/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.entities.SysEnvironment;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * This class provides information about the application server.
 * @author Marius Messerli
 */
@Stateless
@DeclareRoles(value = {"Admin", "User", "MasterAdmin"})
public class EnvironmentManager implements EnvironmentManagerRemote, EnvironmentManagerLocal {

    @PersistenceContext
    EntityManager em;
    
    @Override
    @RolesAllowed(value = {"User", "Admin", "MasterAdmin"})
    public boolean isServerInhouse() {
        SysEnvironment env = em.find(SysEnvironment.class, SysEnvironment.EnvironmentKeys.LOCATION_INHOUSE.name());
        if (env == null) {
            System.err.println("Non-critical server mis-configuration detected. Please contact support team. Value missing in environment table for key " + SysEnvironment.EnvironmentKeys.LOCATION_INHOUSE.name());
            return false;
        }
        String lower = env.getValue().toLowerCase();
        if (lower.equals("true") || lower.equals("1")) return true;
        return false;
    }

}
