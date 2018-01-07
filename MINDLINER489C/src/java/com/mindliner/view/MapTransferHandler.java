/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 *//*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.mindliner.view;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.events.ObjectChangeManager;
import com.mindliner.exporter.MindlinerObjectTransferable;
import com.mindliner.exporter.MindlinerTransferHandler;
import static com.mindliner.exporter.MindlinerTransferHandler.mindlinerObjectLocalFlavor;
import com.mindliner.image.IconLoader;
import com.mindliner.managers.SearchManagerRemote;
import com.mindliner.serveraccess.OnlineManager;
import com.mindliner.serveraccess.RemoteLookupAgent;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import com.mindliner.clientobjects.MlMapNode;

/**
 *
 * @author M.Messerli
 */
public class MapTransferHandler extends MindlinerTransferHandler {

    private final MindlinerMapper mindlinerMapper;

    public MapTransferHandler(MindlinerMapper managedPanel) {
        this.mindlinerMapper = managedPanel;
    }

    @Override
    public Transferable createTransferable(JComponent c) {
        if (c instanceof MindlinerMapper) {
            MlMapNode currentNodeSelection = MapNodeStatusManager.getCurrentSelection();
            if (currentNodeSelection == null) {
                return null;
            }
            return new MindlinerObjectTransferable(currentNodeSelection);
        } else {
            System.err.println("Warning: createTransferrable called from unhandled component");
            return null;
        }
    }

    @Override
    public boolean canImport(TransferSupport info) {
        return super.canImport(info) || info.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    }

    @Override
    public boolean importData(TransferSupport info) {
        if (!info.isDrop()) {
            return false;
        }
        boolean success = false;
        Transferable t = info.getTransferable();
        MindlinerMapper mapPanel = (MindlinerMapper) info.getComponent();
        DropLocation dropLocation = info.getDropLocation();
        MlMapNode targetNode = null;
        if (mapPanel != null && dropLocation != null) {
            targetNode = mapPanel.getNodeAtScreenLocation(dropLocation.getDropPoint());
        }

        // first choice is intra-JVM transfer (probably intra-application transfer)
        if (info.isDataFlavorSupported(mindlinerObjectLocalFlavor)) {
            try {
                List<mlcObject> dropObjects = (List<mlcObject>) t.getTransferData(mindlinerObjectLocalFlavor);
                if (dropObjects.isEmpty()) {
                    return false;
                }

                if (targetNode == null) {
                    // if objects are not dropped onto a node, then create standalone nodes for them
                    createRootNodes(dropObjects);
                    // load object icons.
                    // not needed when the dropped objects will be linked to an existing node as this will
                    // trigger an OBJECT_CHANGED event which will in return initiate icon loading
                    loadObjectIcons(dropObjects);
                    return true;
                }

                if (dropObjects.size() == 1) {
                    success = linkTwo(targetNode.getObject(), dropObjects.get(0));
                } else {
                    linkMany(targetNode.getObject(), dropObjects);
                }
                mindlinerMapper.addMissingNodes(targetNode);
            } catch (UnsupportedFlavorException | IOException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Drop Data Import", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else if (info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            try {
                List<File> droppedFiles = (List<File>) info.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                if (droppedFiles == null || droppedFiles.isEmpty()) {
                    return false;
                }
                if (uploadFiles(droppedFiles, targetNode == null ? null : targetNode.getObject())) {
                    return false;
                }
            } catch (UnsupportedFlavorException | IOException ex) {
                Logger.getLogger(MapTransferHandler.class.getName()).log(Level.SEVERE, "File drop error", ex);
            }
        }
        return success;
    }

    private void createRootNodes(List<mlcObject> dropObjects) {
        // collect leaves before adding drop object
        Set<Integer> leaves = new HashSet();
        Set<Integer> visited = new HashSet();
        for (MlMapNode n : mindlinerMapper.getNodes()) {
            leaves.addAll(getLeaveNodes(n, visited));
        }

        for (mlcObject o : dropObjects) {
            if (mindlinerMapper.getNodes().isEmpty()) {
                // first object on map needs to be handled by Mindliner2DViewer
                ObjectChangeManager.objectCreated(o);
            } else {
                boolean crossLinked = false;
                if (dropObjects.size() == 1 
                        && mindlinerMapper.isShortestPathEnabled()
                        && OnlineManager.isOnline()) {
                    crossLinked = findAndDdisplayShortestPath(leaves, o);
                }
                if (!crossLinked) {
                    MlMapNode n = mindlinerMapper.getNodeBuilder().wrapObject(o);
                    mindlinerMapper.getNodes().add(n);
                }
            }
        }
        mindlinerMapper.getPositioner().arrangePositions();
        mindlinerMapper.repaint();
    }

    private boolean findAndDdisplayShortestPath(Set<Integer> leaves, mlcObject dropObject) {
        MlMapNode existing = MlMapNodeUtils.findNodeForObject(TreeLinearizer.linearizeAllTrees(mindlinerMapper.getNodes(), true), dropObject.getId());
        if (existing != null) {
            MapNodeStatusManager.setCurrentSelection(existing);
            return true;
        }
        SearchManagerRemote searchManager;
        try {
            searchManager = (SearchManagerRemote) RemoteLookupAgent.getManagerForClass(SearchManagerRemote.class);
        } catch (NamingException ex) {
            Logger.getLogger(MapTransferHandler.class.getName()).log(Level.SEVERE, "Failed to access SearchManager for finding shortest path", ex);
            return false;
        }
        // Get shortest path from server
        int maxLen = mindlinerMapper.getMaxShortestPath();
        List<Integer> shortestPath = searchManager.getShortestPath(leaves, dropObject.getId(), maxLen);
        if (shortestPath == null || shortestPath.isEmpty()) {
            return false;
        }

        // Look for start node from which the shortest path to the dropObject starts
        MlMapNode startNode = MlMapNodeUtils.findNodeForObject(TreeLinearizer.linearizeAllTrees(mindlinerMapper.getNodes(), true), shortestPath.get(0));

        if (startNode == null) { // should not happen
            Logger.getLogger(MapTransferHandler.class.getName()).log(Level.SEVERE, "Server suggested start node for shortest path that does not exist.");
            return false;
        }

        // Go through shortest Path withouth displaying anything. This is to make sure the 
        // shortest path does exist. Maybe there are search restrictions that prevent objects
        // on the shortest path to be displayed. @todo: in this case, ask server for another shortestPath and try again
        boolean unreachablePath = false;
        mlcObject next = startNode.getObject();
        for (int i = 1; i < shortestPath.size(); i++) {
            List<mlcObject> relatives = CacheEngineStatic.getLinkedObjects(next);
            Integer id = shortestPath.get(i);
            next = null;
            for (mlcObject child : relatives) {
                if (child.getId() == id) {
                    next = child;
                    break;
                }
            }
            if (next == null) {
                unreachablePath = true;
                break;
            }
        }
        if (!unreachablePath) {
            // The shortest path can be displayed, now create and expand the new nodes
            return displayNodes(shortestPath, startNode, dropObject.getId());
        } else {
            JOptionPane.showMessageDialog(null, "Server suggested shortest path that cannot be reconstructed. Most possibly search restrictions prevent the display.", "Shortest Path", JOptionPane.INFORMATION_MESSAGE);
        }
        return false;
    }

    private boolean displayNodes(List<Integer> shortestPath, MlMapNode startNode, int dropId) {
        mindlinerMapper.addMissingNodes(startNode);
        mindlinerMapper.repaint();
        shortestPath.remove(0);
        for (Integer id : shortestPath) {
            boolean found = false;
            for (MlMapNode child : startNode.getChildren()) {
                if (child.getObject().getId() == id) {
                    startNode = child;
                    found = true;
                    break;
                }
            }
            if (!found) {
                // should not happen as we checked before (see unreachablePath variable)
                Logger.getLogger(MapTransferHandler.class.getName()).log(Level.SEVERE, "Failed to display nodes for shortest path. The expected child node with object id [{0}] does not exist", id);
                return false;
            }
            mindlinerMapper.addMissingNodes(startNode);
            mindlinerMapper.repaint();
        }

        // Select dropped node
        for (MlMapNode child : startNode.getChildren()) {
            if (child.getObject().getId() == dropId) {
                MapNodeStatusManager.setCurrentSelection(child);
                return true;
            }
        }

        return false;
    }

    private Set<Integer> getLeaveNodes(MlMapNode current, Set<Integer> visited) {
        Set<Integer> result = new HashSet();
        if (visited.contains(current.getObject().getId())) {
            return result;
        } else {
            visited.add(current.getObject().getId());
        }
        if (current.getChildren() != null && !current.getChildren().isEmpty() && current.isExpanded()) {
            for (MlMapNode child : current.getChildren()) {
                Set<Integer> ret = getLeaveNodes(child, visited);
                result.addAll(ret);
            }
        } else {
            result.add(current.getObject().getId());
        }
        return result;
    }

    private void loadObjectIcons(List<mlcObject> dropObjects) {
        Set<mlcObject> objs = new HashSet<>();
        for (mlcObject o : dropObjects) {
            if (o.getIcons() == null) {
                objs.add(o);
            }
        }
        if (!objs.isEmpty()) {
            IconLoader.getInstance().loadIcons(objs);
        }
    }

}
