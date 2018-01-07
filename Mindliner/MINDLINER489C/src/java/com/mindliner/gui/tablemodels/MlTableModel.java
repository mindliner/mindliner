/*
 * MLTableModel.java
 *
 * Created on 23. Dezember 2005, 07:24
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package com.mindliner.gui.tablemodels;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.gui.tablemanager.MlObjectTable;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

/**
 *
 * This class implements a generic table model for the application.
 *
 * @author Marius Messerli
 */
public abstract class MlTableModel extends AbstractTableModel {

    private List<MlTableRow> rows = new ArrayList<>();
    private boolean cellEditing = true;
    private MlObjectTable table = null;

    public static Object getSourceObjectByViewIndex(JTable table, int viewIndex) {
        int selectedModelRow = table.convertRowIndexToModel(viewIndex);
        MlTableModel mtm = (MlTableModel) table.getModel();
        return mtm.getSourceObject(selectedModelRow);
    }

    public MlTableModel(MlObjectTable st) {
        table = st;
    }

    public void setCellEditing(boolean state) {
        cellEditing = state;
    }

    public boolean getCellEditing() {
        return cellEditing;
    }

    /*
     * The method that fills the table row.
     */
    public abstract void setRow(int row, mlcObject o);

    @Override
    public Object getValueAt(int row, int col) {
        MlTableRow tr = rows.get(row);
        return tr.getValueAt(col);
    }

    /**
     * Returns the object associated with the specified row.
     *
     * @param row The row for which the source object is requested.
     * @return The source object for the specified row.
     */
    public mlcObject getSourceObject(int row) {
        MlTableRow tr = rows.get(row);
        return tr.getSourceObject();
    }

    @Override
    public int getColumnCount() {
        return table.getDisplayColumnCount();
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public String getColumnName(int col) {
        List<MlTableColumn> clist = table.getColumns();
        MlTableColumn c = clist.get(col);
        return c.getHeader();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        List<MlTableColumn> clist = table.getColumns();
        MlTableColumn c = clist.get(columnIndex);
        return c.getContentClass();
    }

    public void clear() {
        rows = new ArrayList<>();
    }

    /**
     * The default table model is view-only.
     *
     * @param col the column index
     * @param row the row index
     * @return True for cells that are editable
     */
 
    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    protected MlObjectTable getSearchTable() {
        return table;
    }

    protected boolean isCellEditing() {
        return cellEditing;
    }

    protected List<MlTableRow> getRows() {
        return rows;
    }
}