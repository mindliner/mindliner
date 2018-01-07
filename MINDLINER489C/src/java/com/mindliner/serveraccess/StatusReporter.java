/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.serveraccess;

/**
 *
 * @author marius
 */
public interface StatusReporter {

    public void startTask(int min, int max, boolean rangeKnown, boolean cancellable);
    
    public void setMaximum(int max);

    public void setProgress(int p);

    /**
     * Displays a progress message.
     * @param m The message.
     * @param p The progress (between min and max)
     */
    public void setMessage(String m, int p);

    public void setMessage(String m);

    public boolean isCancelled();

    public void done();
}
