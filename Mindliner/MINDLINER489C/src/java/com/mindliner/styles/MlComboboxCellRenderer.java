/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.styles;

import com.mindliner.gui.color.BaseColorizer;
import com.mindliner.gui.color.FixedKeyColorizer;
import java.awt.Component;
import java.awt.FlowLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.MatteBorder;

/**
 *
 * A custom renderer to implement our color schemes
 *
 * @author Marius Messerli
 */
public class MlComboboxCellRenderer implements ListCellRenderer {

    private final BaseColorizer col;
    private final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

    public MlComboboxCellRenderer(BaseColorizer colorizer) {
        this.col = colorizer;
    }
    
    

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

        JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index,
                isSelected, cellHasFocus);
        renderer.setOpaque(false);
        renderer.setForeground(col.getColorForKey(FixedKeyColorizer.FixedKeys.MAIN_DEFAULT_TEXT));

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
        MatteBorder mb = new MatteBorder(1, 1, 1, 1, col.getColorForKey(FixedKeyColorizer.FixedKeys.TABLE_GRID));
        panel.setBorder(mb);
        panel.add(renderer);
        if (isSelected) {
            panel.setBackground(col.getColorForKey(FixedKeyColorizer.FixedKeys.MAIN_COMBOBOX_SELECTION));
        } else if (cellHasFocus) {
            panel.setBackground(col.getColorForKey(FixedKeyColorizer.FixedKeys.MAIN_COMBOBOX_FOCUS));
        } else {
            panel.setBackground(col.getColorForKey(FixedKeyColorizer.FixedKeys.MAIN_DEFAULT_BACKGROUND));
        }
        return panel;
    }
    
    

}
