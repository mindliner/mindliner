/*
 * mlTableCellRenderer.java
 *
 * Created on 13. November 2006, 14:37
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.mindliner.gui.tablemodels;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.entities.Colorizer;
import com.mindliner.gui.color.BaseColorizer;
import com.mindliner.gui.color.ColorManager;
import com.mindliner.gui.color.FixedKeyColorizer;
import com.mindliner.gui.font.FontPreferences;
import com.mindliner.gui.tablemanager.MlObjectTable;
import com.mindliner.image.LazyImage;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author Marius Messerli
 *
 */
public class MlTableCellRenderer extends JComponent implements TableCellRenderer {

    private boolean colorCoding = true;
    private BaseColorizer colorizer;
    private static final int IMG_HEIGHT = 30;
    private static final int IMG_WIDTH = 60;

    public MlTableCellRenderer(BaseColorizer colorizer) {
        this.colorizer = colorizer;
    }

    public void setColorCoding(boolean colorCoding) {
        this.colorCoding = colorCoding;
    }

    public boolean isColorCoding() {
        return colorCoding;
    }

    public BaseColorizer getColorizer() {
        return colorizer;
    }

    public void setColorizer(BaseColorizer colorizer) {
        this.colorizer = colorizer;
    }

    /**
     * The default implementation of the getTableRendererComponent returns
     * JLabel component.
     *
     * @return The component to fill the specified cell.
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        Font cellFont = FontPreferences.getFont(MlObjectTable.FONT_PREFERENCE_KEY);
        Color backgroundColor;
        Color foregroundColor;
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BorderLayout());
        Font descriptionFont = new Font(cellFont.getName(), cellFont.getStyle(), (int) (cellFont.getSize() * 0.8));
        Component contentComponent;

        Color rowcolor = getCellColor(table, row);
        MlTableModel mtm = (MlTableModel) table.getModel();
        assert (mtm != null);
        TableRowSorter trs = (TableRowSorter) table.getRowSorter();
        assert trs != null : "No TableRowSorter installed on table";

        mlcObject mbo = (mlcObject) mtm.getSourceObject(trs.convertRowIndexToModel(row));

        if (isSelected) {
            foregroundColor = ColorManager.getForegroundColor(rowcolor);
            backgroundColor = rowcolor;
        } else {
            foregroundColor = rowcolor;
            BaseColorizer fkc = ColorManager.getColorizerForType(Colorizer.ColorDriverAttribute.FixedKey);
            backgroundColor = fkc.getColorForObject(FixedKeyColorizer.FixedKeys.TABLE_CELL_BACKGROUND);
            if (mbo.isArchived()) {
                backgroundColor = backgroundColor.darker();
            }
        }

        if (value == null) {
            JLabel label = new JLabel("");
            contentComponent = label;
        } else {
            if (value instanceof Boolean) {
                JCheckBox checkBox = new JCheckBox();
                Boolean state = (Boolean) value;
                checkBox.setSelected(state);
                checkBox.setOpaque(false);
                contentComponent = checkBox;
            } else if (value instanceof Image || value instanceof LazyImage) {
                JLabel label = new JLabel("");
                Image img;
                if (value instanceof Image) {
                    img = (Image) value;
                } else {
                    img = ((LazyImage) value).getImage();
                }
                img = scaleImage(img);
                label.setIcon(new ImageIcon(img));
                contentComponent = label;
            } else {
                JLabel label = new JLabel(value.toString());
                label.setForeground(foregroundColor);
                label.setFont(cellFont);
                contentComponent = label;
            }
        }

        // I check the value class for String because I only want the headline field decorated with description
        if (value != null && value instanceof String && !mbo.getDescription().isEmpty()) {
            labelPanel.add(contentComponent, BorderLayout.NORTH);
            JTextArea descriptionTextArea = new JTextArea(mbo.getDescription());
            descriptionTextArea.setLineWrap(true);
            descriptionTextArea.setFont(descriptionFont);
            descriptionTextArea.setEditable(false);
            descriptionTextArea.setForeground(foregroundColor);
            descriptionTextArea.setBackground(backgroundColor);
            descriptionTextArea.setRows(3);
            labelPanel.add(descriptionTextArea, BorderLayout.CENTER);
        } else {
            labelPanel.add(contentComponent, BorderLayout.CENTER);
            if (mbo.getDescription() == null) {
                System.err.println("description is null on object id=" + mbo.getId());
            }
        }
        labelPanel.setBackground(backgroundColor);
        return labelPanel;
    }

    public Color getCellColor(JTable table, int row) {

        if (isColorCoding() == false) {
            return colorizer.getDefaultColor();
        } else {
            mlcObject o = (mlcObject) MlTableModel.getSourceObjectByViewIndex(table, row);
            Color c = colorizer.getColorForObject(o);
            if (c == null) {
                return colorizer.getDefaultColor();
            }
            return c;
        }
    }

    private Image scaleImage(Image img) {
        if (img.getHeight(null) > IMG_HEIGHT) {
            double f = IMG_HEIGHT / (double) img.getHeight(null);
            int width = (int) Math.min(IMG_WIDTH, img.getWidth(null) * f);
            img = img.getScaledInstance(width, IMG_HEIGHT, Image.SCALE_SMOOTH);
        }
        return img;
    }

}
