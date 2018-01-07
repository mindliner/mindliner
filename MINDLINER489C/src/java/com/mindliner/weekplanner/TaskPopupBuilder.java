/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.weekplanner;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.mlcTask;
import com.mindliner.commands.CommandRecorder;
import com.mindliner.commands.CompletionUpdateCommand;
import com.mindliner.entities.SoftwareFeature;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * Utility class that adds timer menu items to existing menus
 *
 * @author Marius Messerli
 */
public class TaskPopupBuilder {

    boolean separatorBefore;
    boolean separatorAfter;

    public TaskPopupBuilder(boolean separatorBefore, boolean separatorAfter) {
        this.separatorBefore = separatorBefore;
        this.separatorAfter = separatorAfter;
    }

    /**
     * Adds start and stop work menu items for the specified task to the
     * specified menu.
     *
     * @param menu The menu to which the functions are added
     * @param task The task for which the start/stop actions are to be added
     * @param filterWorkUnitsForCurrentWeek If false, then only work units are
     * shown that fall into the current week as specified by the work tracker,
     * otherwise all are shown
     */
    public void addTaskPopupItems(JPopupMenu menu, mlcTask task, boolean filterWorkUnitsForCurrentWeek) {
        ResourceBundle bundle = ResourceBundle.getBundle("com/mindliner/resources/WeekPlan");
        JMenuItem item;
        if (task == null) {
            return;
        }
        if (separatorBefore) {
            menu.add(new JPopupMenu.Separator());
        }

        // mark the task completed/uncompleted
        if (!task.isCompleted()) {
            item = new JMenuItem();
            item.setText(bundle.getString("TaskPopupMarkCompleted"));
            item.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/checkbox.png")));
            item.addActionListener((ActionEvent e) -> {
                CommandRecorder cr = CommandRecorder.getInstance();
                cr.scheduleCommand(new CompletionUpdateCommand(task, true));
            });
            menu.add(item);
        } else {
            // mark the task uncompleted
            item = new JMenuItem();
            item.setText(bundle.getString("TaskPopupMarkUncompleted"));
            item.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/checkbox_unchecked.png")));
            item.addActionListener((ActionEvent e) -> {
                CommandRecorder cr = CommandRecorder.getInstance();
                cr.scheduleCommand(new CompletionUpdateCommand(task, false));
            });
            menu.add(item);
        }

        if (CacheEngineStatic.getCurrentUser().isAuthorizedForFeature(SoftwareFeature.CurrentFeatures.TIME_MANAGEMENT)) {

            if (separatorBefore) {
                menu.add(new JPopupMenu.Separator());
            }

            if (WorkTracker.getUniqueInstance().getCurrentWorkItem() == null || !WorkTracker.getUniqueInstance().getCurrentWorkItem().equals(task)) {

                // the standard start timer icon
                item = new JMenuItem();
                item.setText(bundle.getString("TaskPopupStartWork"));
                item.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/gear_run.png")));
                item.addActionListener((ActionEvent e) -> {
                    WorkTracker.getUniqueInstance().startTracking(task);
                });
                menu.add(item);

                // the menu item to start timer while back-filling to last stop
                item = new JMenuItem();
                item.setText(bundle.getString("TaskPopupStartWorkBackFill"));
                item.setToolTipText(bundle.getString("TaskPopupStartWorkBackFill_TT"));
                item.addActionListener((ActionEvent e) -> {
                    WorkTracker.getUniqueInstance().startTimerBackFilling();
                });
                menu.add(item);

                // the menu item to add past work
                item = new JMenuItem();
                item.setText(bundle.getString("TaskPopupAddPastUnits"));
                item.setToolTipText(bundle.getString("TaskPopupAddPastUnits_TT"));
                item.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/add-3.png")));
                item.addActionListener((ActionEvent e) -> {
                    WorkTracker.getUniqueInstance().addPastWork(task);
                });
                menu.add(item);

                // the work units
                item = new JMenuItem();
                item.setText(bundle.getString("TaskPopupShowWorkUnits"));
                item.addActionListener((ActionEvent e) -> {
                    WorkTracker.getUniqueInstance().showWorkUnits(task, filterWorkUnitsForCurrentWeek);
                });
                menu.add(item);

            } else if (WorkTracker.getUniqueInstance().getCurrentWorkItem().equals(task)) {
                item = new JMenuItem();
                item.setText(bundle.getString("TaskPopupStopWork"));
                item.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/gear_stop.png")));
                item.addActionListener((ActionEvent e) -> {
                    WorkTracker.getUniqueInstance().stopPerformed();
                });
                menu.add(item);
            }
        }

        if (separatorAfter) {
            menu.add(new JPopupMenu.Separator());
        }
    }
}
