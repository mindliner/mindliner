/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.interaction;

import com.mindliner.analysis.UriUtils;
import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.MlcLink;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.commands.CommandRecorder;
import com.mindliner.commands.LinkCommand;
import com.mindliner.commands.UnlinkCommand;
import com.mindliner.events.SelectionManager;
import com.mindliner.gui.UriChooserDialog;
import com.mindliner.gui.UriChooserPanel;
import com.mindliner.view.MapNodeStatusManager;
import com.mindliner.view.MindlinerMapper;
import com.mindliner.view.PopupFactory;
import com.mindliner.view.connectors.NodeConnection;
import com.mindliner.view.positioner.MindmapPositioner;
import com.mindliner.view.positioner.NodePositionYComparator;
import java.awt.Dialog;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import com.mindliner.clientobjects.MlMapNode;
import java.util.ResourceBundle;

/**
 *
 * @author Marius Messerli Created on 23.09.2012, 22:25:42
 */
public class MapperMouseListener implements MouseListener, MouseMotionListener {

    private final MindlinerMapper mapper;
    private final PopupFactory popupFactory;
    private static final Pattern urlPattern = Pattern.compile(
            "\\b((?:https?|ftp|file):/{1,2}[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])", Pattern.CASE_INSENSITIVE);

    public MapperMouseListener(MindlinerMapper mapper) {
        this.mapper = mapper;
        this.popupFactory = new PopupFactory();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        mapper.requestFocus();
        switch (e.getButton()) {
            case MouseEvent.BUTTON1:
                switch (e.getClickCount()) {
                    case 1:
                        MlMapNode n = mapper.getPickIdentifyer().identifyObject(e.getPoint(), null);
                        if (n != null) {
                            SelectionManager.clearConnectionSelection();

                            if (e.isControlDown()) {
                                if (!e.isPopupTrigger()) {
                                    /**
                                     * if we are working on the Mac with a
                                     * single button mouse then we don't want to
                                     * change the selection but simple bring up
                                     * the context menu
                                     */
                                    handleCtrlSelection(n);
                                }
                            } else if (e.isShiftDown()) {
                                handleShiftSelection(n);
                            } else {
                                MapNodeStatusManager.setCurrentSelection(n);
                            }
                        } else {
                            MlMapNode cs = MapNodeStatusManager.getCurrentSelection();
                            if (cs != null) {
                                mapper.repaintSingleNode(cs);
                            }
                            SelectionManager.clearSelection();

                            NodeConnection conn = mapper.getPickIdentifyer().identifyConnection(e.getPoint());
                            if (conn != null) {
                                handleConnectionSelection(e, conn);
                            } else {
                                SelectionManager.clearConnectionSelection();
                            }
                        }
                        mapper.repaint();
                        break;

                    case 2:
                        MlMapNode node = MapNodeStatusManager.getCurrentSelection();
                        if (node != null) {
                            if (node.isExpanded()) {
                                node.setExpanded(false);
                                mapper.repaint();
                            } else {
                                mapper.addMissingNodes(node);
                            }
                        }
                        break;
                }
                break;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // popup trigger is also checked in mouseRelease as this event is handled differently on different OS platforms
        if (e.isPopupTrigger()) {
            showPopup(e);
        }
    }

    private void showPopup(MouseEvent e) {
        MlMapNode n = mapper.getPickIdentifyer().identifyObject(e.getPoint(), null);
        if (n != null) {
            // show context menu only when clicking on a node
            // and keep selection if the node is one of the selection list
            if (!MapNodeStatusManager.getSelectionList().contains(n)) {
                MapNodeStatusManager.setCurrentSelection(n);
                mapper.repaint();
            }
            JPopupMenu nodePopup = popupFactory.createNodePopupMenu(mapper);
            nodePopup.show(e.getComponent(), e.getX(), e.getY());
        } else {
            NodeConnection conn = mapper.getPickIdentifyer().identifyConnection(e.getPoint());
            if (conn != null) {
                if (!SelectionManager.getConnectionSelection().contains(conn)) {
                    if (!MapNodeStatusManager.getSelectionList().isEmpty()) {
                        SelectionManager.clearSelection();
                    }
                    SelectionManager.setConnectionSelection(conn);
                    mapper.repaint();
                }
                ResourceBundle mapBundle = ResourceBundle.getBundle("com/mindliner/resources/Mapper");
                JPopupMenu linkPopup = popupFactory.createLinkPopupMenu(mapBundle, mapper);
                linkPopup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showPopup(e);
        } else if (mapper.getZoomAndPanListener().isDragging()) {
            List<MlMapNode> currentSelection = MapNodeStatusManager.getSelectionList();

            // now that the interaction ends clear the custom position flag
            for (MlMapNode n : currentSelection) {
                n.setCustomPositioning(false);
            }
            if (MapNodeStatusManager.getCandidateTargetNode() != null && !currentSelection.isEmpty()) {
                for (MlMapNode dropNode : currentSelection) {
                    if (!dropNode.equals(MapNodeStatusManager.getCandidateTargetNode())) {

                        /**
                         * Check if the sibling has just been moved to another
                         * position, add corresponding functionality to the pick
                         * identifyer and return the node and the click position
                         * within the node
                         */
                        if (e.isAltDown() && dropNode.getParentNode() != null
                                && MapNodeStatusManager.getCandidateTargetNode().getParentNode() != null
                                && dropNode.getParentNode().equals(MapNodeStatusManager.getCandidateTargetNode().getParentNode())) {

                            MlMapNode candidateTargetNode = MapNodeStatusManager.getCandidateTargetNode();
                            MlcLink link = CacheEngineStatic.getLink(dropNode.getParentNode().getObject().getId(), candidateTargetNode.getObject().getId());
                            if (link != null) {
                                MindmapPositioner.moveSibling(dropNode.getParentNode(), dropNode, link.getRelativeListPosition());
                                break; // get out of the loop as only one object can be moved at a time
                            } else {
                                JOptionPane.showMessageDialog(null, "Sorry, cannot find link to drop target", "Internal Software Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            /**
                             * We create new node for dropped object.
                             * Alternative would be to re-wire the existing node
                             * but this way its safer that object and node
                             * wiring is identical even if the transaction falls
                             * over.
                             */
                            MlMapNode newNode = mapper.getNodeBuilder().wrapObject(dropNode.getObject());
                            linkMindlinerObjects(MapNodeStatusManager.getCandidateTargetNode().getObject(), newNode.getObject());
                            MapNodeStatusManager.getCandidateTargetNode().addChild(newNode);
                            newNode.setParentNode(MapNodeStatusManager.getCandidateTargetNode());
                            MapNodeStatusManager.getCandidateTargetNode().setExpanded(true);
                            if (!e.isShiftDown()) {
                                if (dropNode.getParentNode() != null) {
                                    CommandRecorder cr = CommandRecorder.getInstance();
                                    cr.scheduleCommand(new UnlinkCommand(dropNode.getParentNode().getObject(), dropNode.getObject(), false));
                                }
                                mapper.deleteNode(dropNode, false);
                            }
                        }
                    }
                }
            }
            MapNodeStatusManager.setCandidateTargetNode(null);
            mapper.getPositioner().arrangePositions();
            mapper.repaint();
        } else if (e.isAltDown()) {
            // open any available links of the description
            MlMapNode n = mapper.getPickIdentifyer().identifyObject(e.getPoint(), null);
            if (n != null) {
                String descr = n.getObject().getDescription();
                Matcher matcher = urlPattern.matcher(descr);
                List<URI> uris = new ArrayList<>();
                while (matcher.find()) {
                    try {
                        int matchStart = matcher.start();
                        int matchEnd = matcher.end();
                        String subs = descr.substring(matchStart, matchEnd);
                        URI uri = new URI(subs);
                        uris.add(uri);
                    } catch (URISyntaxException ex) {
                        Logger.getLogger(MapperMouseListener.class.getName()).log(Level.INFO, "Parsed url could not be converted into URL object", ex);
                    }
                }
                if (uris.size() == 1) {
                    UriUtils.openUri(uris.get(0));
                } else if (uris.size() > 1) {
                    UriChooserPanel panel = new UriChooserPanel();
                    UriChooserDialog dialog = new UriChooserDialog(panel);
                    dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                    panel.setDialog(dialog);
                    panel.setUris(uris);
                    dialog.setVisible(true);
                }
            }
        }
        mapper.getZoomAndPanListener().setDragging(false, null);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (mapper.getZoomAndPanListener().isDragging()) {
            try {
                MapNodeStatusManager.setCandidateTargetNode(
                        mapper.getPickIdentifyer().identifyObject(e.getPoint(),
                                MapNodeStatusManager.getSelectionList()));

                Point currentLocation = e.getPoint();
                Point2D location = mapper.getZoomAndPanListener().getInverseTransform(currentLocation);
                List<MlMapNode> selectionList = MapNodeStatusManager.getSelectionList();
                if (selectionList != null && !selectionList.isEmpty()) {
                    MlMapNode node = selectionList.get(0);
                    // de-activate position caching so that translations become visible
                    if (mapper.getPositioner() instanceof MindmapPositioner) {
                        Point2D position = node.getPosition();
                        double currentX = position.getX();
                        double currentY = position.getY();
                        MindmapPositioner mmp = (MindmapPositioner) mapper.getPositioner();
                        mmp.translateFamily(node, (int) location.getX() - currentX, (int) location.getY() - currentY);
                        if (node.getParentNode() != null) {
                            boolean left = node.getParentNode().getPosition().getX() > node.getPosition().getX();
                            mmp.arrangeNodeFamilyPosition(node, left);
                            node.setCustomPositioning(true);
                        }
                    } else {
                        node.setPosition(location);
                    }
                    mapper.repaint();
                }
            } catch (NoninvertibleTransformException ex) {
                Logger.getLogger(MapperMouseListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    private void linkMindlinerObjects(mlcObject o1, mlcObject o2) {
        CommandRecorder cr = CommandRecorder.getInstance();
        cr.scheduleCommand(new LinkCommand(o1, o2, false));
    }

    private void handleShiftSelection(MlMapNode n) {
        MlMapNode old = MapNodeStatusManager.getCurrentSelection();

        // if the new selection is not a sibling of the old selection, no shift selection possible
        if (old == null || old.getParentNode() == null || !old.getParentNode().getChildren().contains(n)) {
            MapNodeStatusManager.setCurrentSelection(n);
            return;
        }

        List<MlMapNode> allSiblings = n.getParentNode().getChildren();
        List<MlMapNode> copy = new ArrayList<>(allSiblings);
        Collections.sort(copy, new NodePositionYComparator());
        int oldInd = copy.indexOf(old);
        int newInd = copy.indexOf(n);
        for (int i = Math.min(oldInd, newInd); i <= Math.max(oldInd, newInd); i++) {
            MlMapNode next = copy.get(i);
            if (!MapNodeStatusManager.isSelected(next)) {
                MapNodeStatusManager.addToCurrentSelection(next);
            }
        }
    }

    private void handleCtrlSelection(MlMapNode n) {
        if (MapNodeStatusManager.isSelected(n)) {
            MapNodeStatusManager.removeFromCurrentSelection(n);
        } else {
            MapNodeStatusManager.addToCurrentSelection(n);
        }
    }

    private void handleConnectionSelection(MouseEvent e, NodeConnection conn) {
        if (e.isControlDown()) {
            if (SelectionManager.isConnectionSelected(conn)) {
                SelectionManager.removeFromConnSelection(conn);
            } else {
                SelectionManager.addToConnectionSelection(conn);
            }
        } else {
            List<NodeConnection> newSel = new ArrayList<>();
            newSel.add(conn);
            SelectionManager.setConnectionSelection(newSel);
        }
    }

}
