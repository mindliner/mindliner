/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.gui.tablemodels;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.categories.mlsPriority;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author messerli
 */
public class MlCellEditorFactory {

    public static TableCellEditor createEditor(Class objectClass){

        JComboBox comboBox = new JComboBox();

        if (objectClass == mlsPriority.class){
            List<mlsPriority> plist = CacheEngineStatic.getPriorities();
            setupComboBox(plist, comboBox);
        }

        else if (objectClass == mlsConfidentiality.class){
            List<mlsConfidentiality> clist = CacheEngineStatic.getConfidentialities();
            setupComboBox(clist, comboBox);
        }

        return new DefaultCellEditor(comboBox);

    }

    private static void setupComboBox(List items, JComboBox cb){
        Collections.sort(items);
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        Iterator it = items.iterator();
        while (it.hasNext()) model.addElement(it.next());
        cb.setModel(model);
    }
}
