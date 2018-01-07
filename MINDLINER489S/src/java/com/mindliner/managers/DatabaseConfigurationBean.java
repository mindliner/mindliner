package com.mindliner.managers;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * This bean performs startup tasks that ensure that the database is updated
 * with current application data. It is carried out at application startup.
 *
 * @author Marius Messerli
 */
@Startup
@Singleton
public class DatabaseConfigurationBean {

    @PersistenceContext
    EntityManager em;

    @EJB
    FeatureManagerLocal featureManager;

    @EJB
    ReleaseManagerRemote releaseManager;
    
    @EJB
    MarketManagerLocal marketManager;

    @PostConstruct
    public void startup() {
        featureManager.verifyAndComplementRequiredSoftwareFeatures();
        releaseManager.verifyAndComplementReleaseTable();
    }
}
