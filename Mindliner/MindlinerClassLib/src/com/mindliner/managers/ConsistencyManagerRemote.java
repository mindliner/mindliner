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
public interface ConsistencyManagerRemote {

    /**
     * Veryfies all objects belonging to the specified client.
     * Verification includes analysis whether relatives and client-specific attributes 
     * (e.g. confidentiality and soon categories) also belong to the same client as the object.
     *
     * @param clientId The id of the client to be verified.
     */
    public void checkClientIntegrity(int clientId);

}
