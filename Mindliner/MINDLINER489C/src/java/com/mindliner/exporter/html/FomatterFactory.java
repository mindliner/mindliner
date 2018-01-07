/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.exporter.html;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcTask;

/**
 *
 * @author Marius Messerli
 * Created on 26.08.2012, 11:14:17
 */
public class FomatterFactory {

    private static TaskFormatter taskFormatter = new TaskFormatter();
    private static NodeFormatter commonFormatter = new NodeFormatter();
    
    public static NodeFormatter getFormatter(mlcObject object){
        if (object instanceof mlcTask) return taskFormatter;
        return commonFormatter;
    }
}
