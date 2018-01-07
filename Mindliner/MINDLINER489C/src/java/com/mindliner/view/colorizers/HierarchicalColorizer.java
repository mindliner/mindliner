package com.mindliner.view.colorizers;

import com.mindliner.entities.Colorizer;
import com.mindliner.gui.color.ColorManager;
import com.mindliner.gui.color.FixedKeyColorizer;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import com.mindliner.clientobjects.MlMapNode;

/**
 * This class clorizes the map nodes according to their branching depth level.
 *
 * @author Marius Messerli
 */
public class HierarchicalColorizer extends NodeColorizerBase {

    protected List<Color> levelColors = null;

    public HierarchicalColorizer() {
        FixedKeyColorizer fkc = (FixedKeyColorizer) ColorManager.getColorizerForType(Colorizer.ColorDriverAttribute.FixedKey);
        levelColors = new ArrayList<>();
        levelColors.add(fkc.getColorForObject(FixedKeyColorizer.FixedKeys.MAP_LEVEL_ZERO));
        levelColors.add(fkc.getColorForObject(FixedKeyColorizer.FixedKeys.MAP_LEVEL_ONE));
        levelColors.add(fkc.getColorForObject(FixedKeyColorizer.FixedKeys.MAP_LEVEL_TWO));
        levelColors.add(fkc.getColorForObject(FixedKeyColorizer.FixedKeys.MAP_LEVEL_THREE));
        levelColors.add(fkc.getColorForObject(FixedKeyColorizer.FixedKeys.MAP_LEVEL_FOUR));        
    }

    @Override
    public void assignNodeColors(List<MlMapNode> nodeList) {
        for (MlMapNode root : nodeList) {
            Color c = levelColors.isEmpty() ? Color.GRAY : levelColors.get(0);
            root.setColor(c);
            assignChildColors(root, 1);
        }
        NodeColorizerBase.assignConnectionColors(nodeList);
    }

    private void assignChildColors(MlMapNode parent, int level) {
        Color c = level < levelColors.size() ? levelColors.get(level) : Color.GRAY;
        for (MlMapNode n : parent.getChildren()) {
            n.setColor(c);
            assignChildColors(n, level + 1);
        }
    }

    public void setLevelColors(List<Color> colors) {
        this.levelColors = colors;
    }

}
