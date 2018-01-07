/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.system;

import com.mindliner.entities.Release;

/**
 * This class has application wide settings.
 *
 * @author Marius Messerli
 */
public class MlSessionClientParams {

    // we need a font that can display a wide range of unicode characters (Chinese, Hindu,...)
    public static final String DEFAULT_FONT_FAMILY = "Arial Unicode MS";
    public static final String DEFAULT_LOCALDOCS_DIRECTORY = ".mindliner";
    public static final Integer DATA_CACHE_FILE_VERSION = 2017091901;
    public static final Integer COLOR_CACHE_FILE_VERSION = 2017091901;

    private static int currentUserId = -1;
    
    private static String currentLoginName;

    public static String getFileExtension() {
        return Release.isDevelopmentState() ? ".dev" : ".mlr";
    }

    public static String getMindlinerLocalDocsPath() {
        return System.getProperty("user.home")
                + System.getProperty("file.separator")
                + DEFAULT_LOCALDOCS_DIRECTORY;
    }

    public static String getDataCacheFileName() {
        return "Cache" + DATA_CACHE_FILE_VERSION.toString() + getFileExtension();
    }

    public static String getColorCacheFileName() {
        return "Cache" + COLOR_CACHE_FILE_VERSION.toString() + getFileExtension();
    }

    public static String getCommandFileName() {
        return "Command" + DATA_CACHE_FILE_VERSION + getFileExtension();
    }

    public static void setCurrentLoginName(String currentLogin) {
        currentLoginName = currentLogin;
    }

    public static void setCurrentUserId(int userId) {
        currentUserId = userId;
    }

    /**
     * The user id is obtained from the cache file with the login name even if
     * the network if down and the user wants to work in offline mode.
     *
     * @return
     */
    public static int getCurrentUserId() {
        if (currentUserId == -1) {
            throw new IllegalStateException("CurrentUserId has not been initialized prior to using it.");
        }
        return currentUserId;
    }

    /**
     * The login name is the first thing the application learns about the person
     * who authorization is to be checked. Even if the network is down this can
     * be used to load the cache file if such a file exists.
     */
    public static String getCurrentLoginName() {
        return currentLoginName;
    }
}
