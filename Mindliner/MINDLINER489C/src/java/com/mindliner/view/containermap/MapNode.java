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
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.entities.Colorizer;
import com.mindliner.gui.ObjectEditorLauncher;
import com.mindliner.gui.color.BaseColorizer;
import com.mindliner.gui.color.ColorManager;
import com.mindliner.gui.color.FixedKeyColorizer;
import com.mindliner.img.icons.MlIconManager;
import com.mindliner.image.IconLoader;
import com.mindliner.image.IconLoader.ObjecCallbackTuple;
import com.mindliner.view.dispatch.MlObjectViewer;
import com.mindliner.view.dispatch.MlViewDispatcherImpl;
import java.awt.EventQueue;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

/**
 * The JavaFX element for an object in the container map. Handles dragging of
 * the node.
 *
 * @author Dominic Plangger
 */
public class MapNode extends VBox implements ContainerMapElement {

    private static final int MAX_LABEL_WIDTH = 80;
    private static final int MAX_EDITOR_WIDTH = 130;
    private static final int MAX_EDITOR_HEIGHT = 90;
    private static final int MAX_LABEL_HEIGHT = 75;
    private static final int EXTRA_CONNECTION_INDICATOR_SIZE = 5;

    private final FXMLController mapController;
    private final ImageView image = new ImageView();
    private final ContextMenu contextMenu = new ContextMenu();
    private final Label label = new Label();
    private final TextArea labelEditor = new TextArea();
    private final Rectangle extraConnectionIndicator = new Rectangle();
    private final ResourceBundle bundle = ResourceBundle.getBundle("com/mindliner/resources/WorksphereMap");

    private mlcObject object;
    private final ElementMover<MapContainer> mover;

    public MapNode() {
        mapController = FXMLController.getInstance();
        setOnMouseReleased((MouseEvent me) -> {
            onMouseReleased(me);
        });
        setOnMouseDragged((MouseEvent me) -> {
            onMouseDragged(me);
        });
        setOnMousePressed((MouseEvent me) -> {
            onMousePressed(me);
        });
        setOnMouseExited((MouseEvent me) -> {
            onMouseExited(me);
        });

        mover = new ElementMover<>(this, MapContainer.class);

        addNodeElements();
    }

    private final void addNodeElements() {
        label.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2 && mapController.isCursorMode()) {
                labelEditor.setText(label.getText());
                labelEditor.setLayoutX(label.getLayoutX());
                labelEditor.setLayoutY(label.getLayoutY());
                replaceNode(label, labelEditor);
                labelEditor.selectAll();
                labelEditor.requestFocus();
            }
        });

        labelEditor.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (!newValue && getChildren().contains(labelEditor)) {
                // TODO refactor. use visibility like in MapLink instead of adding/removing the nodes
                label.setText(labelEditor.getText());
                replaceNode(labelEditor, label);
                mapController.updateObjectHeadline(object, label.getText());
            }
        });

        labelEditor.setOnKeyReleased((KeyEvent event) -> {
            if (event.getCode().equals(KeyCode.ESCAPE)) {
                replaceNode(labelEditor, label);
            }
        });

        setAlignment(Pos.CENTER);
        label.setMaxHeight(MAX_LABEL_HEIGHT);
        label.setPrefWidth(MAX_LABEL_WIDTH);
        Font f = new Font("System Regular", 10);
        label.setFont(f);
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);
        image.setFitHeight(50);
        image.setFitWidth(50);
        image.setPickOnBounds(true);
        image.setPreserveRatio(true);
        image.setOnMouseReleased((e) -> {
            if (MouseButton.SECONDARY.equals(e.getButton())) {
                contextMenu.show(image, e.getScreenX(), e.getScreenY());
            }
        });
        labelEditor.setMaxWidth(MAX_EDITOR_WIDTH);
        labelEditor.setMaxHeight(MAX_EDITOR_HEIGHT);
        labelEditor.setWrapText(true);

        // the indicator that shows whether or not this object has relatives that are not shown on the WSM
        BaseColorizer fkc = ColorManager.getColorizerForType(Colorizer.ColorDriverAttribute.FixedKey);
        extraConnectionIndicator.setHeight(EXTRA_CONNECTION_INDICATOR_SIZE);
        extraConnectionIndicator.setWidth(EXTRA_CONNECTION_INDICATOR_SIZE);
//        extraConnectionIndicator.setY(1.5 * EXTRA_CONNECTION_INDICATOR_SIZE);
        extraConnectionIndicator.setFill(ContainerMapUtils.convertAwtToFx(fkc.getColorForObject(FixedKeyColorizer.FixedKeys.CONTAINER_EXTRA_RELATIVES_INDICATOR)));
        ResourceBundle bundle = ResourceBundle.getBundle("com/mindliner/resources/WorksphereMap");
        Tooltip tt = new Tooltip(bundle.getString("ExtraLinkIndicatorTT"));
        Tooltip.install(extraConnectionIndicator, tt);
        getChildren().add(extraConnectionIndicator);
        getChildren().add(image);
        getChildren().add(label);
        setSelected(false);
    }

    private void buildContextMenu() {
        
        contextMenu.getItems().clear();

        // if this node is another container map then offer the option to show it as such
        if (getObject() instanceof MlcContainerMap) {
            MenuItem showAsContainerMap = new MenuItem(bundle.getString("ViewAsContainerMap"));
            showAsContainerMap.setOnAction((e) -> {
                List<mlcObject> objs = new ArrayList<>();
                objs.add(getObject());
                EventQueue.invokeLater(() -> {
                    MlViewDispatcherImpl.getInstance().display(objs, MlObjectViewer.ViewType.ContainerMap);
                });
            });
            showAsContainerMap.setAccelerator(new KeyCodeCombination(KeyCode.W));
            contextMenu.getItems().add(showAsContainerMap);
        }

        // show this node as the root node of a mindmap
        MenuItem showAsMindmap = new MenuItem(bundle.getString("ViewAsMindmap"));
        showAsMindmap.setOnAction((e) -> {
            List<mlcObject> objs = new ArrayList<>();
            objs.add(getObject());
            EventQueue.invokeLater(() -> {
                MlViewDispatcherImpl.getInstance().display(objs, MlObjectViewer.ViewType.Map);
            });
        });
        showAsMindmap.setAccelerator(new KeyCodeCombination(KeyCode.V));
        contextMenu.getItems().add(showAsMindmap);

        // open this node in the object editor
        MenuItem openInEditor = new MenuItem(bundle.getString("OpenInEditor"));
        openInEditor.setOnAction((e) -> {
            EventQueue.invokeLater(() -> {
                List<mlcObject> objs = new ArrayList<>();
                objs.add(getObject());
                ObjectEditorLauncher.showEditor(objs);
            });
        });
        openInEditor.setAccelerator(new KeyCodeCombination(KeyCode.E));
        contextMenu.getItems().add(openInEditor);
    }

    private boolean hasExtraRelatives() {
        List<mlcObject> relatives = CacheEngineStatic.getLinkedObjects(object);
        for (mlcObject o : relatives) {
            if (!(o instanceof MlcContainerMap || o instanceof MlcContainer)) {
                return true;
            }
        }
        return false;
    }

    private void replaceNode(Node from, Node to) {
        if (!getChildren().contains(from)) {
            return;
        }
        getChildren().remove(from);
        getChildren().add(to);
    }

    private void onMouseExited(MouseEvent event) {
        if (mapController.isLinkMode() && !event.isPrimaryButtonDown()) {
            setSelected(false);
        }
    }

    @Override
    public void onMouseReleased(MouseEvent event) {
        if (mapController.isLinkMode()) {
            // The mouse released event is triggered on the MapNode that registered the mouse pressed event!
            mapController.finishLink(new Point2D(event.getSceneX(), event.getSceneY()));
            event.consume();
            mapController.setCursorMode();
        } else if (mapController.isContainerMode()) {
            // nothing
        } else if (event.isStillSincePress()) {
            if (event.isControlDown()) {
                mapController.addSelection(this);
            } else {
                mapController.setSelection(this);
            }
            event.consume();
        } else if (mapController.isSelected(this)) {
            mover.finish();
            event.consume();
        }

    }

    @Override
    public void onMouseDragged(MouseEvent event) {
        if (mapController.isSelected(this) && mapController.isCursorMode()) {
            if (event.isControlDown()) {
                mapController.initDragCopy(this, event);
                mapController.setSelection(this);
            } else {
                mover.move(event.getSceneX(), event.getSceneY());
                mapController.getLinker().recomputeLink(this);
                event.consume();
            }
        }
    }

    @Override
    public void onMousePressed(MouseEvent event) {
        if (mapController.isLinkMode()) {
            mapController.createLink(this);
            event.consume();
        } else if (mapController.isContainerMode()) {

        } else if (mapController.isSelected(this)) {
            mover.init(event.getSceneX(), event.getSceneY());
            event.consume();
        }
    }

    public void adjustToScale(double ex, double ey, double zoom) {
        // after creation, we apply the global scale to have the node in the same size as the others.
        // As the scale is around the center of the node, we have to translate it after scaling such that
        // the upper left corner still meets the mouse cursor.
        Platform.runLater(() -> {
            Bounds bip = getBoundsInParent();
            double prefH = bip.getHeight();
            double prefW = bip.getWidth();
            double diffH = (prefH - prefH * zoom) * 0.5;
            double diffW = (prefW - prefW * zoom) * 0.5;
            setScaleX(zoom);
            setScaleY(zoom);
            setTranslateX(ex - getLayoutX() - diffW);
            setTranslateY(ey - getLayoutY() - diffH);
        });
    }

    @Override
    public void setSelected(boolean isSelected) {
        if (isSelected) {
            setStyle("-fx-border-color: #0066FF; -fx-border-style: solid inside; -fx-border-radius: 3; -fx-border-width: 2;");
        } else {
            // keep the border but transparent. otherwise the node is moved by a few pixel if it is selected.
            // alternative would be to use border-style: outside, but then border overlaps the label and image
            setStyle("-fx-border-color: transparent; -fx-border-style: solid inside; -fx-border-radius: 3; -fx-border-width: 2;");
        }

    }

    @Override
    public void setObject(mlcObject object) {
        this.object = object;
        setId(object == null ? "" : Integer.toString(object.getId()));
        label.setText(object.getHeadline());
        extraConnectionIndicator.setVisible(hasExtraRelatives());

        if (object.getIcons() == null) {
            // icons are loaded lazy. Therefore first show type icon until real icon is loaded.
            if (image.getImage() == null) {
                setTypeIcon();
            }
            ObjecCallbackTuple ocb = new ObjecCallbackTuple(object, (mlcObject obj) -> {
                Platform.runLater(() -> {
                    setObject(obj);
                });
            });
            List<ObjecCallbackTuple> ocbs = new ArrayList<>();
            ocbs.add(ocb);
            IconLoader.getInstance().loadIcons(ocbs);
            return;
        }

        if (object.getIcons().isEmpty()) {
            // empty icons mean that the icons have been loaded but no icons are set on the object
            setTypeIcon();
        } else {
            // display first icon
            Image img = object.getIcons().get(0).getIcon().getImage();
            setImage(img);
        }
        // the menu is object dependent - therefore set here
        buildContextMenu();
    }

    private void setTypeIcon() {
        MlClassHandler.MindlinerObjectType type = MlClientClassHandler.getTypeByClass(object.getClass());
        Image img = MlIconManager.getImageForType(type, MlIconManager.IconSize.sixtyfour);
        setImage(img);
    }

    @Override
    public mlcObject getObject() {
        return object;
    }

    private void setImage(Image img) {
        javafx.scene.image.Image fxImg = ContainerMapUtils.awtToFxImage(img);
        image.setImage(fxImg);
    }

    @Override
    public ElementMover getMover() {
        return mover;
    }

}
