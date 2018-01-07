/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.weekplanner;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.cal.WeekNumbering;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcTask;
import com.mindliner.clientobjects.mlcWeekPlan;
import com.mindliner.commands.CommandRecorder;
import com.mindliner.commands.WeekplanAddTaskCommand;
import com.mindliner.events.SelectionManager;
import com.mindliner.gui.ObjectMapperLauncher;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.ResourceBundle;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 * @author Marius Messerli
 */
public class WeekplanPopupBuilder {

    public JPopupMenu createWeekplanPopupMenu(mlcWeekPlan plan) {
        ResourceBundle bundle = ResourceBundle.getBundle("com/mindliner/resources/WeekPlan");
        JPopupMenu popup = new JPopupMenu();
        JMenuItem map = new JMenuItem();
        map.setText(bundle.getString("WeekplanPopupMap"));
        map.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, 0));
        map.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/1616/cube_molecule.png")));
        map.addActionListener((ActionEvent e) -> {
            mlcObject sel = SelectionManager.getLastSelection();
            ObjectMapperLauncher.map(sel);
        });
        popup.add(map);

        JMenuItem fwd = new JMenuItem();
        fwd.setText(bundle.getString("WeekplanPopupForward"));
        fwd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/1616/arrow_right_blue.png")));
        Date now = new Date();
        int week = WeekNumbering.getWeek(now);
        int year = WeekNumbering.getYear(now);
        mlcWeekPlan currentPlan = CacheEngineStatic.getWeekPlan(year, week);
        if (plan.getWeek() == week && plan.getYear() == year) {
            fwd.setEnabled(false);
        }
        fwd.addActionListener((ActionEvent e) -> {
            mlcObject sel = SelectionManager.getLastSelection();
            CommandRecorder cr = CommandRecorder.getInstance();
            cr.scheduleCommand(new WeekplanAddTaskCommand(sel, currentPlan));
        });
        popup.add(fwd);

        mlcObject sel = SelectionManager.getLastSelection();
        if (sel != null && sel instanceof mlcTask) {
            TaskPopupBuilder tpb = new TaskPopupBuilder(true, false);
            tpb.addTaskPopupItems(popup, (mlcTask) sel, true);
        }
        return popup;
    }

}
