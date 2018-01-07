/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.importer;

import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.io.IOException;

/**
 *
 * @author M.Messerli
 */
public class MlTextTransfer implements ClipboardOwner {

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }

    public void setClipboardContents(String aString) {
        StringSelection stringSelection = new StringSelection(aString);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, this);
    }

    public String getClipboardContents() {
        String result = "";
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        //odd: the Object param of getContents is not currently used
        Transferable contents = clipboard.getContents(null);
        boolean hasTransferableText =
                (contents != null)
                && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
        if (hasTransferableText) {
            try {
                result = (String) contents.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException ex) {
                //highly unlikely since we are using a standard DataFlavor
                System.out.println(ex);
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }
        return result;
    }

}
