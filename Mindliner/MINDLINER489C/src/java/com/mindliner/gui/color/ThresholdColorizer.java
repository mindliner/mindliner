/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui.color;

/**
 *
 * @author M.Messerli
 */
public interface ThresholdColorizer {
    
    public void setThreshold(double threshold);
    
    public double getThreshold();
    
    /**
     * Returns the description of the meaning of the threshold, e.g. "minutes of age".
     * @return The meaning of the threashold
     */    
    public String getThresholdDescription();
    
}
