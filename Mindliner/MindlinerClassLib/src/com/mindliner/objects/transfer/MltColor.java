/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.objects.transfer;

import com.mindliner.entities.MlsColor;
import java.awt.Color;
import java.io.Serializable;

/**
 *
 * @author Marius Messerli
 */
public class MltColor implements Serializable {

    private int id;
    private MltColorizer colorizer;
    private Color color = Color.BLACK;
    private int driverValue = 0;
    private static final long serialVersionUID = 19640205L;

    public MltColor() {
    }

    public MltColor(MlsColor c, MltColorizer colorizer) {
        id = c.getId();
        this.colorizer = colorizer;
        color = new Color(c.getColor().getRed(),c.getColor().getGreen(), c.getColor().getBlue());
        driverValue = c.getDriverValue();
    }

    public MltColorizer getColorizer() {
        return colorizer;
    }

    public int getDriverValue() {
        return driverValue;
    }

    public int getId() {
        return id;
    }

    public Color getColor() {
        return color;
    }

    public void setDriverValue(int driverValue) {
        this.driverValue = driverValue;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setColor(Color c) {
        color = c;
    }

    @Override
    public String toString() {
        return color + " (for value " + driverValue + ")";
    }
}
