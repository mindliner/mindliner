/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.clipboard;

/**
 * Corresponds to a piece of text of the clipboard content. Can either be a headline or a description 
 * 
 * @author Dominic Plangger
 */
public class TextUnit {
    
    private String text;
    private boolean isHeadline;

    public TextUnit(String text, boolean isHeadline) {
        this.text = text;
        this.isHeadline = isHeadline;
    }

    public String getText() {
        return text;
    }

    public boolean isIsHeadline() {
        return isHeadline;
    }
    
}
