/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.synchronization;

/**
 *
 * @author Marius Messerli
 */
public interface MlSynchProgressReporter {

    public void printLine(String line);

    public void setVisible(boolean visible);
    
    public boolean cancelled();
    
    public void clear();
}
