/*
 * The PathPositioner arranges nodes along the shortest path between two
 * Mindliner nodes with the rest of the information arranged to the left
 * and right of the shortest path nodes.
 */
package com.mindliner.view.positioner;

import com.mindliner.clientobjects.MlMapNode;
import com.mindliner.view.MlNodePath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import javax.swing.plaf.metal.MetalIconFactory;

/**
 *
 * @author Marius Messerli
 */
public class PathPositioner extends ObjectPositioner {
    private final int verticalSpace = 5;

    @Override
    public double arrangePositions() {
        double pathHeight = getNodes().isEmpty() ? 0 : (getNodes().size() - 1) * verticalSpace;
        for (MlMapNode n : getNodes()){
            pathHeight += n.getSize().height;
        }
        
        // second pass to assign positions
        double currentY = pathHeight / 2.0;
        for (int i = 0; i < getNodes().size(); i++){
            MlMapNode n = getNodes().get(i);
            n.setPosition(new Point2D.Double(0, currentY));
            
            
        }
        return 0.0;
    }

    @Override
    public double arrangeNodeFamilyPosition(MlMapNode branchHeadNode, boolean left) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
