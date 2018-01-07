/*
 * FramePreferences.java
 *
 * Created on 1. April 2007, 15:54
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.mindliner.prefs;

import java.awt.Point;
import java.util.prefs.Preferences;

/**
 *
 * @author Marius Messerli
 */
public final class MindlinerFrame implements MlPreference{

    private boolean visible = true;
    private Point location = new Point(20, 20);
    private int sizeX = 400;
    private int sizeY = 400;
    private String preferenceKey;

    /**
     * Creates a new instance of FramePreferences
     * @param prefKey
     */
    public MindlinerFrame(String prefKey) {
        preferenceKey = prefKey;
    }

    public MindlinerFrame(String prefKey, int width, int height) {
        this(prefKey);
        setHeight(height);
        setWidth(width);
        loadPreferences();
    }

    public void setLocation(Point p) {
        location = p;
    }

    public void setLocationX(int x) {
        location.x = x;
    }

    public void setLocationY(int y) {
        location.y = y;
    }

    public Point getLocation() {
        return location;
    }

    public int getLocationX() {
        return location.x;
    }

    public int getLocationY() {
        return location.y;
    }

    public void setWidth(int x) {
        sizeX = x;
    }

    public void setHeight(int y) {
        sizeY = y;
    }

    public int getWidth() {
        return sizeX;
    }

    public int getHeight() {
        return sizeY;
    }

    public void setVisible(boolean b) {
        visible = b;
    }

    public boolean getVisible() {
        return visible;
    }

    public String getPreferenceKey() {
        return preferenceKey;
    }

    @Override
    public void storePreferences() {
        Preferences userPrefs = Preferences.userNodeForPackage(MlPreferenceManager.class);
        userPrefs.putInt(getPreferenceKey() + "height", getHeight());
        userPrefs.putInt(getPreferenceKey() + "width", getWidth());
        userPrefs.putInt(getPreferenceKey() + "locationx", getLocationX());
        userPrefs.putInt(getPreferenceKey() + "locationy", getLocationY());
        userPrefs.putBoolean(getPreferenceKey() + "visible", getVisible());
    }

    private void loadPreferences() {
        Preferences userPrefs = Preferences.userNodeForPackage(MlPreferenceManager.class);
        setVisible(userPrefs.getBoolean(getPreferenceKey() + "visible", getVisible()));
        setWidth(userPrefs.getInt(getPreferenceKey() + "width", getWidth()));
        setHeight(userPrefs.getInt(getPreferenceKey() + "height", getHeight()));
        setLocationX(userPrefs.getInt(getPreferenceKey() + "locationx", getLocationX()));
        setLocationY(userPrefs.getInt(getPreferenceKey() + "locationy", getLocationY()));
    }
}
