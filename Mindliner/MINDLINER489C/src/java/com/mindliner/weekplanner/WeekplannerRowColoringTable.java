/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.mindliner.weekplanner;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.mlcTask;
import com.mindliner.entities.Colorizer;
import com.mindliner.gui.color.BaseColorizer;
import com.mindliner.gui.color.ColorManager;
import com.mindliner.gui.color.FixedKeyColorizer;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

/**
 * A table which alternates the color of even and odd rows. It will not change
 * the selected row's color.
 *
 * @author Marius Messerli
 */
public class WeekplannerRowColoringTable extends JTable {

    private final BaseColorizer colorizer;

    public WeekplannerRowColoringTable() {
        colorizer = ColorManager.getColorizerForType(Colorizer.ColorDriverAttribute.FixedKey);
    }

    public void initialize() {
        setBackground(colorizer.getColorForObject(FixedKeyColorizer.FixedKeys.WEEKPLAN_BACKGROUND));
        setBorder(new EmptyBorder(0, 0, 0, 0));
    }

    protected Color getEvenColor() {
        return colorizer.getColorForObject(FixedKeyColorizer.FixedKeys.WEEKPLAN_EVEN_ROWS);
    }

    protected Color getOddColor() {
        return colorizer.getColorForObject(FixedKeyColorizer.FixedKeys.WEEKPLAN_ODD_ROWS);
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {

        Component c = super.prepareRenderer(renderer, row, column);
        if (!isRowSelected(row)) {
            if (getModel() instanceof WeekTableModel) {
                WeekTableModel wtm = (WeekTableModel) getModel();
                Object o = wtm.getRowObject(row);
                if (o instanceof mlcTask) {
                    if (o.equals(WorkTracker.getUniqueInstance().getCurrentTask())) {
                        c.setBackground(colorizer.getColorForObject(FixedKeyColorizer.FixedKeys.WEEKPLAN_CURRENT_TASK_BACKGROUND));
                        return c;
                    }
                    else if (!CacheEngineStatic.getCurrentWorkers((mlcTask)o).isEmpty()){
                        c.setBackground(colorizer.getColorForObject(FixedKeyColorizer.FixedKeys.WEEKPLAN_CURRENT_TEAM_TASK_BACKGROUND));
                        return c;
                    }
                }
            }
            c.setBackground(row % 2 == 0 ? getEvenColor() : getOddColor());
        }
        return c;
    }

}
