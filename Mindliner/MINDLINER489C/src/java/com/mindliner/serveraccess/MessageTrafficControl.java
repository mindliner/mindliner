/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.serveraccess;

/**
 * Controls which messages are being processed
 * @author Marius Messerli
 */
public interface MessageTrafficControl {
    
    public void setIgnoreAllTraffic(boolean status);
    
}
