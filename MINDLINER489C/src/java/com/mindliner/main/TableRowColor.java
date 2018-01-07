/*
 * ColorDriverBase.java
 *
 * Created on 19. Juli 2006, 16:11
 *
 * This is the base class for all elements read from and stored to sql tables.
 * It provides the functions to read, write, and manage the rows.
 */

package com.mindliner.main;

import java.awt.Color;

/**
 * This class determins the color for a row in a Minliner table.
 * @author messerli
 */
public interface TableRowColor {
        
    public Color getColor();
    public void setColor(Color c);
    
    public String getName();
    
    public int getID();
    
    public void setID(int id);
    
    public void setName(String name);
    
}
