/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.view.colorizers;

import com.mindliner.entities.Colorizer;
import com.mindliner.gui.color.BaseColorizer;
import com.mindliner.gui.color.ColorManager;
import java.util.List;
import com.mindliner.clientobjects.MlMapNode;

/**
 * Colorizes the nodes based on the data pool of the underlying object.
 * @author Marius Messerli
 */
public class NodeDataPoolColorizer extends NodeColorizerBase {

    @Override
    public void assignNodeColors(List<MlMapNode> nodeList) {
        BaseColorizer colorizer = ColorManager.getColorizerForType(Colorizer.ColorDriverAttribute.DataPool);
        for (MlMapNode n : nodeList) {
            n.setColor(colorizer.getColorForObject(n.getObject()));
            assignConnectionColors(n.getChildConnections(), colorizer);
            assignNodeColors(n.getChildren());
        }
    }
    
}
