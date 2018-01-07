/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.exporter;

import com.mindliner.clientobjects.mlcObject;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.mindliner.clientobjects.MlMapNode;

/**
 * This transferrable can carry either a list of objects or a node. In the case
 * of a node this transferrable will handle it recursively or just as a single
 * node.
 *
 * @author Marius Messerli Created on 13.08.2012, 08:32:51
 */
public class MindlinerObjectTransferable implements Transferable {

    protected List<mlcObject> objectList = new ArrayList<>();
    MlMapNode node = null;

    public MindlinerObjectTransferable(List<mlcObject> objects) {
        objectList = objects;
    }

    public MindlinerObjectTransferable(MlMapNode node) {
        this.node = node;
    }
    

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{
            MindlinerTransferHandler.mindlinerObjectLocalFlavor, 
            MindlinerTransferHandler.mindlinerNodeLocalFlavor, 
            DataFlavor.stringFlavor};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        if (MindlinerTransferHandler.mindlinerObjectLocalFlavor.equals(flavor)
                || DataFlavor.stringFlavor.equals(flavor)
                || MindlinerTransferHandler.mindlinerNodeLocalFlavor.equals(flavor)) {
            return true;
        }
        return false;
    }

    /**
     * Objects a transferrable in the specified flavor.
     *
     *
     * @param flavor Binary for local JVM, serialized for inter JVMs and
     * Classname + Key
     */
    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (!isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }
        if (MindlinerTransferHandler.htmlFlavor.equals(flavor)){
            if (node != null){
                return ObjectTextExporter.formatNodeAsHTML(node);
            }
        }
        if (DataFlavor.stringFlavor.equals(flavor)) {
            if (objectList == null) {
                if (node == null) {
                    return "";
                } else {
                    return ObjectTextExporter.formatNodeAsHTML(node);
                }
            } else {
                Iterator it = objectList.iterator();
                StringBuilder sb = new StringBuilder();

                while (it.hasNext()) {
                    mlcObject o = (mlcObject) it.next();
                    sb.append(ObjectTextExporter.formatObject(o));
                    if (it.hasNext()) {
                        sb.append("\n");
                    }
                }
                return sb.toString();
            }
        } else if (MindlinerTransferHandler.mindlinerObjectLocalFlavor.equals(flavor)) {
            return objectList;
        } else {
            return node;
        }
    }
}