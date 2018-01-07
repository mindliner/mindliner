/*
 * Mainprefs.java
 *
 * Created on 18. Januar 2006, 15:48
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package com.mindliner.prefs;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.cache.DefaultObjectAttributes;
import com.mindliner.entities.Release;
import com.mindliner.gui.font.FontPreferences;
import com.mindliner.system.MlSessionClientParams;
import com.mindliner.thread.AutoSaveScheduler;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.Preferences;

/**
 * This class triggers the storage of preferences for (some) ML subsystems.
 * These preferences are loaded very early in the boot phase and you cannot
 * assume that any subsystem has been initialized at this stage.
 *
 *
 * @author Marius Messerli
 */
public class MlPreferenceManager {

    public static MindlinerFrame desktopPrefs;
    private static final String VERSION_KEY = "clientversion";
    private static boolean runCacheMaintenance = false;
    private final static String AUTO_SAVE = "autosave";
    private static boolean autosave = true;

    private static final Set<MlPreference> preferences = new HashSet<>();

    /**
     * Returns a client- and user-specific String that starts with the specified
     * base key.
     *
     * @param baseKey The specific preference key.
     */
    public static String getFullyQualifiedPreferenceKey(String baseKey) {
        return baseKey + "-" + CacheEngineStatic.getCurrentUser().getLoginName();
    }

    public static MindlinerFrame getDesktopPrefs() {
        return desktopPrefs;
    }

    public static void setDesktopPrefs(MindlinerFrame desktopPrefs) {
        MlPreferenceManager.desktopPrefs = desktopPrefs;
    }

    public static void initializePreferences() {
        desktopPrefs = new MindlinerFrame("desktop", 1306, 879);
        registerPreference(desktopPrefs);
        // make sure that the directory for local documents exists
        (new File(MlSessionClientParams.getMindlinerLocalDocsPath())).mkdir();
        Preferences p = Preferences.userNodeForPackage(MlPreferenceManager.class);
        int v = p.getInt(VERSION_KEY, 0);
        autosave = p.getBoolean(AUTO_SAVE, true);
        if (autosave) {
            AutoSaveScheduler.getInstance().start();
        } else {
            AutoSaveScheduler.getInstance().stop();
        }

        // if this is the first time a new version is executed then set the flag to update the cache
        if (v < Release.VERSION_NUMBER) {
            runCacheMaintenance = true;
        }
        FileLocationPreferences.loadPreferences();
        FontPreferences.loadPreferences();
    }

    /**
     * Stores the preferences of the application
     *
     * @todo Some prefs classes are static so their storePreferences() is called
     * statically, others are dynamic and registered and are stored in the loop
     */
    public static void storePreferences() {
        Preferences p = Preferences.userNodeForPackage(MlPreferenceManager.class);
        p.putInt(VERSION_KEY, Release.VERSION_NUMBER);
        p.putBoolean(AUTO_SAVE, autosave);

        for (MlPreference pref : preferences) {
            pref.storePreferences();
        }
        desktopPrefs.storePreferences();
        DefaultObjectAttributes.storePreferences();
        SearchPreferences.storePreferences();
        DividerLocationPreferences.storePreferences();
        FileLocationPreferences.storePreferences();
        FontPreferences.storePreferences();
    }

    public static boolean isRunCacheMaintenance() {
        return runCacheMaintenance;
    }

    public static void registerPreference(MlPreference pref) {
        preferences.add(pref);
    }

    public static boolean isAutosave() {
        return autosave;
    }

    public static void setAutosave(boolean autosave) {
        MlPreferenceManager.autosave = autosave;
    }

}
