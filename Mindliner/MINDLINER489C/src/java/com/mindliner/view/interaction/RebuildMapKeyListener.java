/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.interaction;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.events.MlEventLogger;
import com.mindliner.events.SelectionManager;
import com.mindliner.view.dispatch.MlObjectViewer;
import com.mindliner.view.dispatch.MlViewDispatcherImpl;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 *
 * @author Marius Messerli
 */
public class RebuildMapKeyListener extends KeyAdapter {
    
    @Override
    public void keyReleased(KeyEvent e) {
        
        switch (e.getKeyCode()) {

            // build a new map with the current selection as the new root
            case KeyEvent.VK_V:
                if (!e.isControlDown()) {
                    // if the user makes the node the new root this is equivalent to reading the object
                    MlEventLogger.logReadEvent(SelectionManager.getLastSelection());
                    MlViewDispatcherImpl.getInstance().display(SelectionManager.getSelection(), MlObjectViewer.ViewType.Map);
                }
                break;

            // build a new map with the rating peak as the new root
            case KeyEvent.VK_R:
                mlcObject selection = SelectionManager.getLastSelection();
                if (selection != null) {
                    mlcObject ratingPeak = CacheEngineStatic.getFamilyRatingPeak(selection);
                    if (ratingPeak != null) {
                        MlViewDispatcherImpl.getInstance().display(ratingPeak, MlObjectViewer.ViewType.Map);
                    }
                }
                break;
            
        }
    }
}
