/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.mindliner.weekplanner;

import com.mindliner.entities.Colorizer;
import com.mindliner.gui.color.BaseColorizer;
import com.mindliner.gui.color.ColorManager;
import com.mindliner.gui.color.FixedKeyColorizer;
import com.mindliner.prefs.MlMainPreferenceEditor;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.MatteBorder;
import javax.swing.table.TableCellRenderer;

/**
 * @todo Make one renderer per content class rather than check the type with if
 * below
 *
 * @author Marius Messerli
 */
public class WeekPlanTableCellRenderer implements TableCellRenderer {

    private final int toGoColumnIndex;
    private int todayPlanColumnIndex;

    public WeekPlanTableCellRenderer(int toGoColumnIndex, int todayIndex) {
        this.toGoColumnIndex = toGoColumnIndex;
        this.todayPlanColumnIndex = todayIndex;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JPanel cellPanel = new JPanel();

        Font baseFont = MlMainPreferenceEditor.getWeekplanFont();
        Font normalFont = new Font(baseFont.getFamily(), Font.PLAIN, baseFont.getSize());
        Font boldFont = new Font(baseFont.getFamily(), Font.BOLD, baseFont.getSize());

        BaseColorizer fkc = ColorManager.getColorizerForType(Colorizer.ColorDriverAttribute.FixedKey);
        Color todayColor = fkc.getColorForObject(FixedKeyColorizer.FixedKeys.WEEKPLAN_CURRENT_DAY);
        Color toGoColor = fkc.getColorForObject(FixedKeyColorizer.FixedKeys.WEEKPLAN_TOGO);
        Color selectionColor = fkc.getColorForObject(FixedKeyColorizer.FixedKeys.WEEKPLAN_SELECTION_BACKGROUND);
        Color selectionFontColor = fkc.getColorForObject(FixedKeyColorizer.FixedKeys.WEEKPLAN_SELECTION_FONT);
        Color normalFontColor = fkc.getColorForObject(FixedKeyColorizer.FixedKeys.WEEKPLAN_FONT);

        JLabel label = new JLabel();

        // define alignment
        if (column > 0) {
            label.setHorizontalAlignment(JLabel.RIGHT);
        } else {
            label.setHorizontalAlignment(JLabel.LEFT);
        }

        if (value == null) {
            label.setText("NULL");
            return label;
        }

        // handle special case of "0" - meaning delete the plan unit
        if (value instanceof Integer) {
            Integer i = (Integer) value;
            if (i == 0) {
                label.setText("");
            } else {
                int hours = i / 60;
                int mins = i % 60;
                String text = String.format("%d:%02d", hours, mins);
                label.setText(text);
            }
        } else {
            label.setText(value.toString());
        }

        // define font and foreground
        if (isSelected) {
            label.setFont(normalFont);
            cellPanel.setBackground(selectionColor);
            label.setForeground(selectionFontColor);
        } else {
            if (column == toGoColumnIndex) {
                label.setFont(boldFont);
                label.setForeground(toGoColor);
            } else if (column == todayPlanColumnIndex) {
                label.setFont(boldFont);
                label.setForeground(todayColor);
            } // last row is TOTALS row
            else if (row == table.getRowCount() - 1) {
                label.setFont(boldFont);
                label.setForeground(normalFontColor);
            } else {
                label.setFont(normalFont);
                label.setForeground(normalFontColor);
            }
        }

        // define borders
        int top = 0;
        int bottom = 0;
        int left = 0;
        int right = 0;
        cellPanel.setLayout(new BorderLayout());
        cellPanel.add(label, BorderLayout.CENTER);
        if (row == table.getRowCount() - 1) {
            top = 1;
            bottom = 1;
        }
        if (column == todayPlanColumnIndex) {
            left = 1;
        }
        if (column == todayPlanColumnIndex + 1) {
            right = 1;
        }
        cellPanel.setBorder(new MatteBorder(top, left, bottom, right, table.getGridColor()));
        return cellPanel;
    }

    /**
     * Sets the index of the column that shows today's plan.
     *
     * @param todayIndex The index of the column that shows today's plan. Set -1
     * if none of the columns show today because the week is not the current
     * week.
     */
    public void setTodayPlanColumnIndex(int todayIndex) {
        this.todayPlanColumnIndex = todayIndex;
    }
}
