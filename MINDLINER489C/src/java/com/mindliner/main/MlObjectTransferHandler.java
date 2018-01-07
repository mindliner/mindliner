/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.main;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.exporter.MindlinerTransferHandler;
import static com.mindliner.exporter.MindlinerTransferHandler.mindlinerObjectLocalFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * This handler imports a Mindliner object to assign them to a button that
 * displays the object and its relations.
 * 
 * @author Marius Messerli
 */
public class MlObjectTransferHandler extends MindlinerTransferHandler {

    @Override
    public boolean importData(TransferSupport info) {
        if (!info.isDrop()) {
            return false;
        }
        Transferable t = info.getTransferable();
        if (info.isDataFlavorSupported(mindlinerObjectLocalFlavor)) {
            try {
                if (!(info.getComponent() instanceof MlObjectButton)) {
                    throw new IllegalStateException("MlObjectTransferHandler is installed on wrong component");
                }
                MlObjectButton otb = (MlObjectButton) info.getComponent();
                List<mlcObject> dropObjects = (List<mlcObject>) t.getTransferData(mindlinerObjectLocalFlavor);
                if (dropObjects.isEmpty()) {
                    return false;
                }
                otb.setObject(dropObjects.get(0));
            } catch (UnsupportedFlavorException | IOException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Drop Data Import", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }

}
