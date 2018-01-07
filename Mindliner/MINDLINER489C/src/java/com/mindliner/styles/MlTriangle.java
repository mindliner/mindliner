/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.styles;

import java.awt.Graphics;
import javax.swing.RowSorter;
import javax.swing.SortOrder;

/**
 * Class draws a triangle of the specified size at the specified location
 * @author Marius Messerli
 */
public class MlTriangle {

    public enum Direction {
        North, East, South, West
    }

    private final int[] xCoords = new int[3];
    private final int[] yCoords = new int[3];
    int xStart;
    int yStart;
    private int edgeSize = 6;
    private final Direction direction;

    public MlTriangle(Direction direction, int edgeSize, int xStart, int yStart, boolean filled) {
        this.direction = direction;
        this.edgeSize = edgeSize;
        this.xStart = xStart;
        this.yStart = yStart;
        build();
    }

    private void build() {
        switch (direction) {
            case North:
                xCoords[0] = xStart + 0;
                xCoords[1] = xStart + edgeSize;
                xCoords[2] = xStart + edgeSize / 2;
                yCoords[0] = yStart + edgeSize;
                yCoords[1] = yStart + edgeSize;
                yCoords[2] = yStart + 0;
                break;
            case East:
                xCoords[0] = xStart + 0;
                xCoords[1] = xStart + 0;
                xCoords[2] = xStart + edgeSize;
                yCoords[0] = yStart + 0;
                yCoords[1] = yStart + edgeSize;
                yCoords[2] = yStart + edgeSize / 2;
                break;

            case South:
                xCoords[0] = xStart + 0;
                xCoords[1] = xStart + edgeSize;
                xCoords[2] = xStart + edgeSize / 2;
                yCoords[0] = yStart + 0;
                yCoords[1] = yStart + 0;
                yCoords[2] = yStart + edgeSize;
                break;

            case West:
                xCoords[0] = xStart + 0;
                xCoords[1] = xStart + edgeSize;
                xCoords[2] = xStart + edgeSize;
                yCoords[0] = yStart + edgeSize / 2;
                yCoords[1] = yStart + edgeSize;
                yCoords[2] = yStart + 0;
                break;

            default:
                throw new AssertionError();
        }
    }

    public void paint(Graphics g) {
        g.fillPolygon(xCoords, yCoords, 3);
    }

}
