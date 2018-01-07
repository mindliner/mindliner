/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui.treemodel;

import com.mindliner.clientobjects.mlcObject;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Represents a node in the Node object tree suggested for powerpoint import
 * @author Dominic Plangger
 */
public class ObjectTreeNode extends DefaultMutableTreeNode {
    
    private static final int MAX_DISPLAY_LENGTH = 60;
    
    public ObjectTreeNode(mlcObject obj) {
        super(obj);
    }

    @Override
    public String toString() {
        if (userObject == null) {
            return "";
        }
        mlcObject obj = (mlcObject) userObject;
        if (obj.getHeadline().length() > MAX_DISPLAY_LENGTH) {
            return obj.getHeadline().substring(0, MAX_DISPLAY_LENGTH) + "...";
        }
        return obj.getHeadline();
    }

    @Override
    public void setUserObject(Object userObject) {
        if (userObject instanceof String) {
            mlcObject o = (mlcObject) getUserObject();
            o.setHeadline((String) userObject);
        }
        else {
            super.setUserObject(userObject); 
        }
    }
}
