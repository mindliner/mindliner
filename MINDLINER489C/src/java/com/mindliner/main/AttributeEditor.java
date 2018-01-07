/*
 * AttributeEditor.java
 *
 * This class implement an editor that allows users to change (almost all)
 * attributes of mindliner objects. In addition to the one current object
 * the editor can manage a list of object that are selected.
 * 
 * The editor's fields show the information of the "current" object.
 * If changes to the object occur they are applied to that object.
 *
 * Created on Dec 9, 2008, 10:58:14 AM
 * @author Marius Messerli
 */
package com.mindliner.main;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.categories.mlsPriority;
import com.mindliner.clientobjects.MlcLink;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcTask;
import com.mindliner.contentfilter.Completable;
import com.mindliner.contentfilter.Timed;
import com.mindliner.gui.color.FixedKeyColorizer;
import com.mindliner.styles.MlStyler;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;

/**
 *
 * @author Marius Messerli
 */
public final class AttributeEditor extends javax.swing.JPanel {

    private mlcObject currentObject = null;
    private MlcLink currentConnection = null;
    private List<mlcObject> objects = new ArrayList<>();
    private BulkUpdater bulkUpdater = null;
    private boolean archiveStateChanged;
    private boolean privateStateChanged;
    private boolean completedStateChanged;
    private boolean priorityChanged;
    private boolean dueDateChanged;
    private boolean dataPoolChanged;
    private boolean confidentialityChanged;

    /**
     * Creates new form AttributeEditor
     *
     * @param olist
     */
    public AttributeEditor(List<mlcObject> olist) {
        initComponents();
        this.objects = olist;
        bulkUpdater = new BulkUpdater();
        configureComponents();
        // call this after component init because during init the states are updated already
        initializeState();
    }

    private void initializeState() {
        archiveStateChanged = false;
        privateStateChanged = false;
        completedStateChanged = false;
        priorityChanged = false;
        dueDateChanged = false;
        dataPoolChanged = false;
        confidentialityChanged = false;
    }

    public void saveState() {
        if (privateStateChanged) {
            bulkUpdater.updatePrivacyState(objects, PrivateCheckbox.isSelected());
        }
        if (archiveStateChanged) {
            bulkUpdater.updateArchiveState(objects, Archived.isSelected());
        }
        if (completedStateChanged) {
            bulkUpdater.updateCompletionState(objects, CompletedCheckbox.isSelected());
        }
        if (dueDateChanged) {
            bulkUpdater.updateDueDate(objects, DueDatePicker.getDate());
        }
        if (priorityChanged) {
            mlsPriority p = (mlsPriority) PriorityCombo.getSelectedItem();
            bulkUpdater.updateTaskPriority(objects, p);
        }
        if (dataPoolChanged) {
            bulkUpdater.updateDataPool(objects, ObjectAccess.getDataPool());
        }
        if (confidentialityChanged) {
            bulkUpdater.updateConfidentiality(objects, ObjectAccess.getConfidentiality());
        }
    }

    public void configureComponents() {

        // Priority
        DefaultComboBoxModel dcm = new DefaultComboBoxModel<>();
        List<mlsPriority> plist = CacheEngineStatic.getPriorities();
        for (mlsPriority p : plist) {
            dcm.addElement(p);
        }
        PriorityCombo.setModel(dcm);
        PriorityCombo.setSelectedIndex(0);

        ObjectAccess.configureComponents();
        ObjectAccess.addDataPoolActionListener((ActionEvent e) -> {
            dataPoolChanged = true;
        });
        ObjectAccess.addConfidentialityActionListener((ActionEvent e) -> {
            confidentialityChanged = true;
        });
        this.currentObject = (objects == null || objects.isEmpty()) ? null : objects.get(objects.size() - 1);
        updateControls();
    }

    public void applyColors(FixedKeyColorizer colorizer) {
        Color bg = colorizer.getColorForKey(FixedKeyColorizer.FixedKeys.MAIN_DEFAULT_BACKGROUND);
        Color fg = colorizer.getColorForKey(FixedKeyColorizer.FixedKeys.MAIN_DEFAULT_TEXT);
        setBackground(bg);
        CurrentSelectionLabel.setForeground(fg);
        IDLabel.setForeground(fg);
        DueDateLabel.setForeground(fg);
        PriorityLabel.setForeground(fg);
        DueDatePicker.setForeground(fg);
        DueDatePicker.setBackground(bg);
        ObjectAccess.applyColors(colorizer);
        MlStyler.colorizeComboBox(PriorityCombo, colorizer);
    }

    private void disableControlsAndClearContent() {
        // clear Node Attributes
        ObjectAccess.setEnabled(false);
        PrivateCheckbox.setEnabled(false);
        CompletedCheckbox.setEnabled(false);
        CompletionStateLabel.setEnabled(false);
        DueDatePicker.setDate(null);
        DueDatePicker.setEnabled(false);
        PriorityCombo.setEnabled(false);
        PriorityCombo.setSelectedIndex(-1);
        CurrentSelectionLabel.setText("No Selection");
        IDLabel.setText("ID: ");
    }

    private void enableBaseControls() {
        ObjectAccess.setEnabled(true);
        PrivateCheckbox.setEnabled(true);
        CompletedCheckbox.setEnabled(true);
        CompletionStateLabel.setEnabled(true);
        PriorityCombo.setEnabled(true);
    }

    private void updateControls() {
        NumberFormat doubleFormatter = NumberFormat.getInstance();
        doubleFormatter.setMaximumFractionDigits(2);
        enableBaseControls();
        /**
         * {currentObject = null} prevents the action listeners from doing any
         * work this implementation felt simpler than unregistering the
         * listeners and registering them again
         */

        /**
         * The last object in the selection drives the GUI. If it is a Knowlet,
         * for example, the knowlet-specific attributes are activated.
         */
        Archived.setSelected(currentObject.isArchived());

        // private enabled only for owners
        int ownerId = currentObject.getOwner().getId();
        int userId = CacheEngineStatic.getCurrentUser().getId();
        if (ownerId == userId) {
            PrivateCheckbox.setEnabled(true);
            PrivateCheckbox.setSelected(currentObject.isPrivateAccess());
        } else {
            PrivateCheckbox.setEnabled(false);
        }

        // Completable
        if (currentObject instanceof Completable) {
            Completable c = (Completable) currentObject;
            CompletedCheckbox.setEnabled(true);
            CompletionStateLabel.setEnabled(true);
            CompletedCheckbox.setSelected(c.isCompleted());
        } else {
            CompletedCheckbox.setEnabled(false);
            CompletionStateLabel.setEnabled(false);
        }

        // Timed
        if (currentObject instanceof Timed) {
            Timed t = (Timed) currentObject;
            DueDatePicker.setEnabled(true);
            DueDatePicker.setDate(t.getDueDate());
        } else {
            DueDatePicker.setDate(null);
            DueDatePicker.setEnabled(false);
        }

        // mlcTask
        if (currentObject instanceof mlcTask) {
            mlcTask t = (mlcTask) currentObject;
            PriorityCombo.setEnabled(true);
            PriorityCombo.setSelectedItem(t.getPriority());
        } else {
            PriorityCombo.setEnabled(false);
        }
        switch (objects.size()) {
            case 1:
                CurrentSelectionLabel.setText("");
                break;
            case 2:
                CurrentSelectionLabel.setText("Any change applies to both selected objects");
                break;
            default:
                CurrentSelectionLabel.setText("Any change applies to all " + objects.size() + " selected objects");
                break;
        }

        ObjectAccess.initializeFromObject(currentObject);

        SimpleDateFormat sdf = new SimpleDateFormat();
        if (currentObject.getId() >= 0) {
            IDLabel.setText("ID: " + String.format("%,d", currentObject.getId()));
        } else {
            IDLabel.setText("Temp-ID: " + String.format("%,d", -currentObject.getId()));
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        NodePanel = new javax.swing.JPanel();
        DueDateLabel = new javax.swing.JLabel();
        PriorityCombo = new javax.swing.JComboBox();
        PriorityLabel = new javax.swing.JLabel();
        DueDatePicker = new org.jdesktop.swingx.JXDatePicker();
        CurrentSelectionLabel = new javax.swing.JLabel();
        IDLabel = new javax.swing.JLabel();
        ObjectAccess = new com.mindliner.gui.ObjectAccessControls();
        StatePanel = new javax.swing.JPanel();
        PrivateCheckbox = new javax.swing.JCheckBox();
        Archived = new javax.swing.JCheckBox();
        CompletedCheckbox = new javax.swing.JCheckBox();
        CompletionStateLabel = new javax.swing.JLabel();
        ArchivedStateLabel = new javax.swing.JLabel();
        PrivateStateLabel = new javax.swing.JLabel();

        NodePanel.setOpaque(false);

        DueDateLabel.setFont(DueDateLabel.getFont().deriveFont(DueDateLabel.getFont().getSize()-2f));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/mindliner/resources/GeneralEditor"); // NOI18N
        DueDateLabel.setText(bundle.getString("AE_DueDateLabel")); // NOI18N
        DueDateLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        PriorityCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        PriorityCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PriorityComboActionPerformed(evt);
            }
        });

        PriorityLabel.setFont(PriorityLabel.getFont().deriveFont(PriorityLabel.getFont().getSize()-2f));
        PriorityLabel.setLabelFor(PriorityCombo);
        PriorityLabel.setText(bundle.getString("AttributeEditorPriorityLabel")); // NOI18N

        DueDatePicker.setFont(DueDatePicker.getFont());
        DueDatePicker.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DueDatePickerActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout NodePanelLayout = new javax.swing.GroupLayout(NodePanel);
        NodePanel.setLayout(NodePanelLayout);
        NodePanelLayout.setHorizontalGroup(
            NodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(NodePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(NodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(DueDateLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(DueDatePicker, javax.swing.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
                    .addComponent(PriorityLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(PriorityCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        NodePanelLayout.setVerticalGroup(
            NodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(NodePanelLayout.createSequentialGroup()
                .addComponent(DueDateLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(DueDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(PriorityLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(PriorityCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        CurrentSelectionLabel.setFont(CurrentSelectionLabel.getFont());
        CurrentSelectionLabel.setText(bundle.getString("AE_CurrentSelectionLabel")); // NOI18N

        IDLabel.setFont(IDLabel.getFont().deriveFont(IDLabel.getFont().getStyle() | java.awt.Font.BOLD));
        IDLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        IDLabel.setText("000000");
        IDLabel.setToolTipText("1234");

        ObjectAccess.setOpaque(false);

        StatePanel.setOpaque(false);

        PrivateCheckbox.setToolTipText(bundle.getString("AE_Private_TT")); // NOI18N
        PrivateCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PrivateCheckboxActionPerformed(evt);
            }
        });

        Archived.setToolTipText(bundle.getString("AE_Archived_TT")); // NOI18N
        Archived.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ArchivedActionPerformed(evt);
            }
        });

        CompletedCheckbox.setToolTipText(bundle.getString("AE_Completed_TT")); // NOI18N
        CompletedCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CompletedCheckboxActionPerformed(evt);
            }
        });

        CompletionStateLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/3232/checkbox.png"))); // NOI18N
        CompletionStateLabel.setToolTipText(bundle.getString("AE_Completed_TT")); // NOI18N

        ArchivedStateLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/3232/jar.png"))); // NOI18N
        ArchivedStateLabel.setToolTipText(bundle.getString("AE_Archived_TT")); // NOI18N

        PrivateStateLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/3232/lock.png"))); // NOI18N
        PrivateStateLabel.setToolTipText(bundle.getString("AE_Private_TT")); // NOI18N

        javax.swing.GroupLayout StatePanelLayout = new javax.swing.GroupLayout(StatePanel);
        StatePanel.setLayout(StatePanelLayout);
        StatePanelLayout.setHorizontalGroup(
            StatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(StatePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(StatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(StatePanelLayout.createSequentialGroup()
                        .addComponent(PrivateCheckbox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(PrivateStateLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(StatePanelLayout.createSequentialGroup()
                        .addGroup(StatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(StatePanelLayout.createSequentialGroup()
                                .addComponent(Archived)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ArchivedStateLabel))
                            .addGroup(StatePanelLayout.createSequentialGroup()
                                .addComponent(CompletedCheckbox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(CompletionStateLabel)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        StatePanelLayout.setVerticalGroup(
            StatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(StatePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(StatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(PrivateCheckbox)
                    .addComponent(PrivateStateLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(StatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(Archived)
                    .addComponent(ArchivedStateLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(StatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(CompletedCheckbox)
                    .addComponent(CompletionStateLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(ObjectAccess, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(NodePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(CurrentSelectionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(StatePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(IDLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(IDLabel)
                    .addComponent(CurrentSelectionLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(StatePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(NodePanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(ObjectAccess, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void DueDatePickerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DueDatePickerActionPerformed
        dueDateChanged = true;
    }//GEN-LAST:event_DueDatePickerActionPerformed

    private void PriorityComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PriorityComboActionPerformed
        priorityChanged = true;
    }//GEN-LAST:event_PriorityComboActionPerformed

    private void PrivateCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PrivateCheckboxActionPerformed
        privateStateChanged = true;
    }//GEN-LAST:event_PrivateCheckboxActionPerformed

    private void ArchivedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ArchivedActionPerformed
        archiveStateChanged = true;
    }//GEN-LAST:event_ArchivedActionPerformed

    private void CompletedCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CompletedCheckboxActionPerformed
        completedStateChanged = true;
    }//GEN-LAST:event_CompletedCheckboxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox Archived;
    private javax.swing.JLabel ArchivedStateLabel;
    private javax.swing.JCheckBox CompletedCheckbox;
    private javax.swing.JLabel CompletionStateLabel;
    private javax.swing.JLabel CurrentSelectionLabel;
    private javax.swing.JLabel DueDateLabel;
    private org.jdesktop.swingx.JXDatePicker DueDatePicker;
    private javax.swing.JLabel IDLabel;
    private javax.swing.JPanel NodePanel;
    private com.mindliner.gui.ObjectAccessControls ObjectAccess;
    private javax.swing.JComboBox PriorityCombo;
    private javax.swing.JLabel PriorityLabel;
    private javax.swing.JCheckBox PrivateCheckbox;
    private javax.swing.JLabel PrivateStateLabel;
    private javax.swing.JPanel StatePanel;
    // End of variables declaration//GEN-END:variables
}
