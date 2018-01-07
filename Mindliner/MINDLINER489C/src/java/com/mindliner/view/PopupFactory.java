/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view;

import com.mindliner.analysis.MlClassHandler;
import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.MlClientClassHandler;
import com.mindliner.clientobjects.mlcKnowlet;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcObjectCollection;
import com.mindliner.clientobjects.mlcTask;
import com.mindliner.clientobjects.mlcUser;
import com.mindliner.commands.CommandRecorder;
import com.mindliner.commands.LinkCommand;
import com.mindliner.commands.RelativesOrderedUpdateCommand;
import com.mindliner.commands.TypeChangeCommand;
import com.mindliner.entities.Release;
import com.mindliner.events.SelectionManager;
import com.mindliner.img.icons.MlIconManager;
import com.mindliner.serveraccess.OnlineManager;
import com.mindliner.view.connectors.NodeConnection;
import com.mindliner.view.dispatch.MlObjectViewer;
import com.mindliner.view.dispatch.MlViewDispatcherImpl;
import com.mindliner.weekplanner.TaskPopupBuilder;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import com.mindliner.clientobjects.MlMapNode;
import com.mindliner.clientobjects.MlcImage;
import com.mindliner.commands.bulk.ArchiveBulkUpdateCommand;
import com.mindliner.commands.bulk.BulkUpdateOwnerCommand;
import com.mindliner.gui.CollectionSplitterDialog;
import com.mindliner.gui.ObjectEditorLauncher;
import com.mindliner.image.LazyImage;
import com.mindliner.main.MindlinerMain;

/**
 * Simple factory for link and node popup menus
 *
 * Note: The setAccelerator calls are more or less fake and just so users know
 * what the key is. These would only be active once the menu shows which is not
 * convenient. To make them active on the node without bringing up the context
 * menu events are handled in MapperMouseListener and modifications must be made
 * there too.
 *
 * @author Dominic Plangger
 */
public class PopupFactory {

    public JPopupMenu createNodePopupMenu(final MindlinerMapper mapper) {

        CommandRecorder cr = CommandRecorder.getInstance();
        ResourceBundle mapBundle = ResourceBundle.getBundle("com/mindliner/resources/Mapper");
        JPopupMenu popupMenu = new JPopupMenu();
        JPopupMenu.Separator separator = new JPopupMenu.Separator();
        MlMapNode currentNode = MapNodeStatusManager.getCurrentSelection();

        addObjectEditMenu(mapBundle, popupMenu);
        addMakeRootMenu(mapBundle, popupMenu);

        popupMenu.add(separator);

        addFormatAsTextMenu(mapBundle, popupMenu);
        addCopyUrlToClipboardMenu(mapBundle, popupMenu);
        addSetOwnerMenu(mapBundle, currentNode, popupMenu);
        addToggleIconsMenu(mapBundle, mapper, popupMenu);
        addImageFullscreenMenu(currentNode, mapBundle, popupMenu);
        addFloatChildrenMenu(currentNode, mapBundle, popupMenu);
        addRatingPeakMenu(mapBundle, popupMenu);

        popupMenu.add(separator);

        addDeleteObjectMenu(mapBundle, mapper, popupMenu);
        addUnlinkMenu(mapBundle, mapper, popupMenu);

        popupMenu.add(separator);

        addChangeTypeMenu(mapBundle, mapper, cr, popupMenu);
        addCreateSubCollectionMenu(currentNode, mapBundle, popupMenu);
        addArchiveMenu(mapBundle, cr, popupMenu);
        addTaskMenu(currentNode, popupMenu);

        return popupMenu;
    }

    private void addSetOwnerMenu(ResourceBundle bundle, MlMapNode currentNode, JPopupMenu popupMenu) {
        // add the setOwner menu
        if (currentNode != null && currentNode.getObject().getOwner().equals(CacheEngineStatic.getCurrentUser())) {
            JMenu ownerMenu = createOwnerMenu(bundle);
            popupMenu.add(ownerMenu);
        }
    }

    private void addTaskMenu(MlMapNode currentNode, JPopupMenu popupMenu) {
        if (currentNode != null && currentNode.getObject() instanceof mlcTask) {
            TaskPopupBuilder tpb = new TaskPopupBuilder(true, false);
            tpb.addTaskPopupItems(popupMenu, (mlcTask) currentNode.getObject(), false);
        }
    }

    private void addImageFullscreenMenu(MlMapNode currentNode, ResourceBundle bundle, JPopupMenu popupMenu) {
        if (currentNode.getObject() instanceof MlcImage) {
            final LazyImage limg = CacheEngineStatic.getImageAsync((MlcImage) currentNode.getObject());
            JMenuItem item = new JMenuItem();
            item.setText(bundle.getString("MindMapImageFullscreen"));
            item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, 0));
            item.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/plasma_tv.png")));
            item.addActionListener((ActionEvent e) -> {
                ImageFullscreenViewer.showFullScreen(limg);
            });
            popupMenu.add(item);
        }
    }

    private void addArchiveMenu(ResourceBundle bundle, CommandRecorder cr, JPopupMenu popupMenu) {
        // THE ARCHIVE FUNCTIONALITY
        JMenuItem archive = new JMenuItem();
        MlMapNode currentSelection = MapNodeStatusManager.getCurrentSelection();
        String menuItemText;
        boolean archiveState;
        if (!currentSelection.getObject().isArchived()) {
            menuItemText = bundle.getString("MindMapArchive");
            archiveState = true;
        } else {
            menuItemText = bundle.getString("MindMapDearchive");
            archiveState = false;
        }
        archive.setText(menuItemText);
        archive.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/jar.png")));
        archive.addActionListener((ActionEvent e) -> {
            List<MlMapNode> currentNodes = MapNodeStatusManager.getSelectionList();
            cr.scheduleCommand(new ArchiveBulkUpdateCommand(MlMapNodeUtils.getObjects(currentNodes), archiveState));
        });
        popupMenu.add(archive);
    }

    private void addChangeTypeMenu(ResourceBundle bundle, final MindlinerMapper mapper, CommandRecorder cr, JPopupMenu popupMenu) {
        JMenu type = new JMenu();
        type.setText(bundle.getString("MindMapPopupChangeType"));
        ButtonGroup group = new ButtonGroup();
        List<Class> objectTypes = new ArrayList<>();
        objectTypes.add(mlcKnowlet.class);
        objectTypes.add(mlcObjectCollection.class);
        objectTypes.add(mlcTask.class);
        mlcUser user = CacheEngineStatic.getCurrentUser();

        for (final Class c : objectTypes) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem();
            item.addActionListener((ActionEvent e) -> {
                // do nothing if the same type is selected
                MlMapNode n = MapNodeStatusManager.getCurrentSelection();
                if (n.getObject().getClass().equals(c)) {
                    return;
                }
                // initiate TypeChangeCommand
                mapper.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    TypeChangeCommand cmd = new TypeChangeCommand(n.getObject(), c);
                    if (!OnlineManager.waitForServerMessages()) {
                        // update Mindmap
                        cmd.executeAsync();
                    }
                    cr.scheduleCommand(cmd);
                } finally {
                    mapper.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            });
            setIconAndText(bundle, item, c);
            // select the current type of the object
            MlMapNode currentSelection = MapNodeStatusManager.getCurrentSelection();
            if (currentSelection.getObject().getClass().equals(c)) {
                item.setSelected(true);
            }
            group.add(item);
            type.add(item);
        }

        popupMenu.add(type);
    }

    private void addUnlinkMenu(ResourceBundle bundle, final MindlinerMapper mapper, JPopupMenu popupMenu) {
        JMenuItem unlink = new JMenuItem();
        unlink.setText(bundle.getString("MindMapPopupUnlink"));
        unlink.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_U, 0));
        unlink.addActionListener((ActionEvent e) -> {
            MlMapNode n = MapNodeStatusManager.getCurrentSelection();
            mapper.unlinkNodes(n, n.getParentNode());
        });
        popupMenu.add(unlink);
    }

    private void addDeleteObjectMenu(ResourceBundle bundle, final MindlinerMapper mapper, JPopupMenu popupMenu) {
        JMenuItem deleteObjectMenuItem = new JMenuItem();

        deleteObjectMenuItem.setText(bundle.getString("MindMapPopupDeleteOne"));
        deleteObjectMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0));
        deleteObjectMenuItem.setIcon(
                new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/sign_warning.png")));
        deleteObjectMenuItem.addActionListener((ActionEvent e) -> {
            int rep = JOptionPane.showConfirmDialog(MindlinerMain.getInstance(), "Are you sure you want to delete selected item(s)?", "Deletion", JOptionPane.YES_NO_OPTION);
            if (rep == JOptionPane.YES_OPTION) {
                List<MlMapNode> currentNodes = MapNodeStatusManager.getSelectionList();
                mapper.deleteNodeAndObject(currentNodes);
            }
        });

        popupMenu.add(deleteObjectMenuItem);
    }

    private void addCreateSubCollectionMenu(MlMapNode currentNode, ResourceBundle bundle, JPopupMenu popupMenu) {
        if (OnlineManager.isOnline() && currentNode.getObject() instanceof mlcObjectCollection) {
            JMenuItem subCollectionMenu = new JMenuItem();
            subCollectionMenu.setText(bundle.getString("MindMapPopupBuildSubCollections"));
            subCollectionMenu.setToolTipText("MindMapPopupBuildSubCollections_TT");
            subCollectionMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/text_align_left.png")));
            subCollectionMenu.addActionListener((ActionEvent e) -> {
                CollectionSplitterDialog csd = new CollectionSplitterDialog(null, true);
                csd.setMaxChildCount(5);
                csd.setRootCollection((mlcObjectCollection) currentNode.getObject());
                csd.setVisible(true);
            });
            popupMenu.add(subCollectionMenu);
        }
    }

    private void addFloatChildrenMenu(MlMapNode currentNode, ResourceBundle bundle, JPopupMenu popupMenu) {
        // add menu item to un-order (float) the child nodes
        if (currentNode != null && currentNode.getObject().isRelativesOrdered()) {
            JMenuItem floatChildren = new JMenuItem();
            floatChildren.setText(bundle.getString("MindMapPopupFloatRelatives"));
            floatChildren.setToolTipText("MindMapPopupUnsortRelatives_TT");
            floatChildren.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/bullet_ball_glass_grey.png")));
            floatChildren.addActionListener(new RelativesOrderedActionListener(currentNode));
            popupMenu.add(floatChildren);
        }
    }

    private void addToggleIconsMenu(ResourceBundle bundle, final MindlinerMapper mapper, JPopupMenu popupMenu) {
        JMenuItem toggleItem = new JMenuItem();
        toggleItem.setText(bundle.getString("MindMapPopupToggleIcon"));
        toggleItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, 0));
        toggleItem.addActionListener((ActionEvent e) -> {
            MlMapNode n = MapNodeStatusManager.getCurrentSelection();
            if (n != null) {
                IconGridDialog igd = new IconGridDialog(mapper, n);
                igd.setVisible(true);
            }
        });
        popupMenu.add(toggleItem);
    }

    private void addCopyUrlToClipboardMenu(ResourceBundle bundle, JPopupMenu popupMenu) {
        JMenuItem copyUrlItem = new JMenuItem();
        copyUrlItem.setText(bundle.getString("MindMapPopupCopyLink"));
        copyUrlItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/link.png")));
        copyUrlItem.addActionListener((ActionEvent e) -> {
            MlMapNode cs = MapNodeStatusManager.getCurrentSelection();
            String objectViewerString = Release.WEBAPP_OBJECT_VIEWER_URL + "?id=" + cs.getObject().getId();
            StringSelection stringSelection = new StringSelection(objectViewerString);
            Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
            clpbrd.setContents(stringSelection, null);
        });
        popupMenu.add(copyUrlItem);
    }

    private void addFormatAsTextMenu(ResourceBundle bundle, JPopupMenu popupMenu) {
        // The copy to clipboard
        JMenuItem textFormatterItem = new JMenuItem();
        textFormatterItem.setText(bundle.getString("MindMapPopupCopy"));
        textFormatterItem.addActionListener((ActionEvent e) -> {
            MlMapNode cs = MapNodeStatusManager.getCurrentSelection();
            MapClipboardDialog clipDialog = new MapClipboardDialog(null, true, cs);
            clipDialog.setVisible(true);
        });
        popupMenu.add(textFormatterItem);
    }

    private void addRatingPeakMenu(ResourceBundle bundle, JPopupMenu popupMenu) {
        // FIND THE CURRENT SELECTION'S RATING PEAK
        JMenuItem findRatingPeak = new JMenuItem();
        findRatingPeak.setText(bundle.getString("MindMapPopupFindRatingPeak"));
        findRatingPeak.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, 0));
        findRatingPeak.addActionListener((ActionEvent e) -> {
            mlcObject selection = SelectionManager.getLastSelection();
            if (selection != null) {
                mlcObject ratingPeak = CacheEngineStatic.getFamilyRatingPeak(selection);
                if (ratingPeak != null) {
                    MlViewDispatcherImpl.getInstance().display(ratingPeak, MlObjectViewer.ViewType.Map);
                }
            }
        });
        popupMenu.add(findRatingPeak);
    }

    public JPopupMenu createLinkPopupMenu(ResourceBundle bundle, final MindlinerMapper mapper) {
        JPopupMenu jPopup = new JPopupMenu();
        JMenuItem delete = new JMenuItem();
        delete.setText(bundle.getString("MindMapPopupDelete"));
        delete.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0));
        delete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/sign_warning.png")));
        delete.addActionListener((ActionEvent e) -> {
            List<NodeConnection> sel = SelectionManager.getConnectionSelection();
            NodeConnection lastSel = sel.get(sel.size() - 1);
            MlMapNode holder = lastSel.getHolder();
            MlMapNode relative = lastSel.getRelative();
            mapper.unlinkNodes(relative, holder);
        });
        jPopup.add(delete);

        JPopupMenu.Separator separator = new JPopupMenu.Separator();
        jPopup.add(separator);

        boolean isOneWay = false;
        List<NodeConnection> sel = SelectionManager.getConnectionSelection();
        if (!sel.isEmpty()) {
            NodeConnection lastSel = sel.get(sel.size() - 1);
            isOneWay = lastSel.getLink().isIsOneWay();
        }
        final boolean isOneWayF = isOneWay;

        JMenu direction = new JMenu();
        direction.setText(bundle.getString("MindMapPopupDirection"));
        direction.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/documents_exchange.png")));

        ButtonGroup group = new ButtonGroup();
        final JRadioButtonMenuItem both = new JRadioButtonMenuItem();
        both.setText(bundle.getString("MindMapPopupBoth"));
        both.addActionListener((ActionEvent e) -> {
            if (!isOneWayF) {
                return;
            }
            mapper.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            SelectionManager.getConnectionSelection().stream().forEach((nc) -> {
                MlMapNode holder = nc.getHolder();
                MlMapNode relative = nc.getRelative();
                CommandRecorder cr = CommandRecorder.getInstance();
                cr.scheduleCommand(new LinkCommand(relative.getObject(), holder.getObject(), true));
            });
            mapper.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            mapper.repaint();
        });
        group.add(both);
        direction.add(both);

        final JRadioButtonMenuItem parent2Child = new JRadioButtonMenuItem();
        parent2Child.setText(bundle.getString("MindMapPopupParentChild"));
        parent2Child.addActionListener((ActionEvent e) -> {
            if (isOneWayF) {
                return;
            }
            mapper.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            SelectionManager.getConnectionSelection().stream().forEach((nc) -> {
                MlMapNode parent = nc.getHolder();
                MlMapNode child = nc.getRelative();
                mapper.unlinkNodesOneWay(child, parent);
            });
            mapper.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            mapper.repaint();
        });
        group.add(parent2Child);
        direction.add(parent2Child);

        final JRadioButtonMenuItem child2parent = new JRadioButtonMenuItem();
        child2parent.setText(bundle.getString("MindMapPopupChildParent"));
        child2parent.addActionListener((ActionEvent e) -> {
            mapper.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            SelectionManager.getConnectionSelection().stream().forEach((nc) -> {
                MlMapNode parent = nc.getHolder();
                MlMapNode child = nc.getRelative();
                mapper.unlinkNodesOneWay(parent, child);
            });
            mapper.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            mapper.repaint();
        });
        group.add(child2parent);
        direction.add(child2parent);

        if (isOneWay) {
            parent2Child.setSelected(true);
        } else {
            both.setSelected(true);
        }
        jPopup.add(direction);
        return jPopup;
    }

    public JMenu createOwnerMenu(ResourceBundle bundle) {
        JMenu setOwnerMenu = new JMenu();
        setOwnerMenu.setText(bundle.getString("MindMapPopupOwnerMenu"));
        setOwnerMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/users2.png")));
        List<MlMapNode> selection = MapNodeStatusManager.getSelectionList();
        if (!selection.isEmpty()) {
            CacheEngineStatic.getUsers().stream()
                    .filter((u) -> (u.isActive() && u.getClientIds().contains(selection.get(0).getObject().getClient().getId())))
                    .map((u) -> new UserMenuItem(u)).map((umi) -> {
                umi.addActionListener((ActionEvent e) -> {
                    UserMenuItem actionItem = (UserMenuItem) e.getSource();
                    BulkUpdateOwnerCommand soc = new BulkUpdateOwnerCommand(MlMapNodeUtils.getObjects(selection), actionItem.user);
                    CommandRecorder.getInstance().scheduleCommand(soc);
                });
                return umi;
            }).forEach((umi) -> {
                setOwnerMenu.add(umi);
            });
        }
        return setOwnerMenu;
    }

    /**
     * MAKE THE CURRENT SELECTION THE NEW ROOT NODE
     *
     * @param bundle The resource bundle that has the menu item name
     * @param popupMenu The menu to add to
     */
    private void addMakeRootMenu(ResourceBundle bundle, JPopupMenu popupMenu) {
        JMenuItem makeRoot = new JMenuItem();
        makeRoot.setText(bundle.getString("MindMapPopupMakeRoot"));
        makeRoot.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, 0));
        makeRoot.addActionListener((ActionEvent e) -> {
            MlViewDispatcherImpl.getInstance().display(SelectionManager.getSelection(), MlObjectViewer.ViewType.Map);
        });
        popupMenu.add(makeRoot);
    }

    /**
     * THE EDIT MENU ITEM
     */
    private void addObjectEditMenu(ResourceBundle bundle, JPopupMenu popupMenu) {
        JMenuItem edit = new JMenuItem();
        edit.setText(bundle.getString("MindMapPopupEdit"));
        edit.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, 0));
        edit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/edit-3.png")));
        edit.addActionListener((ActionEvent e) -> {
            MlMapNode n = MapNodeStatusManager.getCurrentSelection();
            ObjectEditorLauncher.showEditor(MlMapNodeUtils.getObjects(MapNodeStatusManager.getSelectionList()));
        });
        popupMenu.add(edit);
    }

    private void setIconAndText(ResourceBundle bundle, JRadioButtonMenuItem item, Class c) {
        item.setToolTipText(bundle.getString("MindMapAttributeInheritance"));
        MlClassHandler.MindlinerObjectType type = MlClientClassHandler.getTypeByClass(c);
        String text = MlClientClassHandler.getNameByType(type);
        item.setText(text);
        ImageIcon ic = MlIconManager.getIconForType(type);
        item.setIcon(ic);
    }

    class UserMenuItem extends JMenuItem {

        mlcUser user;

        public UserMenuItem(mlcUser user) {
            this.user = user;
            setText(user.toString());
        }
    }

    class RelativesOrderedActionListener implements ActionListener {

        private final MlMapNode node;

        public RelativesOrderedActionListener(MlMapNode node) {
            this.node = node;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            CommandRecorder cr = CommandRecorder.getInstance();
            cr.scheduleCommand(new RelativesOrderedUpdateCommand(node.getObject(), false));
        }

    }

}
