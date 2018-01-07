/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import javax.ejb.Remote;

/**
 *
 * @author Marius Messerli
 */
@Remote
public interface EnvironmentManagerRemote {
    
    /**
     * Indicates whether this server is considered a company in-house
     * server or a shared outsourced server.
     * @return 
     */
    boolean isServerInhouse();
    
}
