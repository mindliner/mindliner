/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view;

import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;

/**
 * This class positions a dialog as closely as possible cnetered under the
 * cursor.
 *
 * @author Marius Messerli
 */
public class DialogPositioner {

    /**
     * Returns the origin of a Dialog of the specified width and height
     * so that it lies as closely as possible cnetered under the cursor.
     * 
     * @param width The width of the Dialog to be positioned
     * @param height The height of the Dialog to be positioned
     * @return 
     */
    public static Point getLocation(int width, int height) {
        int locx;
        int locy;

        Point cursorLocation = MouseInfo.getPointerInfo().getLocation();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        // THE X LOCATION
        if (cursorLocation.x > screen.width / 2) {
            if (cursorLocation.x + width / 2 > screen.width) {
                locx = screen.width - width;
            } else {
                locx = cursorLocation.x - width / 2;
            }
        } else {
            if (cursorLocation.x - width / 2 < 0) {
                locx = 0;
            } else {
                locx = cursorLocation.x - width / 2;
            }
        }

        // THE Y LOCATION
        if (cursorLocation.y > screen.height / 2) {
            if (cursorLocation.y + height / 2 > screen.height) {
                locy = screen.height - height;
            } else {
                locy = cursorLocation.y - height / 2;
            }
        } else {
            if (cursorLocation.y - height / 2 < 0) {
                locy = 0;
            } else {
                locy = cursorLocation.y - height / 2;
            }
        }
        return new Point(locx, locy);
    }

}
