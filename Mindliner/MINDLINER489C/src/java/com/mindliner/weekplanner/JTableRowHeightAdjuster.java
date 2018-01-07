/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.weekplanner;

import java.awt.Component;
import javax.swing.JTable;

/**
 * http://stackoverflow.com/questions/1783607/auto-adjust-the-height-of-rows-in-a-jtable
 *
 * @author camickr
 */
public class JTableRowHeightAdjuster {

    /**
     * Function to adjust the height of each row. Call this after the table has
     * been fully populated.
     *
     * @param table
     * @param maxHeight Specify the maximum height of rows or -1 if you accept
     * any height
     */
    public static void updateRowHeights(JTable table, int maxHeight) {
        for (int row = 0; row < table.getRowCount(); row++) {
            int rowHeight = table.getRowHeight();
            for (int column = 0; column < table.getColumnCount(); column++) {
                Component comp = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
                rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
            }
            if (maxHeight > 0) {
                rowHeight = Math.min(rowHeight, maxHeight);
            }
            table.setRowHeight(row, rowHeight);
        }
    }
}
