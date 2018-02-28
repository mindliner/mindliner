package com.mindliner.view;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.MlcImage;
import com.mindliner.clientobjects.mlcNews;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.events.SelectionManager;
import com.mindliner.events.SelectionObserver;
import com.mindliner.image.IconLoader;
import com.mindliner.prefs.FileLocationPreferences;
import com.mindliner.serveraccess.OnlineManager;
import com.mindliner.view.background.DefaultBackgroundPainter;
import com.mindliner.view.connectors.NodeConnection;
import com.mindliner.view.dispatch.MlObjectViewer;
import com.mindliner.view.dispatch.MlViewDispatcherImpl;
import com.mindliner.view.interaction.MapperKeyListener;
import com.mindliner.view.interaction.MapperMouseListener;
import com.mindliner.view.interaction.PickIdentifyer;
import com.mindliner.view.interaction.PickIdentifyerImpl;
import com.mindliner.view.interaction.ZoomPanListener;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import com.mindliner.clientobjects.MlMapNode;

/**
 * This is the main Mindliner view except for tables. The main interaction with
 * this view is through the mouse and single key strokes on the keyboard. The
 * usage is discribed in QuickHelpContent.html.
 *
 * There are engines to convert Mindliner objects to nodes (using configurable
 * decoration), to position the nodes (statically and dynamically), to draw the
 * background, to draw the connectors between nodes (if any), and to assign
 * colors the nodes.
 *
 *
 * author: Marius Messerli
 */
public class MindlinerMapperImpl extends MindlinerMapper implements SelectionObserver {

    private final ZoomPanListener zoomAndPanListener;
    private PickIdentifyer pickIdentifyer = null;

    public MindlinerMapperImpl() {
        this.zoomAndPanListener = new ZoomPanListener(this);
        pickIdentifyer = new PickIdentifyerImpl(this, zoomAndPanListener);
        configureMapper();
        backgroundPainter = new DefaultBackgroundPainter();
        backgroundPainter.initialize();
    }

    private void configureMapper() {
        addMouseListener(zoomAndPanListener);
        addMouseMotionListener(zoomAndPanListener);
        addMouseWheelListener(zoomAndPanListener);
        MapperMouseListener viewPanelMouseListener = new MapperMouseListener(this);
        addMouseListener(viewPanelMouseListener);
        addMouseMotionListener(viewPanelMouseListener);
        addKeyListener(new MapperKeyListener(this));
    }

    private List<MlMapNode> getNodesContainingObject(mlcObject o) {
        List<MlMapNode> nodeList = new ArrayList<>();
        for (MlMapNode n : TreeLinearizer.linearizeAllTrees(getNodes(), true)) {
            if (n.getObject().getId() == o.getId()) {
                nodeList.add(n);
            }
        }
        return nodeList;
    }

    @Override
    public void objectReplaced(int oldId, mlcObject o) {
        // check if there exist any nodes containing the old object
        List<MlMapNode> existingNodes = TreeLinearizer.linearizeAllTrees(getNodes(), true);
        List<MlMapNode> containingNodes = new ArrayList<>();
        for (MlMapNode n : existingNodes) {
            if (n.getObject().getId() == oldId || n.getObject().getId() == o.getId()) {
                containingNodes.add(n);
            }
        }
        boolean repaint = false;
        for (MlMapNode n : containingNodes) {
            // replace the object
            n.setObject(o);
            n.updateChildConnections();
            repaint = true;
            if (n.getParentNode() != null) {
                n.getParentNode().updateChildConnections(); // the relativeId from the link object of the parent is still the oldId
            }
        }
        if (repaint) {
            repaint();
        }
    }

    /**
     * @todo getNodes does not return what the following event handling
     * procedures objectChanged() etc assume - why is this working at all???
     *
     * @param o
     */
    @Override
    public void objectChanged(mlcObject o) {
        if (o instanceof MlcImage) {
            CacheEngineStatic.invalidateImage(o.getId());
        }
        boolean repaintIt = false;
        List<MlMapNode> existingNodes = TreeLinearizer.linearizeAllTrees(getNodes(), true);
        boolean existing = false;
        for (MlMapNode n : existingNodes) {
            if (n.getObject().getId() == o.getId()) {
                existing = true;
            }
        }
        if (existing) {
            // Returns immediately, loads the object icons in background
            IconLoader.getInstance().loadIcons(o);

            List<MlMapNode> nodesContainingObject = getNodesContainingObject(o);
            for (MlMapNode n : nodesContainingObject) {
                n.setObject(o);
                // if is expanded update children as they mave have changed
                if (n.isExpanded()) {
                    n.updateChildConnections();
                    addMissingNodes(n);
                }
                repaintIt = true;
            }
        }
        if (repaintIt) {
            if (!OnlineManager.isOnline()) {
                MlMapNode currentSelection = MapNodeStatusManager.getCurrentSelection();
                if (currentSelection != null) {
                    addMissingNodes(currentSelection);
                }
            } else {
                // these two lines are included in the addMissingNodes() call above
                getColorizer().assignNodeColors(getNodes());
                repaint();
            }
        }
    }

    /**
     * Need to remove child nodes and update the object reference of the parent
     * node.
     *
     * @param o The cached version of the object that was deleted
     */
    @Override
    public void objectDeleted(mlcObject o) {
        List<MlMapNode> nodesToDelete = new ArrayList<>();
        for (MlMapNode n : getNodes()) {
            nodesToDelete.addAll(deleteNodeOfObject(n, o));
        }
        // delete node after iterating children/nodes-list (otherwise ConcurrentModificationException)
        for (MlMapNode node : nodesToDelete) {
            deleteNode(node, true);
        }
        rearrangePositions();
        repaint();
    }
    
    
        /**
     * This class is both a publisher of selection changes as well as a
     * subscriber to selection change events. When this viewer acts as a
     * subscriber it only learns about the object and not the node that was
     * changed and so multiple nodes may contain the changed object and we
     * cannot really decide which one is the "current" object.
     *
     * In the case that this class is the change source we want to keep that
     * information and therefore must check whether the incoming notifications
     * are for objects that are already sel
     *
     * @param newSelection The new selected objects
     */
    @Override
    public void selectionChanged(List<mlcObject> newSelection) {

        if (!isShowing()) {
            return;
        }

        // if the view is empty show the first selection, otherwise preserve the current map
        if (getNodes().isEmpty()) {
            MlViewDispatcherImpl.getInstance().display(newSelection, MlObjectViewer.ViewType.Map);
            ensureNodeInView(true);
            repaint();
        } else {
            List<MlMapNode> nodesContainingNewSelection = new ArrayList<>();
            for (mlcObject o : newSelection) {
                nodesContainingNewSelection.addAll(getNodesContainingObject(o));
            }
            if (MapNodeStatusManager.maybeSetSelectedNodes(nodesContainingNewSelection)) {
                ensureNodeInView(true);
                repaint();
            }
        }
    }

    @Override
    public void clearSelections() {
        MapNodeStatusManager.clearSelection();
    }

    @Override
    public void connectionSelectionChanged(List<NodeConnection> selection) {
        // do nothing
    }

    @Override
    public void clearConnectionSelections() {
        // do nothing
    }


    private List<MlMapNode> getRelatedNodes(mlcObject nodeObject) {
        List<MlMapNode> relatedNodes = new ArrayList<>();
        List<mlcObject> relatedObjects = CacheEngineStatic.getLinkedObjects(nodeObject);
        for (MlMapNode existingMapNode : TreeLinearizer.linearizeAllTrees(getNodes(), true)) {
            if (relatedObjects.contains(existingMapNode.getObject())) {
                relatedNodes.add(existingMapNode);
            }
        }
        return relatedNodes;
    }

    private void insertNode(MlMapNode parentToBe, MlMapNode childToBe, boolean updateSelection) {
        parentToBe.addChild(childToBe);
        parentToBe.setExpanded(true);
        childToBe.setParentNode(parentToBe);
        rearrangePositions();
        if (updateSelection) {
            MapNodeStatusManager.setCurrentSelection(parentToBe);
        }
    }

    /**
     * Gets called when a new object is created by any of Mindliner's
     * mechanisms. This call checks if the new object is related to any of the
     * node's underlying objects. If so it will generate a new node with the
     * newObject as underlyer for each relationship.
     *
     * Note: The case where getNodes().isEmpty when the new object is created is
     * handled by Mindliner2DViewer
     *
     * @param newObject
     */
    @Override
    public void objectCreated(mlcObject newObject) {
        if (newObject instanceof mlcNews) {
            return;
        }
        assert (getNodeBuilder() != null);

        // Need to check if object already in map even though the method name might suggest otherwise.
        // In ExecutionMode.Asynchronous this method is called once by the client directly 
        // and a second time when creation event comes back from the server. In the latter case it already exists.
        List<MlMapNode> existingNodes = TreeLinearizer.linearizeAllTrees(getNodes(), true);
        for (MlMapNode n : existingNodes) {
            if (n.getObject().getId() == newObject.getId()) {
                return;
            }
        }

        // now see if there are any related nodes that we need to link up to
        boolean objectAdded = false;
        for (MlMapNode relative : getRelatedNodes(newObject)) {
            MlMapNode newNode = getNodeBuilder().wrapObject(newObject);
            insertNode(relative, newNode, false);
            objectAdded = true;
        }
        // show newly created objects from the current user when they're not linked to any other objects

        if (!objectAdded // add node to root nodes only if node doesn't have relatives
                && newObject.isOwnedByCurrentUser() // we don't want to show unlinked objects from any other user
                && !getNodes().isEmpty() // this case is handled by Mindliner2DViewer.objectCreated()
                ) {
            MlMapNode newNode = getNodeBuilder().wrapObject(newObject);
            getNodes().add(newNode);
            newNode.setExpanded(true);
            repaint();
        }
        if (objectAdded) {
            rearrangePositions();
            repaint();
        }

    }


    /**
     * Traverses recursively all nodes and deletes the ones who correspond to
     * the given object
     *
     * @param parent The parent node.
     * @param o The object that has been deleted.
     */
    private List<MlMapNode> deleteNodeOfObject(MlMapNode parent, mlcObject o) {
        List<MlMapNode> nodesToDelete = new ArrayList<>();
        for (MlMapNode n : parent.getChildren()) {
            nodesToDelete.addAll(deleteNodeOfObject(n, o));

            // remember node for deletion
            if (n.getObject().equals(o)) {
                nodesToDelete.add(n);
            }
        }
        if (parent.getObject().equals(o) && parent.getParentNode() == null) {
            nodesToDelete.add(parent);
        }
        return nodesToDelete;
    }

    private void selectSiblingThenParentThenNothing(MlMapNode node) {
        MlMapNode parent = node.getParentNode();
        if (parent != null) {
            if (parent.getChildren().size() > 1) {
                int nodeIndex = parent.getChildren().indexOf(node);
                if (nodeIndex < parent.getChildren().size() - 1) {
                    MlMapNode newSel = parent.getChildren().get(nodeIndex + 1);
                    MapNodeStatusManager.setCurrentSelection(newSel);
                } else if (nodeIndex != 0) {
                    MlMapNode newSel = parent.getChildren().get(nodeIndex - 1);
                    MapNodeStatusManager.setCurrentSelection(newSel);
                }
            } else {
                MapNodeStatusManager.setCurrentSelection(parent);
            }
        } else {
            SelectionManager.clearSelection();
        }
    }

    /**
     * Removes the node from the view and unlinks the object from the parent. If
     * that parent-node link was the last link then pop up a dialog asking if
     * the node is to be removed from Mindliner.
     *
     * @param node The node to be deleted
     * @param updateSelection If true the current selection is set to one of the
     * siblings or the parent if no sibling exists
     */
    @Override
    public void deleteNode(MlMapNode node, boolean updateSelection) {
        if (updateSelection) {
            selectSiblingThenParentThenNothing(node);
        }
        if (node.getParentNode() != null) {
            node.getParentNode().removeChild(node);
        }
        super.deleteNode(node, updateSelection);

        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        backgroundPainter.paint(g2, this);
        setupGraphics(g2);
        getPositioner().setFontRenderContext(g2.getFontRenderContext());
        // first pass is only to compute sizes
        render(g2, null, true);
        getPositioner().arrangePositions();
        // second pass is to actually draw
        render(g2, null, false);
    }

    private void setupGraphics(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setTransform(zoomAndPanListener.getCoordTransform());
    }

    @Override
    public void repaintSingleNode(MlMapNode n) {
        if (n != null) {
            Graphics2D g2 = (Graphics2D) getGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setTransform(zoomAndPanListener.getCoordTransform());
            n.draw(g2, true);
        }
    }

    @Override
    public ZoomPanListener getZoomAndPanListener() {
        return zoomAndPanListener;
    }

    @Override
    public PickIdentifyer getPickIdentifyer() {
        return pickIdentifyer;
    }

    @Override
    public MlMapNode getNodeAtScreenLocation(Point p) {
        return pickIdentifyer.identifyObject(p, null);
    }

    private void rearrangePositions() {
        for (MlMapNode n : getNodes()) {
            getPositioner().arrangeNodeFamilyPosition(n, MapNodeStatusManager.isLeft(n));
        }
    }

    @Override
    public void addMissingNodes(MlMapNode parent) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        List<mlcObject> missingRelatives = CacheEngineStatic.getLinkedObjects(parent.getObject());

        List<MlMapNode> nonRelatives = new ArrayList<>();
        // remove all nodes that are already children to the parent 
        for (MlMapNode node : parent.getChildren()) {
            boolean removed = missingRelatives.remove(node.getObject());
            if (!removed) {
                nonRelatives.add(node);
            }
        }

        // remove all nodes that aren't relatives anymore (e.g. others unlinked it)
        for (MlMapNode node : nonRelatives) {
            parent.removeChild(node);
        }

        // remove grandpa to avoid the loops
        if (parent.getParentNode() != null) {
            missingRelatives.remove(parent.getParentNode().getObject());
        }

        parent.setExpanded(true);

        if (!missingRelatives.isEmpty()) {
            for (mlcObject o : missingRelatives) {
                // null if we haven't fetched the icons for this object from the server yet (if any).
                if (o.getIcons() == null) {
                    // initates background download of icons, returns immediately.
                    IconLoader.getInstance().loadIcons(o);
                }
                MlMapNode newRelative = getNodeBuilder().wrapObject(o);
                newRelative.setParentNode(parent);
                parent.addChild(newRelative);
            }
            getColorizer().assignNodeColors(getNodes());
            reAssignNodeColors();
        }
        rearrangePositions();
        repaint();
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     * This function make sure that the newly selected node is fully in the
     * current view, assuming that the view is bigger than the node, so the zoom
     * is not changed.
     *
     * @param translateWhenOffscreenOnly
     */
    @Override
    public void ensureNodeInView(boolean translateWhenOffscreenOnly) {

        Graphics2D g = (Graphics2D) getGraphics();
        // this is the node we want to make sure it is fully on screen
        MlMapNode target = MapNodeStatusManager.getCurrentSelection();
        if (target == null && !getNodes().isEmpty()) {
            target = getNodes().get(0);
        }
        if (target == null) {
            return;
        }
        AffineTransform coordTransform = getZoomAndPanListener().getCoordTransform();
        Point2D transformedTargetPosition = coordTransform.transform(target.getPosition(), null);
        double transX;
        double transY;

        double transformedTargetWidth = target.getSize().width* coordTransform.getScaleX();
        double transformedTargetHeight = target.getSize().height* coordTransform.getScaleY();

        transX = transformedTargetPosition.getX() >= 0 ? 0 : -transformedTargetPosition.getX();
        if (transX == 0) {
            transX = getWidth() >= transformedTargetPosition.getX() + transformedTargetWidth ? 0 : getWidth() - transformedTargetPosition.getX() - transformedTargetWidth;
        }

        transY = transformedTargetPosition.getY() >= 0 ? 0 : (-transformedTargetPosition.getY() + transformedTargetHeight);
        if (transY == 0) {
            transY = transformedTargetPosition.getY() + transformedTargetHeight <= getHeight() ? 0 : getHeight() - (transformedTargetPosition.getY() + transformedTargetHeight);
        }

        if (transX == 0 && transY == 0) {
            return;
        }
        getZoomAndPanListener().translate(transX / coordTransform.getScaleX(), transY / coordTransform.getScaleY());
    }

}
