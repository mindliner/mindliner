/*
 * mlSynchronizationPreferences.java
 *
 * Created on 20. Mai 2008, 11:53
 */
package com.mindliner.synch;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.cache.DefaultObjectAttributes;
import com.mindliner.clientobjects.mlcClient;
import com.mindliner.entities.SoftwareFeature;
import com.mindliner.entities.Syncher.InitialSynchDirection;
import com.mindliner.entities.Syncher.SourceBrand;
import com.mindliner.entities.Syncher.SourceType;
import com.mindliner.entities.Syncher.SynchConflictResolution;
import com.mindliner.gui.color.DataPoolColorizer;
import com.mindliner.main.MindlinerMain;
import com.mindliner.main.ObjectDefaultsDialog;
import com.mindliner.objects.transfer.mltSyncher;
import com.mindliner.synchronization.SynchActor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author Marius Messerli
 */
public class SynchConfigurator extends JPanel implements SynchSettingChangeObserver {

    private static SynchConfigurator INSTANCE = null;

    @SuppressWarnings("static-access")
    public static SynchConfigurator getUniqueInstance() {
        synchronized (SynchConfigurator.class) {
            if (INSTANCE == null) {
                INSTANCE = new SynchConfigurator();
                SynchSettingChangeManager.registerObserver(INSTANCE);
            }
        }
        return INSTANCE;
    }

    private SynchConfigurator() {
        initComponents();
    }

    /**
     * Call this to properly configure this component once the
     * SynchronizationManager has been initialized
     */
    public static void configure() {
        INSTANCE.configureComponents();
    }

    public void configureComponents() {

        DefaultListModel dlm = new DefaultListModel();
        for (SynchActor s : SynchronizationManager.getSynchActors()) {
            dlm.addElement(s);
        }
        SynchActorList.setModel(dlm);

        // the synch direction combo
        DefaultComboBoxModel dcm = new DefaultComboBoxModel();
        dcm.addElement(InitialSynchDirection.Export);
        dcm.addElement(InitialSynchDirection.Import);
        dcm.addElement(InitialSynchDirection.Both);
        DirectionCombo.setModel(dcm);

        // the conflict resolution prefs
        dcm = new DefaultComboBoxModel();
        dcm.addElement(SynchConflictResolution.MindlinerWins);
        dcm.addElement(SynchConflictResolution.ForeignWins);
        dcm.addElement(SynchConflictResolution.Manual);
        ConflictResolutionCombo.setModel(dcm);

        if (!CacheEngineStatic.getCurrentUser().isAuthorizedForFeature(SoftwareFeature.CurrentFeatures.SYNCH_BASICS)) {
            throw new IllegalStateException("No authorization for synchronization subsystem. Program execution must not get here.");
        }
        // the source brand combo on the creation dialog
        dcm = new DefaultComboBoxModel();
        if (CacheEngineStatic.getCurrentUser().isAuthorizedForFeature(SoftwareFeature.CurrentFeatures.SYNCH_OUTLOOK)) {
            dcm.addElement(SourceBrand.Outlook);
        }
        if (dcm.getSize() > 0) {
            NewSourceBrandCombo.setModel(dcm);
        } else {
            // if we don't have at least one authorized 
            NewSourceBrandCombo.setEnabled(false);
            NewSourceTypeCombo.setEnabled(false);
            SyncherAddButton.setEnabled(false);
        }

        // the new source data pool combo
        dcm = new DefaultComboBoxModel();
        for (Integer dpid : CacheEngineStatic.getCurrentUser().getClientIds()) {
            mlcClient dataPool = CacheEngineStatic.getClient(dpid);
            if (dataPool != null) {
                dcm.addElement(dataPool);
            }
        }
        NewSyncherDataPoolCombo.setModel(dcm);
        NewSyncherDataPoolCombo.setSelectedIndex(0);
        NewSyncherDataPoolLabel.setVisible(CacheEngineStatic.getCurrentUser().getClientIds().size() > 1);
        NewSyncherDataPoolCombo.setVisible(CacheEngineStatic.getCurrentUser().getClientIds().size() > 1);

        // the source type combo on the creation dialog
        dcm = new DefaultComboBoxModel();
        for (SourceType t : SourceType.values()) {
            dcm.addElement(t);
        }
        NewSourceTypeCombo.setModel(dcm);
    }

    private void updateSyncherParameters(SynchActor sa) {
        SourceTypeLabel.setText((sa.getSyncher().getType().toString()));
        SourceBrandLabel.setText(sa.getSyncher().getBrand().toString());
        SourceFolderNameInput.setText(sa.getSyncher().getSourceFolder());
        SourceCategory.setText(sa.getSyncher().getCategoryName());
        DirectionCombo.setSelectedItem(sa.getSyncher().getInitialDirection());
        ConflictResolutionCombo.setSelectedItem(sa.getSyncher().getConflictResolution());
        IgnoreCompletedOrExpired.setSelected(sa.getSyncher().isIgnoreCompleted());
        DeleteOnMissingCounterpart.setSelected(sa.getSyncher().isDeleteOnMissingCounterpart());
        ImmediateForeignUpdate.setSelected(sa.getSyncher().isImmediateForeignUpdate());
        ContentCheckingCheckbox.setSelected(sa.getSyncher().isContentCheck());
        SynchItemCountLabel.setText(Integer.toString(sa.getSynchUnits().size()));
    }

    private void updateSynchControlStates(boolean enabled) {
        SourceFolderNameInput.setEnabled(enabled);
        DirectionCombo.setEnabled(enabled);
        ConflictResolutionCombo.setEnabled(enabled);
        IgnoreCompletedOrExpired.setEnabled(enabled);
        DeleteOnMissingCounterpart.setEnabled(enabled);
        ImmediateForeignUpdate.setEnabled(enabled);
    }

    @Override
    public void sourcePathChanged(String newPath) {
        SourceFolderNameInput.setText(newPath);
    }

    private mltSyncher createSyncher(SourceType type, SourceBrand brand, mlcClient dataPool) {
        mltSyncher s = new mltSyncher();
        s.setBrand(brand);
        s.setType(type);
        s.setConflictResolution((SynchConflictResolution) ConflictResolutionCombo.getSelectedItem());
        s.setDeleteOnMissingCounterpart(DeleteOnMissingCounterpart.isSelected());
        s.setIgnoreCompleted(IgnoreCompletedOrExpired.isSelected());
        s.setImmediateForeignUpdate(ImmediateForeignUpdate.isSelected());
        s.setInitialDirection((InitialSynchDirection) DirectionCombo.getSelectedItem());
        s.setSourceFolder(SourceFolderNameInput.getText());
        s.setCategoryName(SourceCategory.getText());
        s.setClientId(dataPool.getId());
        return s;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        NewSyncherDialog = new javax.swing.JDialog();
        NewSourceTypeLabel = new javax.swing.JLabel();
        NewSourceTypeCombo = new javax.swing.JComboBox();
        NewSourceBrandLabel = new javax.swing.JLabel();
        NewSourceBrandCombo = new javax.swing.JComboBox();
        NewSyncherCreateButton = new javax.swing.JButton();
        NewSyncherCancelButton = new javax.swing.JButton();
        NewSyncherDataPoolLabel = new javax.swing.JLabel();
        NewSyncherDataPoolCombo = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        SynchActorList = new javax.swing.JList();
        SynchSourcePanel = new javax.swing.JPanel();
        SourceURLLabel = new javax.swing.JLabel();
        SourceFolderNameInput = new javax.swing.JTextField();
        SourceFolderChooser = new javax.swing.JButton();
        SourceTypeLabelLabel = new javax.swing.JLabel();
        SourceTypeLabel = new javax.swing.JLabel();
        SourceBrandLabelLabel = new javax.swing.JLabel();
        SourceBrandLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        SourceCategory = new javax.swing.JTextField();
        SynchParamsPanel = new javax.swing.JPanel();
        SynchronizeDirectionLabel = new javax.swing.JLabel();
        DirectionCombo = new javax.swing.JComboBox();
        ConflictResolutionLabel = new javax.swing.JLabel();
        ConflictResolutionCombo = new javax.swing.JComboBox();
        IgnoreCompletedOrExpired = new javax.swing.JCheckBox();
        DeleteOnMissingCounterpart = new javax.swing.JCheckBox();
        ImmediateForeignUpdate = new javax.swing.JCheckBox();
        DefaultAttributesButton = new javax.swing.JButton();
        ContentCheckingCheckbox = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        SyncherAddButton = new javax.swing.JButton();
        SyncherDeleteButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        SynchItemCountLabel = new javax.swing.JLabel();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/mindliner/resources/Synchronization"); // NOI18N
        NewSyncherDialog.setTitle(bundle.getString("SynchConf_NewSyncherTitle")); // NOI18N
        NewSyncherDialog.setModal(true);

        NewSourceTypeLabel.setLabelFor(NewSourceTypeCombo);
        NewSourceTypeLabel.setText(bundle.getString("SyncConf_SourceTypeLabel")); // NOI18N

        NewSourceTypeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        NewSourceTypeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NewSourceTypeComboActionPerformed(evt);
            }
        });

        NewSourceBrandLabel.setLabelFor(NewSourceBrandCombo);
        NewSourceBrandLabel.setText(bundle.getString("SynchConf_SourceBrandLabel")); // NOI18N

        NewSourceBrandCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        NewSyncherCreateButton.setText(bundle.getString("SynchConfNewSyncherCreateButton")); // NOI18N
        NewSyncherCreateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NewSyncherCreateButtonActionPerformed(evt);
            }
        });

        NewSyncherCancelButton.setText(bundle.getString("SynchConfNewSyncherCancelButton")); // NOI18N
        NewSyncherCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NewSyncherCancelButtonActionPerformed(evt);
            }
        });

        NewSyncherDataPoolLabel.setText(bundle.getString("SynchActorDataPoolLabel")); // NOI18N

        NewSyncherDataPoolCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout NewSyncherDialogLayout = new javax.swing.GroupLayout(NewSyncherDialog.getContentPane());
        NewSyncherDialog.getContentPane().setLayout(NewSyncherDialogLayout);
        NewSyncherDialogLayout.setHorizontalGroup(
            NewSyncherDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(NewSyncherDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(NewSyncherDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, NewSyncherDialogLayout.createSequentialGroup()
                        .addComponent(NewSyncherCancelButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(NewSyncherCreateButton))
                    .addGroup(NewSyncherDialogLayout.createSequentialGroup()
                        .addGroup(NewSyncherDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(NewSourceTypeLabel)
                            .addComponent(NewSourceBrandLabel)
                            .addComponent(NewSyncherDataPoolLabel))
                        .addGap(18, 18, 18)
                        .addGroup(NewSyncherDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(NewSourceBrandCombo, 0, 248, Short.MAX_VALUE)
                            .addComponent(NewSourceTypeCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(NewSyncherDataPoolCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        NewSyncherDialogLayout.setVerticalGroup(
            NewSyncherDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(NewSyncherDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(NewSyncherDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(NewSourceTypeLabel)
                    .addComponent(NewSourceTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(NewSyncherDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(NewSourceBrandLabel)
                    .addComponent(NewSourceBrandCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(NewSyncherDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(NewSyncherDataPoolLabel)
                    .addComponent(NewSyncherDataPoolCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(NewSyncherDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(NewSyncherCreateButton)
                    .addComponent(NewSyncherCancelButton))
                .addContainerGap())
        );

        SynchActorList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                SynchActorListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(SynchActorList);

        SynchSourcePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("SynchPrefsTaskSynchPreferences"))); // NOI18N

        SourceURLLabel.setLabelFor(SourceFolderNameInput);
        SourceURLLabel.setText(bundle.getString("SynchPrefsTaskFolderPath")); // NOI18N

        SourceFolderNameInput.setColumns(20);
        SourceFolderNameInput.setToolTipText(bundle.getString("SynchConf_SourceURLInput")); // NOI18N
        SourceFolderNameInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SourceFolderNameInputActionPerformed(evt);
            }
        });

        SourceFolderChooser.setText(bundle.getString("TaskFolderPathSetDefaultButton")); // NOI18N
        SourceFolderChooser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SourceFolderChooserActionPerformed(evt);
            }
        });

        SourceTypeLabelLabel.setLabelFor(SourceTypeLabel);
        SourceTypeLabelLabel.setText(bundle.getString("SynchConf_SourceTypeLabel")); // NOI18N

        SourceTypeLabel.setText("jLabel1");

        SourceBrandLabelLabel.setText(bundle.getString("SynchConf_SourceBrandLabel")); // NOI18N

        SourceBrandLabel.setText("jLabel1");

        jLabel2.setText(bundle.getString("SynchPrefsForeignCategory")); // NOI18N

        SourceCategory.setToolTipText(bundle.getString("SynchPrefsForeignCategory_TT")); // NOI18N
        SourceCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SourceCategoryActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout SynchSourcePanelLayout = new javax.swing.GroupLayout(SynchSourcePanel);
        SynchSourcePanel.setLayout(SynchSourcePanelLayout);
        SynchSourcePanelLayout.setHorizontalGroup(
            SynchSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SynchSourcePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(SynchSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(SourceURLLabel)
                    .addComponent(SourceTypeLabelLabel)
                    .addGroup(SynchSourcePanelLayout.createSequentialGroup()
                        .addGroup(SynchSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(SourceBrandLabelLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(SynchSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, SynchSourcePanelLayout.createSequentialGroup()
                                .addGap(16, 16, 16)
                                .addGroup(SynchSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(SourceCategory)
                                    .addComponent(SourceFolderNameInput, javax.swing.GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(SourceFolderChooser))
                            .addGroup(SynchSourcePanelLayout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(SynchSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(SourceTypeLabel)
                                    .addComponent(SourceBrandLabel))))))
                .addContainerGap(340, Short.MAX_VALUE))
        );
        SynchSourcePanelLayout.setVerticalGroup(
            SynchSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SynchSourcePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(SynchSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(SourceTypeLabelLabel)
                    .addComponent(SourceTypeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(SynchSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(SourceBrandLabelLabel)
                    .addComponent(SourceBrandLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(SynchSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(SourceURLLabel)
                    .addComponent(SourceFolderNameInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SourceFolderChooser))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(SynchSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(SourceCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        SynchParamsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("SynchPrefsGeneralSynchSettings"))); // NOI18N

        SynchronizeDirectionLabel.setLabelFor(DirectionCombo);
        SynchronizeDirectionLabel.setText(bundle.getString("SynchPrefsDirectionLabel")); // NOI18N
        SynchronizeDirectionLabel.setToolTipText(bundle.getString("SynchPrefsInitialDirection_TT")); // NOI18N

        DirectionCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        DirectionCombo.setToolTipText(bundle.getString("SynchronizationDirection_TT")); // NOI18N
        DirectionCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DirectionComboActionPerformed(evt);
            }
        });

        ConflictResolutionLabel.setLabelFor(ConflictResolutionCombo);
        ConflictResolutionLabel.setText(bundle.getString("SynchPrefsConflictResolutionLabel")); // NOI18N

        ConflictResolutionCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        ConflictResolutionCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ConflictResolutionComboActionPerformed(evt);
            }
        });

        IgnoreCompletedOrExpired.setText(bundle.getString("SynchPrefsSynchCompletedName")); // NOI18N
        IgnoreCompletedOrExpired.setToolTipText(bundle.getString("SynchPrefsIgnoreCompleted_TT")); // NOI18N
        IgnoreCompletedOrExpired.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                IgnoreCompletedOrExpiredActionPerformed(evt);
            }
        });

        DeleteOnMissingCounterpart.setText(bundle.getString("SynchPrefsDeleteRemovedObjects")); // NOI18N
        DeleteOnMissingCounterpart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeleteOnMissingCounterpartActionPerformed(evt);
            }
        });

        ImmediateForeignUpdate.setText(bundle.getString("SynchPrefsImmediateForeignUpdate")); // NOI18N
        ImmediateForeignUpdate.setToolTipText(bundle.getString("SynchPrefsTaskImmediateForeignUpdate_TT")); // NOI18N
        ImmediateForeignUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ImmediateForeignUpdateActionPerformed(evt);
            }
        });

        DefaultAttributesButton.setText(bundle.getString("SynchConfigDefaultAttribButton")); // NOI18N
        DefaultAttributesButton.setToolTipText(bundle.getString("SynchConfigDefaultAttrib_TT")); // NOI18N
        DefaultAttributesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DefaultAttributesButtonActionPerformed(evt);
            }
        });

        ContentCheckingCheckbox.setText(bundle.getString("SynchConfig_ContentCheckingTitle")); // NOI18N
        ContentCheckingCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ContentCheckingCheckboxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout SynchParamsPanelLayout = new javax.swing.GroupLayout(SynchParamsPanel);
        SynchParamsPanel.setLayout(SynchParamsPanelLayout);
        SynchParamsPanelLayout.setHorizontalGroup(
            SynchParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SynchParamsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(SynchParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ContentCheckingCheckbox)
                    .addComponent(ImmediateForeignUpdate)
                    .addGroup(SynchParamsPanelLayout.createSequentialGroup()
                        .addGroup(SynchParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(SynchParamsPanelLayout.createSequentialGroup()
                                .addGroup(SynchParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(SynchronizeDirectionLabel)
                                    .addComponent(ConflictResolutionLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(SynchParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(DirectionCombo, 0, 198, Short.MAX_VALUE)
                                    .addComponent(ConflictResolutionCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addComponent(IgnoreCompletedOrExpired, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(DeleteOnMissingCounterpart, javax.swing.GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(DefaultAttributesButton)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        SynchParamsPanelLayout.setVerticalGroup(
            SynchParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SynchParamsPanelLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(SynchParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(DirectionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SynchronizeDirectionLabel)
                    .addComponent(DefaultAttributesButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(SynchParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ConflictResolutionLabel)
                    .addComponent(ConflictResolutionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(IgnoreCompletedOrExpired)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(DeleteOnMissingCounterpart)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ImmediateForeignUpdate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(ContentCheckingCheckbox))
        );

        SyncherAddButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/1616/add-2.png"))); // NOI18N
        SyncherAddButton.setToolTipText(bundle.getString("SynchConf_SyncherAddButton")); // NOI18N
        SyncherAddButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SyncherAddButtonActionPerformed(evt);
            }
        });

        SyncherDeleteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/1616/delete-2.png"))); // NOI18N
        SyncherDeleteButton.setToolTipText(bundle.getString("SynchConf_DeleteSynchronizerButton")); // NOI18N
        SyncherDeleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SyncherDeleteButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(SyncherAddButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(SyncherDeleteButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(SyncherDeleteButton)
                    .addComponent(SyncherAddButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("SynchConfigPreviousItemsSynchedTitle"))); // NOI18N

        jLabel1.setLabelFor(SynchItemCountLabel);
        jLabel1.setText(bundle.getString("SynchConfig_NumberOfItemsSynchedLabel")); // NOI18N

        SynchItemCountLabel.setText("jLabel2");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(SynchItemCountLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(SynchItemCountLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(SynchSourcePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(SynchParamsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(SynchSourcePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(SynchParamsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void SourceFolderChooserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SourceFolderChooserActionPerformed
    SynchActor s = (SynchActor) SynchActorList.getSelectedValue();
    s.chooseSourceFolder();
}//GEN-LAST:event_SourceFolderChooserActionPerformed

    private void NewSourceTypeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NewSourceTypeComboActionPerformed
    }//GEN-LAST:event_NewSourceTypeComboActionPerformed

    private void SynchActorListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_SynchActorListValueChanged
        SynchActor s = (SynchActor) SynchActorList.getSelectedValue();
        if (s != null) {
            updateSynchControlStates(true);
            updateSyncherParameters(s);
        }
    }//GEN-LAST:event_SynchActorListValueChanged

    private void SyncherAddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SyncherAddButtonActionPerformed
        NewSyncherDialog.setLocationRelativeTo(MindlinerMain.getInstance());
        NewSyncherDialog.pack();
        NewSyncherDialog.setVisible(true);
    }//GEN-LAST:event_SyncherAddButtonActionPerformed

    private void NewSyncherCreateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NewSyncherCreateButtonActionPerformed
        SourceType type = (SourceType) NewSourceTypeCombo.getSelectedItem();
        SourceBrand brand = (SourceBrand) NewSourceBrandCombo.getSelectedItem();
        SynchActor newSynchActor = SynchActorFactory.createSynchActor(type, brand);
        mltSyncher syncher = createSyncher(type, brand, (mlcClient) NewSyncherDataPoolCombo.getSelectedItem());
        newSynchActor.setSyncher(syncher);
        SynchronizationManager.registerSyncher(newSynchActor);
        DefaultListModel dlm = (DefaultListModel) SynchActorList.getModel();
        dlm.addElement(newSynchActor);
        SynchActorList.setSelectedValue(newSynchActor, true);
        NewSyncherDialog.setVisible(false);
    }//GEN-LAST:event_NewSyncherCreateButtonActionPerformed

    private void NewSyncherCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NewSyncherCancelButtonActionPerformed
        NewSyncherDialog.setVisible(false);
    }//GEN-LAST:event_NewSyncherCancelButtonActionPerformed

    private void SourceFolderNameInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SourceFolderNameInputActionPerformed
        SynchActor s = (SynchActor) SynchActorList.getSelectedValue();
        if (s != null) {
            s.getSyncher().setSourceFolder(SourceFolderNameInput.getText());
        }
    }//GEN-LAST:event_SourceFolderNameInputActionPerformed

    private void DirectionComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DirectionComboActionPerformed
        SynchActor s = (SynchActor) SynchActorList.getSelectedValue();
        if (s != null) {
            InitialSynchDirection direction = (InitialSynchDirection) DirectionCombo.getSelectedItem();
            switch (direction) {
                case Export:
                    DefaultAttributesButton.setVisible(false);
                    break;
                default:
                    DefaultAttributesButton.setVisible(true);
                    break;
            }
            s.getSyncher().setInitialDirection(direction);
        }
    }//GEN-LAST:event_DirectionComboActionPerformed

    private void ConflictResolutionComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ConflictResolutionComboActionPerformed
        SynchActor s = (SynchActor) SynchActorList.getSelectedValue();
        if (s != null) {
            s.getSyncher().setConflictResolution((SynchConflictResolution) ConflictResolutionCombo.getSelectedItem());
        }
    }//GEN-LAST:event_ConflictResolutionComboActionPerformed

    private void IgnoreCompletedOrExpiredActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_IgnoreCompletedOrExpiredActionPerformed
        SynchActor s = (SynchActor) SynchActorList.getSelectedValue();
        if (s != null) {
            s.getSyncher().setIgnoreCompleted(IgnoreCompletedOrExpired.isSelected());
        }
    }//GEN-LAST:event_IgnoreCompletedOrExpiredActionPerformed

    private void DeleteOnMissingCounterpartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeleteOnMissingCounterpartActionPerformed
        SynchActor s = (SynchActor) SynchActorList.getSelectedValue();
        if (s != null) {
            s.getSyncher().setDeleteOnMissingCounterpart(DeleteOnMissingCounterpart.isSelected());
        }
    }//GEN-LAST:event_DeleteOnMissingCounterpartActionPerformed

    private void ImmediateForeignUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ImmediateForeignUpdateActionPerformed
        SynchActor s = (SynchActor) SynchActorList.getSelectedValue();
        if (s != null) {
            s.getSyncher().setImmediateForeignUpdate(ImmediateForeignUpdate.isSelected());
        }
    }//GEN-LAST:event_ImmediateForeignUpdateActionPerformed

    private void SyncherDeleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SyncherDeleteButtonActionPerformed
        SynchActor s = (SynchActor) SynchActorList.getSelectedValue();
        if (s != null) {
            int answer = JOptionPane.showConfirmDialog(MindlinerMain.getInstance(), "Sure you want to delete the selected synchronizer and its state about items synched so far?", "Deletion Operation", JOptionPane.YES_NO_OPTION);
            if (answer == JOptionPane.YES_OPTION) {
                SynchronizationManager.deleteSyncher(s);
                DefaultListModel dlm = (DefaultListModel) SynchActorList.getModel();
                dlm.removeElement(s);
                SourceFolderNameInput.setText("");
                updateSynchControlStates(false);
            }
        }
    }//GEN-LAST:event_SyncherDeleteButtonActionPerformed

    private void DefaultAttributesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DefaultAttributesButtonActionPerformed
        ObjectDefaultsDialog odd = new ObjectDefaultsDialog(null, true);
        odd.setLocationRelativeTo(this);
        odd.pack();
        odd.setVisible(true);
    }//GEN-LAST:event_DefaultAttributesButtonActionPerformed

    private void ContentCheckingCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ContentCheckingCheckboxActionPerformed
        SynchActor s = (SynchActor) SynchActorList.getSelectedValue();
        if (s != null) {
            s.getSyncher().setContentCheck(ContentCheckingCheckbox.isSelected());
        }
    }//GEN-LAST:event_ContentCheckingCheckboxActionPerformed

    private void SourceCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SourceCategoryActionPerformed
        SynchActor s = (SynchActor) SynchActorList.getSelectedValue();
        if (s != null) {
            s.getSyncher().setCategoryName(SourceCategory.getText());
        }
    }//GEN-LAST:event_SourceCategoryActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox ConflictResolutionCombo;
    private javax.swing.JLabel ConflictResolutionLabel;
    private javax.swing.JCheckBox ContentCheckingCheckbox;
    private javax.swing.JButton DefaultAttributesButton;
    private javax.swing.JCheckBox DeleteOnMissingCounterpart;
    private javax.swing.JComboBox DirectionCombo;
    private javax.swing.JCheckBox IgnoreCompletedOrExpired;
    private javax.swing.JCheckBox ImmediateForeignUpdate;
    private javax.swing.JComboBox NewSourceBrandCombo;
    private javax.swing.JLabel NewSourceBrandLabel;
    private javax.swing.JComboBox NewSourceTypeCombo;
    private javax.swing.JLabel NewSourceTypeLabel;
    private javax.swing.JButton NewSyncherCancelButton;
    private javax.swing.JButton NewSyncherCreateButton;
    private javax.swing.JComboBox NewSyncherDataPoolCombo;
    private javax.swing.JLabel NewSyncherDataPoolLabel;
    private javax.swing.JDialog NewSyncherDialog;
    private javax.swing.JLabel SourceBrandLabel;
    private javax.swing.JLabel SourceBrandLabelLabel;
    private javax.swing.JTextField SourceCategory;
    private javax.swing.JButton SourceFolderChooser;
    private javax.swing.JTextField SourceFolderNameInput;
    private javax.swing.JLabel SourceTypeLabel;
    private javax.swing.JLabel SourceTypeLabelLabel;
    private javax.swing.JLabel SourceURLLabel;
    private javax.swing.JList SynchActorList;
    private javax.swing.JLabel SynchItemCountLabel;
    private javax.swing.JPanel SynchParamsPanel;
    private javax.swing.JPanel SynchSourcePanel;
    private javax.swing.JButton SyncherAddButton;
    private javax.swing.JButton SyncherDeleteButton;
    private javax.swing.JLabel SynchronizeDirectionLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
