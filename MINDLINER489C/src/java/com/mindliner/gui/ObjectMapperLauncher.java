/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.image.IconLoader;
import com.mindliner.view.dispatch.MlObjectViewer;
import com.mindliner.view.dispatch.MlViewDispatcherImpl;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JOptionPane;

/**
 * A central place to initiate mapping of one or several objects
 *
 * @author Marius Messerli
 */
public class ObjectMapperLauncher {

    /**
     * Ensures icons are loaded (asynchronously) for all objects and then
     * initiate the mapping.
     * 
     * @param objects 
     */
    public static void map(List<mlcObject> objects) {
        if (objects == null) {
            JOptionPane.showMessageDialog(null,
                    "Please select one or multiple rows as new map roots.", "Map Creation", JOptionPane.ERROR_MESSAGE);
        } else {
            Set<mlcObject> objWithoutIcns = new HashSet<>();
            for (mlcObject o : objects) {
                if (o.getIcons() == null) {
                    objWithoutIcns.add(o);
                }
            }
            // Loads icon asynchronously, returns immediately
            IconLoader.getInstance().loadIcons(objWithoutIcns);
            // display selected objects
            MlViewDispatcherImpl.getInstance().display(objects, MlObjectViewer.ViewType.Map);
        }

    }
    
    /**
     * Maps the specified object after ensuring that associated icons are loaded.
     * 
     * @param object 
     */
    public static void map(mlcObject object){
        List<mlcObject> objects = new ArrayList<>();
        objects.add(object);
        map(objects);
    }
}
