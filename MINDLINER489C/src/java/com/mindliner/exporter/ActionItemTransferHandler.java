/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.exporter;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.gui.tablemanager.DecoratedTable;
import java.awt.datatransfer.Transferable;
import java.util.List;
import javax.swing.JComponent;

/**
 *
 * @author Marius Messerli
 */
public class ActionItemTransferHandler extends MindlinerTransferHandler {

    @Override
    public Transferable createTransferable(JComponent c) {
        if (c instanceof DecoratedTable) {
            DecoratedTable table = (DecoratedTable) c;
            List<mlcObject> mbos = table.getMainTable().getSelectedSourceObjects();
            return new ActionItemObjectTransferrable(mbos);
        } else {
            System.err.println("warning: createTransferrable called from unknown component");
            return null;
        }
    }

}
