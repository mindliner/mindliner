/*
 * mlPreferenceEditor.java
 *
 * Created on 23. Oktober 2007, 18:23
 */
package com.mindliner.prefs;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.cache.MlCacheException;
import com.mindliner.entities.SoftwareFeature;
import com.mindliner.exceptions.InsufficientAccessRightException;
import com.mindliner.exceptions.NonExistingObjectException;
import com.mindliner.gui.MlDialogUtils;
import com.mindliner.gui.ObjectEditor;
import com.mindliner.gui.color.BaseColorizer;
import com.mindliner.gui.color.ColorManager;
import com.mindliner.gui.color.ColorSchemeManager;
import com.mindliner.gui.color.RangeColorizer;
import com.mindliner.gui.color.ThresholdColorizer;
import com.mindliner.gui.tablemanager.MlObjectTable;
import com.mindliner.gui.tablemanager.TableManager;
import com.mindliner.main.MindlinerMain;
import com.mindliner.managers.ColorManagerRemote;
import com.mindliner.objects.transfer.MlTransferColorScheme;
import com.mindliner.serveraccess.OnlineManager;
import com.mindliner.serveraccess.OnlineService;
import com.mindliner.serveraccess.RemoteLookupAgent;
import com.mindliner.styles.MlStyler;
import com.mindliner.synch.SynchConfigurator;
import com.mindliner.view.Mindliner2DViewer;
import com.mindliner.weekplanner.WeekPlanner;
import java.awt.Font;
import javax.naming.NamingException;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 *
 * @author Marius Messerli
 */
public class MlMainPreferenceEditor extends JDialog implements OnlineService {

    
    static final long serialVersionUID = 500;
    private static MlMainPreferenceEditor INSTANCE;

    MlObjectTable mCurrentTable = null;
    CurrentlyEditedCategory currentCategory = null;
    private boolean dialogInitialized = false;

    private OnlineStatus onlineStatus = OnlineStatus.offline;
    private ColorManagerRemote colorManager = null;
    private MlColorChangeListener colorChangeListener = null;
    private MapPreferencesPane mapPreferences;
    private Mindliner2DViewer viewer = null;
    private int connectionPriority = 0;

    /**
     * Returns the singleton instance of this class.
     *
     * @param viewer
     * @return
     */
    public static MlMainPreferenceEditor getInstance(Mindliner2DViewer viewer) {
        if (viewer == null) {
            throw new IllegalArgumentException("The viewer argument must not be null");
        }
        synchronized (MlMainPreferenceEditor.class) {
            if (INSTANCE == null) {
                INSTANCE = new MlMainPreferenceEditor();
                INSTANCE.setMapPreferences(viewer.getMapPreferences());
                INSTANCE.setViewer(viewer);
                INSTANCE.configureDialog();

                if (OnlineManager.isOnline()) {
                    try {
                        INSTANCE.goOnline();
                    } catch (MlCacheException ex) {
                        JOptionPane.showMessageDialog(INSTANCE, ex.getMessage(), "Cannot Go Online", JOptionPane.ERROR_MESSAGE);
                    }
                }
                INSTANCE.setConnectionPriority(OnlineService.LOW_PRIORITY);
                OnlineManager.getInstance().registerService(INSTANCE);
            }
        }
        return INSTANCE;
    }

    public static void showDialog() {
        if (INSTANCE.isVisible() == false) {
            INSTANCE.configureDialog();
            INSTANCE.pack();
            INSTANCE.setLocationRelativeTo(MindlinerMain.getInstance());
            INSTANCE.setVisible(true);
        }
    }

    public static Font getEditorFont() {
        return INSTANCE.textEditorFontChooser.getFont();
    }

    public static Font getWeekplanFont() {
        return INSTANCE.weekplanFontChooser.getFont();
    }

    @Override
    public String getServiceName() {
        return "Preference Editor";
    }

    @Override
    public void goOffline() {
        colorManager = null;
        setOnlineGuiControls(false);
        onlineStatus = OnlineStatus.offline;
    }

    @Override
    public void goOnline() throws MlCacheException {
        if (!onlineStatus.equals(OnlineStatus.online)) {
            try {
                colorManager = (ColorManagerRemote) RemoteLookupAgent.getManagerForClass(ColorManagerRemote.class);
                setOnlineGuiControls(true);
                onlineStatus = OnlineStatus.online;
            } catch (NamingException ex) {
                throw new MlCacheException(ex.getMessage());
            }
        }
    }

    public void setMapPreferences(MapPreferencesPane mapPreferences) {
        this.mapPreferences = mapPreferences;
    }

    private void setOnlineGuiControls(boolean active) {
        UploadColorScheme.setEnabled(active);
        LoadColorScheme.setEnabled(active);
    }

    @Override
    public OnlineStatus getStatus() {
        return onlineStatus;
    }

    @Override
    public int getConnectionPriority() {
        return connectionPriority;
    }

    @Override
    public void setConnectionPriority(int priority) {
        connectionPriority = priority;
    }

    public void setViewer(Mindliner2DViewer viewer) {
        this.viewer = viewer;
    }

    private MlMainPreferenceEditor() {
        initComponents();
        BrightBackground.setSelected(MlStyler.isLightBackground());
        MlStyler.setLightBackground(BrightBackground.isSelected());
        textEditorFontChooser.setPersistenceIdentifyer(ObjectEditor.FONT_PREFERENCE_KEY);
        searchTableFontChooser.setPersistenceIdentifyer(MlObjectTable.FONT_PREFERENCE_KEY);
        if (CacheEngineStatic.getCurrentUser().isAuthorizedForFeature(SoftwareFeature.CurrentFeatures.TIME_MANAGEMENT)) {
            weekplanFontChooser.setPersistenceIdentifyer(WeekPlanner.FONT_PREFERENCE_KEY);
        } else {
            WeekplanFontPanel.setVisible(false);
        }
//        this.pack();
    }

    /**
     * Updates the available values for the
     *
     * @param colorizer selected table and installs a new change listener on the
     * color chooser after the user selected a color definition from the list
     * this method
     *
     */
    public void setValueList(BaseColorizer colorizer) {
        DefaultListModel dlm = new DefaultListModel();
        for (Object o : colorizer.getInputValueList()) {
            dlm.addElement(o);
        }
        ColorizerValueList.setModel(dlm);
        ColorizerValueList.setCellRenderer(new ColorValueListRenderer(colorizer));
        switch (colorizer.getType()) {

            case Threshold:
                if (!(colorizer instanceof ThresholdColorizer)) {
                    throw new IllegalStateException("Colorizer type and class do not agree.");
                }
                ThresholdColorizer tc = (ThresholdColorizer) colorizer;
                ColorizerThresholdField.setText(Double.toString(tc.getThreshold()));
                ThresholdDescription.setText(tc.getThresholdDescription());
                break;

            case Continuous:
                if (!(colorizer instanceof RangeColorizer)) {
                    throw new IllegalStateException("Colorizer type and class do not agree.");
                }
                RangeColorizer rc = (RangeColorizer) colorizer;
                ColorizerRangeMinimumField.setText(Double.toString(rc.getMinimum()));
                ColorizerRangeMaximumField.setText(Double.toString(rc.getMaximum()));
                break;
        }
    }

    private void configureDialog() {

        if (dialogInitialized == false) {

            DefaultListModel dlm = new DefaultListModel();
            for (BaseColorizer c : ColorManager.getColorizers()) {
                dlm.addElement(c);
            }
            ColorizerList.setModel(dlm);

            if (CacheEngineStatic.getCurrentUser().isAuthorizedForFeature(SoftwareFeature.CurrentFeatures.SYNCH_BASICS)) {
                PrefCategoryPane.addTab("Sychronization", SynchConfigurator.getUniqueInstance());
            }

            AutoSave.setSelected(MlPreferenceManager.isAutosave());

            PrefCategoryPane.addTab("Mindmap", mapPreferences);
            MlDialogUtils.addEscapeListener(this);
            dialogInitialized = true;
        }
    }

    private void configureRangeControls(BaseColorizer c) {
        boolean thresholdControlsOn;
        boolean rangeControlsOn;

        switch (c.getType()) {

            case DiscreteStates:
                thresholdControlsOn = false;
                rangeControlsOn = false;
                break;

            case Continuous:
                thresholdControlsOn = false;
                rangeControlsOn = true;
                break;

            case Threshold:
                thresholdControlsOn = true;
                rangeControlsOn = false;
                break;

            default:
                thresholdControlsOn = false;
                rangeControlsOn = false;
                System.err.println("Colorizer with undefined type: " + c.getClass().getName());
        }
        ColorizerThresholdField.setEnabled(thresholdControlsOn);
        ColorizerThresholdLabel.setEnabled(thresholdControlsOn);
        ThresholdDescription.setEnabled(thresholdControlsOn);
        ColorizerRangeMinimumLabel.setEnabled(rangeControlsOn);
        ColorizerRangeMinimumField.setEnabled(rangeControlsOn);
        ColorizerRangeMaximumLabel.setEnabled(rangeControlsOn);
        ColorizerRangeMaximumField.setEnabled(rangeControlsOn);
        pack();
    }

    /**
     * Sets the GUI fields with content of the specified mindliner table object.
     *
     * @param mt The mindliner table object.
     */
    public void setTableEditorFields(MlObjectTable mt) {
        mCurrentTable = mt;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        ColorSchemeDialog = new javax.swing.JDialog();
        jScrollPane1 = new javax.swing.JScrollPane();
        ColorSchemes = new javax.swing.JList();
        ColorSchemeLabel = new javax.swing.JLabel();
        CSLoadButton = new javax.swing.JButton();
        CSCancelButton = new javax.swing.JButton();
        CSDeleteButton = new javax.swing.JButton();
        EditorPanel = new javax.swing.JPanel();
        PrefCategoryPane = new javax.swing.JTabbedPane();
        PrefsColors = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        RangePanel = new javax.swing.JPanel();
        ColorizerThresholdLabel = new javax.swing.JLabel();
        ColorizerThresholdField = new javax.swing.JTextField();
        ColorizerRangeMinimumLabel = new javax.swing.JLabel();
        ColorizerRangeMinimumField = new javax.swing.JTextField();
        ColorizerRangeMaximumLabel = new javax.swing.JLabel();
        ColorizerRangeMaximumField = new javax.swing.JTextField();
        ThresholdDescription = new javax.swing.JLabel();
        ColorizerValuePanel = new javax.swing.JPanel();
        jScrollPane18 = new javax.swing.JScrollPane();
        ColorizerList = new javax.swing.JList();
        jScrollPane11 = new javax.swing.JScrollPane();
        ColorizerValueList = new javax.swing.JList();
        jPanel4 = new javax.swing.JPanel();
        UploadColorScheme = new javax.swing.JButton();
        LoadColorScheme = new javax.swing.JButton();
        ClearAllColors = new javax.swing.JButton();
        BrightBackground = new javax.swing.JCheckBox();
        ColorChooserPanel = new javax.swing.JPanel();
        ColorChooser = new javax.swing.JColorChooser();
        CopyPastePanel = new javax.swing.JPanel();
        CopyColor = new javax.swing.JButton();
        PasteColor = new javax.swing.JButton();
        PrefsFontAndMore = new javax.swing.JPanel();
        FontPanel = new javax.swing.JPanel();
        SearchTableFontPanel = new javax.swing.JPanel();
        searchTableFontChooser = new com.mindliner.gui.font.MlFontChooser();
        TextEditorFontPanel = new javax.swing.JPanel();
        textEditorFontChooser = new com.mindliner.gui.font.MlFontChooser();
        WeekplanFontPanel = new javax.swing.JPanel();
        weekplanFontChooser = new com.mindliner.gui.font.MlFontChooser();
        jPanel2 = new javax.swing.JPanel();
        AutoSave = new javax.swing.JCheckBox();
        SaveDiscardPanel = new javax.swing.JPanel();
        PreferenceCancel = new javax.swing.JButton();
        PreferenceOK = new javax.swing.JButton();

        ColorSchemeDialog.setModal(true);

        ColorSchemes.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(ColorSchemes);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/mindliner/resources/Preferences"); // NOI18N
        ColorSchemeLabel.setText(bundle.getString("PrefsEditorColorSchemeLabel")); // NOI18N

        CSLoadButton.setText(bundle.getString("ColorSchemeOKButton")); // NOI18N
        CSLoadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CSLoadButtonActionPerformed(evt);
            }
        });

        CSCancelButton.setText(bundle.getString("ColorSchemeCancelButton")); // NOI18N
        CSCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CSCancelButtonActionPerformed(evt);
            }
        });

        CSDeleteButton.setText(bundle.getString("PrefsColorSchemeDelete")); // NOI18N
        CSDeleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CSDeleteButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout ColorSchemeDialogLayout = new javax.swing.GroupLayout(ColorSchemeDialog.getContentPane());
        ColorSchemeDialog.getContentPane().setLayout(ColorSchemeDialogLayout);
        ColorSchemeDialogLayout.setHorizontalGroup(
            ColorSchemeDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ColorSchemeDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ColorSchemeDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 282, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ColorSchemeLabel)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ColorSchemeDialogLayout.createSequentialGroup()
                        .addComponent(CSCancelButton)
                        .addGap(63, 63, 63)
                        .addComponent(CSDeleteButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(CSLoadButton)))
                .addContainerGap())
        );
        ColorSchemeDialogLayout.setVerticalGroup(
            ColorSchemeDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ColorSchemeDialogLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(ColorSchemeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(ColorSchemeDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(CSLoadButton)
                    .addComponent(CSCancelButton)
                    .addComponent(CSDeleteButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setTitle(bundle.getString("PreferenceEditorTitle")); // NOI18N
        setModal(true);

        EditorPanel.setLayout(new java.awt.BorderLayout());

        PrefCategoryPane.setTabPlacement(javax.swing.JTabbedPane.LEFT);

        PrefsColors.setLayout(new java.awt.BorderLayout());

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel3.setLayout(new java.awt.BorderLayout());

        ColorizerThresholdLabel.setText(bundle.getString("ColorizerThresholdLabel")); // NOI18N

        ColorizerThresholdField.setColumns(5);
        ColorizerThresholdField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ColorizerThresholdFieldActionPerformed(evt);
            }
        });

        ColorizerRangeMinimumLabel.setText(bundle.getString("ColorizerRangeMinimumLabel")); // NOI18N

        ColorizerRangeMinimumField.setColumns(5);
        ColorizerRangeMinimumField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ColorizerRangeMinimumFieldActionPerformed(evt);
            }
        });

        ColorizerRangeMaximumLabel.setText(bundle.getString("ColorizerRangeMaximumLabel")); // NOI18N

        ColorizerRangeMaximumField.setColumns(5);
        ColorizerRangeMaximumField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ColorizerRangeMaximumFieldActionPerformed(evt);
            }
        });

        java.util.ResourceBundle bundle1 = java.util.ResourceBundle.getBundle("com/mindliner/gui/GuiElements"); // NOI18N
        ThresholdDescription.setText(bundle1.getString("PreferencesThresholdUnitLabel")); // NOI18N

        javax.swing.GroupLayout RangePanelLayout = new javax.swing.GroupLayout(RangePanel);
        RangePanel.setLayout(RangePanelLayout);
        RangePanelLayout.setHorizontalGroup(
            RangePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RangePanelLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(RangePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(ColorizerRangeMinimumLabel)
                    .addComponent(ColorizerThresholdLabel))
                .addGap(18, 18, 18)
                .addGroup(RangePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(ColorizerRangeMinimumField, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ColorizerThresholdField, javax.swing.GroupLayout.Alignment.LEADING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(RangePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(RangePanelLayout.createSequentialGroup()
                        .addComponent(ColorizerRangeMaximumLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ColorizerRangeMaximumField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(ThresholdDescription))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        RangePanelLayout.setVerticalGroup(
            RangePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RangePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(RangePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ColorizerThresholdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ColorizerThresholdLabel)
                    .addComponent(ThresholdDescription))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(RangePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ColorizerRangeMinimumLabel)
                    .addComponent(ColorizerRangeMinimumField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ColorizerRangeMaximumLabel)
                    .addComponent(ColorizerRangeMaximumField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel3.add(RangePanel, java.awt.BorderLayout.SOUTH);

        ColorizerValuePanel.setLayout(new java.awt.BorderLayout());

        jScrollPane18.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("PrefsColorColorizers"))); // NOI18N

        ColorizerList.setToolTipText(bundle.getString("PrefsColorsCategoryList_TT")); // NOI18N
        ColorizerList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                ColorizerListValueChanged(evt);
            }
        });
        jScrollPane18.setViewportView(ColorizerList);

        ColorizerValuePanel.add(jScrollPane18, java.awt.BorderLayout.NORTH);

        jScrollPane11.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("PrefsColorValuesPanel"))); // NOI18N

        ColorizerValueList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "test", "test2", " " };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        ColorizerValueList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        ColorizerValueList.setToolTipText(bundle.getString("PrefsColorsValue_TT")); // NOI18N
        ColorizerValueList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                ColorizerValueListValueChanged(evt);
            }
        });
        jScrollPane11.setViewportView(ColorizerValueList);

        ColorizerValuePanel.add(jScrollPane11, java.awt.BorderLayout.CENTER);

        jPanel3.add(ColorizerValuePanel, java.awt.BorderLayout.CENTER);

        PrefsColors.add(jPanel3, java.awt.BorderLayout.CENTER);

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        UploadColorScheme.setText(bundle.getString("UploadColorScheme")); // NOI18N
        UploadColorScheme.setToolTipText(bundle.getString("PrefsEditorShareColorScheme")); // NOI18N
        UploadColorScheme.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UploadColorSchemeActionPerformed(evt);
            }
        });
        jPanel4.add(UploadColorScheme);

        LoadColorScheme.setText(bundle.getString("LoadColorScheme")); // NOI18N
        LoadColorScheme.setToolTipText(bundle.getString("PrefsEditorDownloadColorScheme")); // NOI18N
        LoadColorScheme.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LoadColorSchemeActionPerformed(evt);
            }
        });
        jPanel4.add(LoadColorScheme);

        ClearAllColors.setText(bundle.getString("PrefsClearAllColorsButton")); // NOI18N
        ClearAllColors.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ClearAllColorsActionPerformed(evt);
            }
        });
        jPanel4.add(ClearAllColors);

        BrightBackground.setText(bundle.getString("PrefsColorSchemeLight")); // NOI18N
        BrightBackground.setToolTipText(bundle.getString("PrefsColorSchemeLight_TT")); // NOI18N
        BrightBackground.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BrightBackgroundActionPerformed(evt);
            }
        });
        jPanel4.add(BrightBackground);

        PrefsColors.add(jPanel4, java.awt.BorderLayout.PAGE_START);

        ColorChooserPanel.setLayout(new java.awt.BorderLayout());
        ColorChooserPanel.add(ColorChooser, java.awt.BorderLayout.CENTER);

        CopyColor.setText(bundle.getString("PreferenceCopyColor")); // NOI18N
        CopyColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CopyColorActionPerformed(evt);
            }
        });
        CopyPastePanel.add(CopyColor);

        PasteColor.setText(bundle.getString("PreferencesPasteColor")); // NOI18N
        PasteColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PasteColorActionPerformed(evt);
            }
        });
        CopyPastePanel.add(PasteColor);

        ColorChooserPanel.add(CopyPastePanel, java.awt.BorderLayout.SOUTH);

        PrefsColors.add(ColorChooserPanel, java.awt.BorderLayout.EAST);

        PrefCategoryPane.addTab(bundle.getString("PreferenceEditorColorsTab"), PrefsColors); // NOI18N

        PrefsFontAndMore.setLayout(new java.awt.BorderLayout());

        FontPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 3, 3));

        SearchTableFontPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("MainPrefsSearchTableFontPanel"))); // NOI18N
        SearchTableFontPanel.setLayout(new javax.swing.BoxLayout(SearchTableFontPanel, javax.swing.BoxLayout.LINE_AXIS));
        SearchTableFontPanel.add(searchTableFontChooser);

        FontPanel.add(SearchTableFontPanel);

        TextEditorFontPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("PreferenceEditorFontControlPanel"))); // NOI18N
        TextEditorFontPanel.setLayout(new javax.swing.BoxLayout(TextEditorFontPanel, javax.swing.BoxLayout.LINE_AXIS));
        TextEditorFontPanel.add(textEditorFontChooser);

        FontPanel.add(TextEditorFontPanel);

        java.util.ResourceBundle bundle2 = java.util.ResourceBundle.getBundle("com/mindliner/resources/WeekPlan"); // NOI18N
        WeekplanFontPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle2.getString("WeekplanFontPanelTitle"))); // NOI18N
        WeekplanFontPanel.setLayout(new javax.swing.BoxLayout(WeekplanFontPanel, javax.swing.BoxLayout.LINE_AXIS));
        WeekplanFontPanel.add(weekplanFontChooser);

        FontPanel.add(WeekplanFontPanel);

        PrefsFontAndMore.add(FontPanel, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        AutoSave.setText(bundle.getString("Prefs_AutoSaveInOfflineMode")); // NOI18N
        jPanel2.add(AutoSave);

        PrefsFontAndMore.add(jPanel2, java.awt.BorderLayout.NORTH);

        PrefCategoryPane.addTab(bundle.getString("PreferenceDialogConfigurationTab"), PrefsFontAndMore); // NOI18N

        EditorPanel.add(PrefCategoryPane, java.awt.BorderLayout.PAGE_START);

        getContentPane().add(EditorPanel, java.awt.BorderLayout.NORTH);

        PreferenceCancel.setText(bundle.getString("PreferenceDialogCancelButton")); // NOI18N
        PreferenceCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PreferenceCancelActionPerformed(evt);
            }
        });
        SaveDiscardPanel.add(PreferenceCancel);

        PreferenceOK.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/3232/floppy_disk.png"))); // NOI18N
        PreferenceOK.setText(bundle.getString("PrefsSave")); // NOI18N
        PreferenceOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PreferenceOKActionPerformed(evt);
            }
        });
        SaveDiscardPanel.add(PreferenceOK);

        getContentPane().add(SaveDiscardPanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void PreferenceCancelActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_PreferenceCancelActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_PreferenceCancelActionPerformed

    private void PreferenceOKActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_PreferenceOKActionPerformed
        ColorManager.storeColorDefinitions();
        TableManager.updateTableColors();
        MlPreferenceManager.setAutosave(AutoSave.isSelected());
        viewer.buildView(); // needed in case map preferences changed
        this.setVisible(false);
    }//GEN-LAST:event_PreferenceOKActionPerformed

private void ColorizerListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_ColorizerListValueChanged
    BaseColorizer c = (BaseColorizer) ColorizerList.getSelectedValue();
    if (c != null) {
        configureRangeControls(c);
        setValueList(c);
    }
}//GEN-LAST:event_ColorizerListValueChanged

    private void CSCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CSCancelButtonActionPerformed
        ColorSchemeDialog.setVisible(false);
    }//GEN-LAST:event_CSCancelButtonActionPerformed

    private void CSLoadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CSLoadButtonActionPerformed
        MlTransferColorScheme cs = (MlTransferColorScheme) ColorSchemes.getSelectedValue();
        if (cs != null) {
            Object[] options = {"Yes, ok to overwrite.", "No"};
            int reply = JOptionPane.showOptionDialog(
                    null,
                    "Overwrites your current color scheme?",
                    "Color Scheme Download",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[1]);
            if (reply == 0) {
                try {
                    // re-load in case the lazy loader did not load the records in the first place
                    MlTransferColorScheme csFullExpanded = colorManager.getScheme(cs.getId());
                    ColorSchemeManager.resetColors();
                    ColorSchemeManager.importColorScheme(csFullExpanded);
                } catch (NonExistingObjectException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Scheme removed in the meantime", JOptionPane.ERROR_MESSAGE);
                } catch (InsufficientAccessRightException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Config Error: Please Inform Your Administrator", JOptionPane.ERROR_MESSAGE);
                }
            }
            ColorSchemeDialog.setVisible(false);
        }
    }//GEN-LAST:event_CSLoadButtonActionPerformed

    private void LoadColorSchemeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LoadColorSchemeActionPerformed
        DefaultListModel dlm = new DefaultListModel();
        for (MlTransferColorScheme cs : colorManager.getAccessibleSchemes()) {
            dlm.addElement(cs);
        }
        ColorSchemes.setModel(dlm);
        ColorSchemeDialog.pack();
        ColorSchemeDialog.setLocationRelativeTo(MindlinerMain.getInstance());
        ColorSchemeDialog.setVisible(true);
    }//GEN-LAST:event_LoadColorSchemeActionPerformed

    private void UploadColorSchemeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UploadColorSchemeActionPerformed
        String name = JOptionPane.showInputDialog("Enter Scheme Name");
        MlTransferColorScheme tcs = ColorSchemeManager.createTransferColorScheme(name);
        try {
            colorManager.createNewCustomScheme(tcs);
        } catch (NonExistingObjectException ex) {
            JOptionPane.showMessageDialog(null, "Could not create scheme", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_UploadColorSchemeActionPerformed

    private void CSDeleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CSDeleteButtonActionPerformed
        MlTransferColorScheme cs = (MlTransferColorScheme) ColorSchemes.getSelectedValue();
        if (cs != null) {
            if (cs.getOwnerId() == CacheEngineStatic.getCurrentUser().getId()) {
                try {
                    colorManager.deleteCustomScheme(cs.getId());
                    DefaultListModel dlm = (DefaultListModel) ColorSchemes.getModel();
                    dlm.removeElement(cs);
                } catch (InsufficientAccessRightException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Config Error: Please Inform Your Administrator", JOptionPane.ERROR_MESSAGE);
                } catch (NonExistingObjectException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Scheme removed in the meantime", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "You can only delete your own schemes.", "Scheme Deletion", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_CSDeleteButtonActionPerformed

    private void ColorizerValueListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_ColorizerValueListValueChanged
        BaseColorizer colorizer = (BaseColorizer) ColorizerList.getSelectedValue();
        if (colorizer != null) {
            Object key = ColorizerValueList.getSelectedValue();
            if (key != null) {
                if (colorChangeListener != null) {
                    ColorChooser.getSelectionModel().removeChangeListener(colorChangeListener);
                }
                colorChangeListener = new MlColorChangeListener(colorizer, key, ColorChooser);
                ColorChooser.getSelectionModel().addChangeListener(colorChangeListener);
            }
        }
    }//GEN-LAST:event_ColorizerValueListValueChanged

    private void CopyColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CopyColorActionPerformed
        PasteColor.setBackground(ColorChooser.getColor());
    }//GEN-LAST:event_CopyColorActionPerformed

    private void PasteColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PasteColorActionPerformed
        ColorChooser.setColor(PasteColor.getBackground());
    }//GEN-LAST:event_PasteColorActionPerformed

    private void ColorizerThresholdFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ColorizerThresholdFieldActionPerformed
        BaseColorizer colorizer = (BaseColorizer) ColorizerList.getSelectedValue();
        if (colorizer != null && colorizer instanceof ThresholdColorizer) {
            ThresholdColorizer tc = (ThresholdColorizer) colorizer;
            try {
                double value = Double.parseDouble(ColorizerThresholdField.getText());
                tc.setThreshold(value);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Threshold", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_ColorizerThresholdFieldActionPerformed

    private void ColorizerRangeMinimumFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ColorizerRangeMinimumFieldActionPerformed
        BaseColorizer colorizer = (BaseColorizer) ColorizerList.getSelectedValue();
        if (colorizer != null && colorizer instanceof RangeColorizer) {
            RangeColorizer rc = (RangeColorizer) colorizer;
            try {
                double min = Double.parseDouble(ColorizerRangeMinimumField.getText());
                rc.setMinimum(min);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Range Minimum", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_ColorizerRangeMinimumFieldActionPerformed

    private void ColorizerRangeMaximumFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ColorizerRangeMaximumFieldActionPerformed
        BaseColorizer colorizer = (BaseColorizer) ColorizerList.getSelectedValue();
        if (colorizer != null && colorizer instanceof RangeColorizer) {
            RangeColorizer rc = (RangeColorizer) colorizer;
            try {
                double max = Double.parseDouble(ColorizerRangeMaximumField.getText());
                rc.setMaximum(max);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Range Maximum", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_ColorizerRangeMaximumFieldActionPerformed

    private void ClearAllColorsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ClearAllColorsActionPerformed
        ColorSchemeManager.resetColors();
    }//GEN-LAST:event_ClearAllColorsActionPerformed

    private void BrightBackgroundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BrightBackgroundActionPerformed
        MlStyler.setLightBackground(BrightBackground.isSelected());
    }//GEN-LAST:event_BrightBackgroundActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox AutoSave;
    private javax.swing.JCheckBox BrightBackground;
    private javax.swing.JButton CSCancelButton;
    private javax.swing.JButton CSDeleteButton;
    private javax.swing.JButton CSLoadButton;
    private javax.swing.JButton ClearAllColors;
    private javax.swing.JColorChooser ColorChooser;
    private javax.swing.JPanel ColorChooserPanel;
    private javax.swing.JDialog ColorSchemeDialog;
    private javax.swing.JLabel ColorSchemeLabel;
    private javax.swing.JList ColorSchemes;
    private javax.swing.JList ColorizerList;
    private javax.swing.JTextField ColorizerRangeMaximumField;
    private javax.swing.JLabel ColorizerRangeMaximumLabel;
    private javax.swing.JTextField ColorizerRangeMinimumField;
    private javax.swing.JLabel ColorizerRangeMinimumLabel;
    private javax.swing.JTextField ColorizerThresholdField;
    private javax.swing.JLabel ColorizerThresholdLabel;
    private javax.swing.JList ColorizerValueList;
    private javax.swing.JPanel ColorizerValuePanel;
    private javax.swing.JButton CopyColor;
    private javax.swing.JPanel CopyPastePanel;
    private javax.swing.JPanel EditorPanel;
    private javax.swing.JPanel FontPanel;
    private javax.swing.JButton LoadColorScheme;
    private javax.swing.JButton PasteColor;
    private javax.swing.JTabbedPane PrefCategoryPane;
    private javax.swing.JButton PreferenceCancel;
    private javax.swing.JButton PreferenceOK;
    private javax.swing.JPanel PrefsColors;
    private javax.swing.JPanel PrefsFontAndMore;
    private javax.swing.JPanel RangePanel;
    private javax.swing.JPanel SaveDiscardPanel;
    private javax.swing.JPanel SearchTableFontPanel;
    private javax.swing.JPanel TextEditorFontPanel;
    private javax.swing.JLabel ThresholdDescription;
    private javax.swing.JButton UploadColorScheme;
    private javax.swing.JPanel WeekplanFontPanel;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane18;
    private com.mindliner.gui.font.MlFontChooser searchTableFontChooser;
    private com.mindliner.gui.font.MlFontChooser textEditorFontChooser;
    private com.mindliner.gui.font.MlFontChooser weekplanFontChooser;
    // End of variables declaration//GEN-END:variables
}
