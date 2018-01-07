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
 */
package com.mindliner.view.colorizers;

import com.mindliner.entities.Colorizer.ColorDriverAttribute;
import com.mindliner.gui.color.BaseColorizer;
import com.mindliner.gui.color.ColorManager;
import java.awt.Color;
import java.util.List;
import com.mindliner.clientobjects.MlMapNode;

/**
 *
 * @author Marius Messerli Created on 19.07.2012, 08:11:50
 */
public class RatingColorizer extends NodeColorizerBase {

    @Override
    public void assignNodeColors(List<MlMapNode> nodeList) {
        BaseColorizer colorizer = ColorManager.getColorizerForType(ColorDriverAttribute.Rating);
        for (MlMapNode n : nodeList) {
            Color c = colorizer.getColorForObject(n.getObject());
            n.setColor(c);
            assignNodeColors(n.getChildren());
        }
        NodeColorizerBase.assignConnectionColors(nodeList);        
    }
}
