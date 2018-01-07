/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.prefs;

import com.mindliner.gui.color.BaseColorizer;
import com.mindliner.gui.color.ColorManager;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 *
 * @author M.Messerli
 */
public class ColorValueListRenderer implements ListCellRenderer {

    public ColorValueListRenderer(BaseColorizer colorizer) {
        this.colorizer = colorizer;
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel cell = new JLabel();
        cell.setOpaque(true);
        if (value != null) {
            cell.setText(value.toString());
            if (isSelected) {
                cell.setBackground(Color.darkGray);
                cell.setForeground(Color.lightGray);
            } else {
                Color bg = colorizer.getColorForKey(value);
                if (bg == null) { // no color has been defined for this value
                    bg = Color.white;
                }
                cell.setBackground(bg);
                cell.setForeground(ColorManager.getForegroundColor(bg));
            }
        } else {
            cell.setText("NA");
            cell.setForeground(Color.red);
            cell.setBackground(Color.black);
        }
        return cell;
    }
    private BaseColorizer colorizer;
}
