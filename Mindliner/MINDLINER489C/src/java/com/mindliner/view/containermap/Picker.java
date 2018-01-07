/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.containermap;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.events.ObjectChangeObserver;
import java.awt.Image;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * Base class for any scroll pane based icon pickers
 *
 * @author Dominic Plangger
 */
abstract public class Picker extends GridPane implements ObjectChangeObserver {

    protected static final int ICON_SIZE = 20;

    protected int gridRows = 5;
    protected final Background iconBackground, iconBackgroundSel, iconBackgroundOver;
    protected final Border iconBorder, iconBorderSel;
    protected final FXMLController mapController;

    public Picker() {
        mapController = FXMLController.getInstance();
        iconBackground = new Background(new BackgroundFill(Color.WHITE, new CornerRadii(3), Insets.EMPTY));
        iconBackgroundOver = new Background(new BackgroundFill(Color.WHITE, new CornerRadii(3), Insets.EMPTY));
        iconBackgroundSel = new Background(new BackgroundFill(Color.web("#256EB8"), new CornerRadii(3), Insets.EMPTY));
        iconBorder = new Border(new BorderStroke(Color.web("#ECECEC"), BorderStrokeStyle.SOLID, new CornerRadii(3), new BorderWidths(1)));
        iconBorderSel = new Border(new BorderStroke(Color.web("#19A3FF"), BorderStrokeStyle.SOLID, new CornerRadii(3), new BorderWidths(1)));
        setPadding(new Insets(4));
        setHgap(3);
        setVgap(3);
    }

    protected StackPane createImageView(Image img, String tooltip) {
        javafx.scene.image.Image fxImg = ContainerMapUtils.awtToFxImage(img);
        ImageView view = new ImageView(fxImg);
        view.setFitHeight(ICON_SIZE);
        view.setFitWidth(ICON_SIZE);
        final StackPane viewWrap = new StackPane();
        viewWrap.getChildren().add(view);
        viewWrap.setPrefHeight(ICON_SIZE + 20);
        viewWrap.setPrefWidth(ICON_SIZE + 20);
        if (!tooltip.isEmpty()) {
            Tooltip tt = new Tooltip(tooltip);
            Tooltip.install(viewWrap, tt);
        }
        viewWrap.setOnMouseEntered((MouseEvent e) -> {
            getScene().setCursor(Cursor.HAND);
            if (iconBackground.equals(viewWrap.getBackground())) {
                viewWrap.setBackground(iconBackgroundOver);
                viewWrap.setBorder(iconBorderSel);
            }
        });
        viewWrap.setOnMouseExited((MouseEvent e) -> {
            getScene().setCursor(Cursor.DEFAULT);
            if (iconBackgroundOver.equals(viewWrap.getBackground())) {
                viewWrap.setBackground(iconBackground);
                viewWrap.setBorder(iconBorder);
            }
        });
        return viewWrap;
    }

    @Override
    public void objectCreated(mlcObject o) {
        // nop
    }
}
