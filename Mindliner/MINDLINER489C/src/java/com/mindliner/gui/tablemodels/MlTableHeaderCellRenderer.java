/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui.tablemodels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.border.MatteBorder;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 * Renders table headers; was necesasry to render dark color schemes, too.
 *
 * @author Marius Messerli
 */
public class MlTableHeaderCellRenderer extends JComponent implements TableCellRenderer {

    Color background;
    Color foreground;

    public MlTableHeaderCellRenderer(Color fg, Color bg) {
        foreground = fg;
        background = bg;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        FlowLayout flowLayout = new FlowLayout();
        flowLayout.setAlignment(FlowLayout.CENTER);
        JPanel fullPanel = new JPanel(flowLayout);
        MatteBorder matteBorder = new MatteBorder(0, 0, 0, 1, foreground);
        fullPanel.setBackground(background);
        fullPanel.setBorder(matteBorder);
        JLabel label = new JLabel((String) value);
        label.setForeground(foreground);
        fullPanel.add(label);

        SortIndicator sortIndicator = null;
        RowSorter<? extends TableModel> trs = table.getRowSorter();
        if (trs != null) {
            List<? extends RowSorter.SortKey> sortKeys = trs.getSortKeys();
            for (RowSorter.SortKey s : sortKeys) {
                if (s.getColumn() == column) {
                    sortIndicator = new SortIndicator(s);
                }
            }
            if (sortIndicator != null) {
                fullPanel.add(sortIndicator);
            }
        }
        return fullPanel;
    }

    class SortIndicator extends JPanel {

        RowSorter.SortKey sortKey;
        int[] xCoords = new int[3];
        int[] yCoords = new int[3];

        public SortIndicator(SortKey s) {
            sortKey = s;
        }

        @Override
        public void paint(Graphics g) {
            boolean needsDrawing = false;

            if (sortKey.getSortOrder() == SortOrder.DESCENDING) {
                xCoords[0] = 0;
                xCoords[1] = 6;
                xCoords[2] = 3;
                yCoords[0] = 0;
                yCoords[1] = 0;
                yCoords[2] = 6;
                needsDrawing = true;

            } else if (sortKey.getSortOrder() == SortOrder.ASCENDING) {
                xCoords[0] = 0;
                xCoords[1] = 6;
                xCoords[2] = 3;
                yCoords[0] = 6;
                yCoords[1] = 6;
                yCoords[2] = 0;
                needsDrawing = true;
            }
            if (needsDrawing) {
                g.setColor(foreground);
                g.fillPolygon(xCoords, yCoords, 3);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(10, 10);
        }

    }

}
