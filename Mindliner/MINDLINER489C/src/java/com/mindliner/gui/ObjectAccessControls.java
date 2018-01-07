/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.cache.DefaultObjectAttributes;
import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.clientobjects.mlcClient;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.commands.CommandRecorder;
import com.mindliner.commands.ConfidentialityUpdateCommand;
import com.mindliner.gui.color.FixedKeyColorizer;
import com.mindliner.styles.MlStyler;
import java.awt.event.ActionListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

/**
 * This class combines the data pool and confidentiality selector which need to
 * be updated in synch. Together they control an object's accessibility.
 *
 * @author Marius Messerli
 */
public class ObjectAccessControls extends javax.swing.JPanel {

    private boolean showAlways = false;
    private mlcObject object = null;

    /**
     * Creates new form ObjectAccessControls
     */
    public ObjectAccessControls() {
        initComponents();
    }

    /**
     * Sets the text and background colors of its components
     * @param colorizer
     */
    public void applyColors(FixedKeyColorizer colorizer) {
        DataPoolLabel.setForeground(colorizer.getColorForKey(FixedKeyColorizer.FixedKeys.MAIN_DEFAULT_TEXT));
        ConfidentialityLabel.setForeground(colorizer.getColorForKey(FixedKeyColorizer.FixedKeys.MAIN_DEFAULT_TEXT));
        MlStyler.colorizeComboBox(DataPoolCombo, colorizer);
        MlStyler.colorizeComboBox(ConfidentialityCombo, colorizer);
        setBackground(colorizer.getColorForKey(FixedKeyColorizer.FixedKeys.MAIN_DEFAULT_BACKGROUND));
    }

    /**
     * This call binds the two controls for data pool and confidentiality to the
     * specified object. If one of the two properties change the corresponding
     * update commands are called on the object
     *
     * @param o
     */
    public void initializeFromObject(mlcObject o) {
        if (o.getConfidentiality() == null) {
            System.err.println("Confidentiality of object " + o.getId() + " is null");
            return;
        }
        if (o.getClient().getId() != o.getConfidentiality().getClient().getId()) {
            System.err.println("ObjectAccessControls: Fixing broken object with id="
                    .concat(Integer.toString(o.getId()))
                    .concat(", conf=")
                    .concat(o.getConfidentiality().getName())
                    .concat("[of ").concat(o.getConfidentiality().getClient().getName()).concat("]")
                    .concat(", client=" + o.getClient().getName())
                    .concat(" so that confidentiality matches data pool"));
            CommandRecorder.getInstance().scheduleCommand(new ConfidentialityUpdateCommand(o, DefaultObjectAttributes.getConfidentiality(o.getClient().getId())));
        }
        this.object = o;
        DataPoolCombo.setSelectedItem(object.getClient());
        adjustConfidentialityCombo2DataPool();
        ConfidentialityCombo.setSelectedItem(object.getConfidentiality());

    }

    @Override
    public void setEnabled(boolean state) {
        DataPoolCombo.setEnabled(state);
        ConfidentialityCombo.setEnabled(state);
    }

    /**
     * Initialize the combos and set the selection according to the user's
     * object defaults
     */
    public void configureComponents() {
        configureDataPoolCombo();
        adjustConfidentialityCombo2DataPool();
    }

    private void configureDataPoolCombo() {
        DefaultComboBoxModel bm = new DefaultComboBoxModel();
        for (Integer id : CacheEngineStatic.getCurrentUser().getClientIds()) {
            mlcClient c = CacheEngineStatic.getClient(id);
            if (c != null) {
                bm.addElement(c);
            }
        }
        DataPoolCombo.setModel(bm);
        mlcClient defaultClient = CacheEngineStatic.getClient(DefaultObjectAttributes.getDataPoolId());
        if (defaultClient == null) {
            Integer defaultClientId = CacheEngineStatic.getCurrentUser().getClientIds().get(0);
            assert defaultClientId != null : "Cannot determine default client id for current user";
            defaultClient = CacheEngineStatic.getClient(defaultClientId);
            assert defaultClient != null : "Cannot determine default client for current user";
        }
        DataPoolCombo.setSelectedItem(defaultClient);
    }

    private void adjustConfidentialityCombo2DataPool() {
        DefaultComboBoxModel bm = new DefaultComboBoxModel();
        mlcClient currentDataPool = (mlcClient) DataPoolCombo.getSelectedItem();
        if (currentDataPool != null) {
            CacheEngineStatic.getConfidentialities(currentDataPool.getId()).stream().forEach((c) -> {
                bm.addElement(c);
            });
            ConfidentialityCombo.setModel(bm);
            ConfidentialityCombo.setSelectedItem(DefaultObjectAttributes.getConfidentiality(currentDataPool.getId()));
        }
        updateVisibility();
    }

    private void updateVisibility() {

        // The Data Pool Combo
        if (showAlways || CacheEngineStatic.getCurrentUser().getClientIds().size() > 1) {
            DataPoolCombo.setVisible(true);
            DataPoolLabel.setVisible(true);
        } else {
            DataPoolCombo.setVisible(false);
            DataPoolLabel.setVisible(false);
        }

        // The Confidentiality Combo
        mlcClient currentDataPool = (mlcClient) DataPoolCombo.getSelectedItem();
        if (showAlways || (currentDataPool != null && CacheEngineStatic.getConfidentialities(currentDataPool.getId()).size() > 1)) {
            ConfidentialityCombo.setVisible(true);
            ConfidentialityLabel.setVisible(true);
        } else {
            ConfidentialityCombo.setVisible(false);
            ConfidentialityLabel.setVisible(false);
        }
    }

    public void addDataPoolActionListener(ActionListener listener) {
        DataPoolCombo.addActionListener(listener);
    }

    public void addConfidentialityActionListener(ActionListener listener) {
        ConfidentialityCombo.addActionListener(listener);
    }

    /**
     * Updates the data pool control and initializes the confidentiality control
     * with the user's default confidentiality for the specified data pool
     *
     * @param dataPool The new data pool to be selected
     */
    public void setDataPool(mlcClient dataPool) {
        if (dataPool != null) {
            DataPoolCombo.setSelectedItem(dataPool);
            adjustConfidentialityCombo2DataPool();
        }
    }

    public mlcClient getDataPool() {
        return (mlcClient) DataPoolCombo.getSelectedItem();
    }

    /**
     * Updates the confidentiality and also checks/sets the data pool selector
     * to match the data pool of the specified confidentiality
     *
     * @param conf
     */
    public void setConfidentiality(mlsConfidentiality conf) {
        if (conf != null) {
            mlcClient dp = (mlcClient) DataPoolCombo.getSelectedItem();
            if (dp == null || dp.getId() != conf.getClient().getId()) {
                DataPoolCombo.setSelectedItem(CacheEngineStatic.getClient(conf.getClient().getId()));
            }
        }
        ConfidentialityCombo.setSelectedItem(conf);
    }

    public mlsConfidentiality getConfidentiality() {
        return (mlsConfidentiality) ConfidentialityCombo.getSelectedItem();
    }

    public boolean isShowAlways() {
        return showAlways;
    }

    public void setShowAlways(boolean showAlways) {
        this.showAlways = showAlways;
    }

    /**
     * This is a bit unfortunate to leak this component but I need this in the
     * BrizNode to bind the component to the node
     *
     * @return
     */
    public JComboBox getDataPoolCombo() {
        return DataPoolCombo;
    }

    public JComboBox getConfidentialityCombo() {
        return ConfidentialityCombo;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        DataPoolLabel = new javax.swing.JLabel();
        DataPoolCombo = new javax.swing.JComboBox();
        ConfidentialityLabel = new javax.swing.JLabel();
        ConfidentialityCombo = new javax.swing.JComboBox();

        DataPoolLabel.setFont(DataPoolLabel.getFont().deriveFont(DataPoolLabel.getFont().getSize()-2f));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/mindliner/resources/GeneralEditor"); // NOI18N
        DataPoolLabel.setText(bundle.getString("AEDDataPool")); // NOI18N

        DataPoolCombo.setFont(DataPoolCombo.getFont());
        DataPoolCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        DataPoolCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DataPoolComboActionPerformed(evt);
            }
        });

        ConfidentialityLabel.setFont(ConfidentialityLabel.getFont().deriveFont(ConfidentialityLabel.getFont().getSize()-2f));
        ConfidentialityLabel.setText(bundle.getString("AEDConfidentiality")); // NOI18N

        ConfidentialityCombo.setFont(ConfidentialityCombo.getFont());
        ConfidentialityCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(ConfidentialityCombo, 0, 186, Short.MAX_VALUE)
            .addComponent(ConfidentialityLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(DataPoolCombo, 0, 186, Short.MAX_VALUE)
            .addComponent(DataPoolLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(DataPoolLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(DataPoolCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ConfidentialityLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ConfidentialityCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void DataPoolComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DataPoolComboActionPerformed
        adjustConfidentialityCombo2DataPool();
    }//GEN-LAST:event_DataPoolComboActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox ConfidentialityCombo;
    private javax.swing.JLabel ConfidentialityLabel;
    private javax.swing.JComboBox DataPoolCombo;
    private javax.swing.JLabel DataPoolLabel;
    // End of variables declaration//GEN-END:variables
}
