/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui.tablemodels;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.MlcImage;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.image.LazyImage;
import com.mindliner.image.LazyImage.CompletionHandler;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Dominic Plangger
 */
public class ImageTableRow extends MlTableRow {
    
    private RowCompletionHandler handler;

    public ImageTableRow(mlcObject o, int cols, JTable table) {
        super(o, cols);
        handler.setTable(table);
    }

    @Override
    public int addSpecificColumns(int currCol) {
        currCol = addHeadlineColumn(currCol);
        handler = new RowCompletionHandler();
        MlcImage oc = (MlcImage) sourceObject;
//        if (numCols > currCol) {
//            cellObjects[currCol++] = oc.getType() == null ? "" : oc.getType();
//        }
        if (numCols > currCol ) {
            if (oc.getType() != null) {
                switch (oc.getType()) {
                    case Icon:
                        // special case icons: MlcImage directly contains the image for icons, in all other cases, the image is stored in the ImageCache
                        cellObjects[currCol++] = oc.getIcon().getImage();
                        break;
                    default:
                        LazyImage lImg = CacheEngineStatic.getImageAsync(oc);
                        // We need to initiate a fireTableDataChanged after the image is loaded completely to update the table
                        lImg.registerCompletionHandler(handler);
                        cellObjects[currCol++] = lImg;
                }
            }
        }
        if (numCols > currCol) {
            cellObjects[currCol++] = oc.getUrl() == null ? "" : oc.getUrl();
        }
        return currCol;
    }
    
    private class RowCompletionHandler implements CompletionHandler {
        
        private JTable table = null;

        @Override
        public void completed() {
            if (table != null) {
                AbstractTableModel atm = (AbstractTableModel) table.getModel();
                atm.fireTableDataChanged();
            }
        }
        
        public void setTable(JTable table) {
            this.table = table;
        }
    }
    
}
