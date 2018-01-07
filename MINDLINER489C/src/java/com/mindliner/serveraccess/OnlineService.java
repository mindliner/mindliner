/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.serveraccess;

import com.mindliner.cache.MlCacheException;

/**
 * This interface must be implemented by all services that require an online
 * connection to the server.
 *
 * @author marius
 */
public interface OnlineService {
    
    public static final int LOW_PRIORITY = 10000;
    public static final int MEDIUM_PRIORITY = 1000;
    public static final int HIGH_PRIORITY = 10;

    public enum OnlineStatus {

        offline,
        goingOnline,
        goingOffline,
        online
    }

    public OnlineStatus getStatus();

    public void goOffline();

    public void goOnline() throws MlCacheException;

    public String getServiceName();

    /**
     * A lower number means a higher priority. The connection priority defines
     * in which sequence the services will go online.
     *
     * @return The priority of this service.
     */
    public int getConnectionPriority();

    public void setConnectionPriority(int priority);
}
