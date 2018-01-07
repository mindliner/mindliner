/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui;

/**
 * This interfaces defines an object that is capable of displaying and editing
 * the text attributes headline and description of Mindliner objects.
 *
 * @author Marius Messerli
 */
public interface TextDisplayEditorContainer {

    // this is used to show that the object has not been persisted to the server
    public static final int UNPERSISTED_OBJECT_ID = -1;
    
    /**
     * Switches the session from display to editing mode.
     */
    public void startEditing();

    /**
     * Aborts the current editing session without making any changes to the
     * object.
     */
    public void abortEditing();

    /**
     * Ends the editing session.
     */
    public void endEditing();
    
    /**
     * Locally stores any prefs inbetween sessions.
     */
    public void storePreferences();
}
