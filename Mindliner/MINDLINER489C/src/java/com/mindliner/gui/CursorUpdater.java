/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Frame;

/**
 * This class updates teh cursor on the specified component and on the outer
 * application frame.
 *
 * @author Marius Messerli
 */
public class CursorUpdater {

    public static void setCursor(Component component, Cursor cursor) {
        component.setCursor(cursor);
        Frame frame = getFrame(component);
        if (frame != null) {
            frame.setCursor(cursor);
        }
    }

    public static Frame getFrame(Component c) {
        if (c instanceof Frame) {
            return (Frame) c;
        }
        while ((c = c.getParent()) != null) {
            if (c instanceof Frame) {
                return (Frame) c;
            }
        }

        return null;
    }
}
