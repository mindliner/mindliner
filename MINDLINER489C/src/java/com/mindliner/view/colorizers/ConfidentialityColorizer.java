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
 * @author Marius Messerli
 */
public class ConfidentialityColorizer extends NodeColorizerBase{

    @Override
    public void assignNodeColors(List<MlMapNode> nodeList) {
        BaseColorizer colorizer = ColorManager.getColorizerForType(ColorDriverAttribute.Confidentiality);
        for (MlMapNode n : nodeList) {
            Color c = colorizer.getColorForObject(n.getObject());
            n.setColor(c);
            assignNodeColors(n.getChildren());
        }
        NodeColorizerBase.assignConnectionColors(nodeList);
    }
    
    
}
