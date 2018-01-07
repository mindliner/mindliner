package com.mindliner.gui.tablemodels;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcTask;

/**
 *
 * @author Marius Messerli
 */
public class TaskTableRow extends MlTableRow {

    public TaskTableRow(mlcObject obj, int numberOfColumns) {
        super(obj, numberOfColumns);
    }

    @Override
    public int addSpecificColumns(int currCol) {
        currCol = addHeadlineColumn(currCol);
        mlcTask t = (mlcTask) sourceObject;
    
        if (numCols > currCol) {
            cellObjects[currCol++] = t.isCompleted();
        }

        if (numCols > currCol) {
            cellObjects[currCol++] = t.getPriority();
        }
        if (numCols > currCol) {
            cellObjects[currCol++] = t.getDueDate();
        }

        // convert effort from milliseconds to minutes
        if (numCols > currCol) {
            cellObjects[currCol++] = t.getEffortEstimation();
        }
        return currCol;
    }
}
