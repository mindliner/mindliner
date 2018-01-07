/*
 * ObjectDefaults.java
 *
 * Created on 12.08.2007, 00:40:35
 */
package com.mindliner.cache;

import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.categories.mlsPriority;
import com.mindliner.entities.MlUserPreferences;
import com.mindliner.entities.MlObjectDefaultsConfidentialities;
import com.mindliner.managers.UserManagerRemote;
import com.mindliner.objects.transfer.MltObjectDefaultConfidentialities;
import com.mindliner.prefs.ConfidentialityRegistryHandler;
import com.mindliner.prefs.MlPreferenceManager;
import com.mindliner.serveraccess.OnlineManager;
import com.mindliner.serveraccess.RemoteLookupAgent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.naming.NamingException;

/**
 * This class manages preferences and defaults for Mindliner objectcs.
 *
 * @author Marius Messerli
 */
public class DefaultObjectAttributes {

    private static final String CONFIDENTIALITY_KEY = "confidentiality";
    private static final String CLIENT_KEY = "client";
    private static final String PRIORITY_KEY = "priority";
    private static final String PRIVACY_KEY = "privacy";
    private static final String ADOPTLAST_KEY = "adoptlast";

    private static boolean adoptLastEdit = false;

    // there is one confidentiality default per client
    private static Map<Integer, mlsConfidentiality> clientConfidentialityDefaults = new HashMap<>();
    private static int dataPoolId = -1;
    private static mlsPriority priority = null;
    private static boolean privateAccess = false;

    public static mlsConfidentiality getConfidentiality(int clientId) {
        mlsConfidentiality result = clientConfidentialityDefaults.get(clientId);
        if (result == null) {
            List<mlsConfidentiality> clientConfidentialities = CacheEngineStatic.getConfidentialities(clientId);
            if (clientConfidentialities != null && !clientConfidentialities.isEmpty()) {
                result = clientConfidentialities.get(0);
                clientConfidentialityDefaults.put(clientId, result);
            }
        }
        return result;
    }

    public static mlsConfidentiality getConfidentialityForDefaultClient() {
        return getConfidentiality(getDataPoolId());
    }

    public static void setConfidentiality(mlsConfidentiality conf) {
        clientConfidentialityDefaults.put(conf.getClient().getId(), conf);
    }

    public static void updateConfidentiality(mlsConfidentiality newConfidentiality) {
        if (adoptLastEdit) {
            setConfidentiality(newConfidentiality);
        }
    }

    public static void setPriority(mlsPriority p) {
        priority = p;
    }

    public static void updatePriority(mlsPriority newPriority) {
        if (adoptLastEdit) {
            setPriority(newPriority);
        }
    }

    public static mlsPriority getPriority() {
        return priority;
    }

    public static void setPrivateAccess(boolean b) {
        privateAccess = b;
    }

    public static void updatePrivateAccess(boolean newState) {
        if (adoptLastEdit) {
            setPrivateAccess(newState);
        }
    }

    public static boolean getPrivateAccess() {
        return privateAccess;
    }

    public static int getDataPoolId() {
        return dataPoolId;
    }

    public static void setDataPoolId(int clientId) {
        DefaultObjectAttributes.dataPoolId = clientId;
    }

    public static void updateDataPoolId(int dataPoolId) {
        if (adoptLastEdit) {
            setDataPoolId(dataPoolId);
        }
    }

    public static void loadPreferences() {

        Preferences userPrefs = Preferences.userNodeForPackage(CacheEngineStatic.class);

        dataPoolId = userPrefs.getInt(MlPreferenceManager.getFullyQualifiedPreferenceKey(CLIENT_KEY), dataPoolId);
        if (dataPoolId == -1) {
            // let the software throw an NPE if there are no clients for the user
            dataPoolId = CacheEngineStatic.getCurrentUser().getClientIds().get(0);
        }
        if (dataPoolId == -1) {
            throw new IllegalStateException("Cannot determine the default client for new objects");
        }

        int pid = userPrefs.getInt(MlPreferenceManager.getFullyQualifiedPreferenceKey(PRIORITY_KEY), -1);
        if (pid != -1) {
            priority = CacheEngineStatic.getPriority(pid);
        }
        if (priority == null) {
            List<mlsPriority> plist = CacheEngineStatic.getPriorities();
            priority = plist.get(0);
        }
        privateAccess = userPrefs.getBoolean(MlPreferenceManager.getFullyQualifiedPreferenceKey(PRIVACY_KEY), privateAccess);
        adoptLastEdit = userPrefs.getBoolean(MlPreferenceManager.getFullyQualifiedPreferenceKey(ADOPTLAST_KEY), adoptLastEdit);
        clientConfidentialityDefaults = ConfidentialityRegistryHandler.loadConfidentialities(CONFIDENTIALITY_KEY);

        // if online re-load from server
        if (OnlineManager.isOnline()) {
            try {
                UserManagerRemote umr = (UserManagerRemote) RemoteLookupAgent.getManagerForClass(UserManagerRemote.class);
                MlUserPreferences up = umr.getUserPreferences(CacheEngineStatic.getCurrentUser().getId());
                if (up != null) {
                    clientConfidentialityDefaults.clear();
                    privateAccess = up.getPrivateflag();
                    priority = up.getPriority();
                    if (up.getDataPool() != null) {
                        dataPoolId = up.getDataPool().getId();
                        for (MlObjectDefaultsConfidentialities oc : up.getObjectDefaultsConfidentialities()) {
                            mlsConfidentiality conf = oc.getConfidentiality();
                            if (conf != null) {
                                clientConfidentialityDefaults.put(conf.getClient().getId(), conf);
                            }
                        }
                    }
                }
            } catch (NamingException ex) {
                Logger.getLogger(DefaultObjectAttributes.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void storePreferences() {

        // store the defaults in the registry in case we are offline now of later
        Preferences userPrefs = Preferences.userNodeForPackage(CacheEngineStatic.class);
        ConfidentialityRegistryHandler.storeConfidentialities(clientConfidentialityDefaults, CONFIDENTIALITY_KEY);
        if (priority != null) {
            userPrefs.putInt(MlPreferenceManager.getFullyQualifiedPreferenceKey(PRIORITY_KEY), priority.getId());
        }
        userPrefs.putBoolean(MlPreferenceManager.getFullyQualifiedPreferenceKey(PRIVACY_KEY), privateAccess);
        userPrefs.putBoolean(MlPreferenceManager.getFullyQualifiedPreferenceKey(ADOPTLAST_KEY), adoptLastEdit);
        if (OnlineManager.isOnline()) {

            // also store it on the server for consistency between desktop and web app
            try {
                UserManagerRemote umr = (UserManagerRemote) RemoteLookupAgent.getManagerForClass(UserManagerRemote.class);
                List<MltObjectDefaultConfidentialities> confis = new ArrayList<>();
                for (Integer cid : clientConfidentialityDefaults.keySet()) {
                    mlsConfidentiality clientConf = clientConfidentialityDefaults.get(cid);
                    if (clientConf != null) {
                        MltObjectDefaultConfidentialities confi = new MltObjectDefaultConfidentialities();
                        confi.setConfidentialityId(clientConf.getId());
                        confi.setDataPoolId(cid);
                        confis.add(confi);
                    }
                }
                umr.setObjectDefaults(CacheEngineStatic.getCurrentUser().getId(), dataPoolId, privateAccess, priority.getId(), confis);
            } catch (NamingException ex) {
                Logger.getLogger(DefaultObjectAttributes.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static boolean isAdoptLastEdit() {
        return adoptLastEdit;
    }

    public static void setAdoptLastEdit(boolean adoptLastEdit) {
        DefaultObjectAttributes.adoptLastEdit = adoptLastEdit;
    }
}
