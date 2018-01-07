/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.objects.transfer;

import com.mindliner.entities.Colorizer;
import com.mindliner.entities.Colorizer.ColorizerValueType;
import com.mindliner.entities.MlsColor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marius Messerli Created on 16.09.2012, 19:15:29
 */
public class MltColorizer implements Serializable {

    private String colorizerClassName;
    private int schemeId;
    private double minimumOrThreshold;
    private double maximum;
    private Colorizer.ColorizerValueType type;
    private List<MltColor> colors = new ArrayList<MltColor>();

    /**
     * This constructor is used to transmit client data to the server.
     */
    public MltColorizer() {
    }

    /**
     * This constructor is used if server data is to be transmitted to the
     * client.
     *
     * @param c
     */
    public MltColorizer(Colorizer c) {
        colorizerClassName = c.getColorizerClassName();
        schemeId = c.getScheme().getId();
        minimumOrThreshold = c.getMinimumOrThreshold();
        maximum = c.getMaximum();
        type = c.getType();
        for (MlsColor color : c.getColors()) {
            colors.add(new MltColor(color, this));
        }
    }

    public String getColorizerClassName() {
        return colorizerClassName;
    }

    public List<MltColor> getColors() {
        return colors;
    }

    public double getMaximum() {
        return maximum;
    }

    public double getMinimumOrThreshold() {
        return minimumOrThreshold;
    }

    public int getSchemeId() {
        return schemeId;
    }

    public ColorizerValueType getType() {
        return type;
    }

    public void setColorizerClassName(String colorizerClassName) {
        this.colorizerClassName = colorizerClassName;
    }

    public void setColors(List<MltColor> colors) {
        this.colors = colors;
    }

    public void setMaximum(double maximum) {
        this.maximum = maximum;
    }

    public void setMinimumOrThreshold(double minimumOrThreshold) {
        this.minimumOrThreshold = minimumOrThreshold;
    }

    public void setSchemeId(int schemeId) {
        this.schemeId = schemeId;
    }

    public void setType(ColorizerValueType type) {
        this.type = type;
    }
}
