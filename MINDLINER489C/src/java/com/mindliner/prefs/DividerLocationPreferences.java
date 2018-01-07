/*
 * DividerLocationPreferences.java
 *
 * Created on 25.09.2007, 15:32:36
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.prefs;

import java.util.prefs.Preferences;

/**
 *
 * @author Marius Messerli
 */
public class DividerLocationPreferences {

    private static final String MAIN_KEY = "dividerlocationmain";
    private static final String MIDDLE_BOTTOM_KEY = "dividerlocationmiddlebottom";
    private static final String TOOLBAR_CONTENT_KEY = "toolbarcontentdivider";
    
    private static int dividerLocationMain = 472;
    private static int dividerLocationMiddleBottom = 100;
    private static int toolbarContent = 350;

    public static void loadPreferences() {

        Preferences userPrefs = Preferences.userNodeForPackage(MlPreferenceManager.class);

        setDividerLocationMain(userPrefs.getInt(MAIN_KEY, dividerLocationMain));
        setDividerLocationMiddleBottom(userPrefs.getInt(MIDDLE_BOTTOM_KEY, dividerLocationMiddleBottom));
        setToolbarContent(userPrefs.getInt(TOOLBAR_CONTENT_KEY, toolbarContent));
    }

    public static void storePreferences() {
        Preferences userPrefs = Preferences.userNodeForPackage(MlPreferenceManager.class);
        userPrefs.putInt(MAIN_KEY, getDividerLocationMain());
        userPrefs.putInt(MIDDLE_BOTTOM_KEY, getDividerLocationMiddleBottom());
        userPrefs.putInt(TOOLBAR_CONTENT_KEY, getToolbarContent());
    }

    public static void setDividerLocationMain(int loc) {
        dividerLocationMain = loc;
    }

    public static int getDividerLocationMain() {
        return dividerLocationMain;
    }

    public static int getDividerLocationMiddleBottom() {
        return dividerLocationMiddleBottom;
    }

    public static void setDividerLocationMiddleBottom(int i) {
        dividerLocationMiddleBottom = i;
    }

    public static int getToolbarContent() {
        return toolbarContent;
    }

    public static void setToolbarContent(int toolbarContent) {
        DividerLocationPreferences.toolbarContent = toolbarContent;
    }

}
