/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * CommandEditor.java
 *
 * Author: Marius Messerli
 *
 * Created on Oct 20, 2009, 5:51:32 PM
 */

package com.mindliner.gui;

import com.mindliner.commands.CommandEditor;
import com.mindliner.analysis.MlClassHandler;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.commands.CommandRecorder;
import com.mindliner.commands.MindlinerCommand;
import com.mindliner.main.MindlinerMain;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;

/**
 *
 * @author Marius Messerli
 */
public class CommandEditorImpl extends JPanel implements CommandEditor{

    /** Creates new form CommandEditor */
    public CommandEditorImpl() {
        initComponents();
    }

    @Override
    public void updateCommandList(){
        CommandRecorder cr = CommandRecorder.getInstance();
        DefaultListModel dlm = new DefaultListModel();
        for (MindlinerCommand c : cr.getQueue()){
            dlm.addElement(c);
        }
        CommandList.setModel(dlm);
        CommandCountLabel.setText(CommandList.getModel().getSize() + " commands");
    }

    private void updateDescription(MindlinerCommand c){
        StyledDocument sd = new DefaultStyledDocument();

        if (c != null){
            try {
                TextStyleManager tsm = TextStyleManager.getUniqueInstance();
                String newline = System.getProperty("line.separator");
                String classNameOnly = MlClassHandler.getClassNameOnly(c.getClass().getName());
                sd.insertString(sd.getLength(), classNameOnly + newline + newline, tsm.getHeaderStyle());
                mlcObject mbo = c.getObject();
                if (mbo != null){
                    sd.insertString(sd.getLength(), "O: " + mbo.getHeadline() + newline, tsm.getBodyStyle());
                    if (c.getDetails().length() > 0){
                        sd.insertString(sd.getLength(), "Details: " + c.getDetails() + newline, tsm.getBodyStyle());
                    }
                }
                CommandDetailPane.setStyledDocument(sd);
            }
            catch (BadLocationException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Update Detail Pane", JOptionPane.ERROR_MESSAGE);
            }
        }

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        CommandsPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        CommandList = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        DommandDetailPanel = new javax.swing.JPanel();
        CommandCountLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        CommandDetailPane = new javax.swing.JTextPane();
        CommandsControlPanel = new javax.swing.JPanel();
        DeleteAll = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        CommandList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        CommandList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                CommandListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(CommandList);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/mindliner/resources/General"); // NOI18N
        jLabel1.setText(bundle.getString("CommandEditorTitle")); // NOI18N

        javax.swing.GroupLayout CommandsPanelLayout = new javax.swing.GroupLayout(CommandsPanel);
        CommandsPanel.setLayout(CommandsPanelLayout);
        CommandsPanelLayout.setHorizontalGroup(
            CommandsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 416, Short.MAX_VALUE)
            .addGroup(CommandsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(CommandsPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(CommandsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE)
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addContainerGap()))
        );
        CommandsPanelLayout.setVerticalGroup(
            CommandsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 275, Short.MAX_VALUE)
            .addGroup(CommandsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(CommandsPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jLabel1)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        add(CommandsPanel, java.awt.BorderLayout.CENTER);

        DommandDetailPanel.setLayout(new java.awt.BorderLayout());

        CommandCountLabel.setText("10");
        DommandDetailPanel.add(CommandCountLabel, java.awt.BorderLayout.PAGE_START);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jScrollPane2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jScrollPane2.setPreferredSize(new java.awt.Dimension(4, 100));

        CommandDetailPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jScrollPane2.setViewportView(CommandDetailPane);

        jPanel1.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        DeleteAll.setText(bundle.getString("CommandEditorClear")); // NOI18N
        DeleteAll.setToolTipText(bundle.getString("CommandEditorDeleteAll_TT")); // NOI18N
        DeleteAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeleteAllActionPerformed(evt);
            }
        });
        CommandsControlPanel.add(DeleteAll);

        jPanel1.add(CommandsControlPanel, java.awt.BorderLayout.SOUTH);

        DommandDetailPanel.add(jPanel1, java.awt.BorderLayout.CENTER);

        add(DommandDetailPanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void DeleteAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeleteAllActionPerformed
    if (JOptionPane.showConfirmDialog(MindlinerMain.getInstance(),
            "Sure to delete all the above modifications (Note: the cache will still reflect current state)?",
            "Offline Commands", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
        CommandRecorder.getInstance().clearQueue();
        updateCommandList();
    }
}//GEN-LAST:event_DeleteAllActionPerformed

    private void CommandListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_CommandListValueChanged
        MindlinerCommand c = (MindlinerCommand) CommandList.getSelectedValue();
        if (c != null) updateDescription(c);
    }//GEN-LAST:event_CommandListValueChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel CommandCountLabel;
    private javax.swing.JTextPane CommandDetailPane;
    private javax.swing.JList CommandList;
    private javax.swing.JPanel CommandsControlPanel;
    private javax.swing.JPanel CommandsPanel;
    private javax.swing.JButton DeleteAll;
    private javax.swing.JPanel DommandDetailPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    // End of variables declaration//GEN-END:variables

}