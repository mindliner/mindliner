/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.colorizers;

import com.mindliner.clientobjects.MlMapNode;
import com.mindliner.entities.Colorizer;
import com.mindliner.gui.color.ColorManager;
import com.mindliner.gui.color.FixedKeyColorizer;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * This class colorizes
 *
 * @author Marius Messerli
 */
public class BranchColorizer extends NodeColorizerBase {

    protected List<Color> branchColors = null;

    public BranchColorizer() {
        FixedKeyColorizer fkc = (FixedKeyColorizer) ColorManager.getColorizerForType(Colorizer.ColorDriverAttribute.FixedKey);
        branchColors = new ArrayList<>();
        branchColors.add(fkc.getColorForObject(FixedKeyColorizer.FixedKeys.MAP_LEVEL_ZERO));
        branchColors.add(fkc.getColorForObject(FixedKeyColorizer.FixedKeys.MAP_LEVEL_ONE));
        branchColors.add(fkc.getColorForObject(FixedKeyColorizer.FixedKeys.MAP_LEVEL_TWO));
        branchColors.add(fkc.getColorForObject(FixedKeyColorizer.FixedKeys.MAP_LEVEL_THREE));
        branchColors.add(fkc.getColorForObject(FixedKeyColorizer.FixedKeys.MAP_LEVEL_FOUR));
    }

    @Override
    public void assignNodeColors(List<MlMapNode> nodeList) {
        for (MlMapNode root : nodeList) {

            root.setColor(branchColors.get(0));
            // the counter starts at 1 because the first color is the root's color
            for (int i = 1; i <= root.getChildren().size(); i++) {
                MlMapNode branch = root.getChildren().get(i-1);
                assignBranchColor(branch, i);
            }
        }
        NodeColorizerBase.assignConnectionColors(nodeList);
    }

    private void assignBranchColor(MlMapNode branchHead, int ordinal) {
        Color c = ordinal < branchColors.size() ? branchColors.get(ordinal) : Color.GRAY;
        branchHead.setColor(c);
        for (MlMapNode n : branchHead.getChildren()) {
            n.setColor(c);
            assignChildColors(n);
        }
    }

    private void assignChildColors(MlMapNode parent) {
        for (MlMapNode n : parent.getChildren()) {
            n.setColor(parent.getColor());
            assignChildColors(n);
        }
    }

    public void setLevelColors(List<Color> colors) {
        this.branchColors = colors;
    }
}
