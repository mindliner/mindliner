/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui;

import com.mindliner.analysis.UriUtils;
import java.awt.Desktop;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

/**
 * This class evaluates if the caret sits on a URL and follows the hyperlink if the user ALT-clicks into a word.
 *
 * @author Marius Messerli
 */
public class TextPaneUriListener implements CaretListener, KeyListener {

    private final JTextPane textPane;
    private boolean controlDown = false;

    public TextPaneUriListener(JTextPane pane) {
        this.textPane = pane;
    }

    /**
     * Returns the word around the caret.
     *
     * @param location
     * @return The word within which the carret resides or an empty string if the caret is before a space or the text pane is empty
     */
    private String findSurroundingWord(int location) {
        try {
            StyledDocument sd = textPane.getStyledDocument();

            // if the caret is before or after all text we don't have a surrounding word
            if (location == 0) {
                return "";
            }
            if (location == sd.getLength()) {
                return "";
            }

            String docText = sd.getText(0, sd.getLength());
            if (docText.isEmpty()) {
                return "";
            }

            // scan upstream to white space before or start of text
            int start = Math.min(location, docText.length() - 1);

            // if the caret is before a space then we don't have any surrounding word, carest must be inside word
            if (Character.isWhitespace(docText.charAt(start))) {
                return "";
            }
            while (start > 0 && !Character.isWhitespace(docText.charAt(start))) {
                start--;
            }
            // move one step forward past the space
            if (Character.isWhitespace(docText.charAt(start))) {
                start++;
            }

            // scan downstream to next white space or end of text
            int end = Math.min(location, docText.length());
            while (end < docText.length() && !Character.isWhitespace(docText.charAt(end))) {
                end++;
            }
            return docText.substring(start, end);
        } catch (BadLocationException ex) {
            Logger.getLogger(TextPaneUriListener.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        if (controlDown) {
            if (Desktop.isDesktopSupported()) {
                String word = findSurroundingWord(e.getDot());
                URI uri = UriUtils.getAbsoluteUri(word);
                if (uri != null) {
                    UriUtils.openUri(uri);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Cannot launch external content - not supported", "Opening External Content", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        controlDown = e.isControlDown();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        controlDown = e.isControlDown();
    }
}
