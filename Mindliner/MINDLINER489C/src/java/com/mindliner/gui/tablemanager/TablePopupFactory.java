/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui.tablemanager;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.MlcContainerMap;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcTask;
import com.mindliner.entities.SoftwareFeature;
import com.mindliner.gui.MindlinerObjectDeletionHandler;
import com.mindliner.gui.ObjectEditorLauncher;
import com.mindliner.gui.ObjectMapperLauncher;
import com.mindliner.view.dispatch.MlObjectViewer;
import com.mindliner.view.dispatch.MlViewDispatcherImpl;
import com.mindliner.weekplanner.TaskPopupBuilder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

/**
 *
 * This class produce the popup menu for the search tables
 *
 * @author Marius Messerli
 */
public class TablePopupFactory {

    List<mlcObject> objects;
    static ResourceBundle bundle = ResourceBundle.getBundle("com/mindliner/resources/Tables");

    public TablePopupFactory(List<mlcObject> objects) {
        this.objects = objects;
    }

    public JPopupMenu createPopup() {

        JPopupMenu popup = new JPopupMenu();
        JPopupMenu.Separator separator = new JPopupMenu.Separator();

        // The Map Menu
        JMenuItem item = new JMenuItem();
        item.setText(bundle.getString("MindTablePopupMap"));
        item.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/cube_molecule.png")));
        item.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, 0));
        item.addActionListener((ActionEvent e) -> {
            ObjectMapperLauncher.map(objects);
        });
        popup.add(item);

        // The WSM Menu
        if (CacheEngineStatic.getCurrentUser().isAuthorizedForFeature(SoftwareFeature.CurrentFeatures.WORKSPHEREMAP)
                && !objects.isEmpty()
                && objects.get(0) instanceof MlcContainerMap) {
            item = new JMenuItem();
            item.setText(bundle.getString("MindTablePopupWSM"));
            item.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/chart_dot.png")));
            item.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, 0));
            item.addActionListener((ActionEvent e) -> {
                MlViewDispatcherImpl.getInstance().display(objects.get(0), MlObjectViewer.ViewType.ContainerMap);

            });
        }
        popup.add(item);

        // The edit menu
        item = new JMenuItem();
        item.setText(bundle.getString("MindTablePopupEdit"));
        item.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, 0));
        item.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/edit-3.png")));
        item.addActionListener((ActionEvent e) -> {
            ObjectEditorLauncher.showEditor(objects);
        });
        popup.add(item);

        popup.add(separator);

        // The delete menu
        item = new JMenuItem();
        item.setText(bundle.getString("MindTablePopupDelete"));
        item.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0));
        item.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/sign_warning.png")));
        item.addActionListener((ActionEvent e) -> {
            MindlinerObjectDeletionHandler.delete(objects);
        });
        popup.add(item);

        if (objects.get(0) instanceof mlcTask) {
            TaskPopupBuilder tpb = new TaskPopupBuilder(true, false);
            tpb.addTaskPopupItems(popup, (mlcTask) objects.get(0), false);
        }
        return popup;
    }
}
