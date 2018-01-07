/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.thread;

import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 * Extends the SwingWorker with simple exception handling. It overwrites the 'done' method to log any exceptions thrown in the doInBackground method.
 * @author dominic
 */
public abstract class SimpleSwingWorker<T, V> extends SwingWorker<T, V> {

    @Override
    protected void done() {
        try {
            get();
        } catch (final InterruptedException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "SwingWorker has been interrupted", ex);
        } catch (final ExecutionException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Exception while executing doInBackground of SwingWorker", ex);
        }
    }
}
