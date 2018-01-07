/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.cache;

/**
 *
 * @author M.Messerli
 */
public class OfflineException extends RuntimeException {

    @Override
    public String getMessage() {
        return "The application is offline and trying to perform an online action.";
    }

    
}
