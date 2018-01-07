/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.prefs;

import com.mindliner.categories.mlsMindlinerCategory;


/**
 *
 * @author messerli
 */
public class CurrentlyEditedCategory {

    public CurrentlyEditedCategory(Class c, int mode){
        editingMode = mode;
        objectClass = c;
    }
    
    public CurrentlyEditedCategory(Class c, int mode, mlsMindlinerCategory mc){
        this(c, mode);
        editedObject = mc;
    }
    
    public Class getObjectClass(){
        return objectClass;
    }
    
    public int getEditingMode(){
        return editingMode;
    }
    
    public mlsMindlinerCategory getEditedObject(){
        return editedObject;
    }
    
    public static final int ADD_NEW = 0;
    public static final int RENAME = 1;
    
    int editingMode;
    Class objectClass = null;
    mlsMindlinerCategory editedObject = null;
}
