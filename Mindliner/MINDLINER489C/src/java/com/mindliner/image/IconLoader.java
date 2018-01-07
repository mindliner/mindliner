/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.image;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.MlcImage;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.managers.ObjectManagerRemote;
import com.mindliner.serveraccess.OnlineManager;
import com.mindliner.serveraccess.RemoteLookupAgent;
import com.mindliner.view.MindlinerMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class providing asynchronous Icon loading. 
 * Should be used whenever a new object should be displayed on the map. The Icon Loader avoids any
 * delays for fetching the object icons from the server as it loads them asynchronously.
 * @author Dominic Plangger
 */
public class IconLoader {
    
    private static IconLoader INSTANCE = null;
    private Timer timer = null;
    private Map<Integer, ObjecCallbackTuple> workload = null;
    private static final int SCHEDULING_INT = 300; // ms
    private MindlinerMapper mapper = null;
    private FinishCallback callback = null;
    
    public interface FinishCallback {
        public void onFinish(mlcObject obj);
    }
    
    public static class ObjecCallbackTuple {
        public final mlcObject object;
        public final FinishCallback callback;

        public ObjecCallbackTuple(mlcObject object, FinishCallback callback) {
            this.object = object;
            this.callback = callback;
        }
    }
    
    public static IconLoader getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new IconLoader();
        }
        return INSTANCE;
    }

    public IconLoader() {
        timer = new Timer();
        workload = new HashMap<>();
        timer.schedule(new IconLoadingTask(), 0, SCHEDULING_INT);
    }

    public void setMapper(MindlinerMapper mapper) {
        this.mapper = mapper;
    }

    public void setCallback(FinishCallback callback) {
        this.callback = callback;
    }
    
    /**
     * Adds the given objects to the list of objects for which the icons should be loaded.
     * Will return immediately. Icons will be loaded eventually.
     * @param objects 
     */
    public void loadIcons(Set<mlcObject> objects) {
        List<ObjecCallbackTuple> ocbs = new ArrayList<>();
        for (mlcObject o : objects) {
            FinishCallback cb = (mlcObject obj) -> {mapper.repaint();};
            ObjecCallbackTuple ocb = new ObjecCallbackTuple(o, cb);
            ocbs.add(ocb);
        }
        loadIcons(ocbs);
    }
    
    public synchronized void loadIcons(List<ObjecCallbackTuple> ocbs) {
        if (ocbs != null && OnlineManager.isOnline()) {
            for (ObjecCallbackTuple ocb : ocbs) {
                if (ocb.object != null) {
                    workload.put(ocb.object.getId(), ocb);
                }
            }
        }
    }
    
    public void loadIcons(mlcObject object) {
        Set<mlcObject> list = new HashSet<>();
        list.add(object);
        loadIcons(list);
    }
    
    private class IconLoadingTask extends TimerTask {

        @Override
        public void run() {
            if (!workload.isEmpty() && OnlineManager.isOnline()) {
                try {
                    ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
                    Map<Integer, ObjecCallbackTuple> objs;
                    // copy map. That avoids any thread interference while serializing object in the next remote call
                    synchronized (IconLoader.this) {
                        objs = new HashMap<>(workload);
                    }
                    Set<Integer> arg = new HashSet<>(objs.keySet());
                    Map<Integer, List<Integer>> map = omr.getObjectIcons(arg);
                    synchronized (IconLoader.this) {
                        map.entrySet().stream().forEach((entry) -> {
                            ObjecCallbackTuple ocb = workload.get(entry.getKey());
                            List<MlcImage> icons = CacheEngineStatic.getIcons(entry.getValue());
                            ocb.object.setIcons(icons);
                            workload.remove(entry.getKey());
                        });
                    }
                    for (ObjecCallbackTuple ocb : objs.values()) {
                        ocb.callback.onFinish(ocb.object);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(IconLoader.class.getName()).log(Level.SEVERE, null, ex);
                    synchronized (IconLoader.this) {
                        workload.clear();
                    }
                }
            }
        }
        
    }
    
}
