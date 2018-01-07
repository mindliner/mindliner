/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.synch;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marius Messerli
 */
public class SynchSettingChangeManager {

    public static List<SynchSettingChangeObserver> observers = new ArrayList<>();

    public static void sourcePathChanged(String path){
        for (SynchSettingChangeObserver  o : observers){
            o.sourcePathChanged(path);
        }
    }
    
    public static void registerObserver(SynchSettingChangeObserver o) {
        if (!observers.contains(o)) {
            observers.add(o);
        }
    }
    
    public static void unregisterObserver(SynchSettingChangeObserver o){
        observers.remove(o);
    }
}
