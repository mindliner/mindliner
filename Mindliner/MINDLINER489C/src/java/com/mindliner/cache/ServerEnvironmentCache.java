/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.cache;

import com.mindliner.managers.EnvironmentManagerRemote;
import com.mindliner.serveraccess.RemoteLookupAgent;
import javax.naming.NamingException;

/**
 * Describes the server environment.
 *
 * @author Marius Messerli
 */
public class ServerEnvironmentCache {

    private static final boolean inhouseInitialized = false;
    private static boolean inhouse = false;

    public static boolean isInhouse() throws MlCacheException {
        if (inhouseInitialized) {
            return inhouse;
        }
        try {
            EnvironmentManagerRemote er = (EnvironmentManagerRemote) RemoteLookupAgent.getManagerForClass(EnvironmentManagerRemote.class);
            inhouse = er.isServerInhouse();
            return inhouse;
        } catch (NamingException ex) {
            throw new MlCacheException(ex.getMessage());
        }
    }

}
