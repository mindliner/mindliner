/*
 * FilterPreferences.java
 *
 * Created on 25.09.2007, 16:33:13
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.prefs;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.contentfilter.BaseFilter.SortingMode;
import com.mindliner.contentfilter.TimeFilter.TimePeriod;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * This class manages the search preferences. Note that some prefs need to be
 * client specific while others don't. This only affects useres who log into
 * more than one client on the same computer.
 *
 * @author messerli
 */
public class SearchPreferences {

    private static boolean isShowForeignElements = true;
    private static boolean isShowExpiredElements = false;
    private static boolean searchInFiles = false;
    private static boolean isShowPrivateElement = true;
    private static int maxDisplayRecords = 100;
    private static mlcObject oneThing = null;
    private static int searchDepth = 0;
    private static SortingMode defaultSorting = SortingMode.Modification;
    private static TimePeriod maxModificationAge = TimePeriod.All;
    private static Map<Integer, mlsConfidentiality> clientConfidentialities = new HashMap<>();
    private static final String DEFAULTSORTING_KEY = "searchFilterdefaultsorting";
    private static final String SHOWFOREIGN_KEY = "searchFiltershowforeign";
    private static final String SHOWPRIVATE_KEY = "searchFiltershowprivate";
    private static final String SHOWEXPIRED_KEY = "searchFiltershowexpired";
    private static final String SEARCH_IN_FILES_KEY = "searchFiltersearchinfiles";
    private static final String MAXRECORDS_KEY = "searchFiltermaxdisplayrecords";
    private static final String CONFIDENTIALITY_KEY = "searchFilterconfidentiality";
    private static final String MODIFICATIONAGE_KEY = "searchFilterModificationAge";
    private static final String ONE_THING_ID = "searchFilterOneThingId";
    private static final String SEARCH_DEPTH = "searchDepth";

    public static void loadPreferences() {
        Preferences userPrefs = Preferences.userNodeForPackage(SearchPreferences.class);
        int intValue;
        try {
            setDefaultSorting(SortingMode.valueOf(userPrefs.get(MlPreferenceManager.getFullyQualifiedPreferenceKey(DEFAULTSORTING_KEY), defaultSorting.toString())));
        } catch (IllegalArgumentException ie) {
            setDefaultSorting(SortingMode.Modification);
        }

        setShowForeignElements(userPrefs.getBoolean(MlPreferenceManager.getFullyQualifiedPreferenceKey(SHOWFOREIGN_KEY), isShowForeignElements));
        setShowPrivateElements(userPrefs.getBoolean(MlPreferenceManager.getFullyQualifiedPreferenceKey(SHOWPRIVATE_KEY), isShowPrivateElement));
        setShowExpired(userPrefs.getBoolean(MlPreferenceManager.getFullyQualifiedPreferenceKey(SHOWEXPIRED_KEY), isShowExpiredElements));
        setSearchInFiles(userPrefs.getBoolean(MlPreferenceManager.getFullyQualifiedPreferenceKey(SEARCH_IN_FILES_KEY), isSearchInFiles()));
        setMaxDisplayRecords(userPrefs.getInt(MlPreferenceManager.getFullyQualifiedPreferenceKey(MAXRECORDS_KEY), maxDisplayRecords));
        clientConfidentialities = ConfidentialityRegistryHandler.loadConfidentialities(CONFIDENTIALITY_KEY);

        try {
            maxModificationAge = TimePeriod.valueOf(userPrefs.get(MlPreferenceManager.getFullyQualifiedPreferenceKey(MODIFICATIONAGE_KEY), getMaxModificationAge().name()));
        } catch (IllegalArgumentException ex) {
            maxModificationAge = TimePeriod.All;
        }
        int oneThingId = userPrefs.getInt(MlPreferenceManager.getFullyQualifiedPreferenceKey(ONE_THING_ID), -1);
        if (oneThingId != -1) {
            oneThing = CacheEngineStatic.getObject(oneThingId);
        } else {
            oneThing = null;
        }
        searchDepth = userPrefs.getInt(MlPreferenceManager.getFullyQualifiedPreferenceKey(SEARCH_DEPTH), searchDepth);
    }

    public static void storePreferences() {
        Preferences userPrefs = Preferences.userNodeForPackage(SearchPreferences.class);
        int id;
        userPrefs.putBoolean(MlPreferenceManager.getFullyQualifiedPreferenceKey(SHOWFOREIGN_KEY), getShowForeignElements());
        userPrefs.putBoolean(MlPreferenceManager.getFullyQualifiedPreferenceKey(SHOWPRIVATE_KEY), getShowPrivateElements());
        userPrefs.putBoolean(MlPreferenceManager.getFullyQualifiedPreferenceKey(SHOWEXPIRED_KEY), getShowExpired());
        userPrefs.putBoolean(MlPreferenceManager.getFullyQualifiedPreferenceKey(SEARCH_IN_FILES_KEY), isSearchInFiles());
        userPrefs.putInt(MlPreferenceManager.getFullyQualifiedPreferenceKey(MAXRECORDS_KEY), getMaxDisplayRecords());
        userPrefs.put(MlPreferenceManager.getFullyQualifiedPreferenceKey(MODIFICATIONAGE_KEY), getMaxModificationAge().name());
        ConfidentialityRegistryHandler.storeConfidentialities(clientConfidentialities, CONFIDENTIALITY_KEY);

        userPrefs.put(MlPreferenceManager.getFullyQualifiedPreferenceKey(DEFAULTSORTING_KEY), getDefaultSorting().toString());

        if (oneThing == null) {
            userPrefs.remove(MlPreferenceManager.getFullyQualifiedPreferenceKey(ONE_THING_ID));
            System.out.println("no One Thing defined");
            
        } else {
            userPrefs.putInt(MlPreferenceManager.getFullyQualifiedPreferenceKey(ONE_THING_ID), oneThing.getId());
            System.out.println("loaded The One Thing : " + oneThing.getHeadline());
        }
        userPrefs.putInt(MlPreferenceManager.getFullyQualifiedPreferenceKey(SEARCH_DEPTH), searchDepth);
    }

    public static void setShowForeignElements(boolean b) {
        isShowForeignElements = b;
    }

    public static void setShowPrivateElements(boolean b) {
        isShowPrivateElement = b;
    }

    public static boolean getShowForeignElements() {
        return isShowForeignElements;
    }

    public static boolean getShowPrivateElements() {
        return isShowPrivateElement;
    }

    public static void setMaxDisplayRecords(int rec) {
        maxDisplayRecords = rec;
    }

    public static int getMaxDisplayRecords() {
        return maxDisplayRecords;
    }

    public static void setShowExpired(boolean b) {
        isShowExpiredElements = b;
    }

    public static boolean getShowExpired() {
        return isShowExpiredElements;
    }

    public static boolean isSearchInFiles() {
        return searchInFiles;
    }

    public static void setSearchInFiles(boolean searchInFiles) {
        SearchPreferences.searchInFiles = searchInFiles;
    }

    public static void setConfidentiality(mlsConfidentiality c, int clientId) {
        clientConfidentialities.put(clientId, c);
    }

    public static mlsConfidentiality getConfidentiality(int clientId) {
        return clientConfidentialities.get(clientId);
    }

    public static TimePeriod getMaxModificationAge() {
        return maxModificationAge;
    }

    public static void setMaxModificationAge(TimePeriod maxModificationAge) {
        SearchPreferences.maxModificationAge = maxModificationAge;
    }

    public static void setDefaultSorting(SortingMode ds) {
        defaultSorting = ds;
    }

    public static SortingMode getDefaultSorting() {
        return defaultSorting;
    }

    public static mlcObject getOneThing() {
        return oneThing;
    }

    public static void setOneThing(mlcObject oneThing) {
        SearchPreferences.oneThing = oneThing;
    }

    public static int getSearchDepth() {
        return searchDepth;
    }

    public static void setSearchDepth(int searchDepth) {
        SearchPreferences.searchDepth = searchDepth;
    }

}
