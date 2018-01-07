/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui.tablemodels;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.gui.tablemanager.MlObjectTable;
import javax.swing.JTable;

/**
 *
 * @author Dominic Plangger
 */
public class ImageTableModel extends MlTableModel {
    
    public ImageTableModel(MlObjectTable s) {
        super(s);
    }
    
    @Override
    public void setRow(int row, mlcObject o) {
        JTable jTable = getSearchTable().getJTable();
        if (row > (getRows().size() - 1)) {
            ImageTableRow itr = new ImageTableRow(o, getColumnCount(), jTable);
            getRows().add(itr);
        } else {
            ImageTableRow itr = (ImageTableRow) getRows().get(row);
            itr.setObject(o);
        }
    }
}
