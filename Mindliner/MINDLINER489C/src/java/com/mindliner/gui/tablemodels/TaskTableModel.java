/*
 * TaskTableModel.java
 *
 * Created on 1. Januar 2006, 08:15
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package com.mindliner.gui.tablemodels;

import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.categories.mlsPriority;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcTask;
import com.mindliner.commands.CommandRecorder;
import com.mindliner.commands.CompletionUpdateCommand;
import com.mindliner.commands.ConfidentialityUpdateCommand;
import com.mindliner.commands.PriorityUpdateCommand;
import com.mindliner.gui.tablemanager.MlObjectTable;

/**
 *
 * @author Marius Messerli
 */
public class TaskTableModel extends MlTableModel {

    /**
     * Creates a new instance of TaskTableModel
     */
    public TaskTableModel(MlObjectTable s) {
        super(s);
    }

    @Override
    public void setRow(int row, mlcObject o) {
        if (row > (getRows().size() - 1)) {
            MlTableRow tr = new TaskTableRow((mlcObject) o, getColumnCount());
            getRows().add(tr);
        } else {
            MlTableRow tr = (MlTableRow) getRows().get(row);
            tr.setObject(o);
        }
    }

    /*
     * Don't need to implement this method unless your table's data can change.
     */
    @Override
    public void setValueAt(Object value, int row, int modelColumn) {
        mlcTask t = (mlcTask) getSourceObject(row);
        CommandRecorder cr = CommandRecorder.getInstance();

        switch (modelColumn) {
            case 1:
                Boolean b = (Boolean) value;
                cr.scheduleCommand(new CompletionUpdateCommand(t, b));
                break;

            case 2:
                mlsPriority p = (mlsPriority) value;
                cr.scheduleCommand(new PriorityUpdateCommand(t, p));
                break;

            case 5:
                mlsConfidentiality conf = (mlsConfidentiality) value;
                cr.scheduleCommand(new ConfidentialityUpdateCommand(t, conf));
                break;

        }
        setRow(row, t);
    }

    /*
     * Specifies that the first and fourth column is editable
     */
    @Override
    public boolean isCellEditable(int row, int modelColumn) {
        if (isCellEditing() == false) {
            return false;
        }
        if (modelColumn == 1 || modelColumn == 2 || modelColumn == 4) {
            return true;
        } else {
            return false;
        }
    }
}
