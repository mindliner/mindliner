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

import java.awt.Color;
import java.util.List;
import com.mindliner.clientobjects.MlMapNode;

/**
 *
 * @author marius
 */
public class SpiralColorizer extends NodeColorizerBase {

    @Override
    public void assignNodeColors(List<MlMapNode> nodeList){
        double maxX = 0;
        double maxY = 0;
        double min;
        for (MlMapNode n : nodeList){
            maxX = Math.max(maxX, n.getPosition().getX());
            maxY = Math.max(maxY, n.getPosition().getY());
        }

        min = Math.min(maxX, maxY);

        for (MlMapNode n : nodeList){

            double radialDistance = Math.sqrt((double) n.getPosition().getX() * (double) n.getPosition().getX() + (double) n.getPosition().getY() * (double) n.getPosition().getY());
            float alpha = 0;

            if (radialDistance > 0)
            {
                alpha = (float) (0.2 + ((radialDistance / min) * 0.8));
                alpha = Math.max((float) 0.2, alpha);
                alpha = Math.min(1, alpha);
            }
            else alpha = (float) 0.2;

            Color randomColor = getRandomColor();
            float red = randomColor.getRed();
            float green = randomColor.getGreen();
            float blue = randomColor.getBlue();

            red = red / 255;
            green = green / 255;
            blue = blue / 255;

            Color fg2 = new Color(red, green, blue, alpha);
            n.setColor(fg2);
        }
    }

}
