/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.enums;

/**
 *
 * @author Dominic Plangger
 */
public enum CMContainerStrokeStyles {
    
    SOLID("Solid"),
    DOTTED("Dotted"),
    DASHED("Dashed");
    
    @Override    
    public String toString() {
        return name; //To change body of generated methods, choose Tools | Templates.
    }
    
    private String name;
    
    CMContainerStrokeStyles(String name) {
        this.name = name;
    }
}
