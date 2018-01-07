/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.gui.treemodel;

import com.mindliner.clientobjects.mlcObject;

/**
 * Tree node that allows also saving a byte array in the node. Used in particular
 * for image data.
 * @author Dominic Plangger
 */
public class ImageTreeNode extends ObjectTreeNode{
    
    public final byte[] data;
    public final String mime;

    public ImageTreeNode(byte[] data, String mime, mlcObject obj) {
        super(obj);
        this.data = data;
        this.mime = mime;
    }
    
}
