/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui.tablemodels;

import com.mindliner.clientobjects.mlcNews;
import com.mindliner.clientobjects.mlcObject;

/**
 *
 * @author Marius Messerli
 */
public class ActionItemTableRow extends MlTableRow {

    /**
     * Creates a new instance of ContactTableRow
     *
     * @param obj The object to be show in this row
     * @param numberOfColumns The number of columns required
     */
    public ActionItemTableRow(mlcObject obj, int numberOfColumns) {
        super(obj, numberOfColumns);
    }

    @Override
    public int addSpecificColumns(int currCol) {
        mlcNews ai = (mlcNews) sourceObject;
        currCol = addHeadlineColumn(currCol);
        if (numCols > currCol) {
            cellObjects[currCol++] = ai.getNewsType();
        }
        return currCol;
    }
}
