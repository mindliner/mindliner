/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.common;

/**
 *
 * @author dominic
 */
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

public class CheckBoxList extends JList {

    public CheckBoxList() {
        super();

        setModel(new DefaultListModel());
        setCellRenderer(new CheckBoxListCellRenderer());

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!isEnabled()) {
                    return;
                }
                int index = locationToIndex(e.getPoint());

                if (index != -1) {
                    Object obj = getModel().getElementAt(index);
                    if (obj instanceof JCheckBox) {
                        JCheckBox checkbox = (JCheckBox) obj;

                        checkbox.setSelected(!checkbox.isSelected());
                        repaint();
                    }
                }
            }
        });

        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    @SuppressWarnings("unchecked")
    public int[] getCheckedIdexes() {
        java.util.List list = new java.util.ArrayList();
        DefaultListModel dlm = (DefaultListModel) getModel();
        for (int i = 0; i < dlm.size(); ++i) {
            Object obj = getModel().getElementAt(i);
            if (obj instanceof JCheckBox) {
                JCheckBox checkbox = (JCheckBox) obj;
                if (checkbox.isSelected()) {
                    list.add(new Integer(i));
                }
            }
        }

        int[] indexes = new int[list.size()];

        for (int i = 0; i < list.size(); ++i) {
            indexes[i] = ((Integer) list.get(i)).intValue();
        }

        return indexes;
    }

    @SuppressWarnings("unchecked")
    public java.util.List getCheckedItems() {
        java.util.List list = new java.util.ArrayList();
        DefaultListModel dlm = (DefaultListModel) getModel();
        for (int i = 0; i < dlm.size(); ++i) {
            Object obj = getModel().getElementAt(i);
            if (obj instanceof JCheckBox) {
                JCheckBox checkbox = (JCheckBox) obj;
                if (checkbox.isSelected()) {
                    list.add(checkbox);
                }
            }
        }
        return list;
    }

    public void makeSelectionToAll(boolean selected) {
        DefaultListModel dlm = (DefaultListModel) getModel();
        for (int i = 0; i < dlm.size(); ++i) {
            Object obj = getModel().getElementAt(i);
            if (obj instanceof JCheckBox) {
                JCheckBox checkbox = (JCheckBox) obj;
                checkbox.setSelected(selected);
            }
        }
    }
}