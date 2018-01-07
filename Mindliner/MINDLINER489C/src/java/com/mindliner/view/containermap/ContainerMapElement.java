/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.containermap;

import com.mindliner.clientobjects.mlcObject;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

/**
 *
 * @author Dominic Plangger
 */
public interface ContainerMapElement extends Selectable{
    
    public mlcObject getObject();
    
    public void setObject(mlcObject obj);
    
    public void onMousePressed(MouseEvent event);
    
    public void onMouseDragged(MouseEvent event);
    
    public void onMouseReleased(MouseEvent event);
    
    public ElementMover getMover();
    
}
