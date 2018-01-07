/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.mindliner.cache;

import com.mindliner.categories.MlsNewsType;
import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.categories.mlsPriority;
import com.mindliner.managers.CategoryManagerRemote;
import com.mindliner.managers.SecurityManagerRemote;
import com.mindliner.serveraccess.OnlineService;
import com.mindliner.serveraccess.RemoteLookupAgent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.NamingException;

/**
 *
 * @author Marius Messerli
 */
public class CategoryCacheImpl implements CacheAgent, CategoryCache, OnlineService {

    CategoryManagerRemote categoryManager = null;
    SecurityManagerRemote securityManager = null;
    private OnlineStatus onlineStatus = OnlineStatus.offline;
    private Map<Integer, mlsPriority> priorities = null;
    private Map<Integer, mlsConfidentiality> confidentialities = null;
    private Map<Integer, MlsNewsType> messageTypes = null;
    private static final String CATEGORY_CACHE_NAME_EXTENSION = "category";
    private int connectionPriority = 0;

    public CategoryCacheImpl() {
        clearCache();
    }

    @Override
    public void initialize() throws MlCacheException {
        try {
            File f = new File(CacheEngineStatic.getDataCacheFilePath(CATEGORY_CACHE_NAME_EXTENSION));
            FileInputStream fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);
            loadCache(ois);
        } catch (IOException | ClassNotFoundException ex) {
            goOnline();
            reloadCacheData();
        }
    }

    @Override
    public String getServiceName() {
        return "Category Cache";
    }

    @Override
    public void goOffline() {
        categoryManager = null;
        securityManager = null;
        onlineStatus = OnlineStatus.offline;
    }

    @Override
    public void goOnline() throws MlCacheException {
        try {
            categoryManager = (CategoryManagerRemote) RemoteLookupAgent.getManagerForClass(CategoryManagerRemote.class);
            securityManager = (SecurityManagerRemote) RemoteLookupAgent.getManagerForClass(SecurityManagerRemote.class);
            onlineStatus = OnlineStatus.online;
        } catch (NamingException ex) {
            throw new MlCacheException(ex.getMessage());
        }
    }

    @Override
    public OnlineStatus getStatus() {
        return onlineStatus;
    }

    /**
     * Reloads the categories from the server; requires a functional connection
     * to the server.
     */
    private void reloadCacheData() {

        clearCache();

        List l = securityManager.getAllAllowedConfidentialities();
        for (Object o : l) {
            mlsConfidentiality c = (mlsConfidentiality) o;
            confidentialities.put(c.getId(), c);
        }

        l = categoryManager.getAllActionItemTypes();
        for (Object o : l) {
            MlsNewsType w = (MlsNewsType) o;
            messageTypes.put(w.getId(), w);
        }

        l = categoryManager.getAllPriorities();
        for (Object o : l) {
            mlsPriority p = (mlsPriority) o;
            priorities.put(p.getId(), p);
        }
    }

    private void clearCache() {
        if (priorities != null) {
            priorities.clear();
        } else {
            priorities = new HashMap<>();
        }
        if (confidentialities != null) {
            confidentialities.clear();
        } else {
            confidentialities = new HashMap<>();
        }
        if (messageTypes != null) {
            messageTypes.clear();
        } else {
            messageTypes = new HashMap<>();
        }
    }

    private void loadCache(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        priorities = (HashMap<Integer, mlsPriority>) ois.readObject();
        confidentialities = (HashMap<Integer, mlsConfidentiality>) ois.readObject();
        messageTypes = (HashMap<Integer, MlsNewsType>) ois.readObject();
    }

    @Override
    public void storeCache() throws MlCacheException {
        ObjectOutputStream oos = null;
        try {
            File f = new File(CacheEngineStatic.getDataCacheFilePath(CATEGORY_CACHE_NAME_EXTENSION));
            try (FileOutputStream fos = new FileOutputStream(f)) {
                oos = new ObjectOutputStream(fos);
                oos.writeObject(priorities);
                oos.writeObject(confidentialities);
                oos.writeObject(messageTypes);
            }
        } catch (IOException ex) {
            throw new MlCacheException("Could not store cache: " + ex.getMessage());
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
            } catch (IOException ex) {
                throw new MlCacheException("Could not store cache: " + ex.getMessage());
            }
        }
    }

    @Override
    public MlsNewsType getActionItemType(int id) {
        if (messageTypes != null) {
            return messageTypes.get(id);
        } else {
            reloadCacheData();
            return messageTypes.get(id);
        }
    }

    @Override
    public mlsConfidentiality getConfidentiality(int id) {
        if (confidentialities.size() > 0) {
            mlsConfidentiality conf = confidentialities.get(id);
            return conf;
        } else {
            reloadCacheData();
            return confidentialities.get(id);
        }
    }

    @Override
    public mlsConfidentiality getClosestConfidentialityForLevel(int level) {
        mlsConfidentiality returnValue = null;
        int delta = 10000;
        for (mlsConfidentiality c : getConfidentialities()) {
            int currentDelta = Math.abs(c.getClevel() - level);
            if (currentDelta < delta) {
                delta = currentDelta;
                returnValue = c;
            }
        }
        return returnValue;
    }

    @Override
    public mlsPriority getPriority(int id) {
        if (priorities != null) {
            return priorities.get(id);
        } else {
            reloadCacheData();
            return priorities.get(id);
        }
    }

    @Override
    public mlsPriority getPriority(String name) {
        Collection<mlsPriority> plist = priorities.values();
        for (mlsPriority p : plist) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    @Override
    public List<mlsConfidentiality> getConfidentialities() {
        if (confidentialities.isEmpty()) {
            throw new IllegalStateException("At least one confidentiality must exist.");
        }
        List l = new ArrayList<>();
        l.addAll(confidentialities.values());
        return l;
    }

    @Override
    public List<mlsConfidentiality> getConfidentialities(int clientId) {
        List<mlsConfidentiality> clientConfis = new ArrayList<>();
        for (mlsConfidentiality c : confidentialities.values()) {
            if (c.getClient().getId() == clientId) {
                clientConfis.add(c);
            }
        }
        return clientConfis;
    }

    @Override
    public List<mlsPriority> getPriorities() {
        if (priorities.isEmpty()) {
            throw new IllegalStateException("At least one task priority must exist");
        }
        List l = new ArrayList<>();
        l.addAll(priorities.values());
        return l;
    }

    @Override
    public List<MlsNewsType> getActionItemTypes() {
        if (messageTypes.isEmpty()) {
            throw new IllegalStateException("At least one message type must exist");
        }
        List l = new ArrayList<>();
        l.addAll(messageTypes.values());
        return l;
    }

    @Override
    public void performOnlineCacheMaintenance(boolean forced) {
        reloadCacheData();
    }

    @Override
    public int getConnectionPriority() {
        return connectionPriority;
    }

    @Override
    public void setConnectionPriority(int priority) {
        connectionPriority = priority;
    }

}
