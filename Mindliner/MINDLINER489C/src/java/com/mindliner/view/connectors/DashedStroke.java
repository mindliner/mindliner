/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.connectors;

import java.awt.BasicStroke;

/**
 *
 * @author Marius Messerli Created on 05.09.2012, 18:48:10
 */
public class DashedStroke {

    final static float miterLimit = 10f;
    final static float[] dashPattern = {10f};
    final static float dashPhase = 5f;

    public static BasicStroke getStroke(double storkeThickness) {
        return new BasicStroke(
                (float) storkeThickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                miterLimit, dashPattern, dashPhase);
    }
}
