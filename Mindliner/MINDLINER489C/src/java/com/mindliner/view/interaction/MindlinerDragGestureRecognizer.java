/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.interaction;

import com.mindliner.view.MapNodeStatusManager;
import com.mindliner.view.MindlinerMapper;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import com.mindliner.clientobjects.MlMapNode;

/**
 *
 * @author Marius Messerli Created on 21.08.2012, 14:45:15
 */
public class MindlinerDragGestureRecognizer implements DragGestureListener {

    private final MindlinerMapper mapper;

    public MindlinerDragGestureRecognizer(MindlinerMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {
        MlMapNode dragNode = mapper.getNodeAtScreenLocation(dge.getDragOrigin());
        // switch to drag mode only after having clicked on a node but remain in pan mode if clicking on background
        if (dragNode != null) {

            mapper.getZoomAndPanListener().setDragging(true, dge.getDragOrigin());

            // ensure the drag node is in the current selection
            // if there are multiple objects selected and dragNode is one of them then don't alter selection
            if (!MapNodeStatusManager.isSelected(dragNode)) {
                MapNodeStatusManager.setCurrentSelection(dragNode);
            }
        }
    }
}
