/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.background;

import java.awt.Color;
import java.awt.Graphics2D;
import javax.swing.JPanel;

/**
 *
 * @author M.Messerli
 */
public interface BackgroundPainter {
        
    public void paint(Graphics2D g2, JPanel panel);
    
    public void setBackground(Color bg);
    
    /**
     * Returns the background color if the painter is a SingleColor painter or else
     * returns an average color.
     * 
     * @return 
     */
    public Color getBackground();
    
    /**
     * Determins if the background is dark or bright
     * 
     * @return True if the luminocity is less than 0.5, false otherwise
     */
    public boolean isDark();
    
    /**
     * This function is used to initialize a background before first use. This
     * could be to load images, to create a algorithmic pattern, etc.
     */
    public void initialize();
    
    
}
