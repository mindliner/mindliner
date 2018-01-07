/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.interaction;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.MlcImage;
import com.mindliner.clientobjects.mlcKnowlet;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clipboard.ClipboardObjectCreator;
import com.mindliner.clipboard.ClipboardParser;
import com.mindliner.clipboard.TextUnit;
import com.mindliner.commands.CommandRecorder;
import com.mindliner.commands.ImageUpdateCommand;
import com.mindliner.commands.ObjectCreationCommand;
import com.mindliner.commands.TextUpdateCommand;
import com.mindliner.entities.MlsImage;
import com.mindliner.events.SelectionManager;
import com.mindliner.gui.ClipboardObjectsDialog;
import com.mindliner.gui.ClipboardObjectsPanel;
import com.mindliner.main.MindlinerMain;
import com.mindliner.thread.SimpleSwingWorker;
import com.mindliner.view.IconGridDialog;
import com.mindliner.view.MapNodeStatusManager;
import com.mindliner.view.MindlinerMapper;
import com.mindliner.view.connectors.NodeConnection;
import com.mindliner.view.dispatch.MlObjectViewer;
import com.mindliner.view.dispatch.MlViewDispatcherImpl;
import com.mindliner.view.positioner.MindmapPositioner;
import com.mindliner.view.positioner.NodePositionXComparator;
import com.mindliner.view.positioner.NodePositionYComparator;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JOptionPane;
import com.mindliner.clientobjects.MlMapNode;
import com.mindliner.gui.ObjectEditorLauncher;
import com.mindliner.image.LazyImage;
import com.mindliner.view.MlMapNodeUtils;
import com.mindliner.view.ImageFullscreenViewer;

/**
 * This class handles the input keys for the mapper viewer. The viewer is
 * largely controlled by the mouse and teh keyboard, so this class handles more
 * than half of the interactions with the view.
 *
 * @author M.Messerli Created on 23.09.2012, 22:07:25
 */
public class MapperKeyListener extends KeyAdapter {

    MindlinerMapper mapper;

    private enum MapDirection {

        West, East, North, South
    }

    private boolean isLeftNode(MlMapNode node) {
        if (node.getParentNode() == null) {
            return false;
        }
        return node.getParentNode().getPosition().getX() > node.getPosition().getX();
    }

    private void selectAdjacentSibling(MlMapNode node, MapDirection direction) {
        if (node.getParentNode() != null && node.getParentNode().getChildren().size() > 1) {
            MlMapNode parent = node.getParentNode();
            List<MlMapNode> copy = new ArrayList<>();
            copy.addAll(parent.getChildren()); // leave original sequence untouched
            switch (direction) {
                case North:
                    Collections.sort(copy, new NodePositionYComparator());
                    int nodeIndex = copy.indexOf(node);
                    if (nodeIndex > 0) {
                        MapNodeStatusManager.setCurrentSelection(copy.get(nodeIndex - 1));
                    }
                    break;

                case South:
                    Collections.sort(copy, new NodePositionYComparator());
                    nodeIndex = copy.indexOf(node);
                    if (nodeIndex < copy.size() - 1) {
                        MapNodeStatusManager.setCurrentSelection(copy.get(nodeIndex + 1));
                    }
                    break;

                case East:
                    Collections.sort(copy, new NodePositionXComparator());
                    nodeIndex = copy.indexOf(node);
                    if (nodeIndex < copy.size() - 1) {
                        MapNodeStatusManager.setCurrentSelection(copy.get(nodeIndex + 1));
                    }
                    break;

                case West:
                    Collections.sort(copy, new NodePositionXComparator());
                    nodeIndex = copy.indexOf(node);
                    if (nodeIndex > 0) {
                        MapNodeStatusManager.setCurrentSelection(copy.get(nodeIndex - 1));
                    }
                    break;

                default:
                    throw new AssertionError();
            }
            mapper.ensureNodeInView(true);
        }
    }

    public MapperKeyListener(MindlinerMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {

            case KeyEvent.VK_DELETE:
                if (MapNodeStatusManager.getCurrentSelection() != null) {
                    int rep = JOptionPane.showConfirmDialog(MindlinerMain.getInstance(), "Are you sure you want to delete selected item(s)?", "Deletion", JOptionPane.YES_NO_OPTION);
                    if (rep == JOptionPane.YES_OPTION) {
                        List<MlMapNode> currentNodes = MapNodeStatusManager.getSelectionList();
                        mapper.deleteNodeAndObject(currentNodes);
                    }
                } else if (!SelectionManager.getConnectionSelection().isEmpty()) {

                    int rep = JOptionPane.showConfirmDialog(MindlinerMain.getInstance(), "Are you sure you want to remove the selected link(s)?", "Deletion", JOptionPane.YES_NO_OPTION);
                    if (rep == JOptionPane.YES_OPTION) {
                        e.consume();
                        unlink(SelectionManager.getConnectionSelection());
                    }
                }
                break;

            case KeyEvent.VK_SPACE:
                if (MapNodeStatusManager.getCurrentSelection() == null) {
                    return;
                }

                MapNodeStatusManager.getCurrentSelection().setExpanded(!MapNodeStatusManager.getCurrentSelection().isExpanded());
                mapper.getPositioner().arrangePositions();
                mapper.repaint();
                break;

            case KeyEvent.VK_PAGE_UP:
                MlMapNode cs = MapNodeStatusManager.getCurrentSelection();
                if (cs != null && cs.getParentNode() != null && cs.getParentNode().getChildren().size() > 1) {
                    MapNodeStatusManager.setCurrentSelection(cs.getParentNode().getChildren().get(0));
                }
                break;

            case KeyEvent.VK_PAGE_DOWN:
                cs = MapNodeStatusManager.getCurrentSelection();
                if (cs != null && cs.getParentNode() != null && cs.getParentNode().getChildren().size() > 1) {
                    MapNodeStatusManager.setCurrentSelection(cs.getParentNode().getChildren().get(cs.getParentNode().getChildren().size() - 1));
                }
                break;

            case KeyEvent.VK_LEFT:
                cs = MapNodeStatusManager.getCurrentSelection();
                if (cs == null) {
                    if (!mapper.getNodes().isEmpty()) {
                        MapNodeStatusManager.setCurrentSelection(mapper.getNodes().get(0));
                    }
                    return;
                }
                if (isLeftNode(cs)) {
                    if (cs.getChildren().isEmpty()) {
                        mapper.addMissingNodes(cs);
                    }
                    if (!cs.getChildren().isEmpty()) {
                        MlMapNode child = cs.getChildren().get(0);
                        MapNodeStatusManager.setCurrentSelection(child);
                        cs.setExpanded(true);
                    }
                } else if (cs.getParentNode() != null) {
                    MapNodeStatusManager.setCurrentSelection(cs.getParentNode());
                }
                mapper.ensureNodeInView(true);
                mapper.repaint();
                break;

            case KeyEvent.VK_RIGHT:
                cs = MapNodeStatusManager.getCurrentSelection();
                if (cs == null) {
                    if (!mapper.getNodes().isEmpty()) {
                        MapNodeStatusManager.setCurrentSelection(mapper.getNodes().get(0));
                    }
                    return;
                }
                if (isLeftNode(cs)) {
                    if (cs.getParentNode() != null) {
                        MapNodeStatusManager.setCurrentSelection(cs.getParentNode());
                    }
                } else {
                    if (cs.getChildren().isEmpty()) {
                        mapper.addMissingNodes(cs);
                    }
                    if (!cs.getChildren().isEmpty()) {
                        MlMapNode child = cs.getChildren().get(0);
                        MapNodeStatusManager.setCurrentSelection(child);
                        cs.setExpanded(true);
                    }
                }
                mapper.getPositioner().arrangePositions();
                mapper.reAssignNodeColors();
                mapper.ensureNodeInView(true);
                mapper.repaint();
                break;

            case KeyEvent.VK_DOWN:
                cs = MapNodeStatusManager.getCurrentSelection();
                if (cs == null) {
                    return;
                }
                if (!e.isAltDown()) {
                    selectAdjacentSibling(cs, MapDirection.South);
                } else {
                    MlMapNode parent = cs.getParentNode();
                    if (parent != null) {
                        // let's take the current index as default (not perfect as there might be invisible relatives
                        int index = parent.getChildren().indexOf(cs);
                        if (index < parent.getChildren().size() - 1) {
                            index++;
                            MindmapPositioner.moveSibling(parent, cs, index);
                        }
                    }
                }
                mapper.ensureNodeInView(true);
                mapper.repaint();
                break;

            case KeyEvent.VK_F:
                cs = MapNodeStatusManager.getCurrentSelection();
                if (cs.getObject() instanceof MlcImage) {
                    final LazyImage limg = CacheEngineStatic.getImageAsync((MlcImage) cs.getObject());
                    ImageFullscreenViewer.showFullScreen(limg);
                }
                break;

            case KeyEvent.VK_I:
                cs = MapNodeStatusManager.getCurrentSelection();
                if (cs != null) {
                    IconGridDialog igd = new IconGridDialog(mapper, cs);
                    if (!igd.hasIcons()) {
                        JOptionPane.showMessageDialog(null, "There are no icons configured for this data room, contact your administrator.");
                    } else {
                        igd.setVisible(true);
                    }
                }
                break;

            case KeyEvent.VK_UP:
                cs = MapNodeStatusManager.getCurrentSelection();
                if (cs == null) {
                    return;
                }
                if (!e.isAltDown()) {
                    selectAdjacentSibling(cs, MapDirection.North);
                } else {
                    MlMapNode parent = cs.getParentNode();
                    if (parent != null) {
                        // let's take the current index as default (not perfect as there might be invisible relatives
                        int index = parent.getChildren().indexOf(cs);
                        if (index > 0) {
                            index--;
                            MindmapPositioner.moveSibling(parent, cs, index);
                        }
                    }
                }
                mapper.ensureNodeInView(true);
                mapper.repaint();
                break;

            case KeyEvent.VK_E:
                ObjectEditorLauncher.showEditor(MlMapNodeUtils.getObjects(MapNodeStatusManager.getSelectionList()));
                break;

            case KeyEvent.VK_INSERT:
                MindlinerMain.createNewObject(mlcKnowlet.class,
                        MapNodeStatusManager.getCurrentSelection() == null ? null : MapNodeStatusManager.getCurrentSelection().getObject());
                break;

            case KeyEvent.VK_ENTER: // the ENTER is really messy for the moment....
                cs = MapNodeStatusManager.getCurrentSelection();
                if (cs != null) {
                    // select the parent node so that the new node gets linked to the parent and not the sibling
                    if (cs.getParentNode() != null) {
                        MindlinerMain.createNewObject(mlcKnowlet.class, cs.getParentNode().getObject());
                    } else {
                        MindlinerMain.createNewObject(mlcKnowlet.class, null);
                    }
                } else {
                    MindlinerMain.createNewObject(mlcKnowlet.class, null);
                }
                break;

            case KeyEvent.VK_V:
                if (e.isControlDown()) {
                    ClipboardParser parser = new ClipboardParser();
                    URL url = parser.parseClipboardAsURL();
                    if (url != null) {
                        try {
                            // clipboard content is a valid url -> create node directly
                            URLConnection conn = url.openConnection();
                            InputStream is = conn.getInputStream();
                            ImageInputStream iis = ImageIO.createImageInputStream(is);
                            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
                            CommandRecorder cr = CommandRecorder.getInstance();
                            if (readers.hasNext()) {
                                // URL is an image we can read, therefore create an URL Image object
                                ObjectCreationCommand cmd = new ObjectCreationCommand(null, MlcImage.class, "", "");
                                cr.scheduleCommand(cmd);
                                cr.scheduleCommand(new ImageUpdateCommand(cmd.getObject(), null, cmd.getObject().getHeadline(), MlsImage.ImageType.URL, url.toString()));
                            } else {
                                // URL is not an image, create a knowlet with the title of the page as headline
                                ObjectCreationCommand cmd = new ObjectCreationCommand(null, mlcKnowlet.class, "Image", url.toString());
                                cr.scheduleCommand(cmd);
                                PageTitleParser htmlParser = new PageTitleParser(cmd.getObject(), conn);
                                htmlParser.execute();
                            }
                            break;
                        } catch (IOException ex) {
                            Logger.getLogger(MapperKeyListener.class.getName()).log(Level.INFO, "Failed to analyze URL from clipboard content", ex);
                        }
                    }

                    // all other cases: show clipboard creation dialog
                    List<TextUnit> textUnits = parser.parseClipboard();
                    List<mlcObject> objects = ClipboardObjectCreator.createObjects(textUnits);
                    ClipboardObjectsPanel panel = new ClipboardObjectsPanel();
                    panel.setObjects(objects);
                    MlMapNode sel = MapNodeStatusManager.getCurrentSelection();
                    if (sel != null) {
                        panel.setParentObject(sel.getObject());
                    }
                    ClipboardObjectsDialog dialog = new ClipboardObjectsDialog(panel);
                    dialog.setVisible(true);

                } else {
                    // don't do anything here - this event is handled by the RebuildMapKeyListener 
                }
                break;
            case KeyEvent.VK_X:
                cs = MapNodeStatusManager.getCurrentSelection();
                if (cs == null) {
                    return;
                }
                MlViewDispatcherImpl.getInstance().display(cs.getObject(), MlObjectViewer.ViewType.Spreadsheet);
                break;
            case KeyEvent.VK_W:
                cs = MapNodeStatusManager.getCurrentSelection();
                if (cs == null) {
                    return;
                }
                MlViewDispatcherImpl.getInstance().display(cs.getObject(), MlObjectViewer.ViewType.ContainerMap);
                break;
            case KeyEvent.VK_U:
                if (MapNodeStatusManager.getCurrentSelection() != null) {
                    mapper.unlinkNodes(MapNodeStatusManager.getCurrentSelection(), MapNodeStatusManager.getCurrentSelection().getParentNode());
                }
                break;

            case KeyEvent.VK_H:
                if (MapNodeStatusManager.getCurrentSelection() == null) {
                    return;
                }
                mapper.deleteNode(MapNodeStatusManager.getCurrentSelection(), true);
                break;
        }

    }

    /**
     * Translates the view so that the currently selected node is centered on
     * the screen
     */
    private void centerOnSelectionObs() {
        MlMapNode cs = MapNodeStatusManager.getCurrentSelection();
        if (cs != null) {
            Graphics2D g = (Graphics2D) mapper.getGraphics();

            double targetX = mapper.getWidth() / 2 + cs.getSize().width;
            double targetY = mapper.getHeight() / 2 + cs.getSize().height;

            AffineTransform coordTransform = mapper.getZoomAndPanListener().getCoordTransform();

            // transformed position of the selected node
            Point2D transSelPos = coordTransform.transform(cs.getPosition(), null);

            double transX = targetX - transSelPos.getX() / mapper.getZoomAndPanListener().getScale();
            double transY = targetY - transSelPos.getY() / mapper.getZoomAndPanListener().getScale();

            mapper.getZoomAndPanListener().translate(transX, transY);
        }
    }

    private void unlink(List<NodeConnection> connectionSelection) {
        for (NodeConnection nc : connectionSelection) {
            MlMapNode holder = nc.getHolder();
            MlMapNode relative = nc.getRelative();
            mapper.unlinkNodes(relative, holder);
        }
    }

    /**
     * Customized version of
     * http://www.gotoquiz.com/web-coding/programming/java-programming/how-to-extract-titles-from-web-pages-in-java/
     * Tries to extract the title of the webpage and (if successfull) sets the
     * title as headline of the object
     */
    private class PageTitleParser extends SimpleSwingWorker {

        /* the CASE_INSENSITIVE flag accounts for
         * sites that use uppercase title tags.
         * the DOTALL flag accounts for sites that have
         * line feeds in the title text */
        private final Pattern TITLE_TAG = Pattern.compile("\\<title>(.*)\\</title>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        private final mlcObject object;
        URLConnection conn;
        private final String url;

        public PageTitleParser(mlcObject object, URLConnection conn) {
            this.object = object;
            this.conn = conn;
            this.url = conn.getURL().toString();
        }

        @Override
        protected Object doInBackground() {
            try {
                List<String> field = conn.getHeaderFields().get("Content-Type");
                if (field == null || field.isEmpty() || (!field.contains("text/html") && !field.get(0).contains("text/html"))) {
                    return null; // don't continue if not HTML
                } else {
                    InputStream in = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    int n = 0, totalRead = 0;
                    char[] buf = new char[1024];
                    StringBuilder content = new StringBuilder();

                    // read until EOF or first 16384 characters
                    while (totalRead < 16384 && (n = reader.read(buf, 0, buf.length)) != -1) {
                        content.append(buf, 0, n);
                        totalRead += n;
                    }
                    reader.close();

                    // extract the title
                    Matcher matcher = TITLE_TAG.matcher(content);
                    if (matcher.find()) {
                        /* replace any occurrences of whitespace (which may
                         * include line feeds and other uglies) as well
                         * as HTML brackets with a space */
                        String title = matcher.group(1).replaceAll("[\\s\\<>]+", " ").trim();
                        if (!title.isEmpty()) {
                            CommandRecorder cr = CommandRecorder.getInstance();
                            cr.scheduleCommand(new TextUpdateCommand(object, title, object.getDescription()));
                        }
                    }
                    return null;
                }
            } catch (IOException ex) {
                Logger.getLogger(MapperKeyListener.class.getName()).log(Level.FINE, "Failed to extract title of URL: " + url, ex);
                return null;
            }
        }

    }
}
