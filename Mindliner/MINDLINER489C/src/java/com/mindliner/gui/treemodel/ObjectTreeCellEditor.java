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
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * A custom cell editor. Needed because we use custom icons (during editing, the component from the Editor is rendered, and not from the ObjectTreeCellRenderer)
 * @author Dominic Plangger
 */
public class ObjectTreeCellEditor extends DefaultTreeCellEditor {
    
    public ObjectTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
        super(tree,renderer);
    }

    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
        // the following super call sets the editingIcon, therefore we call it first
        Component comp = super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row); //To change body of generated methods, choose Tools | Templates.
        ObjectTreeNode node = (ObjectTreeNode) value;
        mlcObject obj = (mlcObject) node.getUserObject();
        MlClassHandler.MindlinerObjectType type = MlClientClassHandler.getTypeByClass(obj.getClass());
        ImageIcon ic = MlIconManager.getIconForType(type);
        editingIcon = ic;
        return comp;
    }
    
    
    
}
