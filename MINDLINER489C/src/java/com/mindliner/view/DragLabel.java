/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.mindliner.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author M.Messerli
 */
public class DragLabel {

    public DragLabel(int objectCount) {
        this.objectCount = objectCount;
    }

    
    public void draw(Graphics2D g, int startx, int starty, int endx, int endy, DragMode mode) {
        Font font = g.getFont();
        StringBuilder actionStringBuilder = new StringBuilder();
        actionStringBuilder.append(mode.toString());
        actionStringBuilder.append(" ");
        actionStringBuilder.append(Integer.toString(objectCount));
        Rectangle2D stringBounds = font.getStringBounds(actionStringBuilder.toString(), g.getFontRenderContext());
        int midPointX = (startx + endx) / 2;
        int midPointY = (starty + endy) / 2;
        g.fillRoundRect(
                midPointX - boxSizeX / 2, 
                midPointY - boxSizeY / 2, boxSizeX, boxSizeY, 3, 3);
        g.setColor(Color.black);
        g.drawString(actionStringBuilder.toString(), 
                midPointX - boxSizeX / 2 + stringOffsetX, 
                midPointY - boxSizeY / 2 + stringOffsetY - (int) stringBounds.getY());
    }

    public enum DragMode {
        Move, Copy
    };
    private int stringOffsetX = 3;
    private int stringOffsetY = 0;
    private int boxSizeX = 50;
    private int boxSizeY = 20;
    private int objectCount = 0;
}
