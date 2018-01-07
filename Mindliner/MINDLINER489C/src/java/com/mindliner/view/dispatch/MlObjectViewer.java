/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.dispatch;

import com.mindliner.clientobjects.mlcObject;
import java.util.List;

/**
 * Implements ML viewers that are capable of producing a (certain type of ) view
 * of the specified objects.
 * 
 * @author Marius Messerli
 */
public interface MlObjectViewer {
    
        public static enum ViewType{
        GenericTable,
        Map,
        Spreadsheet,
        ContainerMap,
        NewsTable,
        Any
        }
    
    public void display(mlcObject object, ViewType type);
        
    public void display(List<mlcObject> objects, ViewType type);

    public boolean isSupported(ViewType type);
    
    public void back();
    
    /**
     * Turns the viewer on or off.
     * 
     * @param state 
     */
    public void setActive(boolean state);

}
