/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.events;

import com.mindliner.main.MindlinerMain;
import com.mindliner.managers.UserManagerRemote;
import com.mindliner.serveraccess.OnlineManager;
import com.mindliner.serveraccess.RemoteLookupAgent;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;

/**
 * The task that contacts the server at regular intervalls to show I am alive
 * and to fetch stuff from the server.
 * @author Marius Messerli
 */
public class HeartBeatTask extends TimerTask{
    
        private static final int HEARTBEAT_TIMEOUT = 10 * 1000; // ms
   

        private HeartBeatThread thread = null;
        private boolean forcedOfflineMode = false;

        /**
         * indicates whether the HeartBeatTask forced Mindliner to go offline
         * (i.e. when server could not be reached anymore) or the user forced
         * Mindlinr to go offline
         *
         * @return
         */
        public boolean isForcedOfflineMode() {
            return forcedOfflineMode;
        }

        public void setForcedOfflineMode(boolean forcedOfflineMode) {
            this.forcedOfflineMode = forcedOfflineMode;
        }


        @Override
        public void run() {
            if (thread == null || !thread.isAlive()) {
                thread = new HeartBeatThread();
                thread.start();
            }

            try {
                thread.join(HEARTBEAT_TIMEOUT);
                if (thread.isAlive() || thread.isHasError()) {
                    // go offline silently if last heartbeat is still hangig or had an error
                    if (OnlineManager.isOnline()) {
                        Logger.getLogger(MindlinerMain.class.getName()).log(Level.INFO, "TimerTask: initiated going offline...");
                        forcedOfflineMode = true;
                        OnlineManager.goOffline();
                        // invalidate all ejb references.
                        // we don't know the exact reason for going offline, but if the reason is a server restart,
                        // we would need to lookup all ejb references again otherwise a NoSuchEJBException is thrown
                        RemoteLookupAgent.clear();
                    }
                } else if (!OnlineManager.isOnline() && forcedOfflineMode) {
                    Logger.getLogger(MindlinerMain.class.getName()).log(Level.INFO, "TimerTask: initiated going online...");
                    OnlineManager.goOnline();
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(MindlinerMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * We do heartbeat in a background thread as it may take up to 60s to
     * timeout at userManager.heartBeat() if server is not reachable.
     */
    class HeartBeatThread extends Thread {

        private boolean hasError = false;

        public boolean isHasError() {
            return hasError;
        }

        @Override
        public void run() {
            try {
                UserManagerRemote userManager = (UserManagerRemote) RemoteLookupAgent.getManagerForClass(UserManagerRemote.class);
                userManager.heartBeat();
            } catch (NamingException ex) {
                Logger.getLogger(MindlinerMain.class.getName()).log(Level.INFO, null, ex);
                hasError = true;
            }
        }
    }
