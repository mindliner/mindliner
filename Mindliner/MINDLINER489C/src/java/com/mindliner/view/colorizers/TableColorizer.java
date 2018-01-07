/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.colorizers;

import com.mindliner.gui.color.ColorManager;
import java.awt.Color;
import java.util.List;
import com.mindliner.clientobjects.MlMapNode;

/**
 * This colorizer usese the color scheme defined in the SearchTables and applies
 * it to the object on the map.
 *
 * @author Marius Messerli
 */
public class TableColorizer extends NodeColorizerBase {

    @Override
    public void assignNodeColors(List<MlMapNode> nodeList) {
        for (MlMapNode n : nodeList) {
            assignNodeColors(n);
        }
        NodeColorizerBase.assignConnectionColors(nodeList);
    }

    private void assignNodeColors(MlMapNode node) {
        Color color = ColorManager.getMostSpecificColor(node.getObject());
        node.setColor(color);
        for (MlMapNode n : node.getChildren()) {
            assignNodeColors(n);
        }
    }
}
