/*
 * ContactTable.java
 *
 * Created on 11. Januar 2006, 19:48
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.mindliner.gui.tablemodels;

import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.clientobjects.mlcContact;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.commands.CommandRecorder;
import com.mindliner.commands.ConfidentialityUpdateCommand;
import com.mindliner.gui.tablemanager.MlObjectTable;

/**
 *
 * @author Marius Messerli
 */
public class ContactTableModel extends MlTableModel {

    /** Creates a new instance of ContactTable */
    public ContactTableModel(MlObjectTable s) {
        super(s);
    }

    @Override
    public void setRow(int row, mlcObject o){
        if (row > (getRows().size() - 1)) {
            ContactTableRow tr = new ContactTableRow (o, getColumnCount());
            getRows().add(tr);
        }
        else {
            ContactTableRow  tr = (ContactTableRow) getRows().get(row);
            tr.setObject(o);
        }
    }


    /**
     *Updates the project object and database record if a cell was edited.
     */
    @Override
    public void setValueAt(Object value, int row, int modelColumn) {

        mlcContact c = (mlcContact) getSourceObject(row);
        CommandRecorder cr = CommandRecorder.getInstance();

        switch (modelColumn) {
            case 3:
                mlsConfidentiality conf = (mlsConfidentiality) value;
                cr.scheduleCommand(new ConfidentialityUpdateCommand(c, conf));
                break;
        }
//        c = (mlcContact) CacheEngine.getSingle(c.getId());
        setRow(row, c);         
    }

    /*
     *Specifies that the first and fourth column is editable
     */
    @Override
    public boolean isCellEditable(int row, int modelColumn) {
        if (isCellEditing() == false) return false;

        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        if (modelColumn == 3) {
            return true;
        }
        else {
            return false;
        }
    }

}
