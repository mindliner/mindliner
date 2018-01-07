/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.enums;

/**
 * Indicates the start/end orientation for containermap links
 * @author Dominic Plangger
 */
public enum Position {
    TOP, BOTTOM, LEFT, RIGHT;

    static public boolean isOpposite(Position a, Position b) {
        return (a.equals(TOP) && b.equals(BOTTOM)) || (a.equals(BOTTOM) && b.equals(TOP)) || (a.equals(LEFT) && b.equals(RIGHT)) || (a.equals(RIGHT) && b.equals(LEFT));
    }

    static public Position getOpposite(Position a) {
        switch (a) {
            case BOTTOM:
                return TOP;
            case TOP:
                return BOTTOM;
            case LEFT:
                return RIGHT;
            case RIGHT:
                return LEFT;
            default:
                throw new IllegalArgumentException("Unsupported Position type " + a);
        }
    }

    static public boolean isHorizontal(Position a) {
        return a.equals(TOP) || a.equals(BOTTOM);
    }
    
}
