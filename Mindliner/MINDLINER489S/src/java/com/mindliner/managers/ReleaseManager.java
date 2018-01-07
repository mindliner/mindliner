package com.mindliner.managers;

import com.mindliner.entities.Release;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * This bean manages the integrity of Mindliner releases, lets the client
 * check if it is compatible with the current server release and shows
 * users where to find information about the current release.
 *
 * @author Marius Messerli
 */
@Stateless
public class ReleaseManager implements ReleaseManagerRemote, ReleaseManagerLocal {

    @PersistenceContext
    EntityManager em;
    @EJB
    UserManagerLocal userManager;

    @Override
    public void verifyAndComplementReleaseTable() {
        System.out.println("verifying release records, current release number is " + Release.VERSION_NUMBER);
        Query nq = em.createNamedQuery("Release.findByVersionNumber");
        nq.setParameter("versionNumber", Release.VERSION_NUMBER);

        try {
            nq.getSingleResult();
        } catch (NoResultException ex) {
            Release r = new Release();
            r.setVersionNumber(Release.VERSION_NUMBER);
            r.setVersionString(Release.VERSION_STRING);
            r.setOldestDesktopVersion(Release.OLDEST_DESKTOP_VERSION);
            /**
             * latest desktop version is always the version number of the
             * current version at deploy time and may be changed later though
             * the admin web portal
             */
            r.setLatestDesktopVersion(Release.VERSION_NUMBER);
            r.setReleaseDate(Release.RELEASE_DATE);
            em.persist(r);
            em.flush();
            Logger.getLogger(ReleaseManager.class.getName()).log(Level.INFO, null, "Added new release to database: " + Release.VERSION_STRING);
        }
    }

    @Override
    public Release getCurrentServerRelease() {
        Query nq = em.createNamedQuery("Release.findByVersionNumber");
        nq.setParameter("versionNumber", Release.VERSION_NUMBER);
        return (Release) nq.getSingleResult();
    }

    @Override
    public void updateReleaseDetails(int releaseId, String releaseNoteURL, String distributionUrl, int highestCompatibleClientVersion) {
        Release r = em.find(Release.class, releaseId);
        if (r != null) {
            r.setReleaseNotesUrl(releaseNoteURL);
            r.setLatestDesktopVersion(highestCompatibleClientVersion);
            r.setDistributionUrl(distributionUrl);
        }
    }

}
