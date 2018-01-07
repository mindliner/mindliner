/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.containermap;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.MlcImage;
import com.mindliner.clientobjects.mlcClient;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.commands.LinkCommand;
import com.mindliner.commands.UnlinkCommand;
import com.mindliner.enums.LinkRelativeType;
import com.mindliner.events.ObjectChangeManager;
import java.awt.Image;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/**
 *
 * @author Dominic Plangger
 */
public class IconPicker extends Picker{
    
    
    private List<MlcImage> selectedIcons = new ArrayList<>();
    private final Map<Integer, Pane> iconMap = new HashMap<>();
    private List<mlcObject> objects;

    public List<MlcImage> getSelectedIcons() {
        return selectedIcons;
    }
    
    public IconPicker(mlcClient client) {
        List<MlcImage> icons = CacheEngineStatic.getIcons(client);
        for (int i = 0; i < icons.size(); i++) {
            final MlcImage icon = icons.get(i);
            Image img = icon.getIcon().getImage();
            final StackPane viewWrap = createImageView(img, icon.getDescription());
            viewWrap.setOnMouseClicked((MouseEvent e) -> {
                if (selectedIcons.contains(icon)) {
                    selectedIcons.remove(icon);
                    viewWrap.setBackground(iconBackground);
                    viewWrap.setBorder(iconBorder);
                    objects.stream().forEach((o) -> {
                        mapController.addCommand(new UnlinkCommand(o, icon, false, LinkRelativeType.ICON_OBJECT));
                        o.getIcons().remove(icon);
                        ObjectChangeManager.objectChanged(o);
                    });
                }
                else {
                    selectedIcons.add(icon);
                    viewWrap.setBackground(iconBackgroundSel);
                    viewWrap.setBorder(iconBorderSel);
                    objects.stream().forEach((o) -> {
                        mapController.addCommand(new LinkCommand(o, icon, false, LinkRelativeType.ICON_OBJECT));
                        o.getIcons().add(icon);
                        ObjectChangeManager.objectChanged(o);
                    });
                }
            });
            iconMap.put(icon.getId(), viewWrap);
            add(viewWrap, i % gridRows, i / gridRows);
        }
    }

    public void setObjects(List<mlcObject> objs) {
        objects = objs;
        selectedIcons.clear();
        List<MlcImage> icons = new ArrayList<>();
        objs.stream().forEach((obj) -> {icons.addAll(obj.getIcons());});
        for (Pane p : iconMap.values()) {
            p.setBackground(iconBackground);
            p.setBorder(iconBorder);
        }
        for (MlcImage icon : icons) {
            if (iconMap.containsKey(icon.getId())) {
                Pane p = iconMap.get(icon.getId());
                p.setBackground(iconBackgroundSel);
                p.setBorder(iconBorderSel);
                selectedIcons.add(icon);
            }
            else {
                Logger.getLogger(IconPicker.class.getName()).log(Level.WARNING, "Icon with id {0} is not available for the current data pool", icon.getId());
            }
        }
    }

    @Override
    public void objectChanged(mlcObject o) {
        if (objects.contains(o)) {
            // seems strange but remove operates on the mlcObject 'equal' method that only consideres the ID
            // therefore the removed and added objects are not the same
            objects.remove(o);
            objects.add(o);
            setObjects(objects);
        }
    }

    @Override
    public void objectDeleted(mlcObject o) {
        if (objects.contains(o)) {
            objects.remove(o);
            setObjects(objects);
        }
    }

    @Override
    public void objectReplaced(int oldId, mlcObject o) {
        Iterator<mlcObject> it = objects.iterator();
        boolean removed = false;
        while (it.hasNext() && !removed) {
            mlcObject ob = it.next();
            if (ob.getId() == oldId) {
                it.remove();
                removed = true;
            }
        }
        
        if (removed) {
            objects.add(o);
            setObjects(objects);
        }
    }

}
