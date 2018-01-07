/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 *//*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 *//*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 *//*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.mindliner.view.colorizers;

import com.mindliner.view.colorizers.NodeColorizerBase;
import com.mindliner.entities.Colorizer.ColorDriverAttribute;
import com.mindliner.gui.color.BaseColorizer;
import com.mindliner.gui.color.ColorManager;
import java.util.List;
import com.mindliner.clientobjects.MlMapNode;

/**
 *
 * @author M.Messerli
 */
public class ModificationAgeColorizer extends NodeColorizerBase {

    @Override
    public void assignNodeColors(List<MlMapNode> nodeList) {
        BaseColorizer colorizer = ColorManager.getColorizerForType(ColorDriverAttribute.ModificationAge);
        for (MlMapNode n : nodeList) {
            n.setColor(colorizer.getColorForObject(n.getObject()));
            assignConnectionColors(n.getChildConnections(), colorizer);
            assignNodeColors(n.getChildren());
        }
    }
}
