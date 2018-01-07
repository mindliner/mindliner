/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui;

import javax.swing.JPopupMenu;

/**
 *
 * @author dominic
 */
public class MlPopupMenu<T> extends JPopupMenu{
    
    private T node;

    public T getNode() {
        return node;
    }

    public void setNode(T node) {
        this.node = node;
    }
    
}
