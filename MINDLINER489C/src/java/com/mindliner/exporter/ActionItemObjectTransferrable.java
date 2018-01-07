/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.exporter;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.mlcNews;
import com.mindliner.clientobjects.mlcObject;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * This class transfer action items.
 *
 * @author Marius Messerli
 */
public class ActionItemObjectTransferrable extends MindlinerObjectTransferable {

    /**
     * For Action items the unerdlying Mindliner object is "exported" rather
     * than the action item itself.
     *
     * @param objects
     */
    public ActionItemObjectTransferrable(List<mlcObject> objects) {
        super(objects); // just to comply with the constructor, these objects are deleted again just now
        List<mlcObject> userObjects = new ArrayList<>();
        for (mlcObject o : objects) {
            if (o instanceof mlcNews) {
                mlcNews ai = (mlcNews) o;
                mlcObject aio = CacheEngineStatic.getObject(ai.getUserObjectId());
                if (aio != null) {
                    userObjects.add(aio);
                }
            }
        }
        objectList = userObjects;
    }

}
