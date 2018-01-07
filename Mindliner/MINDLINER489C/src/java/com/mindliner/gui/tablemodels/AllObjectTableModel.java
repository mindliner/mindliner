/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.gui.tablemodels;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.gui.tablemanager.MlObjectTable;


/**
 *
 * @author Marius Messerli
 */
public class AllObjectTableModel extends MlTableModel {

    /**
     * @param t The table for which this model is used
     */
    public AllObjectTableModel(MlObjectTable t) {
        super(t);
    }

    @Override
    public void setRow(int row, mlcObject o){
        if (row > (getRows().size() - 1)) {
            AllObjectTableRow ctr = new AllObjectTableRow  ((mlcObject) o, getColumnCount());
            getRows().add(ctr);
        }
        else {
            AllObjectTableRow  ctr = (AllObjectTableRow ) getRows().get(row);
            ctr.setObject(o);
        }
    }

}
