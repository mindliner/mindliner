/*
 * GeneralEditor.java renamed to TextEditorPanel renamed to ObjectEditor
 *
 * Created on Nov 28, 2008, 9:25:22 AM
 */
package com.mindliner.gui;

import com.mindliner.analysis.UriUtils;
import com.mindliner.clientobjects.MlClientClassHandler;
import com.mindliner.clientobjects.MlcContainerMap;
import com.mindliner.clientobjects.MlcImage;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.commands.CommandRecorder;
import com.mindliner.commands.ImageUpdateCommand;
import com.mindliner.commands.ObjectCreationCommand;
import com.mindliner.commands.TextUpdateCommand;
import com.mindliner.entities.Colorizer;
import com.mindliner.entities.MlsImage;
import com.mindliner.events.MlEventLogger;
import com.mindliner.events.ObjectChangeManager;
import com.mindliner.events.SearchTermManager;
import com.mindliner.gui.color.ColorManager;
import com.mindliner.gui.color.FixedKeyColorizer;
import com.mindliner.img.icons.MlIconManager;
import com.mindliner.main.AttributeEditor;
import com.mindliner.main.MindlinerMain;
import com.mindliner.prefs.MlMainPreferenceEditor;
import com.mindliner.styles.MlStyler;
import com.mindliner.serveraccess.OnlineManager;
import com.mindliner.view.containermap.FXMLController;
import com.mindliner.view.dispatch.MlObjectViewer;
import com.mindliner.view.dispatch.MlViewDispatcherImpl;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JDialog;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * A graphical editor for one or many objects. If more than one object is edited
 * then only the editor components for the common attributes are enabled and the
 * text fields are disabled.
 *
 * @author Marius Messerli
 */
public final class ObjectEditor extends JDialog {

    private static final String EDITOR_WIDTH = "editorwidth";
    private static final String EDITOR_HEIGHT = "editorheight";
    private static final String EDITOR_X_POSITION = "editorxposition";
    private static final String EDITOR_Y_POSITION = "editoryposition";

    public static final String FONT_PREFERENCE_KEY = "objecteditor";
    // the empty string is used to initialize the editor styles and will be removed after the edit
    private static final String LEADING_SPACE = " ";
    private List<mlcObject> editObjects = null;
    private final SimpleAttributeSet headlineStyle = new SimpleAttributeSet();
    private final SimpleAttributeSet uriStyle = new SimpleAttributeSet();
    private final SimpleAttributeSet selectionStyle = new SimpleAttributeSet();
    // relative is used only in case a new object is edited and persisted
    private mlcObject relative = null;
    private AttributeEditor attributeEditor = null;

    public ObjectEditor(List<mlcObject> objects) {
        initComponents();
        editObjects = objects;
        configureComponents();
    }

    private void configureComponents() {
        assert editObjects != null : "Objects must not be null";
        assert !editObjects.isEmpty() : "Objects must not be empty";

        attributeEditor = new AttributeEditor(editObjects);
        AttributesPanel.add(attributeEditor, BorderLayout.CENTER);
        TextPaneUriListener tpl = new TextPaneUriListener(DescriptionTextPane);
        DescriptionTextPane.addCaretListener(tpl);
        DescriptionTextPane.addKeyListener(tpl);
        initializeColorsAndStyle();
        adaptControlsToObjects();
        if (HeadlineTextPane.hasFocus() == false) {
            HeadlineTextPane.requestFocus();
        } else {
            // without this hack the carret is invisible although the editing works well ?!
            DescriptionTextPane.requestFocus();
            HeadlineTextPane.requestFocus();
        }
        loadPreferences();
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                storePreferences();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                storePreferences();
            }

        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ObjectEditorLauncher.unregisterFromOpenEditors(ObjectEditor.this);
                dispose();
            }
        });
    }

    private void initializeColorsAndStyle() {
        FixedKeyColorizer fkc = (FixedKeyColorizer) ColorManager.getColorizerForType(Colorizer.ColorDriverAttribute.FixedKey);
        Color fg = fkc.getColorForObject(FixedKeyColorizer.FixedKeys.MAIN_DEFAULT_TEXT);
        Color bg = fkc.getColorForObject(FixedKeyColorizer.FixedKeys.MAIN_DEFAULT_BACKGROUND);
        Color caret = fkc.getColorForObject(FixedKeyColorizer.FixedKeys.MAIN_DEFAULT_TEXT_CARET);
        getContentPane().setBackground(bg);

        Font font = MlMainPreferenceEditor.getEditorFont();
        StyleConstants.setForeground(headlineStyle, fg);
        StyleConstants.setFontFamily(headlineStyle, font.getName());
        StyleConstants.setFontSize(headlineStyle, font.getSize());

        StyleConstants.setForeground(uriStyle, fg);
        StyleConstants.setFontFamily(uriStyle, font.getName());
        StyleConstants.setFontSize(uriStyle, font.getSize());
        StyleConstants.setUnderline(uriStyle, true);

        StyleConstants.setForeground(selectionStyle, fg);
        StyleConstants.setBackground(selectionStyle, new Color(196, 212, 232));
        StyleConstants.setFontFamily(selectionStyle, font.getName());
        StyleConstants.setFontSize(selectionStyle, font.getSize());

        DescriptionTextPane.setCaretColor(caret);
        HeadlineTextPane.setBackground(bg);
        HeadlineTextPane.setCaretColor(caret);
        HeadlineLabel.setForeground(fg);
        HeadlineLabel.setOpaque(true);
        HeadlineLabel.setBackground(bg);
        DescriptionLabel.setForeground(fg);
        DescriptionLabel.setOpaque(true);
        DescriptionLabel.setBackground(bg);
        ImageURLLabel.setForeground(fg);
        ImageURLLabel.setBackground(bg);

        AttributesPanel.setBackground(fkc.getColorForObject(FixedKeyColorizer.FixedKeys.MAIN_DEFAULT_BACKGROUND));
        ObjectSignatureLabel.setForeground(fg);
        MlStyler.colorizeTextPane(HeadlineTextPane, bg, fg);
        MlStyler.colorizeTextPane(DescriptionTextPane, bg, fg);
        MlStyler.colorizeButton(SaveButton, fkc);
        MlStyler.colorizeButton(AbortEditButton, fkc);
        attributeEditor.applyColors(fkc);
    }

    public void setEditorFont(Font f) {
        StyleConstants.setFontFamily(headlineStyle, f.getName());
        StyleConstants.setFontSize(headlineStyle, f.getSize());
        DescriptionTextPane.setFont(f);
    }

    private void adaptControlsToObjects() {
        if (editObjects.size() == 1) {

            mlcObject editObject = editObjects.get(0);
            SimpleDateFormat sdf = new SimpleDateFormat();

            HeadlineTextPane.setEnabled(true);
            DescriptionTextPane.setEnabled(true);
            updateHeadline(editObject.getHeadline(), false);
            updateDescription(editObject.getDescription());

            if (editObject instanceof MlcImage) {
                ImagePanel.setVisible(true);
                ImageURL.setEnabled(true);
                ImageURL.setText(((MlcImage) editObject).getUrl());
            } else {
                ImagePanel.setVisible(false);
            }
            if (editObject.getId() != mlcObject.NEW_OBJECT_ID) {
                MlEventLogger.logReadEvent(editObject);
                ObjectSignatureLabel.setText("id: " + editObject.getId() + ", owner: " + editObject.getOwner().toString() + ", mod: "
                        + sdf.format(editObject.getModificationDate()));

            } else {
                ObjectSignatureLabel.setText("id: <not stored yet>");
            }
            IconLabel.setIcon(MlIconManager.getIconForType(MlClientClassHandler.getTypeByClass(editObject.getClass())));
        } else {
            // more than one object selected for editing
            ImagePanel.setVisible(false);
            HeadlinePanel.setVisible(false);
            HeadlineTextPane.setEnabled(false);
            DescriptionPanel.setVisible(false);
            DescriptionTextPane.setEnabled(false);
            ImageURL.setEnabled(false);
        }
    }

    /**
     * This function inserts the specified word. It uses different styles for
     * nicknames, hyperlinks, search terms, and normal text.
     *
     * @param sd The document to append text to
     * @param word The word to append
     */
    private void insertWord(StyledDocument sd, String word) {
        try {

            // then handle URIs
            URI uri = UriUtils.getAbsoluteUri(word);
            if (uri != null) {
                sd.insertString(sd.getLength(), word, uriStyle);
            } else {

                // now handle search terms
                String firstSelectionTerm = "";
                for (String selectionTerm : SearchTermManager.getSearchWords()) {
                    if (firstSelectionTerm.length() == 0 && word.toLowerCase().contains(selectionTerm.toLowerCase())) {
                        firstSelectionTerm = selectionTerm;
                    }
                }
                if (firstSelectionTerm.length() > 0) {
                    int startIndex = word.toLowerCase().indexOf(firstSelectionTerm.toLowerCase());
                    int endIndex = startIndex + firstSelectionTerm.length();
                    if (startIndex != 0) {
                        sd.insertString(sd.getLength(), word.substring(0, startIndex), headlineStyle);
                    }
                    sd.insertString(sd.getLength(), word.substring(startIndex, endIndex), selectionStyle);
                    if (endIndex < word.length()) {
                        sd.insertString(sd.getLength(), word.substring(endIndex), headlineStyle);
                    }
                } else {
                    sd.insertString(sd.getLength(), word, headlineStyle);
                }
            }
        } catch (BadLocationException ex) {
            Logger.getLogger(ObjectEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This function updates the headline field with the specified text printing
     * nick names in color.
     *
     * @param newText
     * @param appendEndSpace
     * @todo I first need to split the lines, then the words, otherwise I get
     * inter-OS issues with line breaks.
     */
    private void updateHeadline(String newText, boolean appendEndSpace) {
        StyledDocument sd = new DefaultStyledDocument();
        StringTokenizer st = new StringTokenizer(newText);
        try {
            if (!st.hasMoreTokens()) {
                // initialize the style even if there is nothing to show
                sd.insertString(sd.getLength(), " ", headlineStyle);
            } else {
                for (; st.hasMoreTokens();) {
                    insertWord(sd, st.nextToken());

                    /**
                     * Inserts a space if there are more words to come Note: The
                     * space character is used no matter what delimiter was in
                     * the original headline before loading it into the editor.
                     * We don't allow line breaks here.
                     */
                    if (st.hasMoreTokens()) {
                        sd.insertString(sd.getLength(), " ", headlineStyle);
                    }
                }
                if (appendEndSpace) {
                    sd.insertString(sd.getLength(), " ", headlineStyle);
                }
            }
        } catch (BadLocationException ex) {
            Logger.getLogger(ObjectEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
        HeadlineTextPane.setStyledDocument(sd);
        HeadlineTextPane.setCaretPosition(sd.getLength());
    }

    private boolean hasPrintables(String word) {
        boolean hasPrintables = false;
        char[] wordCharArray = word.toCharArray();
        for (Character c : wordCharArray) {
            if (!Character.isWhitespace(c)) {
                hasPrintables = true;
            }
        }
        return hasPrintables;
    }

    private boolean formatDescription(StyledDocument sd, String text) {
        boolean lineInserted = false;
        String delimiters = " \t\n\r\f";
        StringTokenizer delimitedTokenizer = new StringTokenizer(text, delimiters, true);
        try {
            if (!delimitedTokenizer.hasMoreTokens()) {
                // initialize the style even if there is nothing to show
                sd.insertString(sd.getLength(), " ", headlineStyle);
            } else {
                for (; delimitedTokenizer.hasMoreTokens();) {
                    String word = delimitedTokenizer.nextToken();
                    if (delimiters.contains(word)) {
                        // all delimiters in normal style
                        sd.insertString(sd.getLength(), word, headlineStyle);
                    } else if ((word.length() > 0 && hasPrintables(word))) {
                        insertWord(sd, word);
                        lineInserted = true;
                    }
                }
            }
            return lineInserted;
        } catch (BadLocationException ex) {
            Logger.getLogger(ObjectEditor.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    private void updateDescription(String newText) {
        StyledDocument sd = new DefaultStyledDocument();
        formatDescription(sd, newText);
        DescriptionTextPane.setStyledDocument(sd);
        DescriptionTextPane.setCaretPosition(sd.getLength());
    }

    /**
     * If the editObject is a newly created mlcObject that has not been
     * persisted to the client cache or the server then this call create a
     * proper persisted object and makes it the editObject.
     */
    private void checkForAndPersistNewObject(String headline, String description) {
        if (editObjects.size() == 1 && editObjects.get(0).getId() == mlcObject.NEW_OBJECT_ID) {
            CommandRecorder cr = CommandRecorder.getInstance();
            ObjectCreationCommand cmd = new ObjectCreationCommand(relative, editObjects.get(0).getClass(), headline, description);
            cr.scheduleCommand(cmd);
            editObjects.clear();
            editObjects.add(cmd.getObject());
            // in online mode the clients are notified by the server if new objects are created, in offline mode the following call takes care
            if (!OnlineManager.waitForServerMessages()) {
                ObjectChangeManager.objectCreated(editObjects.get(0));
            }
        }
    }

    private void saveChanges() {

        if (editObjects.size() == 1) {

            mlcObject editObject = editObjects.get(0);

            boolean isNew = editObject.getId() == mlcObject.NEW_OBJECT_ID;

            // remove leading spaces from headline and description - these were inserted to set the style only
            String headline = HeadlineTextPane.getText();
            if (!headline.isEmpty() && headline.substring(0, 1).equals(LEADING_SPACE)) {
                headline = headline.substring(1);
            }
            String description = DescriptionTextPane.getText();
            if (!description.isEmpty() && description.substring(0, 1).equals(LEADING_SPACE)) {
                description = description.substring(1);
            }
            checkForAndPersistNewObject(headline, description);
            editObject = editObjects.get(0);
            CommandRecorder cr = CommandRecorder.getInstance();

            if (!isNew) {
                // creation command also sets the headline and description field
                // therefore we only need TextUpdateCommand for changes
                cr.scheduleCommand(new TextUpdateCommand(editObject, headline, description));
            }
            if (editObject instanceof MlcImage) {
                MlcImage image = (MlcImage) editObject;
                cr.scheduleCommand(new ImageUpdateCommand(image, image.getIcon(), headline, MlsImage.ImageType.URL, ImageURL.getText()));
            }
            if (editObject instanceof MlcContainerMap) {
                FXMLController controller = FXMLController.getInstance();
                if (controller != null) {
                    MlViewDispatcherImpl.getInstance().display(editObject, MlObjectViewer.ViewType.ContainerMap);
                }
            }

        } else {
            // more than one objects are being edited, nothing to do here 
            // its all taken care of by the attribute editor
        }
        attributeEditor.saveState();
    }

    public void setRelative(mlcObject relative) {
        this.relative = relative;
    }

    private void checkAndHandleESCKey(KeyEvent evt) {
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                closeEditor();
                break;
        }
    }

    private void closeEditor() {
        setVisible(false);
        ObjectEditorLauncher.unregisterFromOpenEditors(this);
        dispose();
    }

    private void loadPreferences() {
        Preferences p = Preferences.userNodeForPackage(ObjectEditor.class);

        int width = p.getInt(EDITOR_WIDTH, 450);
        int height = p.getInt(EDITOR_HEIGHT, 300);

        int defaultPositionX = p.getInt(EDITOR_X_POSITION, -1);
        int defaultPositionY = p.getInt(EDITOR_Y_POSITION, -1);

        setSize(new Dimension(width, height));
        if (defaultPositionX != -1 && defaultPositionY != -1) {
            setLocation(defaultPositionX, defaultPositionY);
        } else {
            setLocationRelativeTo(MindlinerMain.getInstance());
        }
    }

    public void storePreferences() {
        Preferences p = Preferences.userNodeForPackage(ObjectEditor.class);
        p.putInt(EDITOR_WIDTH, getWidth());
        p.putInt(EDITOR_HEIGHT, getHeight());
        p.putInt(EDITOR_X_POSITION, getBounds().x);
        p.putInt(EDITOR_Y_POSITION, getBounds().y);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        HeadlinePanel = new javax.swing.JPanel();
        HeadlineLabel = new javax.swing.JLabel();
        HeadlineScroller = new javax.swing.JScrollPane();
        HeadlineTextPane = new javax.swing.JTextPane();
        DescriptionPanel = new javax.swing.JPanel();
        DescriptionLabel = new javax.swing.JLabel();
        DescriptionScoller = new javax.swing.JScrollPane();
        DescriptionTextPane = new javax.swing.JTextPane();
        AttributesPanel = new javax.swing.JPanel();
        SouthPanel = new javax.swing.JPanel();
        ImagePanel = new javax.swing.JPanel();
        ImageURLLabel = new javax.swing.JLabel();
        ImageURL = new javax.swing.JTextField();
        OKCancelButtonPanel = new javax.swing.JPanel();
        SaveButton = new javax.swing.JButton();
        AbortEditButton = new javax.swing.JButton();
        SignaturePanel = new javax.swing.JPanel();
        IconLabel = new javax.swing.JLabel();
        ObjectSignatureLabel = new javax.swing.JLabel();

        setMinimumSize(new java.awt.Dimension(600, 300));

        HeadlinePanel.setLayout(new java.awt.BorderLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/mindliner/resources/GeneralEditor"); // NOI18N
        HeadlineLabel.setText(bundle.getString("TextEditorHeadlineLabel")); // NOI18N
        HeadlineLabel.setOpaque(true);
        HeadlinePanel.add(HeadlineLabel, java.awt.BorderLayout.NORTH);

        HeadlineScroller.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(130, 130, 130)));
        HeadlineScroller.setPreferredSize(new java.awt.Dimension(2, 50));

        HeadlineTextPane.setBackground(new java.awt.Color(254, 254, 254));
        HeadlineTextPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        HeadlineTextPane.setToolTipText(bundle.getString("TextEditorHeadline_TT")); // NOI18N
        HeadlineTextPane.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                HeadlineTextPaneKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                HeadlineTextPaneKeyReleased(evt);
            }
        });
        HeadlineScroller.setViewportView(HeadlineTextPane);

        HeadlinePanel.add(HeadlineScroller, java.awt.BorderLayout.SOUTH);

        getContentPane().add(HeadlinePanel, java.awt.BorderLayout.NORTH);

        DescriptionPanel.setBackground(new java.awt.Color(223, 199, 175));
        DescriptionPanel.setLayout(new java.awt.BorderLayout());

        DescriptionLabel.setText(bundle.getString("TextEditorDescriptionLabel")); // NOI18N
        DescriptionLabel.setOpaque(true);
        DescriptionPanel.add(DescriptionLabel, java.awt.BorderLayout.NORTH);

        DescriptionScoller.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        DescriptionTextPane.setBackground(new java.awt.Color(254, 254, 254));
        DescriptionTextPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        DescriptionTextPane.setToolTipText(bundle.getString("TextEditorDescription_TT")); // NOI18N
        DescriptionTextPane.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DescriptionTextPaneKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                DescriptionTextPaneKeyReleased(evt);
            }
        });
        DescriptionScoller.setViewportView(DescriptionTextPane);

        DescriptionPanel.add(DescriptionScoller, java.awt.BorderLayout.CENTER);

        getContentPane().add(DescriptionPanel, java.awt.BorderLayout.CENTER);

        AttributesPanel.setBackground(new java.awt.Color(204, 204, 204));
        AttributesPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        AttributesPanel.setLayout(new java.awt.BorderLayout());

        SouthPanel.setOpaque(false);
        SouthPanel.setLayout(new java.awt.BorderLayout(3, 0));

        ImagePanel.setOpaque(false);
        ImagePanel.setLayout(new java.awt.BorderLayout());

        ImageURLLabel.setText(bundle.getString("TextEditorImageUrlLabel")); // NOI18N
        ImageURLLabel.setOpaque(true);
        ImagePanel.add(ImageURLLabel, java.awt.BorderLayout.WEST);

        ImageURL.setText("jTextField1");
        ImagePanel.add(ImageURL, java.awt.BorderLayout.CENTER);

        SouthPanel.add(ImagePanel, java.awt.BorderLayout.NORTH);

        OKCancelButtonPanel.setOpaque(false);
        OKCancelButtonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 1, 1));

        SaveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/3232/document_ok.png"))); // NOI18N
        SaveButton.setToolTipText(bundle.getString("EditorSaveChanges_TT")); // NOI18N
        SaveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaveButtonActionPerformed(evt);
            }
        });
        OKCancelButtonPanel.add(SaveButton);

        AbortEditButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/3232/document_delete.png"))); // NOI18N
        AbortEditButton.setToolTipText(bundle.getString("EditorAbortEditing_TT")); // NOI18N
        AbortEditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AbortEditButtonActionPerformed(evt);
            }
        });
        OKCancelButtonPanel.add(AbortEditButton);

        SouthPanel.add(OKCancelButtonPanel, java.awt.BorderLayout.EAST);

        SignaturePanel.setOpaque(false);
        SignaturePanel.setLayout(new javax.swing.BoxLayout(SignaturePanel, javax.swing.BoxLayout.LINE_AXIS));

        IconLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/3232/folder2_blue.png"))); // NOI18N
        SignaturePanel.add(IconLabel);

        ObjectSignatureLabel.setFont(new java.awt.Font("DejaVu Sans", 0, 10)); // NOI18N
        ObjectSignatureLabel.setText("object details");
        SignaturePanel.add(ObjectSignatureLabel);

        SouthPanel.add(SignaturePanel, java.awt.BorderLayout.CENTER);

        AttributesPanel.add(SouthPanel, java.awt.BorderLayout.SOUTH);

        getContentPane().add(AttributesPanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void HeadlineTextPaneKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_HeadlineTextPaneKeyReleased
        checkAndHandleESCKey(evt);
    }//GEN-LAST:event_HeadlineTextPaneKeyReleased

    private void HeadlineTextPaneKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_HeadlineTextPaneKeyPressed
        switch (evt.getKeyCode()) {

            case KeyEvent.VK_ENTER:
                evt.consume();
                saveChanges();
                closeEditor();
                break;

            case KeyEvent.VK_TAB:
                evt.consume();
                DescriptionTextPane.requestFocus();
                break;
        }
    }//GEN-LAST:event_HeadlineTextPaneKeyPressed

    private void DescriptionTextPaneKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DescriptionTextPaneKeyPressed
        switch (evt.getKeyCode()) {

            case KeyEvent.VK_TAB:
                evt.consume();
                HeadlineTextPane.requestFocus();
                break;
        }
    }//GEN-LAST:event_DescriptionTextPaneKeyPressed

    private void SaveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveButtonActionPerformed
        saveChanges();
        closeEditor();
    }//GEN-LAST:event_SaveButtonActionPerformed

    private void AbortEditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AbortEditButtonActionPerformed
        closeEditor();
    }//GEN-LAST:event_AbortEditButtonActionPerformed

    private void DescriptionTextPaneKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DescriptionTextPaneKeyReleased
        checkAndHandleESCKey(evt);
    }//GEN-LAST:event_DescriptionTextPaneKeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton AbortEditButton;
    private javax.swing.JPanel AttributesPanel;
    private javax.swing.JLabel DescriptionLabel;
    private javax.swing.JPanel DescriptionPanel;
    private javax.swing.JScrollPane DescriptionScoller;
    private javax.swing.JTextPane DescriptionTextPane;
    private javax.swing.JLabel HeadlineLabel;
    private javax.swing.JPanel HeadlinePanel;
    private javax.swing.JScrollPane HeadlineScroller;
    private javax.swing.JTextPane HeadlineTextPane;
    private javax.swing.JLabel IconLabel;
    private javax.swing.JPanel ImagePanel;
    private javax.swing.JTextField ImageURL;
    private javax.swing.JLabel ImageURLLabel;
    private javax.swing.JPanel OKCancelButtonPanel;
    private javax.swing.JLabel ObjectSignatureLabel;
    private javax.swing.JButton SaveButton;
    private javax.swing.JPanel SignaturePanel;
    private javax.swing.JPanel SouthPanel;
    // End of variables declaration//GEN-END:variables
}
