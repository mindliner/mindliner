/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import javax.ejb.Local;

/**
 *
 * @author Marius Messerli
 */
@Local
public interface EnvironmentManagerLocal {

    /**
     * Tells whether this is considered a company in-house server or a shared
     * outsourced public server.
     * @return 
     */
    boolean isServerInhouse();
    
}
