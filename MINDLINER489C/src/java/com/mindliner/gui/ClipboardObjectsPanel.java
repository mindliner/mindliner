/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui;

import com.mindliner.analysis.MlClassHandler;
import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.MlClientClassHandler;
import com.mindliner.clientobjects.mlcKnowlet;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcObjectCollection;
import com.mindliner.clientobjects.mlcTask;
import com.mindliner.clientobjects.mlcUser;
import com.mindliner.commands.CommandRecorder;
import com.mindliner.commands.ObjectCreationCommand;
import com.mindliner.events.ObjectChangeManager;
import com.mindliner.img.icons.MlIconManager;
import com.mindliner.serveraccess.OnlineManager;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

/**
 * Manages all objects which should be created from the clipboard content
 * 
 * @author Dominic Plangger
 */
public class ClipboardObjectsPanel extends javax.swing.JPanel {
    
    private List<mlcObject> objects;
    private ClipboardObjectsDialog dialog; // surrounding dialog
    private mlcObject parent = null;

    /**
     * Creates new form ClipboardObjectsPanel
     */
    public ClipboardObjectsPanel() {
        initComponents();
    }
    
    public void replaceObject(mlcObject old, mlcObject newObj) {
        int i = objects.indexOf(old);
        objects.remove(old);
        objects.add(i, newObj);
    }
    
    public void removeObject(mlcObject obj) {
        objects.remove(obj);
    }
    

    public void setObjects(List<mlcObject> objects) {
        this.objects = objects;
        updatePanels();
    }

    public void setParentObject(mlcObject parent) {
        this.parent = parent;
    }
    
    public void setDialog(ClipboardObjectsDialog dialog) {
        this.dialog = dialog;
    }
    
    private void updatePanels() {
        MainPanel.removeAll();
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(MainPanel);
        GroupLayout.ParallelGroup parallelGroup = layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING);
        GroupLayout.SequentialGroup sequentialGroup = layout.createSequentialGroup().addContainerGap();
        System.out.println();
        for (mlcObject obj : objects) {
            System.out.println(obj.getId());
            ClipboardSingleObjectPanel p = new ClipboardSingleObjectPanel();
            p.setObject(obj);
            p.setParent(this);
            parallelGroup.addComponent(p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
            sequentialGroup.addComponent(p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
        }

        
        MainPanel.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(parallelGroup))
        );
                
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sequentialGroup)
        );
        
        MainPanel.revalidate();
        MainPanel.repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        CreateButton = new javax.swing.JButton();
        DiscardButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        MainPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();

        setMaximumSize(new java.awt.Dimension(500, 32767));
        setPreferredSize(new java.awt.Dimension(500, 800));

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel1.setText("Objects to create from clipboard content");

        CreateButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/add2-3.png"))); // NOI18N
        CreateButton.setText("Create");
        CreateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CreateButtonActionPerformed(evt);
            }
        });

        DiscardButton.setText("Discard");
        DiscardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DiscardButtonActionPerformed(evt);
            }
        });

        jSeparator1.setForeground(new java.awt.Color(153, 153, 153));

        jLabel2.setText("To change object type, click on the type symbol");

        jScrollPane1.setBorder(null);

        MainPanel.setPreferredSize(new java.awt.Dimension(632, 800));

        javax.swing.GroupLayout MainPanelLayout = new javax.swing.GroupLayout(MainPanel);
        MainPanel.setLayout(MainPanelLayout);
        MainPanelLayout.setHorizontalGroup(
            MainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 632, Short.MAX_VALUE)
        );
        MainPanelLayout.setVerticalGroup(
            MainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 800, Short.MAX_VALUE)
        );

        jScrollPane1.setViewportView(MainPanel);

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/1616/add-2.png"))); // NOI18N
        jLabel3.setText("New");
        jLabel3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel3MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 91, Short.MAX_VALUE)
                        .addComponent(CreateButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(DiscardButton)
                        .addContainerGap())))
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addContainerGap())
            .addComponent(jSeparator1)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(CreateButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(DiscardButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2)
                        .addGap(3, 3, 3)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 675, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void CreateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CreateButtonActionPerformed
        dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        CommandRecorder cr = CommandRecorder.getInstance();
        for (mlcObject o : objects) {
            // create each object
            ObjectCreationCommand cmd = new ObjectCreationCommand(parent, o.getClass(), o.getHeadline(), o.getDescription());
            cr.scheduleCommand(cmd);
            if (!OnlineManager.waitForServerMessages()) {
                ObjectChangeManager.objectCreated(cmd.getObject());
            }
        }
        dialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        dialog.setVisible(false);
    }//GEN-LAST:event_CreateButtonActionPerformed

    private void DiscardButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DiscardButtonActionPerformed
        dialog.setVisible(false);
    }//GEN-LAST:event_DiscardButtonActionPerformed

    private void jLabel3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel3MouseClicked
        JPopupMenu jpopup = createTypePopup();
        jpopup.show(evt.getComponent(), evt.getX(), evt.getY());
    }//GEN-LAST:event_jLabel3MouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton CreateButton;
    private javax.swing.JButton DiscardButton;
    private javax.swing.JPanel MainPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    // End of variables declaration//GEN-END:variables

    
    public JPopupMenu createTypePopup() {
        JPopupMenu jPopup = new JPopupMenu();
        ButtonGroup group = new ButtonGroup();
        List<Class> objectTypes = new ArrayList<>();
        objectTypes.add(mlcKnowlet.class);
        objectTypes.add(mlcObjectCollection.class);
        objectTypes.add(mlcTask.class);
        mlcUser user = CacheEngineStatic.getCurrentUser();
         for (final Class c : objectTypes) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem();
            item.addActionListener((ActionEvent e) -> {
                createNewObject(c);
            });
            setMenuItemLabel(item, c);
            group.add(item);
            jPopup.add(item);
        }
         return jPopup;
    }
    
    public static void setMenuItemLabel(JMenuItem item, Class c) {
        MlClassHandler.MindlinerObjectType type = MlClientClassHandler.getTypeByClass(c);
        String text = MlClientClassHandler.getNameByType(type);
        ImageIcon ic = MlIconManager.getIconForType(type);
        item.setIcon(ic);
        item.setText(text);
    }

    private void createNewObject(Class c) {
        CommandRecorder cr = CommandRecorder.getInstance();
        int tempId = cr.getTemporaryId();
        mlcObject o;
        try {
            o = (mlcObject) c.newInstance();
        } catch (Exception ex) {
            Logger.getLogger(ClipboardObjectsPanel.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Unexpected error while creating new Object. Skipping object creation.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        o.setId(tempId);
        objects.add(0, o);
        updatePanels();
    }
}