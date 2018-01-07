/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.prefs;

import com.mindliner.entities.Colorizer;
import com.mindliner.gui.color.BaseColorizer;
import com.mindliner.gui.color.ColorManager;
import com.mindliner.gui.color.FixedKeyColorizer;
import com.mindliner.view.MindlinerMapper;
import com.mindliner.view.background.BackgroundPainter;
import com.mindliner.view.background.BackgroundPainterFactory;
import com.mindliner.view.background.DefaultBackgroundPainter;
import com.mindliner.view.background.DefaultBackgroundPainter.BackgroundPainterType;
import com.mindliner.view.background.ImageBackgroundPainter;
import com.mindliner.view.background.ImageBackgroundPainter.ImageSample;
import com.mindliner.view.background.ImageBackgroundPainter.Layout;
import java.awt.Color;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 * This class handles the prefrences for mind maps. It is use in two places: the
 * dialog is inserted into the MlMainPreferenceEditor. A reference to it is also
 * used in Mindliner2DViewer to get access to the values.
 *
 * @author Marius Messerli
 */
public class MapPreferencesPane extends javax.swing.JPanel {

    public static final String FONT_PREFERENCE_KEY = "mapnode";
    private static final String BACKGROUND_COLOR = "backgroundColor";
    private static final String NODE_CHARACTERS = "nodeMaxCharacters";
    private static final String BACKGROUND_PAINTER = "backgroundPainer";
    private static final String BACKGROUND_LAYOUT = "backgroundLayout";
    private static final String BACKGROUND_IMAGE = "backgroundImageSample";
    private static final String DECORATOR_IMAGES = "decoratorImages";
    private static final String DECORATOR_DESCRIPTION = "decoratorDescription";
    private static final String DECORATOR_ATTRIBUTES = "decoratorAttributes";
    private static final String MAX_SHORTEST_PATH = "maxshortestpath";
    private static final String SHORTEST_PATH_ENABLED = "shortestpathenabled";
    private final MindlinerMapper mapper;
    private static final Integer DEFAULT_SHORTEST_PATH = 5;

    /**
     * Creates new form MapPreferences
     *
     * @param mapper The mapper for which this class defines the prefs
     */
    public MapPreferencesPane(MindlinerMapper mapper) {
        this.mapper = mapper;
        initComponents();
        configureComponents();
    }

    private void configureComponents() {

        // The type of selection frame around objects
        DefaultComboBoxModel dcm = new DefaultComboBoxModel();

        // The background painting choices
        dcm = new DefaultComboBoxModel();
        dcm.addElement(DefaultBackgroundPainter.BackgroundPainterType.SingleColor);
        dcm.addElement(DefaultBackgroundPainter.BackgroundPainterType.Image);
        dcm.setSelectedItem(DefaultBackgroundPainter.BackgroundPainterType.SingleColor);
        BackgroundPainterCombo.setModel(dcm);

        // The background image layout choices
        dcm = new DefaultComboBoxModel();
        dcm.addElement(ImageBackgroundPainter.Layout.center);
        dcm.addElement(ImageBackgroundPainter.Layout.stretch);
        BackgroundPainterImageLayoutCombo.setModel(dcm);

        // The background image choies
        dcm = new DefaultComboBoxModel();
        dcm.addElement(ImageBackgroundPainter.ImageSample.Bamboo);
        dcm.addElement(ImageBackgroundPainter.ImageSample.Jeans);
        dcm.addElement(ImageBackgroundPainter.ImageSample.Dunes);
        dcm.addElement(ImageBackgroundPainter.ImageSample.CobbleStone);
        dcm.addElement(ImageBackgroundPainter.ImageSample.KahnDhaka);
        dcm.addElement(ImageBackgroundPainter.ImageSample.KahnFDRPark);
        dcm.addElement(ImageBackgroundPainter.ImageSample.BlackBoard);
        dcm.addElement(ImageBackgroundPainter.ImageSample.ParkInRome);
        BackgroundPainterImageCombo.setModel(dcm);

        int length = mapper.getMaxShortestPath();
        MaxShortestPath.setValue(length);

        loadPreferences();

        mapFontChooser.setPersistenceIdentifyer(FONT_PREFERENCE_KEY);
    }

    private void updateBackgroundPainterImagePrefsControls(boolean state) {
        BackgroundPainterImageCombo.setVisible(state);
        BackgroundPainterImageLabel.setVisible(state);
        BackgroundPainterImageLayoutCombo.setVisible(state);
        BackgroundPainterImageLayoutLabel.setVisible(state);
    }

    public JComboBox getBackgroundPainterCombo() {
        return BackgroundPainterCombo;
    }

    public JComboBox getBackgroundPainterImageCombo() {
        return BackgroundPainterImageCombo;
    }

    public JLabel getBackgroundPainterImageLabel() {
        return BackgroundPainterImageLabel;
    }

    public JComboBox getBackgroundPainterImageLayoutCombo() {
        return BackgroundPainterImageLayoutCombo;
    }

    public JCheckBox getDescriptionDecorator() {
        return ShowDescription;
    }

    public int getWordMaxLength() {
        try {
            return Integer.parseInt(WordMaxLength.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error Specifying Word Length", JOptionPane.ERROR_MESSAGE);
            return 60;
        }
    }
    
    public boolean isShowAttributes(){
        return ShowAttributes.isSelected();
    }
    
    public boolean  isShowDescription(){
        return ShowDescription.isSelected();
    }
    
    public boolean isShowImages(){
        return ShowImages.isSelected();
    }

    public void loadPreferences() {
        Preferences userPrefs = Preferences.userNodeForPackage(MapPreferencesPane.class);

        loadBackgroundPainterPrefs(userPrefs);

        WordMaxLength.setText(Integer.toString(userPrefs.getInt(NODE_CHARACTERS, 80)));
        ShowImages.setSelected(userPrefs.getBoolean(DECORATOR_IMAGES, ShowImages.isSelected()));
        ShowDescription.setSelected(userPrefs.getBoolean("prefsDecoratorDescription", ShowDescription.isSelected()));
        ShowAttributes.setSelected(userPrefs.getBoolean(DECORATOR_ATTRIBUTES, ShowAttributes.isSelected()));

        int length = userPrefs.getInt(MAX_SHORTEST_PATH, -1);
        if (length != -1) {
            MaxShortestPath.setText(String.valueOf(length));
            mapper.setMaxShortestPath(length);

            boolean spEnabled = userPrefs.getBoolean(SHORTEST_PATH_ENABLED, true);
            ShortestPathEnabled.setSelected(spEnabled);
            MaxShortestPath.setEnabled(spEnabled);
            mapper.setShortestPathEnabled(spEnabled);
        }
    }

    private void loadBackgroundPainterPrefs(Preferences userPrefs) {
        BackgroundPainterType painterType;
        try {
            painterType = BackgroundPainterType.valueOf(userPrefs.get(BACKGROUND_PAINTER, BackgroundPainterType.SingleColor.name()));
        } catch (IllegalArgumentException ex) {
            painterType = BackgroundPainterType.SingleColor;
        }
        BackgroundPainterCombo.setSelectedItem(painterType);

        // THE BACKGROUND IMAGE (ACTIVE FOR PAINTER TYPE IMAGE ONLY)
        ImageSample imageSample;
        String imageSampleString = userPrefs.get(BACKGROUND_IMAGE, ImageSample.Bamboo.toString());
        try {
            imageSample = ImageSample.valueOf(imageSampleString);
        } catch (IllegalArgumentException ex) {
            imageSample = ImageSample.Bamboo;
        }
        BackgroundPainterImageCombo.setSelectedItem(imageSample);

        // THE BACKGROUND IMAGE LAYOUT (ACTIVE FOR PAINTER TYPE IMAGE ONLY)
        ImageBackgroundPainter.Layout imageLayout;
        String imageLayoutString = userPrefs.get(BACKGROUND_LAYOUT, ImageBackgroundPainter.Layout.center.toString());
        try {
            imageLayout = ImageBackgroundPainter.Layout.valueOf(imageLayoutString);
        } catch (IllegalArgumentException ex) {
            imageLayout = ImageBackgroundPainter.Layout.center;
        }
        BackgroundPainterImageLayoutCombo.setSelectedItem(imageLayout);
        BackgroundPainter bp = BackgroundPainterFactory.createBackgroundPainter(painterType, imageSample, imageLayout);
        mapper.setBackgroundPainter(bp);
    }

    public void storePreferences() {

        Preferences userPrefs = Preferences.userNodeForPackage(MapPreferencesPane.class);

        int nodeChars = Integer.parseInt(WordMaxLength.getText());
        userPrefs.putInt(NODE_CHARACTERS, nodeChars);
        userPrefs.putInt(BACKGROUND_COLOR + "-r", mapper.getBackground().getRed());
        userPrefs.putInt(BACKGROUND_COLOR + "-g", mapper.getBackground().getGreen());
        userPrefs.putInt(BACKGROUND_COLOR + "-b", mapper.getBackground().getBlue());
        BackgroundPainterType painter = (BackgroundPainterType) BackgroundPainterCombo.getSelectedItem();
        userPrefs.put(BACKGROUND_PAINTER, painter.name());
        userPrefs.put(BACKGROUND_LAYOUT, (String) BackgroundPainterImageLayoutCombo.getSelectedItem().toString());
        userPrefs.put(BACKGROUND_IMAGE, (String) BackgroundPainterImageCombo.getSelectedItem().toString());

        userPrefs.putBoolean(DECORATOR_IMAGES, ShowImages.isSelected());
        userPrefs.putBoolean(DECORATOR_DESCRIPTION, ShowDescription.isSelected());
        userPrefs.putBoolean(DECORATOR_ATTRIBUTES, ShowAttributes.isSelected());

        userPrefs.putInt(MAX_SHORTEST_PATH, Integer.valueOf(MaxShortestPath.getText()));
        userPrefs.putBoolean(SHORTEST_PATH_ENABLED, ShortestPathEnabled.isSelected());

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        CheckboxPanel = new javax.swing.JPanel();
        ShowDescription = new javax.swing.JCheckBox();
        ShowAttributes = new javax.swing.JCheckBox();
        ShowImages = new javax.swing.JCheckBox();
        FontChooserContainer = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        WordMaxLength = new javax.swing.JTextField();
        mapFontChooser = new com.mindliner.gui.font.MlFontChooser();
        jPanel3 = new javax.swing.JPanel();
        BackgroundColor = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        BackgroundPainterCombo = new javax.swing.JComboBox();
        BackgroundPainterImageLayoutCombo = new javax.swing.JComboBox();
        BackgroundPainterImageLayoutLabel = new javax.swing.JLabel();
        BackgroundPainterImageCombo = new javax.swing.JComboBox();
        BackgroundPainterImageLabel = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        ShortestPathEnabled = new javax.swing.JCheckBox();
        MaxShortestPath = new javax.swing.JFormattedTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/mindliner/resources/Mapper"); // NOI18N
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("MapPrefsNodeDecoratorBorder"))); // NOI18N

        ShowDescription.setSelected(true);
        ShowDescription.setText(bundle.getString("MapPrefsDescriptionDecoratorCheckbox")); // NOI18N

        ShowAttributes.setSelected(true);
        ShowAttributes.setText(bundle.getString("MapPrefsAttributeDecoratorLabel")); // NOI18N

        ShowImages.setSelected(true);
        ShowImages.setText(bundle.getString("MapPreferencesShowImages")); // NOI18N

        javax.swing.GroupLayout CheckboxPanelLayout = new javax.swing.GroupLayout(CheckboxPanel);
        CheckboxPanel.setLayout(CheckboxPanelLayout);
        CheckboxPanelLayout.setHorizontalGroup(
            CheckboxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CheckboxPanelLayout.createSequentialGroup()
                .addComponent(ShowAttributes)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(ShowDescription)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(ShowImages)
                .addContainerGap(157, Short.MAX_VALUE))
        );
        CheckboxPanelLayout.setVerticalGroup(
            CheckboxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CheckboxPanelLayout.createSequentialGroup()
                .addGroup(CheckboxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ShowAttributes)
                    .addComponent(ShowDescription)
                    .addComponent(ShowImages))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(CheckboxPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(CheckboxPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        FontChooserContainer.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("MapPrefsNodeTextAttributes"))); // NOI18N
        FontChooserContainer.setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new java.awt.BorderLayout(5, 0));

        jLabel2.setText(bundle.getString("MapMaxTextLength")); // NOI18N
        jPanel2.add(jLabel2, java.awt.BorderLayout.WEST);

        WordMaxLength.setText("80");
        jPanel2.add(WordMaxLength, java.awt.BorderLayout.CENTER);

        FontChooserContainer.add(jPanel2, java.awt.BorderLayout.SOUTH);
        FontChooserContainer.add(mapFontChooser, java.awt.BorderLayout.CENTER);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("MapPrefsBackGroundColorTitle"))); // NOI18N

        BackgroundColor.setText(bundle.getString("ViewFrameBackgroundColorButton")); // NOI18N
        BackgroundColor.setToolTipText(bundle.getString("MapBackgroundColor_TT")); // NOI18N
        BackgroundColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BackgroundColorActionPerformed(evt);
            }
        });

        jLabel11.setText(bundle.getString("MapPrefsBackgroundPainterLabel")); // NOI18N

        BackgroundPainterCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        BackgroundPainterCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BackgroundPainterComboActionPerformed(evt);
            }
        });

        BackgroundPainterImageLayoutCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        BackgroundPainterImageLayoutCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BackgroundPainterImageLayoutComboActionPerformed(evt);
            }
        });

        BackgroundPainterImageLayoutLabel.setText(bundle.getString("MapPrefsImageBackgroundLayout")); // NOI18N

        BackgroundPainterImageCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        BackgroundPainterImageCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BackgroundPainterImageComboActionPerformed(evt);
            }
        });

        BackgroundPainterImageLabel.setText(bundle.getString("MapPrefsBackgroundImageUrl")); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11)
                            .addComponent(BackgroundPainterImageLayoutLabel)
                            .addComponent(BackgroundPainterImageLabel))
                        .addGap(21, 21, 21)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(BackgroundPainterImageLayoutCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(BackgroundPainterCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(BackgroundPainterImageCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(65, 65, 65)
                        .addComponent(BackgroundColor)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(BackgroundColor)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(BackgroundPainterCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BackgroundPainterImageLayoutCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(BackgroundPainterImageLayoutLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BackgroundPainterImageCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(BackgroundPainterImageLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        java.util.ResourceBundle bundle1 = java.util.ResourceBundle.getBundle("com/mindliner/resources/Preferences"); // NOI18N
        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle1.getString("PathDetectionTitle"))); // NOI18N

        ShortestPathEnabled.setText(bundle.getString("ShortestPathCheckbox")); // NOI18N
        ShortestPathEnabled.setToolTipText(bundle.getString("EnableShortestPath_TT")); // NOI18N
        ShortestPathEnabled.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        ShortestPathEnabled.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ShortestPathEnabledActionPerformed(evt);
            }
        });

        MaxShortestPath.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(java.text.NumberFormat.getIntegerInstance())));
        MaxShortestPath.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                MaxShortestPathFocusLost(evt);
            }
        });

        jLabel1.setText(bundle.getString("ShortestPathMaxLength")); // NOI18N
        jLabel1.setToolTipText(bundle.getString("ShortestPathMapPathLength_TT")); // NOI18N

        jLabel3.setText(bundle.getString("ShortestPathLinksText")); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ShortestPathEnabled)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(MaxShortestPath, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ShortestPathEnabled)
                    .addComponent(MaxShortestPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(FontChooserContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(FontChooserContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void BackgroundColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BackgroundColorActionPerformed
        Color newBG = JColorChooser.showDialog(null, "Choose Background Color", mapper.getBackground());
        if (newBG != null) {
            BaseColorizer fkc = ColorManager.getColorizerForType(Colorizer.ColorDriverAttribute.FixedKey);
            fkc.setColor(FixedKeyColorizer.FixedKeys.MAP_BACKGROUND, newBG);
            mapper.setBackground(newBG);
        }
    }//GEN-LAST:event_BackgroundColorActionPerformed

    private void BackgroundPainterImageComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BackgroundPainterImageComboActionPerformed
        ImageBackgroundPainter.ImageSample sample = (ImageBackgroundPainter.ImageSample) BackgroundPainterImageCombo.getSelectedItem();
        BackgroundPainter bp = BackgroundPainterFactory.createBackgroundPainter(BackgroundPainterType.Image, sample, Layout.stretch);
        mapper.setBackgroundPainter(bp);
    }//GEN-LAST:event_BackgroundPainterImageComboActionPerformed

    private void BackgroundPainterImageLayoutComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BackgroundPainterImageLayoutComboActionPerformed
        BackgroundPainter bp = mapper.getBackgroundPainter();
        if (bp instanceof ImageBackgroundPainter) {
            ImageBackgroundPainter ibp = (ImageBackgroundPainter) bp;
            ibp.setLayout((ImageBackgroundPainter.Layout) BackgroundPainterImageLayoutCombo.getSelectedItem());
        }
    }//GEN-LAST:event_BackgroundPainterImageLayoutComboActionPerformed

    private void BackgroundPainterComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BackgroundPainterComboActionPerformed
        DefaultBackgroundPainter.BackgroundPainterType bpt = (DefaultBackgroundPainter.BackgroundPainterType) BackgroundPainterCombo.getSelectedItem();
        switch (bpt) {
            case SingleColor:
                mapper.setBackgroundPainter(BackgroundPainterFactory.createBackgroundPainter(bpt, null, null));
                updateBackgroundPainterImagePrefsControls(false);
                break;

            case Image:
                mapper.setBackgroundPainter(BackgroundPainterFactory.createBackgroundPainter(bpt, ImageSample.ParkInRome, Layout.stretch));
                updateBackgroundPainterImagePrefsControls(true);
                break;

            default:
                throw new AssertionError();
        }
    }//GEN-LAST:event_BackgroundPainterComboActionPerformed

    private void ShortestPathEnabledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ShortestPathEnabledActionPerformed
        MaxShortestPath.setEnabled(ShortestPathEnabled.isSelected());
        mapper.setShortestPathEnabled(ShortestPathEnabled.isSelected());
    }//GEN-LAST:event_ShortestPathEnabledActionPerformed

    private void MaxShortestPathFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_MaxShortestPathFocusLost
        String text = MaxShortestPath.getText();

        int length;
        try {
            length = Integer.valueOf(text);
        } catch (NumberFormatException ex) {
            MaxShortestPath.setText(DEFAULT_SHORTEST_PATH.toString());
            length = DEFAULT_SHORTEST_PATH;
        }
        mapper.setMaxShortestPath(length);
    }//GEN-LAST:event_MaxShortestPathFocusLost

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BackgroundColor;
    private javax.swing.JComboBox BackgroundPainterCombo;
    private javax.swing.JComboBox BackgroundPainterImageCombo;
    private javax.swing.JLabel BackgroundPainterImageLabel;
    private javax.swing.JComboBox BackgroundPainterImageLayoutCombo;
    private javax.swing.JLabel BackgroundPainterImageLayoutLabel;
    private javax.swing.JPanel CheckboxPanel;
    private javax.swing.JPanel FontChooserContainer;
    private javax.swing.JFormattedTextField MaxShortestPath;
    private javax.swing.JCheckBox ShortestPathEnabled;
    private javax.swing.JCheckBox ShowAttributes;
    private javax.swing.JCheckBox ShowDescription;
    private javax.swing.JCheckBox ShowImages;
    private javax.swing.JTextField WordMaxLength;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private com.mindliner.gui.font.MlFontChooser mapFontChooser;
    // End of variables declaration//GEN-END:variables
}
