/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.mindliner.weekplanner;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcTask;
import com.mindliner.clientobjects.mlcWeekPlan;
import com.mindliner.commands.CommandRecorder;
import com.mindliner.commands.WeekplanAddTaskCommand;
import com.mindliner.exporter.MindlinerTransferHandler;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.TransferHandler.TransferSupport;

/**
 *
 * @author Marius Messerli
 */
public class WorkPlanTransferHandler extends MindlinerTransferHandler {

    @Override
    public boolean importData(TransferSupport info) {
        if (!info.isDrop()) {
            return false;
        }

        boolean success = false;
        boolean nonTaskErrorDelivered = false;
        Transferable t = info.getTransferable();
        if (!(info.getComponent() instanceof WeekplannerRowColoringTable)) {
            return false;
        }
        WeekplannerRowColoringTable planTable = (WeekplannerRowColoringTable) info.getComponent();
        WeekTableModel model = (WeekTableModel) planTable.getModel();
        mlcWeekPlan weekPlan = model.getWeekPlan();

        if (info.isDataFlavorSupported(mindlinerObjectLocalFlavor)) {
            try {
                List<mlcObject> dropObjects = (List<mlcObject>) t.getTransferData(getMindlinerObjectLocalFlavor());
                if (dropObjects.isEmpty()) {
                    return false;
                }
                for (mlcObject o : dropObjects) {
                    if (o instanceof mlcTask) {
                        boolean contains = false;
                        for (Integer id  : weekPlan.getTasksIds()) {
                            if (id == o.getId()) {
                                contains = true;
                            }
                        }
                        if (contains == false) {
                            CommandRecorder cr = CommandRecorder.getInstance();
                            cr.scheduleCommand(new WeekplanAddTaskCommand(o, weekPlan));
                        }
                    }
                    else {
                        // only show the error once per drop
                        if (!nonTaskErrorDelivered) {
                            JOptionPane.showMessageDialog(null, "Sorry: You can only add tasks to week plans", "Weekplan Addition", JOptionPane.ERROR_MESSAGE);
                            nonTaskErrorDelivered = true;
                        }
                    }
                }
            } catch (UnsupportedFlavorException | IOException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Drop Data Import", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return success;
    }
}
