/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.entities;

import java.awt.Color;
import java.io.Serializable;
import javax.persistence.*;

/**
 *
 * @author Marius Messerli
 */
@Entity
@Table(name = "colors")
public class MlsColor implements Serializable {

    private int id;
    private int driverValue = -1;
    private Colorizer colorizer = null;
    private int red = 0;
    private int green = 0;
    private int blue = 0;
    private static final long serialVersionUID = 19640205L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int getId() {
        return id;
    }

    @ManyToOne
    @JoinColumn(name = "COLORIZER_ID", referencedColumnName = "ID")
    public Colorizer getColorizer() {
        return colorizer;
    }

    public void setColorizer(Colorizer colorizer) {
        this.colorizer = colorizer;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "r=" + red + ", g=" + green + ", b=" + blue + " for value " + Integer.toString(driverValue);
    }

    public void setDriverValue(int value) {
        driverValue = value;
    }

    @Column(name = "DRIVER_VALUE")
    public int getDriverValue() {
        return driverValue;
    }

    protected void setRed(int r) {
        red = r;
    }

    @Column(name = "RED")
    protected int getRed() {
        return red;
    }

    protected void setGreen(int g) {
        green = g;
    }

    @Column(name = "GREEN")
    protected int getGreen() {
        return green;
    }

    protected void setBlue(int b) {
        blue = b;
    }

    @Column(name = "BLUE")
    protected int getBlue() {
        return blue;
    }

    public void setColor(Color c) {
        red = c.getRed();
        green = c.getGreen();
        blue = c.getBlue();
    }

    @Transient
    public Color getColor() {
        return new Color(red, green, blue);
    }

}
