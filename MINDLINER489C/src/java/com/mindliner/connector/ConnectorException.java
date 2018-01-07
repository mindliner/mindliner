/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.connector;

/**
 *
 * @author Dominic Plangger
 */
public class ConnectorException extends Exception {
    
    boolean isAuthorisationError;

    public boolean isAuthorisationError() {
        return isAuthorisationError;
    }
    
    public ConnectorException(String msg, Exception ex, boolean authError) {
        super(msg, ex);
        isAuthorisationError = authError;
    }
    
    public ConnectorException(String msg, Exception ex) {
        super(msg, ex);
    }
    
    public ConnectorException(String msg) {
        super(msg);
    }
}
