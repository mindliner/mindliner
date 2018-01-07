/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui.tablemanager;

import com.mindliner.categories.mlsMindlinerCategory;
import com.mindliner.gui.tablemodels.MlTableModel;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

/**
 *
 * @author Marius Messerli
 */
public class ColumnSizeManager {

    /**
     * Specifies the column width of the tables.
     * @param objectTable The table for whcih the column sizes need adjustment
     */
    public static void checkColumnSizes(MlObjectTable objectTable) {

        JTable table = objectTable.getJTable();
        MlTableModel tableModel = (MlTableModel) table.getModel();

        if (tableModel.getColumnCount() < 2 || tableModel.getRowCount() == 0) {
            return;
        }

        TableColumn column;

//        TableCellRenderer headerRenderer =
//                table.getTableHeader().getDefaultRenderer();

        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            Object cell = tableModel.getValueAt(0, i);
            if (cell instanceof Boolean || cell instanceof ImageIcon || cell instanceof Image) {
                column = table.getColumnModel().getColumn(i);
                if (column.getWidth() > 30) {
                    column.setMinWidth(20);
                    column.setMaxWidth(36);
                    column.setPreferredWidth(30);
                }
            }

            if (cell instanceof mlsMindlinerCategory) {
                column = table.getColumnModel().getColumn(i);
                if (column.getWidth() > 60) {
                    column.setMinWidth(40);
                    column.setMaxWidth(60);
                    column.setPreferredWidth(50);
                }
            }
        }
    }
}
