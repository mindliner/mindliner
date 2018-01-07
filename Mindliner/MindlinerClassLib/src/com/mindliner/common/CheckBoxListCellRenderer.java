/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.common;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author dominic
 */
public class CheckBoxListCellRenderer extends DefaultListCellRenderer {

    protected static Border noFocusBorder1 = new EmptyBorder(1, 1, 1, 1);

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        ObjectCheckBox checkbox = null;
        if (value instanceof String) {
            String name = (String) value;
            checkbox = new ObjectCheckBox(name, true);
        }
        else {
            checkbox = (ObjectCheckBox) value;
        }
        checkbox.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
        checkbox.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
        checkbox.setEnabled(isEnabled());
        checkbox.setFont(getFont());
        checkbox.setFocusPainted(false);
        checkbox.setBorderPainted(true);
        checkbox.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder")
                : noFocusBorder1);

        return checkbox;
    }
}
