/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.mindliner.events;

import com.mindliner.clientobjects.mlcObject;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is the central place in the mindlner client where notifications
 * due to object changes are handled.
 *
 * @author Marius Messerli
 */
public class ObjectChangeManager {

    private static final List<ObjectChangeObserver> observers = new ArrayList<>();

    public static void registerObserver(ObjectChangeObserver observer) {
        if (observers.contains(observer) == false) {
            observers.add(observer);
        }
    }

    public static void objectChanged(mlcObject object) {
        List<ObjectChangeObserver> currentObservers = new ArrayList<>(observers);
        for (ObjectChangeObserver observer : currentObservers) {
            observer.objectChanged(object);
        }
    }

    public static void objectDeleted(mlcObject object) {
        for (ObjectChangeObserver observer : observers) {
            observer.objectDeleted(object);
        }
    }

    public static void objectReplaced(int oldId, mlcObject object) {
        List<ObjectChangeObserver> currentObservers = new ArrayList<>(observers);
        for (ObjectChangeObserver observer : currentObservers) {
            observer.objectReplaced(oldId, object);
        }
    }

    public static void objectsDeleted(List<mlcObject> objects) {
        List<ObjectChangeObserver> currentObservers = new ArrayList<>(observers);
        for (ObjectChangeObserver observer : currentObservers) {
            for (mlcObject object : objects) {
                observer.objectDeleted(object);
            }
        }
    }

    public static void objectCreated(mlcObject object) {
        // copy list as it may change as a result of the new object
        List<ObjectChangeObserver> currentObservers = new ArrayList<>(observers);

        for (ObjectChangeObserver observer : currentObservers) {
            observer.objectCreated(object);
        }
    }

}
