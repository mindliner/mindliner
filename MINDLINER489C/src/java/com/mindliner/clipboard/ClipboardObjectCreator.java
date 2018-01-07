/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.clipboard;

import com.mindliner.clientobjects.mlcKnowlet;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.commands.CommandRecorder;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates a list of objects which will be suggested for creation to the user after
 * he/she pasted clipboard content into mindliner (i.e. pressed CTRL + V)
 * 
 * @author Dominic Plangger
 */
public class ClipboardObjectCreator {
    
    public static final int MAX_OBJECTS = 10;
    
    public static List<mlcObject> createObjects(List<TextUnit> textUnits) {
        List<mlcObject> result = new ArrayList<>();
        if (textUnits == null || textUnits.isEmpty()) {
            return result;
        }
        // case: size = 1: set text unit either as description or headline
        if (textUnits.size() == 1) {
            mlcObject o = createDefaultObject();
            TextUnit u = textUnits.get(0);
            if (u.isIsHeadline()) {
                o.setHeadline(u.getText());
            }
            else {
                o.setDescription(u.getText());
                String headline = suggestHeadline(u.getText());
                o.setHeadline(headline);
            }
            result.add(o);
            return result;
        }
        // case: size > 1: if current is headline and next is description -> take them together into one object.
        // If current and next is headline, create one object with headline of current
        // if current is description, create on object with text of current and leave headline empty
        for (int i = 0; i < textUnits.size(); i++) {
            TextUnit curr = textUnits.get(i);
            mlcObject o = createDefaultObject();
            if (curr.isIsHeadline()) {
                o.setHeadline(curr.getText());
                if (i+1 < textUnits.size()) {
                    TextUnit next = textUnits.get(i + 1);
                    if (!next.isIsHeadline()) {
                        o.setDescription(next.getText());
                        i++;
                    }
                }
            }
            else {
                o.setDescription(curr.getText());
                String headline = suggestHeadline(curr.getText());
                o.setHeadline(headline);
            }
            result.add(o);
            
            if (result.size() >= MAX_OBJECTS) {
                break;
            }
        }
        return result;
    }
    
    private static mlcObject createDefaultObject() {
        CommandRecorder cr = CommandRecorder.getInstance();
        int tempId = cr.getTemporaryId();
        mlcKnowlet k = new mlcKnowlet();
        k.setId(tempId);
        return k;
    }

    private static String suggestHeadline(String text) {
        // TODO: is there a way to create a meaningful headline out of a short description text?
        return "";
    }
    
}
