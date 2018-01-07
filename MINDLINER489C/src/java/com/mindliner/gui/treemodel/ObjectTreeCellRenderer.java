/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui.treemodel;

import com.mindliner.analysis.MlClassHandler;
import com.mindliner.clientobjects.MlClientClassHandler;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.img.icons.MlIconManager;
import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * Custom Renderer to support custom icons for tree nodes
 * @author Dominic Plangger
 */
public class ObjectTreeCellRenderer extends DefaultTreeCellRenderer{

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus); 
        ObjectTreeNode node = (ObjectTreeNode) value;
        mlcObject obj = (mlcObject) node.getUserObject();
        
        MlClassHandler.MindlinerObjectType type = MlClientClassHandler.getTypeByClass(obj.getClass());
        ImageIcon ic = MlIconManager.getIconForType(type);
        
        setIcon(ic);
        
        return this;
    }

}
