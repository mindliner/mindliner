/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.containermap;

import com.mindliner.clientobjects.MlcContainer;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.enums.CMContainerStrokeStyles;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

/**
 * The JavaFX element for a container in the container map.
 *
 * @author Dominic Plangger
 */
public class MapContainer extends Group implements ContainerMapElement {

    private static final int MAX_LABEL_HEIGHT = 70;
    private static final int MAX_EDITOR_SIZE = 130;

    private static final double DEFAULT_STROKE_WIDTH = 2;
    private static final double SELECTED_STROKE_WIDTH = 2;
    private static final double SHADOW_STROKE_WIDTH = 10;
    private static final double HANDLE_SIZE = 10;
    private static final double MIN_SIZE = 15;

    private final FXMLController mapController;
    public final Rectangle rect = new Rectangle();
    private final Rectangle shadow = new Rectangle();
    public final Label label = new Label();
    private final HBox labelBox = new HBox();
    private final Button settingButton = new Button();
    private final TextArea textField = new TextArea();
    private SimpleDoubleProperty currX, currY;
    private double startLx, startLy;
    private double startWidth, startHeight, startX, startY, startTx, startTy;
    private MlcContainer object;
    private final ElementMover<MapNode> mover;
    private final Rectangle ulHandle, urHandle, llHandle, lrHandle;
    private String webColor;
    private double opacity = 0;
    private double strokeWidth = DEFAULT_STROKE_WIDTH;
    private CMContainerStrokeStyles strokeStyle = CMContainerStrokeStyles.SOLID;
    private boolean isSelected;

    public MapContainer(double layoutX, double layoutY, String color, String text) {
        ulHandle = new Rectangle(HANDLE_SIZE, HANDLE_SIZE);
        urHandle = new Rectangle(HANDLE_SIZE, HANDLE_SIZE);
        llHandle = new Rectangle(HANDLE_SIZE, HANDLE_SIZE);
        lrHandle = new Rectangle(HANDLE_SIZE, HANDLE_SIZE);
        mapController = FXMLController.getInstance();
        mover = new ElementMover<>(this, MapNode.class);
        mover.setLayoutTarget(rect);
        rect.setLayoutX(layoutX);
        rect.setLayoutY(layoutY);
        rect.setArcHeight(3);
        rect.setArcWidth(3);
        rect.setStrokeWidth(DEFAULT_STROKE_WIDTH);
        rect.setStrokeType(StrokeType.INSIDE);
        rect.setMouseTransparent(true);
        getChildren().add(rect); // important to add the rect before adding other elements. Otherwise 100% opacity hides the other things
        setColor(color);
        shadow.xProperty().bind(rect.layoutXProperty().add(SHADOW_STROKE_WIDTH / 2));
        shadow.yProperty().bind(rect.layoutYProperty().add(SHADOW_STROKE_WIDTH / 2));
        shadow.heightProperty().bind(rect.heightProperty().subtract(2 * (SHADOW_STROKE_WIDTH / 2)));
        shadow.widthProperty().bind(rect.widthProperty().subtract(2 * (SHADOW_STROKE_WIDTH / 2)));
        shadow.setStroke(Color.TRANSPARENT);
        shadow.setFill(null); // important to make only stroke clickable
        shadow.setStrokeWidth(SHADOW_STROKE_WIDTH);
        getChildren().add(shadow);
        initializeHandles();
        initializeListeners();
        label.setText(text);
        label.setTextFill(Color.web(webColor));
        Background background = new Background(new BackgroundFill(Color.WHITE, new CornerRadii(3), Insets.EMPTY));
        label.setBackground(background);
        label.setPadding(new Insets(2));
        label.maxWidthProperty().bind(rect.widthProperty().subtract(rect.strokeWidthProperty()));
        label.setWrapText(true);
        label.setMaxHeight(MAX_LABEL_HEIGHT);
        textField.maxWidthProperty().bind(Bindings.max(label.widthProperty(), MAX_EDITOR_SIZE));
        textField.setMaxHeight(MAX_EDITOR_SIZE);
        textField.setWrapText(true);
        labelBox.getChildren().add(label);
        labelBox.getChildren().add(settingButton);
        labelBox.setSpacing(5);
        Image img = new Image(getClass().getResourceAsStream("/com/mindliner/img/icons/1616/gear.png"));
        ImageView iv = new ImageView(img);
        iv.setFitHeight(12);
        iv.setFitWidth(12);
        settingButton.setGraphic(iv);
        settingButton.setPadding(new Insets(2));
//        settingButton.setBackground(Background.EMPTY);
        settingButton.setOnMouseReleased((event) -> {
            setSelected(false);
            try {
                Dialog<ContainerProperties> dialog = createSettingDialog();
                Optional<ContainerProperties> res = dialog.showAndWait();
                if (res.isPresent()) {
                    ContainerProperties cp = res.get();
                    setContainerOpacity(cp.getOpacity());
                    setStrokeWidth(cp.getStrokeWidth());
                    setStrokeStyle(cp.getStrokeStyle());
                    setColor(cp.getColorSilent());
                    // TODO: refactor. Simple hack to check if user canceled or OK'ed the dialog
                    if (cp.getLabel() != null) {
                        mapController.updateContainer(this, null, null);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(MapContainer.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        settingButton.setVisible(false);
    }

    public void setStrokeWidth(double strokeWidth) {
        this.strokeWidth = strokeWidth;
        rect.setStrokeWidth(strokeWidth);
        // TODO:refactor. redraw selected border is a hack
        setSelected(isSelected);
    }

    public double getStrokeWidth() {
        return strokeWidth;
    }

    public double getContainerOpacity() {
        return opacity;
    }

    public void setContainerOpacity(double opacity) {
        this.opacity = opacity;
        rect.setFill(Color.web(webColor, opacity));
    }

    public final void setColor(String webColor) {
        this.webColor = webColor;
        rect.setStroke(Color.web(webColor));
        rect.setFill(Color.web(webColor, opacity));
        label.setTextFill(Color.web(webColor));
    }

    public CMContainerStrokeStyles getStrokeStyle() {
        return strokeStyle;
    }

    public void setStrokeStyle(CMContainerStrokeStyles style) {
        strokeStyle = style;
        switch (style) {
            case DASHED:
                rect.getStrokeDashArray().setAll(20d, 20d);
                break;
            case DOTTED:
                rect.getStrokeDashArray().setAll(1d, 10d);
                break;
            case SOLID:
                rect.getStrokeDashArray().clear();
                break;
        }
    }

    private void initializeHandles() {
        // Bind the handlers to the 4 corners
        ulHandle.xProperty().bind(rect.layoutXProperty());
        ulHandle.yProperty().bind(rect.layoutYProperty());
        urHandle.xProperty().bind(rect.layoutXProperty().add(rect.widthProperty()).subtract(HANDLE_SIZE));
        urHandle.yProperty().bind(rect.layoutYProperty());
        llHandle.xProperty().bind(rect.layoutXProperty());
        llHandle.yProperty().bind(rect.layoutYProperty().add(rect.heightProperty()).subtract(HANDLE_SIZE));
        lrHandle.xProperty().bind(rect.layoutXProperty().add(rect.widthProperty()).subtract(HANDLE_SIZE));
        lrHandle.yProperty().bind(rect.layoutYProperty().add(rect.heightProperty()).subtract(HANDLE_SIZE));

        EventHandler<MouseEvent> pressHandler = (MouseEvent event) -> {
            startWidth = rect.getWidth();
            startHeight = rect.getHeight();
            startLx = rect.getLayoutX();
            startLy = rect.getLayoutY();
            startX = event.getSceneX();
            startY = event.getSceneY();
            startTx = getTranslateX();
            startTy = getTranslateY();
        };
        EventHandler<MouseEvent> releaseHandler = (MouseEvent event) -> {
            if (!event.isStillSincePress()) {
                mover.finish();
            }
        };

        // Set proper cursor for each handle
        Rectangle[] handles = {ulHandle, urHandle, llHandle, lrHandle};
        final Cursor[] cursors = {Cursor.NW_RESIZE, Cursor.NE_RESIZE, Cursor.SW_RESIZE, Cursor.SE_RESIZE};
        for (int i = 0; i < handles.length; i++) {
            handles[i].setStroke(Color.TRANSPARENT);
            handles[i].setFill(Color.TRANSPARENT);
            final Cursor c = cursors[i];
            handles[i].setOnMouseEntered((MouseEvent event) -> {
                getScene().setCursor(c);
            });
            handles[i].setOnMouseExited((MouseEvent event) -> {
                getScene().setCursor(Cursor.DEFAULT);
            });
            handles[i].setOnMousePressed(pressHandler);
            handles[i].setOnMouseReleased(releaseHandler);
        }

        // set drag handler for each handle
        ulHandle.setOnMouseDragged((MouseEvent event) -> {
            Point2D p = getUnscaledDifference(event.getSceneX(), event.getSceneY());
            if (startWidth - p.getX() > MIN_SIZE) {
                rect.setWidth(startWidth - p.getX());
                rect.setLayoutX(startLx + p.getX());
                compensateScaleMovementX(p);
            }
            if (startHeight - p.getY() > MIN_SIZE) {
                rect.setHeight(startHeight - p.getY());
                rect.setLayoutY(startLy + p.getY());
                compensateScaleMovementY(p);
            }
            mover.highlightIntersections();
            mapController.getLinker().recomputeLink(this);
            event.consume();
        });
        urHandle.setOnMouseDragged((MouseEvent event) -> {
            Point2D p = getUnscaledDifference(event.getSceneX(), event.getSceneY());
            if (startWidth + p.getX() > MIN_SIZE) {
                rect.setWidth(startWidth + p.getX());
                compensateScaleMovementX(p);
            }
            if (startHeight - p.getY() > MIN_SIZE) {
                rect.setHeight(startHeight - p.getY());
                rect.setLayoutY(startLy + p.getY());
                compensateScaleMovementY(p);
            }
            mover.highlightIntersections();
            mapController.getLinker().recomputeLink(this);
            event.consume();
        });
        lrHandle.setOnMouseDragged((MouseEvent event) -> {
            Point2D p = getUnscaledDifference(event.getSceneX(), event.getSceneY());
            if (startWidth + p.getX() > MIN_SIZE) {
                rect.setWidth(startWidth + p.getX());
                compensateScaleMovementX(p);
            }
            if (startHeight + p.getY() > MIN_SIZE) {
                rect.setHeight(startHeight + p.getY());
                compensateScaleMovementY(p);
            }
            mover.highlightIntersections();
            mapController.getLinker().recomputeLink(this);
            event.consume();
        });
        llHandle.setOnMouseDragged((MouseEvent event) -> {
            Point2D p = getUnscaledDifference(event.getSceneX(), event.getSceneY());
            if (startWidth - p.getX() > MIN_SIZE) {
                rect.setWidth(startWidth - p.getX());
                rect.setLayoutX(startLx + p.getX());
                compensateScaleMovementX(p);
            }
            if (startHeight + p.getY() > MIN_SIZE) {
                rect.setHeight(startHeight + p.getY());
                compensateScaleMovementY(p);
            }
            mover.highlightIntersections();
            mapController.getLinker().recomputeLink(this);
            event.consume();
        });
        getChildren().addAll(handles);
    }

    // a scale is always applied around the center of the container. But if the user changes a container, we want the scale
    // to be applied around the corner that the user is currently changing. That's why we have to add a compensation translation
    // to come up for the pivot change.
    public void compensateScaleMovementY(Point2D p) {
        double ty = p.getY() * (getScaleY() - 1) / 2;
        setTranslateY(startTy + ty);
    }

    public void compensateScaleMovementX(Point2D p) {
        double tx = p.getX() * (getScaleX() - 1) / 2;
        setTranslateX(startTx + tx);
    }

    private void initializeListeners() {
        labelBox.setOnMouseEntered((event) -> {
            settingButton.setVisible(true);
        }
        );
        labelBox.setOnMouseExited((event) -> {
            settingButton.setVisible(false);
        }
        );
        shadow.setOnMouseReleased((MouseEvent event) -> {
            onMouseReleased(event);
        });
        shadow.setOnMousePressed((MouseEvent event) -> {
            onMousePressed(event);
        });
        shadow.setOnMouseDragged((MouseEvent event) -> {
            onMouseDragged(event);
        });
        label.setOnMouseReleased((MouseEvent event) -> {
            onMouseReleased(event);
        });
        label.setOnMousePressed((MouseEvent event) -> {
            onMousePressed(event);
        });
        label.setOnMouseDragged((MouseEvent event) -> {
            onMouseDragged(event);
        });
        label.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2 && mapController.isCursorMode()) {
                textField.setText(label.getText());
                replaceNode(label, textField);
                textField.selectAll();
                textField.requestFocus();
            }
        });

        textField.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (!newValue) {
                updateLabel();
            }
        });
        textField.setOnKeyReleased((KeyEvent event) -> {
            if (event.getCode().equals(KeyCode.ESCAPE)) {
                replaceNode(textField, label);
                textField.setText(label.getText());
            }
            event.consume();
        });
    }

    private void updateLabel() {
        label.setText(textField.getText());
        replaceNode(textField, label);
        if (!label.getText().equals(object.getHeadline())) {
            object.setHeadline(label.getText());
            mapController.updateContainer(this, null, null);
        }
    }

    private void replaceNode(Node from, Node to) {
        if (!labelBox.getChildren().contains(from)) {
            return;
        }
        // TODO refactor: use visibility instead of adding/removing the nodes
        labelBox.getChildren().remove(from);
        labelBox.getChildren().add(0, to);
    }

    @Override
    public void setObject(mlcObject obj) {
        // to implement the ContainerMapElement interface, we must accept an mlcObject instead of an MlcContainer
        if (obj instanceof MlcContainer) {
            MlcContainer container = (MlcContainer) obj;
            this.object = container;
            if (container.getColor() == null) {
                // means container is not yet initialized (e.g. after ObjectCreationCommand and before ContainerUpdateCommand).
                // simply discard update the node with the next object change event
                return;
            }
            label.setText(container.getHeadline());
            rect.setStroke(Color.web(container.getColor()));
            // changing the layout in a panned/zoomed environment requires also updating the translations
            mover.updateLayout(container.getPosX(), container.getPosY());
            setStrokeStyle(container.getStrokeStyle());
            setStrokeWidth(container.getStrokeWidth());
            setContainerOpacity(container.getOpacity());
        } else {
            throw new IllegalArgumentException("Cannot set object type " + obj.getClass() + " to a map container");
        }
    }

    /**
     * Adds the label to the container and adjusts the container to the zoom
     * level.
     *
     * @param zoom
     */
    public void finishCreation(double zoom, boolean isScreenOrigin) {
        addLabelBox();
        adjustToScale(zoom, isScreenOrigin);
    }

    private void addLabelBox() {
        getChildren().add(1, labelBox); // insert label at first position such that the upper left resize handle is on top of it and receives events
        labelBox.layoutXProperty().bind(rect.layoutXProperty().add(3).add(rect.strokeWidthProperty()));
        labelBox.layoutYProperty().bind(rect.layoutYProperty().add(3).add(rect.strokeWidthProperty()));
    }

    /**
     * Sets the container scale and adjust its position so that the scale
     * happens around the top left corner and not the center
     *
     * @param zoom The new zoom
     * @param isScreenOrigin Whether or not the container originated in this
     * instance by user interaction or was created in response to an update
     * message received
     */
    private void adjustToScale(double zoom, boolean isScreenOrigin) {
        // if the scenery was already zoomed when the container was created, we need to apply the zoom factor also
        // onto the container, such that we can store the un-zoomed bounds (width/height) of the container.
        if (zoom != 1) {
            if (isScreenOrigin) {
                rect.setWidth(rect.getWidth() / zoom);
                rect.setHeight(rect.getHeight() / zoom);
            }
            double prefH = getHeight();
            double prefW = getWidth();
            double diffH = (prefH - prefH * zoom) * 0.5;
            double diffW = (prefW - prefW * zoom) * 0.5;
            setScaleX(zoom);
            setScaleY(zoom);
            setTranslateX(getTranslateX() - diffW);
            setTranslateY(getTranslateY() - diffH);
        }
    }

    public void initDrag(double x, double y) {
        currX = new SimpleDoubleProperty(x);
        currY = new SimpleDoubleProperty(y);
        startLx = rect.getLayoutX();
        startLy = rect.getLayoutY();
    }

    public void mouseDragged(double x, double y) {
        double dx = x - currX.doubleValue();
        rect.setWidth(Math.abs(dx));
        double dy = y - currY.doubleValue();
        rect.setHeight(Math.abs(dy));

        // makes drawing in all direction possible
        // rectangle does not understand negative values
        // therefore we simply move the drawing root (upper left corner) when difference gets negative
        if (dx < 0) {
            rect.setLayoutX(startLx + dx);
        }
        if (dy < 0) {
            rect.setLayoutY(startLy + dy);
        }
    }

    @Override
    public void onMousePressed(MouseEvent event) {
        if (mapController.isSelected(this) && mapController.isCursorMode()) {
            mover.init(event.getSceneX(), event.getSceneY());
        } else if (mapController.isLinkMode()) {
            mapController.createLink(this);
            event.consume();
        }
    }

    @Override
    public void onMouseDragged(MouseEvent event) {
        if (mapController.isSelected(this) && mapController.isCursorMode()) {
            if (event.isControlDown()) {
                mapController.initDragCopyContainer(this, event);
                mapController.setSelection(this); // to unselect this
            } else {
                mover.move(event.getSceneX(), event.getSceneY());
                mapController.getLinker().recomputeLink(this);
                event.consume();
            }
        }
    }

    @Override
    public void onMouseReleased(MouseEvent event) {
        if (mapController.isCursorMode()) {
            if (event.getClickCount() == 1) {
                if (event.isStillSincePress()) {
                    if (event.isControlDown()) {
                        mapController.addSelection(MapContainer.this);
                    } else {
                        mapController.setSelection(MapContainer.this);
                    }
                    event.consume();
                } else if (mapController.isSelected(this)) {
                    mover.finish();
                    event.consume();
                }
            }
        } else if (mapController.isLinkMode()) {
            // The mouse released event is triggered on the MapContainer that registered the mouse pressed event!
            mapController.finishLink(new Point2D(event.getSceneX(), event.getSceneY()));
            event.consume();
        }

    }

    private Dialog<ContainerProperties> createSettingDialog() throws IOException {
        ContainerProperties reset = new ContainerProperties(null, null);
        reset.setOpacity(opacity);
        reset.setStrokeWidth(strokeWidth);
        reset.setStrokeStyle(getStrokeStyle());
        reset.setColorSilent(webColor);

        Dialog<ContainerProperties> dialog = new Dialog<>();
        dialog.setTitle("Styling");
        dialog.setHeaderText("Choose your container styling");

        GridPane content = FXMLLoader.<GridPane>load(MapContainer.class.getResource("FXMLContainerDialog.fxml"));
        content.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(content);

        ColorPicker colButton = (ColorPicker) content.lookup("#colorButton");
        colButton.getStyleClass().add("button");
        colButton.setStyle("-fx-color-label-visible: false ;");
        colButton.setBorder(null);
        colButton.setBackground(null);
        colButton.setPadding(new Insets(0));

        Slider opSlider = (Slider) content.lookup("#opacitySlider");
        Slider swSlider = (Slider) content.lookup("#strokeWidthSlider");
        Slider ssSlider = (Slider) content.lookup("#strokeStyleSlider");
        Label opLabel = (Label) content.lookup("#opacityLabel");
        Label swLabel = (Label) content.lookup("#strokeWidthLabel");
        Label ssLabel = (Label) content.lookup("#strokeStyleLabel");
        Label colLabel = (Label) content.lookup("#colorLabel");

        opSlider.valueProperty().addListener((ov, old_val, new_val) -> {
            opLabel.setText(String.format("%.2f", new_val));
            setContainerOpacity(new_val.doubleValue());
        });
        swSlider.valueProperty().addListener((ov, old_val, new_val) -> {
            swLabel.setText(String.format("%d", new_val.intValue()));
            setStrokeWidth(new_val.doubleValue());
        });
        ssSlider.valueProperty().addListener((ov, old_val, new_val) -> {
            CMContainerStrokeStyles cmstyle = CMContainerStrokeStyles.values()[new_val.intValue()];
            ssLabel.setText(cmstyle.toString());
            setStrokeStyle(cmstyle);
        });
        colButton.valueProperty().addListener((ov, old_val, new_val) -> {
            String colorStr = ContainerMapUtils.colorToString(new_val);
            colLabel.setText(colorStr);
            setColor(colorStr);
        });

        opSlider.setValue(opacity);
        swSlider.setValue(strokeWidth);
        ssSlider.setValue(strokeStyle.ordinal());
        colButton.setValue((Color) rect.getStroke());

        ButtonType buttonTypeOk = new ButtonType("Okay", ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);

        dialog.setResultConverter((b) -> {
            if (b == buttonTypeOk) {
                double op = opSlider.getValue();
                double sw = swSlider.getValue();
                int ss = (int) ssSlider.getValue();
                CMContainerStrokeStyles style = CMContainerStrokeStyles.values()[ss];
                ContainerProperties res = new ContainerProperties(null, null);
                res.setOpacity(op);
                res.setStrokeStyle(style);
                res.setStrokeWidth(sw);
                String colorStr = ContainerMapUtils.colorToString(colButton.getValue());
                res.setColorSilent(colorStr);
                res.setLabel("new");
                return res;
            } else {
                return reset;
            }
        });

        return dialog;
    }

    @Override
    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
        rect.setStrokeWidth(isSelected ? strokeWidth + SELECTED_STROKE_WIDTH : strokeWidth);
    }

    public double getWidth() {
        return rect.getWidth();
    }

    public double getHeight() {
        return rect.getHeight();
    }

    public String getLabel() {
        return label.getText();
    }

    public String getColorString() {
        Color color = (Color) rect.getStroke();
        return ContainerMapUtils.colorToString(color);
    }

    public String getColor() {
        return webColor;
    }

    @Override
    public MlcContainer getObject() {
        return object;
    }

    public void setWidth(double width) {
        rect.setWidth(width);
    }

    public void setHeight(double height) {
        rect.setHeight(height);
    }

    private Point2D getUnscaledDifference(double x, double y) {
        double dx = (x - startX) / getScaleX();
        double dy = (y - startY) / getScaleX();
        return new Point2D(dx, dy);
    }

    public boolean isOnBoundary(Point2D p) {
        Bounds bp = getBoundsInParent();
        if (!bp.contains(p)) {
            return false;
        }

        return Math.abs(bp.getMinX() - p.getX()) < SHADOW_STROKE_WIDTH
                || Math.abs(bp.getMaxX() - p.getX()) < SHADOW_STROKE_WIDTH
                || Math.abs(bp.getMinY() - p.getY()) < SHADOW_STROKE_WIDTH
                || Math.abs(bp.getMaxY() - p.getY()) < SHADOW_STROKE_WIDTH;
    }

    @Override
    public ElementMover getMover() {
        return mover;
    }
}
