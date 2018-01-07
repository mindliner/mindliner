/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.prefs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * This class holds the preferred locations of the file open/save dialog for the
 * specified types of I/O.
 *
 * @author Marius Messerli
 */
public class FileLocationPreferences {

    private static final String LOCATION_KEY = "ioloc";
    private static final String LOCATION_COUNT_KEY = "ioloccount";

    private static final Map<String, String> locationPrefMap = new HashMap<>();

    public static void storePreferences() {
        Preferences p = Preferences.userNodeForPackage(FileLocationPreferences.class);

        // first store all keys so we can load them again
        p.putInt(LOCATION_COUNT_KEY, locationPrefMap.size());
        List<String> keyList = new ArrayList<>(locationPrefMap.keySet());
        for (int i = 0; i < keyList.size(); i++) {
            String key = keyList.get(i);
            p.put(LOCATION_KEY + i, key);
        }

        // then store all key values
        for (String key : locationPrefMap.keySet()) {
            p.put(key, locationPrefMap.get(key));
        }
    }

    public static void loadPreferences() {
        locationPrefMap.clear();
        Preferences p = Preferences.userNodeForPackage(FileLocationPreferences.class);
        int keyCount = p.getInt(LOCATION_COUNT_KEY, 0);
        for (int i = 0; i < keyCount; i++) {
            String key = p.get(LOCATION_KEY + i, "");
            if (!key.isEmpty()) {
                String val = p.get(key, "");
                if (!val.isEmpty()) {
                    locationPrefMap.put(key, val);
                }
            }
        }
    }

    /**
     * Stores the specified value under the specified key.
     * @param key A key describing a use case for the File Dialog (PPTImport or PictureExport, or something)
     * @param value The file (mostly folder) location for that key.
     */
    public static void setLocation(String key, String value) {
        locationPrefMap.put(key, value);
    }

    /**
     * Returns a smart default for a file dialog.
     *
     * @param key The key of a location type that reflects a recurring use case for the dialog (PreferedImportLocation or PictureLocation) that you have stored before with setLocation
     * 
     * @return The folder location associated with the key
     */
    public static String getLocation(String key) {
        String loc = locationPrefMap.get(key);
        if (loc == null) {
            loc = System.getProperty("user.home");
        }
        return loc;
    }
}
