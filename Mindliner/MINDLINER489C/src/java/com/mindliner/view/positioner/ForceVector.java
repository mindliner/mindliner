/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.positioner;

/**
 * Implements a 3D force vector.
 *
 * @author Marius Messerli
 */
public class ForceVector {

    public ForceVector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Constructor that computes a unit vector for the specified angle
     * @param angle The angle in degrees for a unit vector
     */
    public ForceVector(double angle) {
        x = Math.sin(angle);
        y = Math.cos(angle);
    }

    public void add(ForceVector that) {
        x = x + that.getX();
        y = y + that.getY();
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getLength() {
        return Math.sqrt(x * x + y * y);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ForceVector other = (ForceVector) obj;
        if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x)) {
            return false;
        }
        if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 41 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        return hash;
    }

    @Override
    public String toString() {
        return "ForceVector{" + "x=" + x + ", y=" + y + '}';
    }
    private double x;
    private double y;
}
