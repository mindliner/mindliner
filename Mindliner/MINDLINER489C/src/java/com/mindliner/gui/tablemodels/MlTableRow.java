package com.mindliner.gui.tablemodels;

import com.mindliner.clientobjects.mlcObject;

/**
 * This class provides a basic implementation for the model for a single table
 * row. Created on 1. Januar 2006, 08:25
 *
 * @author Marius Messerli
 */
public abstract class MlTableRow {
    // variable holds the rows

    protected Object[] cellObjects;
    protected mlcObject sourceObject;
    protected int numCols = 0;

    /**
     * Creates a new object by specifying the number of columns and the object
     * from which to take the content.
     *
     * @param o The object that defines the content of the row.
     * @param cols The number of columns for this row.
     */
    public MlTableRow(mlcObject o, int cols) {
        numCols = cols;
        cellObjects = new Object[numCols];
        setObject(o);
    }

    /**
     * This method adds those columsn that are most specific to a particular
     * object type.
     *
     * @param currentColumnIndex
     * @return The currentColumnIndex after the specific additions
     */
    public abstract int addSpecificColumns(int currentColumnIndex);
    
    /**
     * Most (but not all) subclasses use the headline as first column. Therefore
     * the method is made available here.
     * @param columnIndex The column where the headline is to be inserted.
     * @return The column index where the next value is to be inserted.
     */
    protected int addHeadlineColumn(int columnIndex){
        if (numCols > columnIndex) {
            StringBuilder sb = new StringBuilder();
            if (sourceObject.isPrivateAccess() == true) {
                sb.append("[P] ");
            }
            sb.append(sourceObject.getHeadline());
            cellObjects[columnIndex++] = sb.toString();
        }
        return columnIndex;
    }

    /**
     * Defines the column values for all mindliner objects that do not need any
     * special handling. Others extend this class and override setObject.
     *
     * @param o The source object.
     */
    public final void setObject(mlcObject o) {
        int currCol = 0;
        sourceObject = o;

        currCol = addSpecificColumns(currCol);

        // add the remaining generic columns
        if (numCols > currCol) {
            cellObjects[currCol++] = sourceObject.getOwner();
        }
        if (numCols > currCol) {
            cellObjects[currCol++] = sourceObject.getModificationDate();
        }
        if (numCols > currCol) {
            cellObjects[currCol++] = sourceObject.getConfidentiality();
        }
        
        // fill remaining columns with a dash
        for (int i = currCol++; i < numCols; i++) {
            cellObjects[i] = "-";
        }

    }

    /**
     * Returns a reference to the source object, i.e. the object from which the
     * content was copied, for this row.
     *
     * @return A reference to the source object.
     */
    public mlcObject getSourceObject() {
        return sourceObject;
    }

    /**
     * Returns the specified column.
     *
     * @param col The column number.
     * @return A vector of Object defining the column objects.
     */
    public Object getColumn(int col) {
        return cellObjects[col];
    }

    /**
     * Assigns the content of the specified column.
     *
     * @param col The column number.
     * @param o The object vector for this row.
     */
    public void setColumn(int col, Object o) {
        cellObjects[col] = o;
    }

    /**
     * Returns the number of columns for this row.
     *
     * @return The number of columns.
     */
    public int getNumberOfColumns() {
        return numCols;
    }

    /**
     * Assigns the value of a single cell.
     *
     * @param value The object at the specified location.
     * @param col The column number.
     */
    public void setValueAt(Object value, int col) {
        if (col > numCols) {
            throw new IllegalStateException("setValueAt: column index out of range.");
        }
        cellObjects[col] = value;
    }

    /**
     * Returns the cell value at the specified locatcion
     *
     * @param col The column number.
     * @return A reference to the Object in the specified cell.
     */
    public Object getValueAt(int col) {
        if (col > numCols) {
            return 0;
        }
        return cellObjects[col];
    }
}
