/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.mindliner.exporter;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcTask;
import com.mindliner.commands.FileUploadCommand;
import com.mindliner.commands.CommandRecorder;
import com.mindliner.commands.LinkCommand;
import com.mindliner.commands.UnlinkCommand;
import com.mindliner.connector.CloudConnector;
import com.mindliner.connector.FileUploadThread;
import com.mindliner.gui.tablemanager.DecoratedTable;
import com.mindliner.img.icons.MlIconLoader;
import com.mindliner.serveraccess.OnlineManager;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import com.mindliner.clientobjects.MlMapNode;

/**
 * This class is the base class for all transfer handlers in Mindliner. There
 * are two object types to transfer: mlcObjec and mlcNode. The handler must be
 * able to export and import both.
 *
 * @author Marius Messerli
 */
public class MindlinerTransferHandler extends TransferHandler {

    public static final String MINDLINER_OBJECT_MIMETYPE = DataFlavor.javaJVMLocalObjectMimeType + ";class=com.mindliner.clientobjects.mlcObject";
    public static final String MINDLINER_MAPNODE_MIMETYPE = DataFlavor.javaJVMLocalObjectMimeType + ";class=com.mindliner.clientobjects.MlMapNode";
    public static final String htmlType = "text/html;class=java.lang.String";
    public static DataFlavor mindlinerObjectLocalFlavor;
    public static DataFlavor mindlinerNodeLocalFlavor;
    public static DataFlavor htmlFlavor;
    
    public MindlinerTransferHandler(String property) {
        super(property);
    }
    
    public MindlinerTransferHandler() {
    }

    public enum LinkMode {

        Link, Unlink
    }
    protected LinkMode linkMode = LinkMode.Link;

    static {
        try {
            mindlinerObjectLocalFlavor = new DataFlavor(MINDLINER_OBJECT_MIMETYPE);
            mindlinerNodeLocalFlavor = new DataFlavor(MINDLINER_MAPNODE_MIMETYPE);
            htmlFlavor = new DataFlavor(htmlType);
        } catch (ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error initializing transfer handler.", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Mindliner supports three kinds of types. Only the MeetingElementTable can
     * move. All other tables link.
     *
     * @param c
     * @return
     */
    @Override
    public int getSourceActions(JComponent c) {
        if (c instanceof DecoratedTable) {
            return LINK;
        }
        // subclasses may need other source actions
        else return super.getSourceActions(c);
    }

    @Override
    public Transferable createTransferable(JComponent c) {
        if (c instanceof DecoratedTable) {
            DecoratedTable table = (DecoratedTable) c;
            List<mlcObject> mbos = table.getMainTable().getSelectedSourceObjects();
            return new MindlinerObjectTransferable(mbos);
        } else {
            return super.createTransferable(c);
        }
    }

    @Override
    public void exportToClipboard(JComponent comp, Clipboard clip, int action) throws IllegalStateException {
        if (comp instanceof DecoratedTable) {
            Transferable t = createTransferable(comp);
            clip.setContents(t, null);
        }
        else {
            super.exportToClipboard(comp, clip, action);
        }
        
    }

    protected void setLinkMode(LinkMode lm) {
        linkMode = lm;
    }

    protected LinkMode getLinkMode() {
        return linkMode;
    }

    /**
     * Support two flavours. If the object is represented by references (same
     * JVM) it is imported directly by type casting. If the content is
     * represented as String an attemp is made to reconstruct the object from
     * the string where the string is expected to comply with the following
     * format: <classname> <id>
     *
     * @param info
     * @return
     */
    @Override
    public boolean importData(TransferHandler.TransferSupport info) {
        if (!info.isDrop()) {
            return false;
        }
        boolean success = false;
        mlcObject targetObject;
        Transferable t = info.getTransferable();
        DecoratedTable targetTable = (DecoratedTable) info.getComponent();
        linkMode = DecoratedTable.getLinkMode((JTable.DropLocation) info.getDropLocation(), targetTable);

        // first choice is intra-JVM transfer (probably intra-application transfer)
        if (info.isDataFlavorSupported(mindlinerObjectLocalFlavor)) {
            try {

                targetObject = targetTable.getMainTable().getSelectedSourceObject();

                List<mlcObject> dropObjects = (List<mlcObject>) t.getTransferData(mindlinerObjectLocalFlavor);
                if (dropObjects.isEmpty()) {
                    return false;
                }
                if (dropObjects.size() == 1) {
                    success = linkTwo(targetObject, dropObjects.get(0));
                } else {
                    linkMany(targetObject, dropObjects);
                }
            } catch (UnsupportedFlavorException | IOException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Drop Data Import", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } // second choice is string based from another application
        else if (!success && info.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                String s = (String) t.getTransferData(DataFlavor.stringFlavor);
                mlcObject dropObject = reconstructObjectFromString(s);
                targetObject = targetTable.getMainTable().getSelectedSourceObject();
                if (dropObject != null) {
                    success = linkTwo(targetObject, dropObject);
                }
            } catch (UnsupportedFlavorException ex) {
                JOptionPane.showMessageDialog(null, "Don't know how to import object. Sorry", "Drop Data Handling", JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Drop Data Handling", JOptionPane.ERROR_MESSAGE);
            }
        }
        return success;
    }

    protected mlcObject reconstructObjectFromString(String s) {
        String[] words = s.split(" ");
        int id;
        try {
            id = Integer.parseInt(words[1]);
            return CacheEngineStatic.getObject(id);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(null, "Dropped object is not a mindliner object. Can't link", "Drop Data Handling", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /**
     * Links the dropped element to all selected items in the target table.
     * @param targetObject
     * @param dropObjects
     */
    protected boolean linkMany(mlcObject targetObject, List<mlcObject> dropObjects) {
        CommandRecorder cr = CommandRecorder.getInstance();
        if (linkMode == LinkMode.Link) {
            for (mlcObject dropObject : dropObjects) {
                cr.scheduleCommand(new LinkCommand(targetObject, dropObject, false));
            }
        } else {
            for (mlcObject dropObject : dropObjects) {
                cr.scheduleCommand(new UnlinkCommand(targetObject, dropObject, false));
            }
        }
        return true;
    }

    protected boolean linkTwo(mlcObject targetObject, mlcObject dropObject) {
        CommandRecorder cr = CommandRecorder.getInstance();
        if (dropObject.equals(targetObject) && linkMode == LinkMode.Link) {
            JOptionPane.showMessageDialog(null, "I can't link an object to itself. Ignoring the request.", "Object Linking", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (linkMode == LinkMode.Link) {
            cr.scheduleCommand(new LinkCommand(targetObject, dropObject, false));
        } else {
            cr.scheduleCommand(new UnlinkCommand(targetObject, dropObject, false));
        }
        return true;
    }

    /**
     * Does the flavor list have a Color flavor?
     * @param flavors
     * @return 
     */
    protected boolean hasMindlinerObjectLocalFlavor(DataFlavor[] flavors) {
        if (mindlinerObjectLocalFlavor == null) {
            return false;
        }

        for (int i = 0; i < flavors.length; i++) {
            if (mindlinerObjectLocalFlavor.equals(flavors[i])) {
                return true;
            }
        }
        return false;
    }

    protected boolean hasStringDataFlavor(DataFlavor[] flavors) {
        for (DataFlavor flavor : flavors) {
            if (DataFlavor.stringFlavor.equals(flavor)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Overridden to include a check for a color flavor.
     * @param info
     * @return 
     */
    @Override
    public boolean canImport(TransferHandler.TransferSupport info) {

        if (info.isDataFlavorSupported(mindlinerObjectLocalFlavor) || info.isDataFlavorSupported(mindlinerNodeLocalFlavor)) {
            return true;
        }
        return false;
    }

    /**
     * This function is actually never called. Various bug reports explain that
     * AWT does not provide underlying functionality for all platforms and
     * things are difficult.
     * @return 
     */
    @Override
    public Icon getVisualRepresentation(Transferable t) {
        try {
            if (t.getTransferData(mindlinerObjectLocalFlavor) instanceof mlcTask) {
                return MlIconLoader.getApplicationIcon();
            }
        } catch (UnsupportedFlavorException | IOException ex) {
            Logger.getLogger(MindlinerTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    protected DataFlavor getMindlinerObjectLocalFlavor() {
        return mindlinerObjectLocalFlavor;
    }

    protected void setMindlinerObjectLocalFlavor(DataFlavor f) {
        mindlinerObjectLocalFlavor = f;
    }

    protected boolean uploadFiles(List<File> droppedFiles, mlcObject target) throws FileNotFoundException {
        CloudConnector connector = CloudConnector.getCurrentConnector();
    
        if (!OnlineManager.isOnline()) {
            // if we are offline, create an upload command that will be executed later when going online
            FileUploadCommand cmd = new FileUploadCommand(droppedFiles, target, connector);
            CommandRecorder.getInstance().scheduleCommand(cmd);
            return true;
        }

        // authentication needs to be done only once
        if (!connector.authenticate()) {
            return true;
        }
        // start asynchronous file uploader
        FileUploadThread uploader = new FileUploadThread(droppedFiles, target, connector);
        uploader.execute();
        return false;
    }
    
    /**
     * Can be used by subclasses to refer to default implementation of TransferHandler. For example
     * for copy&paste support in text fields
     * @param info
     * @return 
     */
    protected boolean importDataOriginal(TransferSupport info) {
        return super.importData(info);
    }
}
