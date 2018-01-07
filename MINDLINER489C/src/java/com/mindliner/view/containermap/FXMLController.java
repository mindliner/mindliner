/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.containermap;

import com.mindliner.analysis.MlClassHandler;
import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.MlClientClassHandler;
import com.mindliner.clientobjects.MlcContainer;
import com.mindliner.clientobjects.MlcContainerMap;
import com.mindliner.clientobjects.MlcImage;
import com.mindliner.clientobjects.MlcLink;
import com.mindliner.clientobjects.mlcKnowlet;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.commands.CommandRecorder;
import com.mindliner.commands.LinkCommand;
import com.mindliner.commands.MindlinerOnlineCommand;
import com.mindliner.commands.ObjectCreationCommand;
import com.mindliner.commands.ObjectDeletionCommand;
import com.mindliner.commands.CMAddCommand;
import com.mindliner.commands.CMDeleteCommand;
import com.mindliner.commands.CMUpdateContainerCommand;
import com.mindliner.commands.CMUpdateLinkCommand;
import com.mindliner.commands.CMUpdateNodeCommand;
import com.mindliner.commands.DataPoolUpdateCommand;
import com.mindliner.commands.TextUpdateCommand;
import com.mindliner.entities.Colorizer;
import com.mindliner.enums.LinkRelativeType;
import com.mindliner.events.ObjectChangeManager;
import com.mindliner.events.ObjectChangeObserver;
import com.mindliner.events.SelectionManager;
import com.mindliner.events.SelectionObserver;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.gui.ObjectEditorLauncher;
import com.mindliner.gui.color.BaseColorizer;
import com.mindliner.gui.color.ColorManager;
import com.mindliner.gui.color.FixedKeyColorizer;
import com.mindliner.main.MindlinerMain;
import com.mindliner.objects.transfer.MltContainer;
import com.mindliner.objects.transfer.MltContainermapObjectLink;
import com.mindliner.objects.transfer.MltContainermapObjectPosition;
import com.mindliner.view.connectors.NodeConnection;
import com.mindliner.view.dispatch.MlObjectViewer;
import com.mindliner.view.dispatch.MlViewDispatcherImpl;
import java.awt.EventQueue;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.stage.Stage;
import javax.naming.NamingException;

/**
 * FXML Controller class. Implements all general GUI actions on the container
 * map.
 *
 * Starts a daemon thread at the start that will process all commands
 * asynchronously.
 *
 * @author Dominic Plangger
 */
public class FXMLController implements Initializable, ObjectChangeObserver, SelectionObserver {

    private static final String FOCUS_RECTANGLE_COLOR = "#73BEF0";
    private MapContainer currContainer = null;
    private MlcContainerMap mapObject = null;
    private LinkManager linkManager = null;
    private final List<Selectable> selectedNodes = new ArrayList<>();
    private ContainerMapElement tempElement = null;
    private IconPicker iconPicker = null;
    private TypePicker typePicker = null;
    private boolean isCreatingNode = false;

    private double lastPanX, lastPanY;
    private double globalScale = 1;

    private static FXMLController instance = null;
    private List<ContainerProperties> templateContainers = null;

    @FXML
    private ComboBox<ContainerProperties> containerSelection;
    @FXML
    private Group nodeGroup;
    @FXML
    private Label mapLabel;
    @FXML
    private Button newMapButton;
    @FXML
    private StackPane rootPane;
    @FXML
    private Pane mapPane;
    @FXML
    private AnchorPane hoverPane;
    @FXML
    private ScrollPane IconPickerPane;
    @FXML
    private ScrollPane TypePickerPane;
    @FXML
    private ToggleButton containerToggleButton;
    @FXML
    private ToggleGroup mainToggleGroup;
    @FXML
    private ToggleButton linkToggleButton;
    @FXML
    private ToggleButton cursorToggleButton;
    @FXML
    private Button resetZoomAndPan;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        linkManager = new LinkManager(nodeGroup);
        instance = this;
        // TODO: refactor. implement change and selection observer  in ContainerMap and delegate to controller?
        ObjectChangeManager.registerObserver(this);
        SelectionManager.registerObserver(this);
        disableControls(true);

        ContainerMapUtils.setButtonIcon(linkToggleButton, "/com/mindliner/img/icons/external/connector.png", 16);
        ContainerMapUtils.setButtonIcon(containerToggleButton, "/com/mindliner/img/icons/external/rectangle.png", 16);
        ContainerMapUtils.setButtonIcon(cursorToggleButton, "/com/mindliner/img/icons/external/pointer.png", 16);
    }

    void init(MlcContainerMap root) {
        linkManager.clearAll();
        nodeGroup.getChildren().clear();
        selectedNodes.clear();
        cursorToggleButton.setSelected(true);
        mapLabel.setText(root.getHeadline());
        mapObject = root;
        globalScale = 1;
        disableControls(false);
        iconPicker = new IconPicker(mapObject.getClient());
        if (typePicker == null) {
            typePicker = new TypePicker();
        }
        iconPicker.setObjects(new ArrayList<>());
        typePicker.setObject(null);
        IconPickerPane.setContent(iconPicker);
        IconPickerPane.setBorder(Border.EMPTY);
        TypePickerPane.setContent(typePicker);
        TypePickerPane.setBorder(Border.EMPTY);
        BaseColorizer fkc = ColorManager.getColorizerForType(Colorizer.ColorDriverAttribute.FixedKey);
        java.awt.Color awtBg = fkc.getColorForKey(FixedKeyColorizer.FixedKeys.MAP_BACKGROUND);

        // got a NPE because of a negative color value !
        int red = awtBg.getRed();
        int green = awtBg.getGreen();
        int blue = awtBg.getBlue();
        if (red >= 0 && red <= 255
                && green >= 0 && green <= 255
                && blue >= 0 && blue <= 255) {
            Background background = new Background(new BackgroundFill(Color.rgb(red, green, blue), new CornerRadii(3), Insets.EMPTY));
            mapPane.setBackground(background);
        }
        updateContainerCombo(); // someone might have changed the colors in the preference editor
    }

    void clear() {
        nodeGroup.getChildren().clear();
        selectedNodes.clear();
        cursorToggleButton.setSelected(true);
        mapLabel.setText("");
        disableControls(true);
    }

    private void disableControls(boolean disableState) {
        containerToggleButton.setDisable(disableState);
        linkToggleButton.setDisable(disableState);
        containerSelection.setDisable(disableState);
        cursorToggleButton.setDisable(disableState);
    }

    public static FXMLController getInstance() {
        return instance;
    }

    void createLink(Node source) {
        linkManager.createLink(source);
    }

    void finishLink(Point2D endPoint) {
        MapLink link = linkManager.finishLink(endPoint);
        if (link != null) {
            updateLink(link, true);
        }
    }

    // depending on isAdding it either adds or removes the link
    public void updateLink(MapLink link, boolean isAdding) {
        Node start = link.getStartNode();
        Node end = link.getEndNode();
        if (start instanceof ContainerMapElement && end instanceof ContainerMapElement) {
            MltContainermapObjectLink tl = ContainerMapUtils.convertLink(link);
            CMUpdateLinkCommand cmd = new CMUpdateLinkCommand(mapObject, tl, isAdding);
            addCommand(cmd);
        } else {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, "Unexpected link nodes: {0} , {1}", new Object[]{start, end});
            linkManager.removeLink(link);
        }
    }

    /**
     * checks all children in the node group of type clazz for intersection with
     * rectangle (x,y)(w,h)
     *
     * @param requireBoundaryIntersection If false, objects that are fully
     * contained in a container are considered intersecting with the container,
     * if true they are only considered intersecting if they intersect the
     * container boundaries
     */
    <T extends Node> List<T> getIntersections(Class<T> clazz, double x, double y, double w, double h, boolean requireBoundaryIntersection) {
        List<T> intersects = new ArrayList<>();
        for (Node child : nodeGroup.getChildren()) {
            if (clazz.isInstance(child)) { // implies that child is instance of T
                // important to call intersect on bounds in parent, per default intersects assumes local bounds.

                if (child.getBoundsInParent().intersects(x, y, w, h)
                        && (!requireBoundaryIntersection || !child.getBoundsInParent().contains(x, y, w, h))) {
                    intersects.add((T) child);
                }
            }
        }
        return intersects;
    }

    /**
     * Computes the surrounding containers before and after node movement.
     * Unlinks the object from the old containers and links it to the new ones.
     */
    void nodeMoved(double oldx, double oldy, Node node) {
        List<MapLink> links = linkManager.getLinks(node);
        links.stream().forEach((l) -> {
            updateLink(l, true);
        });

        Bounds bip = node.getBoundsInParent();
        double w = bip.getWidth();
        double h = bip.getHeight();
        List<mlcObject> oldIntersections = new ArrayList<>();
        Class<? extends Node> type = node instanceof MapNode ? MapContainer.class : MapNode.class;

        List<mlcObject> newIntersections = new ArrayList<>();
        List<? extends Node> nodes = getIntersections(type, bip.getMinX(), bip.getMinY(), w, h, false);
        for (Node n : nodes) {
            newIntersections.add(((ContainerMapElement) n).getObject());
        }
        nodes = getIntersections(type, oldx, oldy, w, h, false);
        for (Node n : nodes) {
            oldIntersections.add(((ContainerMapElement) n).getObject());
        }

        if (node instanceof MapNode) {
            // node must always be either linked to a container or the map itself
            if (oldIntersections.isEmpty()) {
                oldIntersections.add(mapObject);
            }
            if (newIntersections.isEmpty()) {
                newIntersections.add(mapObject);
            }
            List<mlcObject> intersection = new ArrayList<>(newIntersections);
            intersection.retainAll(oldIntersections);
            oldIntersections.removeAll(intersection);
            newIntersections.removeAll(intersection);
            int lx = (int) Math.round(node.getLayoutX());
            int ly = (int) Math.round(node.getLayoutY());
            CMUpdateNodeCommand cmd = new CMUpdateNodeCommand(((MapNode) node).getObject(), mapObject, oldIntersections, newIntersections, lx, ly, (int) oldx, (int) oldy);
            addCommand(cmd);
        } else {
            for (Node n : nodes) {
                List<MapContainer> parents = getParentContainers((MapNode) n);
                if (parents.isEmpty()) {
                    LinkCommand cmd = new LinkCommand(mapObject, ((MapNode) n).getObject(), false, LinkRelativeType.CONTAINER_MAP);
                    addCommand(cmd);
                }
            }
            updateContainer((MapContainer) node, oldIntersections, newIntersections);
        }
    }

    public void displayObject(mlcObject obj, double x, double y) {
        createNode(obj, x, y, false, true);
    }

    // display an object which was created by another Mindliner instance and/or user
    public void displayNewForeignObject(mlcObject obj, double x, double y) {
        createNode(obj, x, y, true, false);
    }

    void displayNewForeignContainer(MlcContainer container) {
        if (container.getColor() == null) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, "Container {0} has undefined color.", container.getId());
            return;
        }
        Point2D translate = CoordUtil.inferTranslate(container.getPosX(), container.getPosY(), nodeGroup, globalScale);
        MapContainer mapContainer = new MapContainer(container.getPosX(), container.getPosY(), container.getColor(), container.getHeadline());
        mapContainer.setWidth(container.getWidth());
        mapContainer.setHeight(container.getHeight());
        mapContainer.setTranslateX(translate.getX() - container.getPosX());
        mapContainer.setTranslateY(translate.getY() - container.getPosY());
        setContainerAttribs(mapContainer, container);
        mapContainer.finishCreation(globalScale, false);
        nodeGroup.getChildren().add(mapContainer);
    }

    void displayContainer(MlcContainer container) {
        if (container.getColor() == null) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, "Container {0} has undefined color.", container.getId());
            return;
        }
        MapContainer mapContainer = new MapContainer(container.getPosX(), container.getPosY(), container.getColor(), container.getHeadline());
        mapContainer.setWidth(container.getWidth());
        mapContainer.setHeight(container.getHeight());
        setContainerAttribs(mapContainer, container);
        mapContainer.finishCreation(globalScale, true);
        nodeGroup.getChildren().add(mapContainer);
    }

    private void setContainerAttribs(MapContainer mapContainer, MlcContainer containerObject) {
        mapContainer.setContainerOpacity(containerObject.getOpacity());
        mapContainer.setStrokeWidth(containerObject.getStrokeWidth());
        mapContainer.setStrokeStyle(containerObject.getStrokeStyle());
        mapContainer.setObject(containerObject);
    }

    public void displayLink(final MltContainermapObjectLink link) {
        // TODO support one way
        // We run the code for creating the link later, after the actual nodes 
        // have been drawn. Otherwise the bounds of the nodes are not correct.
        Platform.runLater(() -> {
            ContainerMapElement startNode = findMapElement(link.getSourceObjId());
            if (startNode == null) {
                Logger.getLogger(FXMLController.class.getName()).log(Level.INFO, "Object {0} not found for linking. May be caused by restricted filtering or confidentiality.", new Object[]{link.getSourceObjId()});
                return;
            }
            ContainerMapElement endNode = findMapElement(link.getTargetObjId());
            if (endNode == null) {
                Logger.getLogger(FXMLController.class.getName()).log(Level.INFO, "Objects {0} not found for linking. May be caused by restricted filtering or confidentiality.", new Object[]{link.getTargetObjId()});
                return;
            }
            linkManager.createLink((Node) startNode, link);
            linkManager.finishLink((Node) endNode, link.getTargetPosition(), link.getTargetOffset());
        });

    }

    void setSelection(Selectable newNode) {
        if (selectedNodes.contains(newNode)) {
            deselectAll();
        } else {
            deselectAll();
            addSelection(newNode);
        }
        updatePickers();
    }

    void setSelection(List<Selectable> newNodes, boolean notify) {
        ArrayList<Selectable> toDelete = new ArrayList<>(selectedNodes);
        toDelete.removeAll(newNodes);
        toDelete.stream().forEach((n) -> {
            n.setSelected(false);
            selectedNodes.remove(n);
        });
        newNodes.stream().filter((newNode) -> (!selectedNodes.contains(newNode))).forEach((newNode) -> {
            addSelection(newNode, notify);
        });
        updatePickers();
    }

    void addSelection(Selectable newNode, boolean notify) {
        if (selectedNodes.contains(newNode)) {
            newNode.setSelected(false);
            selectedNodes.remove(newNode);
        } else {
            newNode.setSelected(true);
            selectedNodes.add(newNode);
            if (newNode instanceof ContainerMapElement) {
                if (notify) {
                    List<mlcObject> objs = new ArrayList<>();
                    selectedNodes.stream().filter((n) -> n instanceof ContainerMapElement).forEach((cm) -> {
                        objs.add(((ContainerMapElement) cm).getObject());
                    });
                    SelectionManager.setSelection(objs);
                }
                linkManager.setLinksSelected((Node) newNode, true);
            }
        }
        updatePickers();
    }

    void addSelection(Selectable newNode) {
        addSelection(newNode, true);
    }

    private void deselectAll(boolean notify) {
        if (selectedNodes.isEmpty()) {
            return;
        }
        selectedNodes.stream().forEach((n) -> {
            n.setSelected(false);
            linkManager.setLinksSelected((Node) n, false);
        });
        if (notify) {
            SelectionManager.clearSelection();
        }
        selectedNodes.clear();
        updatePickers();
    }

    private void deselectAll() {
        deselectAll(true);
    }

    /**
     * Crates a new map node.
     *
     * @param obj The base object
     * @param x if isGuiOrigin is true this is the screen x coordinate otherwise
     * it is the layout x coordinate
     * @param y
     * @param isNew true if the
     * @param isScreenOrigin True if the user places a new object on the screen,
     * false if the new object was created by another Mindliner instance
     * @return The new node
     */
    private MapNode createNode(mlcObject obj, double x, double y, boolean isNew, boolean isScreenOrigin) {
        final MapNode newNode = new MapNode();
        newNode.setObject(obj);

        if (isNew) {
            if (isScreenOrigin) {
                Point2D layout = CoordUtil.inferLayoutCoords(x, y, nodeGroup, globalScale);
                newNode.setLayoutX(layout.getX());
                newNode.setLayoutY(layout.getY());
                newNode.setTranslateX(x - layout.getX());
                newNode.setTranslateY(y - layout.getY());
                newNode.adjustToScale(x, y, globalScale);
            } else {
                Point2D translation = CoordUtil.inferTranslate(x, y, nodeGroup, globalScale);
                newNode.setLayoutX(x);
                newNode.setLayoutY(y);
                newNode.setTranslateX(translation.getX() - x);
                newNode.setTranslateY(translation.getY() - y);
                newNode.setScaleX(globalScale);
                newNode.setScaleY(globalScale);
            }
        } else {
            newNode.setLayoutX(x);
            newNode.setLayoutY(y);
        }
        nodeGroup.getChildren().add(newNode);
        return newNode;
    }

    private MapContainer createMapContainer(double x, double y, String color, String label, boolean isScreenOrigin) {
        MapContainer newContainer;
        if (isScreenOrigin) {
            Point2D layout = CoordUtil.inferLayoutCoords(x, y, nodeGroup, globalScale);
            double translateX = x - layout.getX();
            double translateY = y - layout.getY();
            newContainer = new MapContainer(layout.getX(), layout.getY(), color, label);
            newContainer.setTranslateX(translateX);
            newContainer.setTranslateY(translateY);
            newContainer.initDrag(x, y);
        } else {
            Point2D translation = CoordUtil.inferTranslate(x, y, nodeGroup, globalScale);
            newContainer = new MapContainer(x, y, color, label);
            newContainer.setTranslateX(translation.getX() - x);
            newContainer.setTranslateY(translation.getY() - y);
            newContainer.initDrag(translation.getX(), translation.getY());
        }
        nodeGroup.getChildren().add(newContainer);
        return newContainer;
    }

    @FXML
    private void onDragEntered(DragEvent event) {
        deselectAll();
        event.acceptTransferModes(TransferMode.ANY);
        tempElement = null;
    }

    /**
     * Returns a list of files that are dragged onto the view or an empty list
     * if no files are dragged
     *
     * @param event The drag event
     * @return A list of files or an empty list
     */
    private List<File> filesDraged(DragEvent event) {
        List<File> fileList = new ArrayList<>();
        Iterator<DataFormat> it = event.getDragboard().getContentTypes().iterator();
        DataFormat ct = it.next();
        if (ct.equals(DataFormat.FILES)) {
            if (event.getDragboard().getContent(ct) instanceof File) {
                fileList.add((File) event.getDragboard().getContent(ct));
            } else if (event.getDragboard().getContent(ct) instanceof List) {
                fileList = (List<File>) event.getDragboard().getContent(ct);
            }
        }
        return fileList;
    }

    @FXML
    private void onDragOver(DragEvent event) {
        if (mapObject == null) {
            event.acceptTransferModes(TransferMode.NONE);
        } else {
            event.acceptTransferModes(TransferMode.ANY);
        }
        double ex = event.getSceneX();
        double ey = event.getSceneY();
//        if (filesDraged(event).isEmpty()) {
        if (tempElement == null) {
            // Use template object as the DnD content is not available here (maybe Swing <-> JavaFX interface bug)
            mlcObject obj = new mlcKnowlet();
            obj.setHeadline("Drop it as desired");
            tempElement = createNode(obj, ex, ey, true, true);
            tempElement.getMover().init(ex, ey);
        } else {
            tempElement.getMover().move(ex, ey);
        }
//    }
    }

    @FXML
    private void onDragExited(DragEvent event) {
        if (tempElement != null) {
            nodeGroup.getChildren().remove(tempElement);
            tempElement = null;
        }
    }

    @FXML
    private void onDragDropped(DragEvent event) {
//        List<File> filesDraged = filesDraged(event);
//        if (!filesDraged.isEmpty()) {
        /**
         * I cannot show a Swing dialog from the JavaFX thread which is why the
         * method that we use in the mind mapper does not work here. We need to
         * create a new dialog in JavaFX or find another way.
         */
//            System.err.println("Sorry: We cannot drag files into the worksphere map directly for now. Please first drag all your resources into the mind map and later drag those new into your worksphere map.");
//                if (!FileUploadHelper.uploadFiles(filesDraged, null)) {
//                    return;
//                }
//                else{
//                    
//                }
//        } else {
        Iterator<DataFormat> it = event.getDragboard().getContentTypes().iterator();
        // second element is the mindliner object, so skip the first
        it.next();
        DataFormat ct = it.next();
        List<?> c1 = (List<?>) event.getDragboard().getContent(ct);
        if (c1 != null && !c1.isEmpty() && c1.get(0) instanceof mlcObject) {
            mlcObject obj = (mlcObject) c1.get(0);
            ContainerMapElement onMap = findMapElement(obj.getId());
            if (onMap != null) {
                // currently we do not support multiple node instances of an object
                // therefore we simply select the already existing node
                setSelection(onMap);
                nodeGroup.getChildren().remove(tempElement);
            } else if (mapObject.equals(obj)) {
                // a map object must not "contain" itself
                nodeGroup.getChildren().remove(tempElement);
            } else {
                tempElement.setObject(obj);
                finishTempNode(tempElement.getMover().getCurrIntersections());
            }
        }
//        }
        // reset for next DnD
        tempElement.getMover().reset();
        tempElement = null;
    }

    @FXML
    private void onScroll(ScrollEvent event) throws NonInvertibleTransformException {
        if (nodeGroup.getChildren().isEmpty()) {
            return;
        }
        double zoomFactor = event.getDeltaY() < 0 ? 0.95 : 1.05;
        double oldScale = globalScale;
        double scale = oldScale;
        scale *= zoomFactor;
        double f = (scale / oldScale) - 1;
        globalScale = scale;
        // Same as panning, we scale each node on the map by itself. Scaling on the group directly 
        // would apply the scale on each new node automatically, which is not wanted (e.g. when drawing a container...).       
        for (Node child : nodeGroup.getChildren()) {
            if (child instanceof MapLink) {
                // Links are always recomputed to the boundsInParent of the source and target node.
                // therefore we do not apply any transformation on them.
                ((MapLink) child).setLabelScale(scale);
                continue;
            }
            // compute difference between current node and the mouse cursor
            double dx = event.getSceneX() - (child.getBoundsInParent().getWidth() / 2 + child.getBoundsInParent().getMinX());
            double dy = event.getSceneY() - (child.getBoundsInParent().getHeight() / 2 + child.getBoundsInParent().getMinY());

            child.setScaleX(scale);
            child.setScaleY(scale);
            // the scale pivot is the current mouse cursor. therefore translate each node according to their distance to the mouse cursor.
            child.setTranslateX(child.getTranslateX() - f * dx);
            child.setTranslateY(child.getTranslateY() - f * dy);
        }
        linkManager.recomputeLinks();
    }

    @FXML
    private void onKeyReleased(KeyEvent event) {
        if (null != event.getCode()) {
            switch (event.getCode()) {
                case DELETE:
                    if (!selectedNodes.isEmpty()) {
                        selectedNodes.stream().forEach((s) -> {
                            removeNode(s, true);
                        });

//                        EventQueue.invokeLater(() -> {
//                            selectedNodes.stream().forEach((s) -> {
//                                removeNode(s, true);
//                            });
//                            deselectAll();
//                        });
                    }
                    break;
                case V:
                    if (!selectedNodes.isEmpty()) {
                        List<mlcObject> objs = getSelectedElements(ContainerMapElement.class);
                        if (!objs.isEmpty()) {
                            EventQueue.invokeLater(() -> {
                                MlViewDispatcherImpl.getInstance().display(objs, MlObjectViewer.ViewType.Map);
                            });
                        }
                    }
                    break;
                case W:
                    if (!selectedNodes.isEmpty()) {
                        List<mlcObject> objs = getSelectedElements(ContainerMapElement.class);
                        if (!objs.isEmpty() && objs.get(0) instanceof MlcContainerMap) {
                            EventQueue.invokeLater(() -> {
                                MlViewDispatcherImpl.getInstance().display(objs, MlObjectViewer.ViewType.ContainerMap);
                            });
                        }
                    }
                    break;
                    
                case E:
                    if (!selectedNodes.isEmpty()) {
                        EventQueue.invokeLater(() -> {
                            ObjectEditorLauncher.showEditor(getSelectedElements(ContainerMapElement.class));
                        });
                    }
                    break;

                default:
                    break;
            }
        }
    }

    private boolean removeNode(Object selectedNode, boolean removeOnServer) {
        if (selectedNode instanceof MapNode) {
            // in case of node deletion, we need special actions like removing object positions in map object.
            // therefore we cannot simply use an ObjectDeletionCommand.
            // However map node deletion removes only the node and not the object itself (unlike when deleting a container)

            // remove all connected links
            // important to delete links first and then the node.
            // otherwise when undoing, one would undo the link deletion before undoing the node deletion, which would not work.
            revomeLinksForNode((Node) selectedNode, removeOnServer);

            if (removeOnServer) {
                MapNode mapNode = (MapNode) selectedNode;
                Bounds bip = mapNode.getBoundsInParent();
                List<MapContainer> intrs = getIntersections(MapContainer.class, bip.getMinX(), bip.getMinY(), bip.getWidth(), bip.getHeight(), false);
                mlcObject obj = mapNode.getObject();
                CMDeleteCommand cmd = new CMDeleteCommand(obj, mapObject, mapNode.getLayoutX(), mapNode.getLayoutY(), intrs);
                addCommand(cmd);
            }

        } else if (selectedNode instanceof MapContainer) {
            revomeLinksForNode((Node) selectedNode, removeOnServer);
            if (removeOnServer) {
                MapContainer cont = (MapContainer) selectedNode;
                relinkToMap(cont);
                mlcObject obj = cont.getObject();
                ObjectDeletionCommand cmd = new ObjectDeletionCommand(obj);
                addCommand(cmd);
                List<mlcObject> objL = new ArrayList<>();
                objL.add(obj);
                ObjectChangeManager.objectsDeleted(objL);
            }
        } else if (selectedNode instanceof MapLink) {
            MapLink link = (MapLink) selectedNode;
            if (removeOnServer) {
                updateLink(link, false);
            }
            linkManager.removeLink(link);
        } else {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, "Unexpected node class selected for removal: {0}", selectedNode.getClass());
            return false;
        }
        nodeGroup.getChildren().remove((Node) selectedNode);
        return true;
    }

    void revomeLinksForNode(Node selectedNode, boolean removeOnServer) {
        List<MapLink> links = linkManager.getLinks((Node) selectedNode);
        for (MapLink link : links) {
            linkManager.removeLink(link);
            if (removeOnServer) {
                updateLink(link, false);
            }
        }
    }

    // normally a node is linked to the parent containers. if it is not contained in any container, we link it directly to the root map object.
    // Therefore if we delete a container, we have to check if we need to link the node to the map.
    private void relinkToMap(MapContainer toDelete) {
        for (Node node : nodeGroup.getChildren()) {
            if (node instanceof MapNode) {
                List<MapContainer> parents = getParentContainers((MapNode) node);
                if (parents.size() == 1 && parents.get(0).equals(toDelete)) {
                    LinkCommand cmd = new LinkCommand(mapObject, ((MapNode) node).getObject(), false, LinkRelativeType.CONTAINER_MAP);
                    addCommand(cmd);
                }
            }
        }
    }

    private List<MapContainer> getParentContainers(MapNode node) {
        List<MapContainer> res = new ArrayList<>();
        for (Node cont : nodeGroup.getChildren()) {
            if (cont instanceof MapContainer) {
                if (cont.getBoundsInParent().intersects(node.getBoundsInParent())) {
                    res.add((MapContainer) cont);
                    break;
                }
            }
        }
        return res;
    }

    @FXML
    private void createNewButtonAction(ActionEvent event) {
        MindlinerMain.getInstance().editNewMindlinerObject(MlcContainerMap.class, null, "", "");
    }

    // Update container object to desired specs and link any nodes inside the container
    void updateContainer(MapContainer container, List<mlcObject> objInsideBefore, List<mlcObject> objsInsideNow) {
        Bounds b = container.getLayoutBounds();
        int x = (int) Math.round(b.getMinX()); // LayoutBounds for Containers are rect.layoutX-0.5/rect.layoutY-0.5. Therefore round up to not lose a pixel each movement
        int y = (int) Math.round(b.getMinY());
        int width = (int) container.getWidth();
        int height = (int) container.getHeight();
        MltContainer newSpecs = new MltContainer(x, y, width, height, container.getColorString(), container.getStrokeWidth(), container.getContainerOpacity(), container.getStrokeStyle());
        newSpecs.setHeadline(container.getLabel());
        MlcContainer obj = container.getObject();
        CMUpdateContainerCommand updateCmd = new CMUpdateContainerCommand(obj, mapObject, newSpecs, objsInsideNow, objInsideBefore);
        addCommand(updateCmd);
    }

    void updateObjectHeadline(mlcObject obj, String text) {
        TextUpdateCommand cmd = new TextUpdateCommand(obj, text, null);
        addCommand(cmd);
    }

    @FXML
    private void onMousePressed(MouseEvent event) {
        if (tempElement != null) {
            return;
        }

        if (isCursorMode()) {
            if (event.isSecondaryButtonDown()) {
                double ex = event.getSceneX();
                double ey = event.getSceneY();
                currContainer = createMapContainer(ex, ey, FOCUS_RECTANGLE_COLOR, "", true);
                currContainer.setContainerOpacity(0.05);
                currContainer.setStrokeWidth(1);
            } else if (!selectedNodes.isEmpty()) {
                selectedNodes.stream().filter((s) -> (s instanceof ContainerMapElement)).forEach((s) -> {
                    ((ContainerMapElement) s).onMousePressed(event);
                });
            } else {
                // initialize scenery pan
                lastPanX = event.getSceneX();
                lastPanY = event.getSceneY();
//                System.out.println("last pan updated to x = " + lastPanX + ", y = " + lastPanY);
            }
        } else if (isContainerMode()) {
            if (event.isSecondaryButtonDown()) {
                // initialize scenery pan (allow panning in container mode by using right mouse button)
                lastPanX = event.getSceneX();
                lastPanY = event.getSceneY();
            } else {
                double ex = event.getSceneX();
                double ey = event.getSceneY();
                Color color = containerSelection.getValue().getColor();
                currContainer = createMapContainer(ex, ey, ContainerMapUtils.colorToString(color), containerSelection.getValue().getLabel(), true);
            }
        } else if (isLinkMode()) {
            // nothing
        }
    }

    @FXML
    private void onMouseDragged(MouseEvent event) {
        if (isCursorMode()) {
            if (event.isSecondaryButtonDown()) {
                updateFocusRectangle(event);
            } else if (tempElement != null) {
                tempElement.getMover().move(event.getSceneX(), event.getSceneY());
            } else if (!selectedNodes.isEmpty() && !event.isStillSincePress()) {
                selectedNodes.stream().filter((s) -> (s instanceof ContainerMapElement)).forEach((s) -> {
                    ((ContainerMapElement) s).onMouseDragged(event);
                });
            } else if (selectedNodes.isEmpty() && !nodeGroup.getChildren().isEmpty()) {
                interactivelyPanSceneryTo(event.getX(), event.getY());
            }
        } else if (isContainerMode()) {
            if (tempElement == null) { // for safety, in case user starts dragging while having a temporary node from the object-shelf
                if (event.isSecondaryButtonDown() && !nodeGroup.getChildren().isEmpty()) {
                    interactivelyPanSceneryTo(event.getX(), event.getY());
                } else {
                    currContainer.mouseDragged(event.getSceneX(), event.getSceneY());
                }
            }
        } else if (isLinkMode()) {
            linkManager.mouseDragged(event);
        }
    }

    @FXML
    private void onMouseReleased(MouseEvent event) {
        if (tempElement != null) {
            if (event.getButton() == MouseButton.SECONDARY) {
                removeTempElement();
            } else { // either drag-copy or new node from node-shelf search list dropped
                finishTempElement();
            }
            // unfocus all possible textfields etc.
            mapPane.requestFocus();
            return;
        }

        if (isCursorMode()) {
            if (event.getButton() == MouseButton.SECONDARY) { // event.isSecondaryButtonDown() not true anymore as button is released...
                // remove focus rectangle
                nodeGroup.getChildren().remove(currContainer);
            } else if (!selectedNodes.isEmpty() && !event.isStillSincePress()) {
                // finish pan of a group of elements                
                selectedNodes.stream().filter((s) -> (s instanceof ContainerMapElement)).forEach((s) -> {
                    ((ContainerMapElement) s).onMouseReleased(event);
                });
            } else {
                // finish scenery pan
                deselectAll();
                linkManager.clear();
            }
        } else if (isContainerMode()) {
            if (event.getButton() == MouseButton.SECONDARY) {
                // finish scenery pan
                deselectAll();
                linkManager.clear();
            } else {
                currContainer.finishCreation(globalScale, true);
                // Create container object and link to map
                ObjectCreationCommand cmd = new ObjectCreationCommand(mapObject, MlcContainer.class, currContainer.getLabel(), "", LinkRelativeType.CONTAINER_MAP);
                addCommand(cmd);
                MlcContainer cont = (MlcContainer) cmd.getObject();
                currContainer.setObject(cont);
                finishTempContainer(currContainer);
                setCursorMode();
                currContainer.toBack();
            }
        } else if (isLinkMode()) {
            // nothing. mouse link events that must be processed are received by the nodes
        }

        // unfocus all possible textfields etc.
        mapPane.requestFocus();
    }

    /**
     * This function assumes that the mouse has been pressed interactively and
     * cannot be used to move the scenery according to a selection event that
     * happened in another part of the program
     *
     * @param newX
     * @param newY
     */
    void interactivelyPanSceneryTo(double newX, double newY) {
        double dx = newX - lastPanX;
        double dy = newY - lastPanY;
        panSceneryBy(dx, dy);
        lastPanX += dx;
        lastPanY += dy;
//        System.out.println("new lastPan X = " + lastPanX + ", Y = " + lastPanY);
    }

    void panSceneryBy(double dx, double dy) {
//        System.out.println("panning by x = " + dx + ", y = " + dy);
        // we pan each child in the group by itself instead of panning simply the parent group.
        // This is for the reason that any new nodes that we add to the group would inherit the panning transformation from the parent,
        // making them appear at another location than they were dropped/created.
        for (Node child : nodeGroup.getChildren()) {
            if (child instanceof MapLink) {
                // Links are always recomputed to the boundsInParent of the source and target node.
                // therefore we do not apply any transformation on them.
                continue;
            }
            // for complete map panning we only set the translate parameters, as this should not influence the values we use to store at server side (layoutX/Y)
            child.setTranslateX(child.getTranslateX() + dx);
            child.setTranslateY(child.getTranslateY() + dy);
        }
        linkManager.recomputeLinks();
    }

    void finishTempElement() {
        mlcObject obj = tempElement.getObject();
        String headline = obj.getHeadline();
        ObjectCreationCommand objectCreationCommand = new ObjectCreationCommand(tempElement instanceof MapContainer ? mapObject : null, tempElement.getObject().getClass(), headline, obj.getDescription(), LinkRelativeType.CONTAINER_MAP);
        addCommand(objectCreationCommand);
        // ensure that new object inherits data pool from WSM
        DataPoolUpdateCommand dataPoolUpdateCommand = new DataPoolUpdateCommand(objectCreationCommand.getObject(), mapObject.getClient());
        addCommand(dataPoolUpdateCommand);
        if (obj.getIcons() != null) {
            for (MlcImage icon : obj.getIcons()) {
                LinkCommand linkcmd = new LinkCommand(objectCreationCommand.getObject(), icon, true, LinkRelativeType.ICON_OBJECT);
                addCommand(linkcmd);
            }
        }
        tempElement.setObject(objectCreationCommand.getObject());
        tempElement.getObject().setIcons(obj.getIcons());
        ((Node) tempElement).setMouseTransparent(false); // is set to true while moving such that the root pane receives the drag events
        if (tempElement instanceof MapNode) {
            List<ContainerMapElement> intrs = tempElement.getMover().getCurrIntersections();
            finishTempNode(intrs);
        } else if (tempElement instanceof MapContainer) {
            finishTempContainer((MapContainer) tempElement);
        }

        tempElement.getMover().reset();
        tempElement = null;
        isCreatingNode = false;
    }

    private void finishTempNode(List<ContainerMapElement> intrs) {
        List<Integer> parents = new ArrayList<>();
        // link the new node with all its enclosing container except if its a
        // container map nodes: for these we must not link them because it would add the enclosing containers to the map represented by the node
        if (!(tempElement.getObject() instanceof MlcContainerMap)) {
            intrs.stream().forEach((cont) -> {
                parents.add(cont.getObject().getId());
            });
        }
        if (parents.isEmpty()) {
            parents.add(mapObject.getId());
        }

        CMAddCommand cmd = new CMAddCommand(mapObject, tempElement.getObject(), parents, (int) ((Node) tempElement).getLayoutX(), (int) ((Node) tempElement).getLayoutY());
        addCommand(cmd);
    }

    private void finishTempContainer(MapContainer c) {
        // link any nodes inside the new container to the container
        List<mlcObject> objsInside = new ArrayList<>();
        Bounds pb = c.getBoundsInParent();
        List<MapNode> nodes = getIntersections(MapNode.class, pb.getMinX(), pb.getMinY(), pb.getWidth(), pb.getHeight(), false);
        // filter out Container Maps as we must not link them to containers since these containers would otherwise show up in the container map represented by the node
        nodes.stream().filter((n) -> (!(n.getObject() instanceof MlcContainerMap))).forEach((node) -> {
            objsInside.add(node.getObject());
        });
        updateContainer(c, null, objsInside);
    }

    public ContainerMapElement findMapElement(int objId) {
        for (Node node : nodeGroup.getChildren()) {
            if (node instanceof ContainerMapElement) {
                ContainerMapElement element = (ContainerMapElement) node;
                if (element.getObject().getId() == objId) {
                    return element;
                }
            }
        }
        return null;
    }

    void setContainerProps(List<ContainerProperties> containerProps) {
        // for debugging purposes to set a fixed set of container types
        templateContainers = containerProps;
        containerSelection.setItems(FXCollections.observableList(containerProps));
        containerSelection.setButtonCell(new MapContainerListCell(false));
        containerSelection.setCellFactory((ListView<ContainerProperties> param) -> new MapContainerListCell(true));
        containerSelection.setValue(containerProps.get(0));
    }

    public void addCommand(MindlinerOnlineCommand cmd) {
        addCommand(cmd, false);
    }

    public void addCommand(MindlinerOnlineCommand cmd, boolean executeSilently) {
        try {
            if (executeSilently) {
                cmd.execute();
            } else {
                CommandRecorder cr = CommandRecorder.getInstance();
                cr.scheduleCommand(cmd);
            }
        } catch (mlModifiedException | NamingException | MlAuthorizationException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, "Command execution failed", ex);
        }
    }

    private void updatePickers() {
        if (iconPicker == null) {
            return;
        }
        List<mlcObject> objs = getSelectedElements(MapNode.class);
        iconPicker.setObjects(objs);

        if (objs.size() == 1) {
            typePicker.setObject(objs.get(0));
        } else {
            typePicker.setObject(null);
        }
    }

    private List<mlcObject> getSelectedElements(Class clazz) {
        List<mlcObject> objs = new ArrayList<>();
        selectedNodes.stream().filter((n) -> (clazz.isInstance(n))).forEach((o) -> {
            objs.add(((ContainerMapElement) o).getObject());
        });
        return objs;
    }

    @FXML
    private void onMouseMoved(MouseEvent event) {
        if (isCreatingNode) {
            tempElement.getMover().move(event.getSceneX(), event.getSceneY());
        }
    }

    @FXML
    private void onMouseExited(MouseEvent event) {
        removeTempElement();
    }

    @FXML
    private void onMouseEnteredIconPicker(MouseEvent event) {
        removeTempElement();
        IconPickerPane.setPrefHeight(200);
    }

    @FXML
    private void onMouseEnteredTypePicker(MouseEvent event) {
        removeTempElement();
    }

    @FXML
    private void onMouseExitedIconPicker(MouseEvent event) {
        if (iconPicker == null) {
            return;
        }
        IconPickerPane.setPrefHeight(50);
        if (!iconPicker.getSelectedIcons().isEmpty()) {
            createPickerNode(event);
        }
        // unfocus from picker
        mapPane.requestFocus();
    }

    @FXML
    private void onMouseExitedTypePicker(MouseEvent event) {
        if (typePicker == null) {
            return;
        }
        if (typePicker.getType() != null) {
            createPickerNode(event);
        }
        mapPane.requestFocus();
    }

    @FXML
    private void initZoomAndPan() {
        if (mapObject == null) {
            return;
        }
        MlViewDispatcherImpl.getInstance().display(mapObject, MlObjectViewer.ViewType.ContainerMap);
    }

    void updateFocusRectangle(MouseEvent event) {
        // focus rectangle (i.e. rubberband)
        currContainer.mouseDragged(event.getSceneX(), event.getSceneY());
        Bounds pb = currContainer.getBoundsInParent();
        List<MapNode> nodes = getIntersections(MapNode.class, pb.getMinX(), pb.getMinY(), pb.getWidth(), pb.getHeight(), true);
        List<MapContainer> containers = getIntersections(MapContainer.class, pb.getMinX(), pb.getMinY(), pb.getWidth(), pb.getHeight(), true);
        containers.remove(currContainer);
        List<Selectable> toSelect = new ArrayList<>(nodes);
        toSelect.addAll(containers);
        setSelection(toSelect, true);
    }

    private void createPickerNode(MouseEvent event) {
        if (selectedNodes.isEmpty()) {
            List<MlcImage> icons = iconPicker.getSelectedIcons();
            MlClassHandler.MindlinerObjectType type = typePicker.getType();
            mlcObject obj = null;
            if (type != null) {
                try {
                    obj = (mlcObject) MlClientClassHandler.getClassByType(type).newInstance();
                } catch (InstantiationException | IllegalAccessException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (obj == null) {
                obj = new mlcKnowlet();
            }
            obj.setIcons(icons);
            MlClassHandler.MindlinerObjectType t = MlClientClassHandler.getTypeByClass(obj.getClass());
            String name = MlClientClassHandler.getNameByType(t);
            obj.setHeadline(name);
            obj.setDescription("");
            double ex = event.getSceneX();
            double ey = event.getSceneY();
            tempElement = createNode(obj, ex, ey, true, true);
            ((Node) tempElement).setMouseTransparent(true);
            tempElement.getMover().init(ex, ey);
            isCreatingNode = true;
        }
    }

    public void initDragCopy(MapNode ma, MouseEvent event) {
        if (tempElement == null) {
            try {
                mlcObject oldo = ma.getObject();
                double x = ma.getBoundsInParent().getMinX();
                double y = ma.getBoundsInParent().getMinY();
                mlcObject newObj = oldo.getClass().newInstance();
                newObj.setHeadline(oldo.getHeadline());
                newObj.setDescription(oldo.getDescription());
                newObj.setIcons(oldo.getIcons());
                tempElement = createNode(newObj, x, y, true, true);
                ((Node) tempElement).setMouseTransparent(true);
                tempElement.getMover().init(event.getSceneX(), event.getSceneY());
            } catch (InstantiationException | IllegalAccessException ex) {
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                // TODO proper notification
            }
        }
    }

    public void initDragCopyContainer(MapContainer c, MouseEvent event) {
        if (tempElement == null) {
            Bounds bip = c.getBoundsInParent();
            MapContainer newc = createMapContainer(bip.getMinX(), bip.getMinY(), c.getColor(), c.getLabel(), true);
            mlcObject oldo = c.getObject();
            mlcObject newObj;
            try {
                newObj = oldo.getClass().newInstance();
                newObj.setHeadline(oldo.getHeadline());
                newObj.setDescription(oldo.getDescription());
                newObj.setIcons(oldo.getIcons());
                newc.setObject(newObj);
                newc.setContainerOpacity(c.getContainerOpacity());
                newc.setStrokeStyle(c.getStrokeStyle());
                newc.setStrokeWidth(c.getStrokeWidth());
                // important to use the width of the boundsInParents instead of c.getWidth which is the unscaled with.
                // finishCreation will then apply the scale to the container
                newc.setWidth(bip.getWidth());
                newc.setHeight(bip.getHeight());
                newc.finishCreation(globalScale, true);
                newc.setMouseTransparent(true);
                newc.getMover().init(event.getSceneX(), event.getSceneY());
                tempElement = newc;
            } catch (InstantiationException | IllegalAccessException ex) {
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                // TODO proper notification
            }
        }
    }

    private void removeTempElement() {
        if (tempElement != null) {
            tempElement.getMover().reset();
            nodeGroup.getChildren().remove(tempElement);
            isCreatingNode = false;
            tempElement = null;
        }
    }

    @FXML
    private void cursorToggleAction(ActionEvent event) {
        if (!containerToggleButton.isSelected()) {
            cursorToggleButton.setSelected(true);
        }
    }

    @FXML
    private void drawToggleAction(ActionEvent event) {
        deselectAll();
        if (!containerToggleButton.isSelected()) {
            cursorToggleButton.setSelected(true);
        }
    }

    @FXML
    private void lineToggleAction(ActionEvent event) {
        deselectAll();
        if (!linkToggleButton.isSelected()) {
            cursorToggleButton.setSelected(true);
        }
    }

    /**
     * This method applies changes to the
     *
     * @param node
     */
    private void updateElementProperties(ContainerMapElement node) {
        // we rely here on the fact that the mapObject is always first updated and afterwards the node (see ContainerMapManager.updateNode).
        // That's why we can access the new object position here.
        if (node instanceof MapNode) {
            MapNode n = (MapNode) node;
            mlcObject obj = n.getObject();
            for (MltContainermapObjectPosition pos : mapObject.getObjectPositions()) {
                if (pos.getObjectId() == obj.getId()) {
                    // changing the layout in a panned/zoomed environment requires also updating the translations
                    n.getMover().updateLayout(pos.getPosX(), pos.getPosY());
                }
            }
        } else if (node instanceof MapContainer) {
            // TODO: refactor. width and height should be set in MapContainer.setObject as is layoutx and layouty.
            // however the width and height are unset in some cases when setObject is called.
            MapContainer c = (MapContainer) node;
            if (c.getObject().getWidth() > 0) {
                applySizeChangeAndCorrectForTranslation(c);
            }
        }
        linkManager.recomputeLink((Node) node);
    }

    private void applySizeChangeAndCorrectForTranslation(MapContainer c) {
        double unscaledDeltaX = c.getObject().getPosX() - c.getBoundsInLocal().getMinX();
        double unscaledDeltaY = c.getObject().getPosY() - c.getBoundsInLocal().getMinY();
        double startTx = c.getTranslateX();
        double startTy = c.getTranslateY();

        c.setWidth(c.getObject().getWidth());
        c.setHeight(c.getObject().getHeight());

        double tx = unscaledDeltaX * (globalScale - 1) / 2;
        double ty = unscaledDeltaY * (globalScale - 1) / 2;
        c.setTranslateX(startTx + tx);
        c.setTranslateY(startTy + ty);
    }

    private interface TwoArgOperation {

        public void operate(ContainerMapElement node, mlcObject obj);
    }

    /**
     * Applies the operate function of the operator to the node in the map that
     * as the object with the specified id.
     */
    private boolean iterateObjects(TwoArgOperation operator, mlcObject o, int id) {
        for (Node node : nodeGroup.getChildren()) {
            if (node instanceof ContainerMapElement) {
                ContainerMapElement cme = (ContainerMapElement) node;
                if (cme.getObject() != null && cme.getObject().getId() == id) { // maybe there is temporary object (e.g. focus rectangle) without object
                    Platform.runLater(() -> operator.operate(cme, o));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void objectChanged(mlcObject o) {
        if (mapObject == null) {
            return;
        }
        if (o.equals(mapObject)) {
            Platform.runLater(() -> {
                mapObject = (MlcContainerMap) o;
                // updating a link is done through the mapObject as there is no mlcObject for a MapLink
                linkManager.updateLinks(mapObject.getObjLinks());
            });
            return;
        }
        boolean found = iterateObjects((node, obj) -> {
            node.setObject(obj);
            updateElementProperties(node);
        }, o, o.getId());
        if (!found) {
            // Remotly created MapNodes/MapContainers on the same CM are not yet drawn and must be displayed
            if (o instanceof MlcContainer) {
                List<mlcObject> relatives = CacheEngineStatic.getLinkedObjects(mapObject);
                for (mlcObject relative : relatives) {
                    // After container obj creation, color is null. We wait for the Change Update Message induced by CMContainerUpdateCommand
                    if (relative instanceof MlcContainer && o.equals(relative) && ((MlcContainer) o).getColor() != null) {
                        MlcLink link = CacheEngineStatic.getLink(mapObject.getId(), relative.getId());
                        if (LinkRelativeType.CONTAINER_MAP.equals(link.getRelativeType())) {
                            Platform.runLater(() -> displayNewForeignContainer((MlcContainer) relative));
                            return;
                        }
                    }
                }
            } else {
                for (MltContainermapObjectPosition objPos : mapObject.getObjectPositions()) {
                    if (objPos.getObjectId() == o.getId()) {
                        Platform.runLater(() -> displayNewForeignObject(o, objPos.getPosX(), objPos.getPosY()));
                        return;
                    }
                }
            }

        }
    }

    @Override
    public void objectDeleted(mlcObject o) {
        if (mapObject == null) {
            return;
        }
        if (o.equals(mapObject)) {
            Platform.runLater(() -> clear());
            return;
        }
        iterateObjects((node, obj) -> {
            removeNode((Node) node, false);
        }, o, o.getId());
    }

    @Override
    public void objectReplaced(int oldId, mlcObject o) {
        if (mapObject == null) {
            return;
        }
        iterateObjects((node, obj) -> {
            node.setObject(obj);
        }, o, oldId);
    }

    @Override
    public void objectCreated(mlcObject o) {
        // only become active if a new container map is created elsewhere and there is no current container map
        if (o instanceof MlcContainerMap && mapObject == null) {
            MlViewDispatcherImpl.getInstance().display(o, MlObjectViewer.ViewType.ContainerMap);
        }
    }

    LinkManager getLinker() {
        return linkManager;
    }

    boolean isSelected(Selectable node) {
        return selectedNodes.contains(node);
    }

    boolean isContainerMode() {
        return containerToggleButton.isSelected();
    }

    boolean isLinkMode() {
        return linkToggleButton.isSelected();
    }

    boolean isCursorMode() {
        return cursorToggleButton.isSelected();
    }

    void setCursorMode() {
        cursorToggleButton.setSelected(true);
    }

    public Group getNodeGroup() {
        return nodeGroup;
    }

    public Stage getStage() {
        return (Stage) nodeGroup.getScene().getWindow();
    }

    public void updateContainerCombo() {
        ContainerProperties sel = containerSelection.getValue();
        containerSelection.setItems(null);
        containerSelection.setItems(FXCollections.observableList(templateContainers));
        containerSelection.getSelectionModel().select(sel);
    }

    @Override
    public void selectionChanged(List<mlcObject> selection) {
        if (mapObject == null) {
            return;
        }
        List<Selectable> elements = new ArrayList<>();
        if (selection != null) {
            selection.stream().forEach((e) -> {
                ContainerMapElement fe = findMapElement(e.getId());
                if (fe != null) {
                    elements.add(fe);
                }
            });
        }
        setSelection(elements, false);
//        centerOnSelection();
    }

    private void centerOnSelection() {
        MapNode firstNode = null;
        for (int i = 0; i < selectedNodes.size() && firstNode == null; i++) {
            Selectable s = selectedNodes.get(i);
            if (s instanceof MapNode) {
                firstNode = (MapNode) s;
            }
        }
        // center the first selected object on the screen
        if (firstNode != null) {
            System.out.println("centering on " + firstNode.getObject().getId()
                    + "\node BIP x = " + firstNode.getBoundsInParent().getMinX()
                    + ", node BIP y = " + firstNode.getBoundsInParent().getMinY());
            double screenCenterX = rootPane.getWidth() / 2;
            double screenCenterY = rootPane.getHeight() / 2;
            double nodeCenterX = (firstNode.getBoundsInParent().getMaxX() - firstNode.getBoundsInParent().getMinX()) / 2;
            double nodeCenterY = (firstNode.getBoundsInParent().getMaxY() - firstNode.getBoundsInParent().getMinY()) / 2;
            double tx = screenCenterX - nodeCenterX;
            double ty = screenCenterY - nodeCenterY;
            interactivelyPanSceneryTo(tx, ty);
        }
    }

    @Override
    public void clearSelections() {
        deselectAll(false);
    }

    @Override
    public void connectionSelectionChanged(List<NodeConnection> selection) {
    }

    @Override
    public void clearConnectionSelections() {
    }
}
