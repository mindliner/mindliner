/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.thread;

import com.mindliner.main.MindlinerMain;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class auto-saves the client application state at regular intervalls
 *
 * @author Marius Messerli
 */
public class AutoSaveScheduler {

    // auto-save intervall in ms
    private static final int AUTOSAVE_INTERVALL = 15 * 60 * 1000;
    private Timer timer = null;
    private static AutoSaveScheduler INSTANCE = null;

    public static AutoSaveScheduler getInstance() {
        synchronized (AutoSaveScheduler.class) {
            if (INSTANCE == null) {
                INSTANCE = new AutoSaveScheduler();
            }
        }
        return INSTANCE;
    }

    private AutoSaveScheduler() {
    }

    public void start() {
        if (timer != null) {
            stop();
        }
        timer = new Timer("Mindliner Auto-Saver");
        Date startTime = new Date();
        startTime.setTime(startTime.getTime() + AUTOSAVE_INTERVALL);
        timer.schedule(new AutoSave(), startTime, AUTOSAVE_INTERVALL);
        Logger.getLogger(AutoSaveScheduler.class.getName()).log(Level.INFO, "Auto-save scheduler started, saving every " + AUTOSAVE_INTERVALL / 1000 / 60 + " minutes");
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            Logger.getLogger(AutoSaveScheduler.class.getName()).log(Level.INFO, "Auto-save scheduler stopped");
        }
    }

    class AutoSave extends TimerTask {

        @Override
        public void run() {
            if (MindlinerMain.getInstance() != null && MindlinerMain.isInitialized()) {
                MindlinerMain.getInstance().persistApplicationState(false);
                Logger.getLogger(AutoSaveScheduler.class.getName()).log(Level.INFO, "Auto-save performed successfully");
            }

        }
    }

}
