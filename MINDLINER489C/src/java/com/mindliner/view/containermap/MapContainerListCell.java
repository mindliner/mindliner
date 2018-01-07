/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.containermap;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;


/**
 * Provides custom cells for container type drop-down
 * @author Dominic Plangger
 */
class MapContainerListCell extends ListCell<ContainerProperties> {

    private final boolean showSetting;
    
    public MapContainerListCell(boolean showSetting) {
        this.showSetting = showSetting; 
   }
    

    @Override
    protected void updateItem(ContainerProperties item, boolean empty) {
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            return;
        }
        super.updateItem(item, empty);
        CustomButton cb = new CustomButton(item.getColor());
        cb.setProperty(item);
        
        Label l = new Label();
        l.setText(item.getLabel());
        l.setTextFill(item.getColor());
        BorderPane cell = new BorderPane();
        cell.setLeft(l);
        if (showSetting) {
            // show the edit button only for the dropdown list cells, but not the 'button' list cell
            cell.setRight(cb);
            BorderPane.setMargin(cb, new Insets(0, 5, 0, 0));
        }
        setGraphic(cell);
    }
    
   
    
    private class CustomButton extends ColorPicker {
        
        ContainerProperties property;

        public CustomButton(Color initColor) {
            super(initColor);
            getStyleClass().add("button");
            setStyle("-fx-color-label-visible: false ;");
            setBorder(null);
            setBackground(null);
            setPadding(new Insets(0));
            setOnAction((event) -> {
                Color nc = this.getValue();
                property.setColor(nc);
                FXMLController.getInstance().updateContainerCombo();
            });
            
        }

        public void setProperty(ContainerProperties property) {
            this.property = property;
        }
    }
    
}
