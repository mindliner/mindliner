/*
 * ContactTableRow.java
 *
 * Created on 11. Januar 2006, 19:51
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 *//*
 * ContactTableRow.java
 *
 * Created on 11. Januar 2006, 19:51
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package com.mindliner.gui.tablemodels;

import com.mindliner.clientobjects.mlcContact;
import com.mindliner.clientobjects.mlcObject;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marius Messerli
 */
public class ContactTableRow extends MlTableRow {

    String[] contactHeaders = {"Firstname", "Lastname", "Email", "Cfd", "Own"};

    public static List<String> getHeaders() {
        List<String> headers = new ArrayList<String>();
        headers.add("Firstname");
        headers.add("Lastname");
        headers.add("Email");
        headers.add("Cfd");
        headers.add("Own");
        return headers;
    }

    /**
     * Creates a new instance of ContactTableRow
     *
     * @param obj The source object for the new table row.
     * @param numberOfColumns The number of columns to be shown in the table.
     */
    public ContactTableRow(mlcObject obj, int numberOfColumns) {
        super(obj, numberOfColumns);
    }

    @Override
    public int addSpecificColumns(int currCol) {
        mlcContact c = (mlcContact) sourceObject;
        if (numCols > currCol) {
            cellObjects[currCol++] = c.getFirstName();
        }
        if (numCols > currCol) {
            cellObjects[currCol++] = c.getMiddleName();
        }
        if (numCols > currCol) {
            cellObjects[currCol++] = c.getLastName();
        }
        if (numCols > currCol) {
            cellObjects[currCol++] = c.getEmail();
        }
        return currCol;
    }
}
