package com.mindliner.managers;

import com.mindliner.entities.Release;
import javax.ejb.Remote;

/**
 *
 * @author Marius Messerli
 */
@Remote
public interface ReleaseManagerRemote {

    /**
     * Checks the release table and checks if the current release is recorded.
     * If so it returns without any changes. If not it create a new record in
     * the release table with key fields set according to the running release.
     */
    public void verifyAndComplementReleaseTable();

    /**
     * Retuns a copy of the description of the current release.
     *
     * @return The release that corresponds to the server.
     */
    public Release getCurrentServerRelease();
}
