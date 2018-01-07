/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.prefs;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.categories.mlsConfidentiality;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Reads and Writes Client Confidentialities to/from Registry
 *
 * @author marius
 */
public class ConfidentialityRegistryHandler {

    private static Preferences getPreferenceHandle() {
        return Preferences.userNodeForPackage(CacheEngineStatic.class);
    }

    /**
     * Stores the specified confidentialities under the user-specific derivative
     * of the specified key
     *
     * @param confidentialities The map holding the client id as the key and the
     * confidentiality id as the value
     * @param registryKeyElement The specific portion of the registry key (must
     * be unique among all users of this class)
     */
    public static void storeConfidentialities(Map<Integer, mlsConfidentiality> confidentialities, String registryKeyElement) {
        String countKey = registryKeyElement + "count";
        String dataKey = registryKeyElement + "key";
        Preferences userPrefs = getPreferenceHandle();

        userPrefs.putInt(MlPreferenceManager.getFullyQualifiedPreferenceKey(countKey), confidentialities.size());
        Iterator idIterator = confidentialities.keySet().iterator();
        for (int i = 0; idIterator.hasNext(); i++) {
            Integer cid = (Integer) idIterator.next();
            String regKey = MlPreferenceManager.getFullyQualifiedPreferenceKey(dataKey) + "-" + Integer.toString(i);
            userPrefs.putInt(regKey, confidentialities.get(cid).getId());
        }
    }

    /**
     *
     * @param registryKeyElement The specific portion of the registry key (must
     * be unique among all users of this class)
     * @return A map holding the client id as the key and the confidentiality id
     * as the value
     */
    public static Map<Integer, mlsConfidentiality> loadConfidentialities(String registryKeyElement) {
        String countKey = registryKeyElement + "count";
        String dataKey = registryKeyElement + "key";
        Preferences userPrefs = getPreferenceHandle();
        Map<Integer, mlsConfidentiality> confidentialities = new HashMap<>();
        int confidentialityCount = userPrefs.getInt(MlPreferenceManager.getFullyQualifiedPreferenceKey(countKey), -1);
        for (int i = 0; i < confidentialityCount; i++) {
            int cid = userPrefs.getInt(MlPreferenceManager.getFullyQualifiedPreferenceKey(dataKey) + "-" + Integer.toString(i), -1);
            if (cid != -1) {
                mlsConfidentiality conf = CacheEngineStatic.getConfidentiality(cid);
                if (conf != null) {
                    confidentialities.put(conf.getId(), conf);
                }
            }
        }
        return confidentialities;
    }
}
