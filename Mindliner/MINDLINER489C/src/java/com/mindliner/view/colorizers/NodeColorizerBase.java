/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


package com.mindliner.view.colorizers;

import com.mindliner.gui.color.BaseColorizer;
import com.mindliner.view.connectors.NodeConnection;
import java.awt.Color;
import java.util.List;
import com.mindliner.clientobjects.MlMapNode;

/**
 * This is the base class for all node colorizers. The system resembles a bit
 * BaseColorizer and its derived classes but in addition there are node specific
 * functions and also node colorizers that don't have an object colorizer
 * counterpart such as colorizing by map level.
 *
 * @author Marius Messerli
 */
public abstract class NodeColorizerBase {

    private Color backgroundColor = Color.BLACK;
    private double maxTransparency = 0.5;

    public enum ColorDriverType {

        Level,
        Branch,
        ModificationAge,
        Owner,
        DataPool,
        Rating,
        Confidentiality
    }

    /**
     * This call assigns the connection colors for the entire tree. It is used
     * if the chosen color scheme is for an attribute that the link does not
     * have (for example confidentiality) and therefore the link to a child
     * takes the color of that child.
     *
     * @param nodeList
     */
    public static void assignConnectionColors(List<MlMapNode> nodeList) {
        for (MlMapNode node : nodeList) {
            for (NodeConnection nc : node.getChildConnections()) {
                nc.setColor(nc.getRelative().getColor());
                assignConnectionColors(node.getChildren());
            }
        }
    }

    public abstract void assignNodeColors(List<MlMapNode> nodeList);

    public void assignConnectionColors(List<NodeConnection> connections, BaseColorizer colorizer) {
        for (NodeConnection nc : connections) {
            nc.setColor(colorizer.getColorForLink(nc.getLink()));
        }
    }

    private boolean isBrightBackground(Color c) {
        float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        if (hsb[2] < 0.5) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * This function returns a random color that fits with the current
     * background color setting.
     *
     * @return If the background is dark the color is bright and vice versa.
     */
    public Color getRandomColor() {
        double offset;
        double factor;

        if (isBrightBackground(backgroundColor)) {
            offset = 0.0;
            factor = 0.4;
        } else {
            offset = 0.7;
            factor = 0.3;
        }
        double r = offset + factor * Math.random();
        double g = offset + factor * Math.random();
        double b = offset + factor * Math.random();
        double a = (1 - maxTransparency) + maxTransparency * Math.random();
        Color color = new Color((float) r, (float) g, (float) b, (float) a);
        return color;
    }

    public Color getSaturationVariation(Color c) {
        float[] hsbColor = Color.RGBtoHSB((int) c.getRed(), (int) c.getGreen(), (int) c.getBlue(), null);
        float saturation = hsbColor[1];
        double random = Math.random() / 2D;
        if (saturation + random > 1) {
            saturation = (float) (saturation - random);
        } else {
            saturation = (float) (saturation + random);
        }
        return new Color(Color.HSBtoRGB(hsbColor[0], saturation, hsbColor[2]));
    }

    public void setBackgroundColor(Color c) {
        backgroundColor = c;
    }

    public void setMaxTransparency(double maxTransparency) {
        this.maxTransparency = maxTransparency;
    }

    public double getMaxTransparency() {
        return maxTransparency;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

}
