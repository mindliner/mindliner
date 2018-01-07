/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.containermap;

import com.mindliner.enums.Position;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;

/**
 * JavaFX element representing a link in the container map (see MapNode and
 * MapContainer). Consists currently only of a simple path but may be extended
 * with labels.
 *
 * @author Dominic Plangger
 */
public class MapLink extends Group implements Selectable {

    private static final int MAX_LABEL_WIDTH = 110;
    private static final int MAX_EDITOR_SIZE = 130;
    private static final double ARROW_WIDTH = 8;
    private static final double ARROW_HEIGHT = 10;
    private static final Color DEFAULT_COLOR = Color.web("#717171");
    private static final Color SELECTED_COLOR = Color.web("#383838");
    private static final double RECOMPUTE_OFFSET = 4;
    private static final int LINE_SHADOW_WITH = 8;

    private final MovableLine l1, l2, l3;
    private Node startNode = null;
    private Node endNode = null;
    private Position startPosition = null;
    private Position endPosition = null;
    private double startOffset, endOffset;
    private double center = -1;
    private double relCenter = -1;
    private double absCenter;
    private double intermediateX, intermediateY;
    private double labelPosition = 0;
    private double labelDragX = 0, labelDragY = 0;
    private boolean isValid = false;
    private boolean isDouble = false;
    private boolean isOneWay = true;
    private boolean dynamicRecompute = true;
    private final FXMLController mapController;
    private Boolean outsideContainer = null;
    private Label label = new Label();
    private TextArea labelEditor = new TextArea();
    private MovableLine labelLine = null;
    private final Polygon headArrow = new Polygon();
    private final Polygon tailArrow = new Polygon();
    private final ContextMenu contextMenu = new ContextMenu();
    private final MenuItem oneWayMenuItem = new MenuItem();
    private final Menu changeSourceAttachmentMenu = new Menu();
    private final Menu changeTargetAttachmentMenu = new Menu();
    private final ResourceBundle bundle = ResourceBundle.getBundle("com/mindliner/resources/WorksphereMap");

    public MapLink(Node startNode) {
        buildContextMenus();
        mapController = FXMLController.getInstance();
        this.startNode = startNode;

        l1 = new MovableLine();
        l2 = new MovableLine();
        l3 = new MovableLine();
        getChildren().addAll(l1, l2, l3, l1.getShadow(), l2.getShadow(), l3.getShadow());
        getChildren().add(label);
        getChildren().add(labelEditor);
        getChildren().add(headArrow);
        getChildren().add(tailArrow);
        headArrow.getPoints().setAll(-ARROW_WIDTH / 2, -ARROW_HEIGHT / 2, 0d, ARROW_HEIGHT / 2, ARROW_WIDTH / 2, -ARROW_HEIGHT / 2);
        headArrow.setFill(DEFAULT_COLOR);
        tailArrow.getPoints().setAll(-ARROW_WIDTH / 2, -ARROW_HEIGHT / 2, 0d, ARROW_HEIGHT / 2, ARROW_WIDTH / 2, -ARROW_HEIGHT / 2);
        tailArrow.setFill(DEFAULT_COLOR);
        tailArrow.setVisible(false);

        Font f = new Font("System Regular", 10);
        label.setMaxWidth(MAX_LABEL_WIDTH);
        label.setWrapText(true);
        label.setFont(f);
        labelEditor.setVisible(false);
        labelEditor.layoutXProperty().bind(label.layoutXProperty());
        labelEditor.layoutYProperty().bind(label.layoutYProperty());
        labelEditor.setMaxWidth(MAX_EDITOR_SIZE);
        labelEditor.setMaxHeight(MAX_EDITOR_SIZE);
        labelEditor.setWrapText(true);
        Background background = new Background(new BackgroundFill(Color.WHITE, new CornerRadii(3), Insets.EMPTY));
        label.setBackground(background);

        label.setOnMousePressed((event) -> {
            labelDragX = event.getSceneX();
            labelDragY = event.getSceneY();
            event.consume();
        });

        label.setOnMouseClicked((event) -> {
            if (event.getClickCount() > 1) {
                labelEditor.setText(label.getText());
                label.setVisible(false);
                labelEditor.setVisible(true);
                labelEditor.requestFocus();
            }
        });

        labelEditor.setOnKeyReleased((event) -> {
            if (event.getCode().equals(KeyCode.ESCAPE)) {
                label.setVisible(true);
                labelEditor.setVisible(false);
                event.consume();
            }
        });

        labelEditor.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (!newValue && labelEditor.isVisible()) {
                updateLabel();
            }
        });

        label.setOnMouseDragged((event) -> {
            double overall = l1.getLength() + l2.getLength() + (l3.isVisible() ? l3.getLength() : 0);
            double ex = event.getSceneX();
            double ey = event.getSceneY();

            double diff;
            if (labelLine.isHorizontal()) {
                diff = ex - labelDragX;
                if (diff > 0 && labelLine.getStartX() > labelLine.getEndX()) {
                    diff = -diff;
                } else if (diff < 0 && labelLine.getStartX() > labelLine.getEndX()) {
                    diff = -diff;
                }
            } else {
                diff = ey - labelDragY;
                if (diff > 0 && labelLine.getStartY() > labelLine.getEndY()) {
                    diff = -diff;
                } else if (diff < 0 && labelLine.getStartY() > labelLine.getEndY()) {
                    diff = -diff;
                }
            }
            double dratio = diff / overall;
            labelPosition += dratio;
            labelPosition = Math.min(1, labelPosition);
            labelPosition = Math.max(0, labelPosition);

            labelDragX = event.getSceneX();
            labelDragY = event.getSceneY();
            placeLabel();
            event.consume();
        });

        label.setOnMouseDragReleased((event) -> {
            mapController.updateLink(this, true);
        });
        label.setOnMouseReleased((event) -> {
            if (!event.isStillSincePress()) {
                mapController.updateLink(this, true);
            }
        });
    }

    private final void buildContextMenus() {
        oneWayMenuItem.setText(bundle.getString("LinkOneWayMenuItem"));
        oneWayMenuItem.setOnAction((e) -> {
            setIsOneWay(!isOneWay);
            mapController.updateLink(this, true);
        });
        contextMenu.getItems().add(oneWayMenuItem);

        changeSourceAttachmentMenu.setText(bundle.getString("LinkChangeSourceNodeAttachmentMenu"));
        changeSourceAttachmentMenu.getItems().add(addPositionItem(Position.TOP, true));
        changeSourceAttachmentMenu.getItems().add(addPositionItem(Position.RIGHT, true));
        changeSourceAttachmentMenu.getItems().add(addPositionItem(Position.BOTTOM, true));
        changeSourceAttachmentMenu.getItems().add(addPositionItem(Position.LEFT, true));
        contextMenu.getItems().add(changeSourceAttachmentMenu);

        changeTargetAttachmentMenu.setText(bundle.getString("LinkChangeTargetNodeAttachmentMenu"));
        changeTargetAttachmentMenu.getItems().add(addPositionItem(Position.TOP, false));
        changeTargetAttachmentMenu.getItems().add(addPositionItem(Position.RIGHT, false));
        changeTargetAttachmentMenu.getItems().add(addPositionItem(Position.BOTTOM, false));
        changeTargetAttachmentMenu.getItems().add(addPositionItem(Position.LEFT, false));
        contextMenu.getItems().add(changeTargetAttachmentMenu);
    }

    /**
     *
     * @param pos
     * @param isSource
     * @return
     */
    private MenuItem addPositionItem(Position pos, boolean isSource) {
        String menuText;
        switch (pos) {
            case TOP:
                menuText = bundle.getString("LinkAttachmentPositionTop");
                break;
            case RIGHT:
                menuText = bundle.getString("LinkAttachmentPositionRight");
                break;
            case BOTTOM:
                menuText = bundle.getString("LinkAttachmentPositionBottom");
                break;
            case LEFT:
                menuText = bundle.getString("LinkAttachmentPositionLeft");
                break;
            default:
                throw new AssertionError();
        }
        MenuItem menuItem = new MenuItem(menuText);
        menuItem.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                if (isSource) {
                    startPosition = pos;
                    recompute();
                    finish();
                } else {
//                    endPosition = pos;
//                    recompute();
//                    finish();
                    resetTo(pos);
                }
            }
        });
        return menuItem;
    }

    private void updateLabel() {
        label.setText(labelEditor.getText());
        label.setVisible(true);
        labelEditor.setVisible(false);
        placeLabel(); // if text got more or less lines, the label must be adjusted and redrawn
        mapController.updateLink(this, true);
    }

    @Override
    public void setSelected(boolean isSelected) {
        Line[] lines = {l1, l2, l3};
        for (Line line : lines) {
            line.setStroke(isSelected ? SELECTED_COLOR : DEFAULT_COLOR);
            line.setStrokeWidth(isSelected ? 2 : 1.5);
        }
    }

    public String getLabel() {
        return label.getText();
    }

    public double getLabelPosition() {
        return labelPosition;
    }

    public void setLabel(String label) {
        this.label.setText(label);
    }

    public void setLabelPosition(double position) {
        this.labelPosition = position;
    }

    public void setStartOffset(double startOffset) {
        this.startOffset = startOffset;
    }

    public void setStartPosition(Position startPosition) {
        this.startPosition = startPosition;
    }

    public Position getStartPosition() {
        return startPosition;
    }

    public Position getEndPosition() {
        return endPosition;
    }

    public void setLabelScale(double scale) {
        label.setScaleX(scale);
        label.setScaleY(scale);
    }

    private boolean makeSimpleLink(double starto, double endo) {
        if (endPosition != null && (Position.isOpposite(startPosition, endPosition) || startPosition.equals(endPosition))) {
            return false;
        }

        l2.disableMovement();
        l3.setVisible(false);
        Point2D start;
        double x, y;
        switch (startPosition) {
            case TOP:
                if (getEndY() + endo >= getStartY()) {
                    return false;
                }
                if (outsideContainer) {
                    if (Position.RIGHT.equals(endPosition) && (getEndX() + getEndWith() >= getStartX() + starto)
                            || Position.LEFT.equals(endPosition) && (getEndX() <= getStartX() + starto)) {
                        return false;
                    }
                }
                start = new Point2D(getStartX() + starto, getStartY());
                y = getEndY() + endo;
                x = getEndX() + (Position.RIGHT.equals(endPosition) ? getEndWith() : 0);
                setSimplePathYX(start, y, x);
                return true;
            case BOTTOM:
                if (getEndY() + endo <= getStartY() + getStartHeight()) {
                    return false;
                }
                if (outsideContainer) {
                    if (Position.RIGHT.equals(endPosition) && (getEndX() + getEndWith() >= getStartX() + starto)
                            || Position.LEFT.equals(endPosition) && (getEndX() <= getStartX() + starto)) {
                        return false;
                    }
                }
                start = new Point2D(getStartX() + starto, getStartY() + getStartHeight());
                y = getEndY() + endo;
                x = getEndX() + (Position.RIGHT.equals(endPosition) ? getEndWith() : 0);
                setSimplePathYX(start, y, x);
                return true;
            case LEFT:
                if (getEndX() + endo >= getStartX()) {
                    return false;
                }
                if (outsideContainer) {
                    if (Position.TOP.equals(endPosition) && (getEndY() <= getStartY() + starto)
                            || Position.BOTTOM.equals(endPosition) && (getEndY() + getEndHeight() >= getStartY() + starto)) {
                        return false;
                    }
                }
                start = new Point2D(getStartX(), getStartY() + starto);
                x = getEndX() + endo;
                y = getEndY() + (Position.BOTTOM.equals(endPosition) ? getEndHeight() : 0);
                setSimplePathXY(start, x, y);
                return true;
            case RIGHT:
                if (getEndX() + endo <= getStartX() + getStartWith()) {
                    return false;
                }
                if (outsideContainer) {
                    if (Position.TOP.equals(endPosition) && (getEndY() <= getStartY() + starto)
                            || Position.BOTTOM.equals(endPosition) && (getEndY() + getEndHeight() >= getStartY() + starto)) {
                        return false;
                    }
                }
                start = new Point2D(getStartX() + getStartWith(), getStartY() + starto);
                x = getEndX() + endo;
                y = getEndY() + (Position.BOTTOM.equals(endPosition) ? getEndHeight() : 0);
                setSimplePathXY(start, x, y);
                return true;
        }
        return false;
    }

    // central computation function. First tries to to a simple link (2 elements) between the two nodes, and if this is not possible
    // it will do a double link (3 elements).
    public final void recompute() {
        Boolean outsideContainerOld = outsideContainer;
        outsideContainer = endNode != null && !endNode.getBoundsInParent().contains(startNode.getBoundsInParent());
        if (outsideContainerOld != null && !Objects.equals(outsideContainerOld, outsideContainer)) {
            center = -1;
        }
        double absStartO = startOffset * (Position.isHorizontal(startPosition) ? getStartWith() : getStartHeight());
        double absEndO = 0;
        if (endPosition != null) {
            absEndO = endOffset * (Position.isHorizontal(endPosition) ? getEndWith() : getEndHeight());
        }

        if (makeSimpleLink(absStartO, absEndO)) {
            isValid = true;
            isDouble = false;
            center = -1;
            placeLabel();
            placeArrows();
            return;
        }
        boolean wasValid = isValid & !isDouble;

        isValid = makeDoubleLink(absStartO, absEndO);
        isDouble = isValid;
        if (isValid && wasValid) {
            // this is for the case that a simple link is conerted to a double
            // link by node movement. Then we must fix the center.
            finish();
        }
        placeLabel();
        placeArrows();
    }

    public void finish() {
        if (isDouble) {
            // compute the actual center after the link has been fixed
            center = relCenter / getStartHeight();
        }
    }

    private void resetTo(Position endP) {
        endPosition = endP;
        center = -1;
        recompute();
        finish();
    }

    private boolean makeDoubleLink(double absStartO, double absEndO) {
        if (endPosition != null && !Position.isOpposite(startPosition, endPosition) && !startPosition.equals(endPosition)) {
            // double links can only connect opposite and same start/end orientations, but not for example TOP and LEFT
            endPosition = Position.getOpposite(startPosition);
            recompute();
            return true;
        }

        l3.setVisible(true);
        double endY, endX, diff, autoCenter;
        Point2D start;
        switch (startPosition) {
            case TOP:
                if (Position.BOTTOM.equals(endPosition)) {
                    if (outsideContainer && getStartY() < getEndY() + getEndHeight()) {
                        endPosition = Position.TOP;
                        recompute();
                        return true;
                    }
                    endY = getEndY() + getEndHeight();
                    autoCenter = 0.9;
                } else {
                    endY = getEndY();
                    autoCenter = endY > getStartY() || !outsideContainer ? 0.1 : 1.1;
                }
                l2.setYMovable();
                diff = Math.abs(getStartY() - endY);
                relCenter = center == -1 ? diff * autoCenter : center * getStartHeight();
                absCenter = getStartY() - relCenter;
                if (outsideContainer && checkNodeMoveVertical()) {
                    return true;
                }

                start = new Point2D(getStartX() + absStartO, getStartY());
                setDoublePathYXY(start, absCenter, getEndX() + absEndO, endY);
                break;

            case BOTTOM:
                double startY = getStartY() + getStartHeight();
                if (Position.TOP.equals(endPosition)) {
                    if (outsideContainer && getStartY() + getStartHeight() > getEndY()) {
                        endPosition = Position.BOTTOM;
                        recompute();
                        return true;
                    }
                    endY = getEndY();
                    autoCenter = 0.9;
                } else {
                    endY = getEndY() + getEndHeight();
                    autoCenter = endY < startY || !outsideContainer ? 0.1 : 1.1;
                }
                l2.setYMovable();
                diff = Math.abs(startY - endY);
                relCenter = center == -1 ? diff * autoCenter : center * getStartHeight();
                absCenter = startY + relCenter;
                if (outsideContainer && checkNodeMoveVertical()) {
                    return true;
                }

                start = new Point2D(getStartX() + absStartO, startY);
                setDoublePathYXY(start, absCenter, getEndX() + absEndO, endY);
                break;

            case LEFT:
                if (Position.RIGHT.equals(endPosition)) {
                    if (outsideContainer && getStartX() < getEndX() + getEndWith()) {
                        endPosition = Position.LEFT;
                        recompute();
                        return true;
                    }
                    endX = getEndX() + getEndWith();
                    autoCenter = 0.9;
                } else {
                    endX = getEndX();
                    autoCenter = endX > getStartX() || !outsideContainer ? 0.1 : 1.1;
                }
                l2.setXMovable();
                diff = Math.abs(getStartX() - endX);
                relCenter = center == -1 ? diff * autoCenter : center * getStartHeight();
                absCenter = getStartX() - relCenter;
                if (outsideContainer && checkNodeMoveHorizontal()) {
                    return true;
                }

                start = new Point2D(getStartX(), getStartY() + absStartO);
                setDoublePathXYX(start, absCenter, getEndY() + absEndO, endX);
                break;

            case RIGHT:
                double startX = getStartX() + getStartWith();
                if (Position.LEFT.equals(endPosition)) {
                    if (outsideContainer && getStartX() + getStartWith() > getEndX()) {
                        endPosition = Position.RIGHT;
                        recompute();
                        return true;
                    }
                    endX = getEndX();
                    autoCenter = 0.9;
                } else {
                    endX = getEndX() + getEndWith();
                    autoCenter = endX < startX || !outsideContainer ? 0.1 : 1.1;
                }
                l2.setXMovable();
                diff = Math.abs(startX - endX);
                relCenter = center == -1 ? diff * autoCenter : center * getStartHeight();
                absCenter = startX + relCenter;
                if (outsideContainer && checkNodeMoveHorizontal()) {
                    return true;
                }

                start = new Point2D(startX, getStartY() + absStartO);
                setDoublePathXYX(start, absCenter, getEndY() + absEndO, endX);
                break;
        }
        return true;
    }

    // When a node is moved vertically and it reaches the horizontal center line element of the double link, 
    // the center must be moved to the other side of the node
    private boolean checkNodeMoveVertical() {
        if (center != -1) {
            if (Position.TOP.equals(endPosition) && absCenter + RECOMPUTE_OFFSET >= getEndY()) {
                if (dynamicRecompute) {
                    resetTo(Position.BOTTOM);
                }
                return true;
            } else if (Position.BOTTOM.equals(endPosition) && absCenter - RECOMPUTE_OFFSET <= getEndY() + getEndHeight()) {
                if (dynamicRecompute) {
                    resetTo(Position.TOP);
                }
                return true;
            }
        }
        return false;
    }

    private boolean checkNodeMoveHorizontal() {
        if (center != -1) {
            if (Position.LEFT.equals(endPosition) && absCenter + RECOMPUTE_OFFSET >= getEndX()) {
                if (dynamicRecompute) {
                    resetTo(Position.RIGHT);
                }
                return true;
            } else if (Position.RIGHT.equals(endPosition) && absCenter - RECOMPUTE_OFFSET <= getEndX() + getEndWith()) {
                if (dynamicRecompute) {
                    resetTo(Position.LEFT);
                }
                return true;
            }
        }
        return false;
    }

    // The center must only be movable between the start end end node. 
    // Returns true if the center is offset-close to the start or end node
    private boolean checkCenterMoveVertical() {
        if (center == -1) {
            return false;
        }
        if (outsideContainer) {
            if (Position.TOP.equals(endPosition) && absCenter + RECOMPUTE_OFFSET >= getEndY()) {
                return true;
            } else if (Position.TOP.equals(startPosition) && absCenter + RECOMPUTE_OFFSET >= getStartY()) {
                return true;
            } else if (Position.BOTTOM.equals(endPosition) && absCenter - RECOMPUTE_OFFSET <= getEndY() + getEndHeight()) {
                return true;
            } else if (Position.BOTTOM.equals(startPosition) && absCenter - RECOMPUTE_OFFSET <= getStartY() + getStartHeight()) {
                return true;
            }
        } else if (Position.TOP.equals(endPosition) && absCenter - RECOMPUTE_OFFSET <= getEndY()) {
            return true;
        } else if (Position.TOP.equals(startPosition) && absCenter + RECOMPUTE_OFFSET >= getStartY()) {
            return true;
        } else if (Position.BOTTOM.equals(endPosition) && absCenter + RECOMPUTE_OFFSET >= getEndY() + getEndHeight()) {
            return true;
        } else if (Position.BOTTOM.equals(startPosition) && absCenter - RECOMPUTE_OFFSET <= getStartY() + getStartHeight()) {
            return true;
        }
        return false;
    }

    private boolean checkCenterMoveHorizontal() {
        if (center == -1) {
            return false;
        }
        if (outsideContainer) {
            if (Position.LEFT.equals(endPosition) && absCenter + RECOMPUTE_OFFSET >= getEndX()) {
                return true;
            } else if (Position.LEFT.equals(startPosition) && absCenter + RECOMPUTE_OFFSET >= getStartX()) {
                return true;
            } else if (Position.RIGHT.equals(endPosition) && absCenter - RECOMPUTE_OFFSET <= getEndX() + getEndWith()) {
                return true;
            } else if (Position.RIGHT.equals(startPosition) && absCenter - RECOMPUTE_OFFSET <= getStartX() + getStartWith()) {
                return true;
            }
        } else if (Position.LEFT.equals(endPosition) && absCenter - RECOMPUTE_OFFSET <= getEndX()) {
            return true;
        } else if (Position.LEFT.equals(startPosition) && absCenter + RECOMPUTE_OFFSET >= getStartX()) {
            return true;
        } else if (Position.RIGHT.equals(endPosition) && absCenter + RECOMPUTE_OFFSET >= getEndX() + getEndWith()) {
            return true;
        } else if (Position.RIGHT.equals(startPosition) && absCenter - RECOMPUTE_OFFSET <= getStartX() + getStartWith()) {
            return true;
        }
        return false;
    }

    public void setCenter(double center) {
        this.center = center;
    }

    public void setIsOneWay(boolean isOneWay) {
        this.isOneWay = isOneWay;
        tailArrow.setVisible(!isOneWay);
        oneWayMenuItem.setText(isOneWay ? bundle.getString("LinkBothWaysMenuItem") : bundle.getString("LinkOneWayMenuItem"));
    }

    public boolean isOneWay() {
        return this.isOneWay;
    }

    public double getCenter() {
        return center;
    }

    public double getStartOffset() {
        return startOffset;
    }

    public double getEndOffset() {
        return endOffset;
    }

    public void setEndNode(Node end, Position p, double offset) {
        this.endPosition = p;
        this.endOffset = offset;
        this.endNode = end;
        recompute();
    }

    public void setEndCoordinates(double endx, double endy) {
        this.intermediateX = endx;
        this.intermediateY = endy;
        this.endPosition = null;
        this.endOffset = 0;
        this.endNode = null;
        recompute();
    }

    // Constructs a link that starts at 'start', then moves 'y' vertically and
    // then 'x' horizontally
    private void setSimplePathYX(Point2D start, double y, double x) {
        l1.setStartX(start.getX());
        l1.setStartY(start.getY());
        l1.setEndX(start.getX());
        l1.setEndY(y);
        l2.setStartX(l1.getEndX());
        l2.setStartY(l1.getEndY());
        l2.setEndX(x);
        l2.setEndY(l1.getEndY());
    }

    private void setSimplePathXY(Point2D start, double x, double y) {
        l1.setStartX(start.getX());
        l1.setStartY(start.getY());
        l1.setEndX(x);
        l1.setEndY(start.getY());
        l2.setStartX(l1.getEndX());
        l2.setStartY(l1.getEndY());
        l2.setEndX(l1.getEndX());
        l2.setEndY(y);
    }

    // Constructs a link that starts at 'start', then moves 'x' horizontally,
    // then 'y' vertically and then again 'x1' horizontally
    private void setDoublePathXYX(Point2D start, double x, double y, double x1) {
        setSimplePathXY(start, x, y);
        l3.setStartX(x);
        l3.setStartY(y);
        l3.setEndX(x1);
        l3.setEndY(y);
    }

    private void setDoublePathYXY(Point2D start, double y, double x, double y1) {
        setSimplePathYX(start, y, x);
        l3.setStartX(x);
        l3.setStartY(y);
        l3.setEndX(x);
        l3.setEndY(y1);
    }

    public Node getStartNode() {
        return startNode;
    }

    public Node getEndNode() {
        return endNode;
    }

    private double getStartX() {
        return startNode.getBoundsInParent().getMinX();
    }

    private double getStartY() {
        return startNode.getBoundsInParent().getMinY();
    }

    private double getEndX() {
        return endNode == null ? intermediateX : endNode.getBoundsInParent().getMinX();
    }

    private double getEndY() {
        return endNode == null ? intermediateY : endNode.getBoundsInParent().getMinY();
    }

    private double getStartWith() {
        return startNode.getBoundsInParent().getWidth();
    }

    private double getStartHeight() {
        return startNode.getBoundsInParent().getHeight();
    }

    private double getEndWith() {
        return endNode == null ? 0 : endNode.getBoundsInParent().getWidth();
    }

    private double getEndHeight() {
        return endNode == null ? 0 : endNode.getBoundsInParent().getHeight();
    }

    public boolean isValid() {
        return isValid;
    }

    private void placeArrows() {
        MovableLine fl = isDouble ? l3 : l2;
        placeArrow(fl, headArrow, new Point2D(fl.getEndX(), fl.getEndY()), new Point2D(fl.getStartX(), fl.getStartY()));
        placeArrow(l1, tailArrow, new Point2D(l1.getStartX(), l1.getStartY()), new Point2D(l1.getEndX(), l1.getEndY()));
    }

    private void placeArrow(MovableLine finalLine, Polygon arrow, Point2D end, Point2D start) {
        double tx = end.getX();
        double ty = end.getY();
        if (finalLine.isHorizontal()) {
            if (start.getX() >= end.getX()) {
                arrow.setRotate(90);
                tx += ARROW_HEIGHT / 2 - 2; // the end of the arrow head must match the end of the line
            } else {
                arrow.setRotate(-90);
                tx -= ARROW_HEIGHT / 2 - 2;
            }
        } else if (start.getY() >= end.getY()) {
            arrow.setRotate(180);
            ty += ARROW_HEIGHT / 2 - 2;
        } else {
            arrow.setRotate(0);
            ty -= ARROW_HEIGHT / 2 - 2;
        }
        arrow.setTranslateX(tx);
        arrow.setTranslateY(ty);
    }

    private void placeLabel() {
        double l1l = l1.getLength();
        double l2l = l2.getLength();
        double l3l = (l3.isVisible() ? l3.getLength() : 0);

        double overall = l1l + l2l + l3l;
        double absPos = overall * labelPosition;
        double x, y;
        if (absPos <= l1l) {
            double sigx = Math.signum(l1.getEndX() - l1.getStartX());
            double sigy = Math.signum(l1.getEndY() - l1.getStartY());

            x = l1.getStartX() + (sigx * absPos);
            y = l1.getStartY() + (sigy * absPos);
            labelLine = l1;
        } else if (absPos <= l1l + l2l) {
            double sigx = Math.signum(l2.getEndX() - l2.getStartX());
            double sigy = Math.signum(l2.getEndY() - l2.getStartY());

            x = l2.getStartX() + (sigx * (absPos - l1l));
            y = l2.getStartY() + (sigy * (absPos - l1l));
            labelLine = l2;
        } else {
            double sigx = Math.signum(l3.getEndX() - l3.getStartX());
            double sigy = Math.signum(l3.getEndY() - l3.getStartY());

            x = l3.getStartX() + (sigx * (absPos - l1l - l2l));
            y = l3.getStartY() + (sigy * (absPos - l1l - l2l));
            labelLine = l3;
        }

        // label may have not been drawn (-> zero layout bounds)
        // TODO refactor. only runLater when placeLabel is called by initial CM draw 
        Platform.runLater(() -> {
            Bounds bip = label.getLayoutBounds();
            double w = bip.getWidth();
            double h = bip.getHeight();
            label.setLayoutX(x - w / 2);
            label.setLayoutY(y - h / 2);
        });
    }

    private void updateLabelPosition(Line l, double newx, double newy) {
        double l1l = l1.getLength();
        double l2l = l2.getLength();
        double l3l = (l3.isVisible() ? l3.getLength() : 0);
        double overall = l1l + l2l + l3l;

        double absx = Math.abs(l.getEndX() - l.getStartX());
        double dist = absx == 0 ? Math.abs(l.getStartY() - newy) : Math.abs(l.getStartX() - newx);

        if (l.equals(l1)) {
            labelPosition = dist / overall;
        } else if (l.equals(l2)) {
            labelPosition = (l1l + dist) / overall;
        } else {
            labelPosition = (l1l + l2l + dist) / overall;
        }
    }

    /**
     * A line that has a congruent transparent 'shadow' line that catches all
     * events. The shadow line is thicker and therefore easier to click.
     */
    private class MovableLine extends Line {

        private final Line shadow;
        private final EventHandler<MouseEvent> xHandler, yHandler, inityHandler, initxHandler, defaultPressHandler;
        private double startCenter;

        public MovableLine() {
            super();
            shadow = new Line();
            shadow.setStrokeWidth(LINE_SHADOW_WITH);
            shadow.setStroke(Color.TRANSPARENT);
            shadow.startXProperty().bind(startXProperty());
            shadow.startYProperty().bind(startYProperty());
            shadow.endXProperty().bind(endXProperty());
            shadow.endYProperty().bind(endYProperty());

            shadow.setOnMouseClicked((event) -> {
                if (event.getClickCount() > 1) {
                    updateLabelPosition(this, event.getSceneX(), event.getSceneY());
                    placeLabel();
                    labelEditor.setText(label.getText());
                    label.setVisible(false);
                    labelEditor.setVisible(true);
                    labelEditor.requestFocus();
                    event.consume();
                }
            });

            // the handler for moving the center horizontally
            xHandler = (MouseEvent event) -> {
                double dx = event.getSceneX() - absCenter;
                dx = Position.LEFT.equals(startPosition) ? -dx : dx;
                center = (relCenter + dx) / getStartHeight();
                recompute();
                if (checkCenterMoveHorizontal()) {
                    center = (relCenter - dx) / getStartHeight();
                    recompute();
                }
                event.consume();
            };

            yHandler = (MouseEvent event) -> {
                double dy = event.getSceneY() - absCenter;
                dy = Position.TOP.equals(startPosition) ? -dy : dy;
                center = (relCenter + dy) / getStartHeight();
                recompute();
                if (checkCenterMoveVertical()) {
                    center = (relCenter - dy) / getStartHeight();
                    recompute();
                }
                event.consume();
            };

            inityHandler = (MouseEvent event) -> {
                getScene().setCursor(Cursor.V_RESIZE);
                dynamicRecompute = false;
                startCenter = center;
                event.consume();
            };

            initxHandler = (MouseEvent event) -> {
                getScene().setCursor(Cursor.H_RESIZE);
                dynamicRecompute = false;
                startCenter = center;
                event.consume();
            };

            defaultPressHandler = (event) -> {
                if (event.isSecondaryButtonDown()) {
                    event.consume();
                }
            };
            // catch mouse pressed event with secondary button, otherwise 
            // FXMLController will start drawing the focus rectangle (rubberband)
            shadow.setOnMousePressed(defaultPressHandler);

            shadow.setOnMouseReleased((MouseEvent event) -> {
                if (event.getButton() == MouseButton.SECONDARY) {
                    contextMenu.show(shadow, event.getScreenX(), event.getScreenY());
                    event.consume();
                    return;
                }
                getScene().setCursor(Cursor.DEFAULT);
                dynamicRecompute = true;
                if (event.isStillSincePress()) {
                    if (event.isControlDown()) {
                        mapController.addSelection(MapLink.this);
                    } else {
                        mapController.setSelection(MapLink.this);
                    }
                } else if (startCenter != center) {
                    mapController.updateLink(MapLink.this, true);
                }

                event.consume();
            });

            shadow.setOnMouseExited((MouseEvent event) -> {
                if (!event.isPrimaryButtonDown()) {
                    getScene().setCursor(Cursor.DEFAULT);
                }
            });
        }

        public void setYMovable() {
            shadow.setOnMouseDragged(yHandler);
            shadow.setOnMousePressed(inityHandler);
            shadow.setOnMouseEntered((MouseEvent event) -> {
                getScene().setCursor(Cursor.V_RESIZE);
            });
        }

        public void setXMovable() {
            shadow.setOnMouseDragged(xHandler);
            shadow.setOnMousePressed(initxHandler);
            shadow.setOnMouseEntered((MouseEvent event) -> {
                getScene().setCursor(Cursor.H_RESIZE);
            });
        }

        public void disableMovement() {
            shadow.setOnMouseDragged(null);
            shadow.setOnMousePressed(defaultPressHandler);
            shadow.setOnMouseEntered(null);
        }

        public Line getShadow() {
            return shadow;
        }

        public double getLength() {
            Bounds bil = getBoundsInLocal();
            return Math.max(bil.getHeight(), bil.getWidth());
        }

        public boolean isHorizontal() {
            return getStartY() - getEndY() == 0;
        }
    }

    @Override
    public String toString() {
        return "start = " + startNode + ", end = " + endNode + ", endPosition = " + endPosition.name();
    }

}
