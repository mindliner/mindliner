/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.dispatch;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.view.dispatch.MlObjectViewer.ViewType;
import java.util.List;

/**
 * Defines the interface to route ML objects to all interested and capable displayers.
 * 
 * @author Marius Messerli
 */
public interface MlViewDispatcher {
    

   /**
    * Displays a single object
    * @param object The object to be displayed
    * @param type The type of view required
    */
    public void display(mlcObject object, ViewType type);
    
    /**
     * Displays a list of objects
     * @param objects The objects to be displayed
     * @param type The type of view required; necessary for viewers that support multiple types of views
     */
    public void display(List<mlcObject> objects, ViewType type);
        
    public void registerViewer(MlObjectViewer viewer);
    
    public void unregisterViewer(MlObjectViewer viewer);
    
    /**
     * Returns to the previous view, if any
     */
    public void back();
}
