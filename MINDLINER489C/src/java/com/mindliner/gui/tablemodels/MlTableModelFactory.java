/*
 * mlTableModelFactory.java
 * 
 * Created on 18.05.2007, 22:39:06
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.mindliner.gui.tablemodels;
import com.mindliner.gui.tablemanager.MlObjectTable;

/**
 *
 * @author messerli
 */
public class MlTableModelFactory {

    public MlTableModelFactory() {
    }

    /**
     * This class creates the table models for the specified table.
     *
     * @param table.getSourceClass() The class for which a table model is
     * requested.
     */
    public static MlTableModel createModel(MlObjectTable table) {

        MlTableModel model = null;
        switch (table.getType()) {
            case Contact:
                model = new ContactTableModel(table);
                break;
            case Task:
                model = new TaskTableModel(table);
                break;
            case Image:
                model = new ImageTableModel(table);
                break;
            case Knowlet:
            case Collection:
            case Map:
            case Container:
            case Any:
                model = new AllObjectTableModel(table);
                break;
            default:
                throw new IllegalStateException("No table model found for specified object class: " + table.getType().name());
        }
        return model;
    }
}