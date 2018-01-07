/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.cache;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.events.ObjectChangeObserver;
import com.mindliner.main.MindlinerMain;
import javax.swing.JOptionPane;

/**
 * This class updates the cached links if any link changes happen.
 *
 * @author Marius Messerli
 */
public class LinksChangeObserver implements ObjectChangeObserver {

    private ObjectCache cache;

    public LinksChangeObserver(ObjectCache objectCache) {
        cache = objectCache;
    }

    @Override
    public void objectChanged(mlcObject o) {
        try {
            cache.updateLinks(o.getId(), true);
        } catch (MlCacheException ex) {
            JOptionPane.showMessageDialog(MindlinerMain.getInstance(), ex.getMessage(), "Failed to update link sets", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void objectDeleted(mlcObject o) {
        // do nothing, the linked objects will get an objectChanged message and do their work separately
    }

    @Override
    public void objectCreated(mlcObject o) {
        // do nothing, objectChanged calls will be sent for this guy
    }

    @Override
    public void objectReplaced(int oldId, mlcObject o) {
        try {
            // load links of new object into cache
            cache.updateLinks(o.getId(), true);
            // updates the cached links of the old object with the new object
            cache.replaceObject(oldId, o);
        } catch (MlCacheException ex) {
            JOptionPane.showMessageDialog(MindlinerMain.getInstance(), ex.getMessage(), "Failed to update link sets", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
}
