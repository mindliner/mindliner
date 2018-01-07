/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.events;

import com.mindliner.clientobjects.MlcLink;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.view.connectors.NodeConnection;
import java.util.List;

/**
 *
 * @author Marius Messerli
 */
public interface SelectionObserver {

    /**
     * Tells the observer that the current object has changed.
     * @param selection The list of objects that are currently selected.
     */
    public void selectionChanged(List<mlcObject> selection);

    public void clearSelections();
    
    public void connectionSelectionChanged(List<NodeConnection> selection);
    
    public void clearConnectionSelections();
}
