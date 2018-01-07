/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.connectors;

import com.mindliner.clientobjects.MlMapNode;

/**
 * Implements a connector who's line thickness is the arithmetic average of
 * ratings of its two end-nodes
 *
 * @author Marius Messerli
 */
public class CurvedClearTextRating extends CurvedConnectorClearText {

    private final float MAX_LINE_WIDTH = 6F;
    private final float MIN_LINE_WIDTH = 2F;

    private final double ratingMin;
    private final double ratingMax;

    public CurvedClearTextRating(double[] boundaries) {
        this.ratingMin = boundaries[0];
        this.ratingMax = boundaries[1];
    }

    @Override
    public float getLineWidth(MlMapNode parent, MlMapNode child) {
        double range = ratingMax - ratingMin;
        if (range == 0) {
            return MIN_LINE_WIDTH;
        }
        double parentWidth = MIN_LINE_WIDTH + (MAX_LINE_WIDTH - MIN_LINE_WIDTH) * (parent.getObject().getRating() - ratingMin) / range;
        double childWidth = MIN_LINE_WIDTH + (MAX_LINE_WIDTH - MIN_LINE_WIDTH) * (child.getObject().getRating() - ratingMax) / range;
        return (float) (parentWidth + childWidth) / 2f;
    }
}
