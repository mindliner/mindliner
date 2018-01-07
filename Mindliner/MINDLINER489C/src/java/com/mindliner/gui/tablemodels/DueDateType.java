/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.gui.tablemodels;

/**
 *
 * @author messerli
 */
public class DueDateType {

    public DueDateType(String n, Type t){
        name = n;
        type = t;
    }
    
    @Override
    public String toString(){ return name; }
    
    public Type getType(){ return type; }
            
    private String name = "";
    private Type type = Type.unscheduled;
            
    public static enum Type {unscheduled, scheduled}
}
