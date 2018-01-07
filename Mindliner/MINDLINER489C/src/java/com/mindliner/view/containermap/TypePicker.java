/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.containermap;

import com.mindliner.analysis.MlClassHandler.MindlinerObjectType;
import com.mindliner.clientobjects.MlClientClassHandler;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.commands.TypeChangeCommand;
import com.mindliner.img.icons.MlIconManager;
import java.awt.Image;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/**
 * Icon picker for the different object types.
 * 
 * @author Dominic Plangger
 */
public class TypePicker extends Picker{
    
    
    private MindlinerObjectType selectedType = null;
    private Map<MindlinerObjectType, Pane> iconMap = new HashMap<>();
    private mlcObject object = null;

    public TypePicker() {
        gridRows = 3;
        List<Image> icons = new ArrayList<>();
        MindlinerObjectType[] types = {MindlinerObjectType.Knowlet, MindlinerObjectType.Task, MindlinerObjectType.Collection};
        for(MindlinerObjectType type : types) {
            icons.add(MlIconManager.getImageForType(type, MlIconManager.IconSize.thirtyTwo));
        }
        
        for (int i = 0; i < icons.size(); i++) {
            Image img = icons.get(i);
            MindlinerObjectType type = types[i];
            final StackPane viewWrap = createImageView(img, "");
            viewWrap.setOnMouseClicked((MouseEvent ev) -> {
                if (type.equals(selectedType)) {
                    if (object == null) {
                        selectedType = null;
                        viewWrap.setBackground(iconBackground);
                        viewWrap.setBorder(iconBorder);
                    }
                    return;
                }
                selectedType = type;
                if (object != null) {
                    TypeChangeCommand cmd = new TypeChangeCommand(object, MlClientClassHandler.getClassByType(selectedType));
                    mapController.addCommand(cmd);
                }
                
                for (Map.Entry<MindlinerObjectType, Pane> e : iconMap.entrySet()) {
                    Pane p = e.getValue();
                    if (e.getKey().equals(type)) {
                        p.setBackground(iconBackgroundSel);
                        p.setBorder(iconBorderSel);
                    } else {
                        p.setBackground(iconBackground);
                        p.setBorder(iconBorder);
                    }
                }
            });
            iconMap.put(type, viewWrap);
            add(viewWrap, i % gridRows, i / gridRows);
        }
    }

    public void setObject(mlcObject obj) {
        object = obj;
        selectedType = obj != null ? MlClientClassHandler.getTypeByClass(obj.getClass()) : null;

        for (Map.Entry<MindlinerObjectType, Pane> e : iconMap.entrySet()) {
            Pane p = e.getValue();
            if (selectedType != null && e.getKey().equals(selectedType)) {
                p.setBackground(iconBackgroundSel);
                p.setBorder(iconBorderSel);
            }
            else {
                p.setBackground(iconBackground);
                p.setBorder(iconBorder);
            }
        }
    }
    
    public MindlinerObjectType getType() {
        return selectedType;
    }

    @Override
    public void objectChanged(mlcObject o) {
        if (object != null && o.getId() == object.getId()) {
            setObject(o);
        }
    }

    @Override
    public void objectDeleted(mlcObject o) {
        if (object != null && o.getId() == object.getId()) {
            setObject(null);
        }
    }

    @Override
    public void objectReplaced(int oldId, mlcObject o) {
        if (object != null && oldId == object.getId()) {
            setObject(o);
        }
    }

}
