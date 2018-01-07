/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.enums;

/**
 *
 * @author Dominic Plangger
 */
public enum ObjectReviewStatus {
    
    REVIEWED("Reviewed"),
    IMPORTED("Imported");
    
    private final String formattedName;
    
    ObjectReviewStatus(String formattedName) {
        this.formattedName = formattedName;
    }

    @Override
    public String toString() {
        return formattedName;
    }
    
}
