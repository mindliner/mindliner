/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.serveraccess;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.cache.MlCacheException;
import com.mindliner.cache.OnlineServicePriorityComparator;
import com.mindliner.commands.CommandRecorder;
import com.mindliner.main.LoginGUI;
import com.mindliner.serveraccess.OnlineService.OnlineStatus;
import com.mindliner.thread.SimpleSwingWorker;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 * This class manages all parts of the application that need to change their
 * behaviour depending on whether the software is in the online or offline
 * state.
 *
 * @author Marius Messerli
 */
public class OnlineManager {

    public static enum ExecutionMode {

        Realtime,
        Asynchronous
    }
    private ExecutionMode executionMode = ExecutionMode.Asynchronous;
    private final List<OnlineService> onlineServices = new ArrayList<>();
    private OnlineStatus onlineStatus = OnlineService.OnlineStatus.goingOnline;
    private StatusReporter statusReporter = null;
    private final String ASYNC_EXECUTION = "executionMode";
    private static OnlineManager INSTANCE = null;
    private String hostname = "";

    public static OnlineManager getInstance() {
        synchronized (OnlineManager.class) {
            if (INSTANCE == null) {
                INSTANCE = new OnlineManager();
            }
        }
        return INSTANCE;
    }
    private JLabel onlineLabel = null;

    public static void setOnlineLabel(JLabel label, String hostname) {
        INSTANCE.onlineLabel = label;
        INSTANCE.hostname = hostname;
        INSTANCE.setOnlineLabelStatus(isOnline());
    }

    public static void setStatusReporter(StatusReporter reporter) {
        getInstance().statusReporter = reporter;
    }

    public static boolean isOnline() {
        return INSTANCE.getOnlineStatus() == OnlineStatus.goingOnline || INSTANCE.onlineStatus == OnlineStatus.online;
    }

    public static void goOffline() {
        getInstance().goOfflineL();
    }

    /**
     * Puts Mindliner online. If the data was edited in offline mode then the
     * records are synchronized as part of this call (implicit call to
     * CommandRecorder.execute())
     */
    public static void goOnline() {
        INSTANCE.goOnlineL();
    }

    public static boolean waitForServerMessages() {
        return INSTANCE.waitForServerMessagesI();
    }

    private OnlineManager() {
    }

    private void goOfflineL() {
        // stops the updater thread
        CommandRecorder.getInstance().cancelCommandProcessor();
        boolean oneFailed = false;
        for (OnlineService o : onlineServices) {
            o.goOffline();
            if (o.getStatus() != OnlineStatus.offline) {
                oneFailed = true;
            }
        }
        if (!oneFailed) {
            setOnlineLabelStatus(false);
            onlineStatus = OnlineStatus.offline;

        } else {
            onlineStatus = OnlineStatus.goingOffline;
        }
    }

    public ExecutionMode getExecutionMode() {
        return executionMode;
    }

    public void setExecutionMode(ExecutionMode executionMode) {
        this.executionMode = executionMode;
        CommandRecorder cr = CommandRecorder.getInstance();
        cr.setExecutionMode(executionMode);
        if (executionMode.equals(ExecutionMode.Realtime)) {
            // do a one-time execution of the command queue. There might be still some commands in the queue
            cr.startCommandProcessor(false, statusReporter);
        }
    }

    /**
     * If true the client should wait for the server messages to come back after
     * object updates and control the GUI with those messages. If false the
     * client should make immediate GUI changes and not wait for server
     * messages.
     */
    public boolean waitForServerMessagesI() {
        if (!isOnline()) {
            return false;
        }
        switch (executionMode) {
            case Realtime:
                return true;
            case Asynchronous:
                return false;
            default:
                throw new AssertionError();
        }
    }

    private void setOnlineLabelStatus(boolean online) {
        if (onlineLabel != null) {
            if (online) {
                if (hostname.isEmpty()) {
                    onlineLabel.setText("Online");
                } else {
                    onlineLabel.setText("Connected to " + hostname);
                }
                onlineLabel.setToolTipText("Click to go offline.");
                onlineLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/3232/hard_drive_network_ok.png")));
            } else {
                onlineLabel.setText("Offline");
                onlineLabel.setToolTipText("Click to go online.");
                onlineLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/3232/hard_drive_network_error.png")));
            }
        }
    }

    /**
     * Going online can take a while and therefore this is delegated to a
     * background task.
     */
    private void goOnlineL() {
        OnlinerTask task = new OnlinerTask();
        task.execute();
    }

    public OnlineStatus getOnlineStatus() {
        return onlineStatus;
    }

    /**
     * Returns the number of registered services.
     */
    public int getServiceCount() {
        return onlineServices.size();
    }

    public void registerService(OnlineService service) {
        if (onlineServices.contains(service) == false) {
            onlineServices.add(service);
        }
    }

    public void unregisterService(OnlineService service) {
        onlineServices.remove(service);
    }

    /**
     * @todo If the user requested to see the login screen at the next login
     * (Utilities menu) then we must ensure that the online flag is set to true
     * no matter what the online status is.
     */
    public void storePreferences() {
        Preferences userPrefs = Preferences.userNodeForPackage(OnlineManager.class);
        if (executionMode == ExecutionMode.Asynchronous) {
            userPrefs.putBoolean(ASYNC_EXECUTION, true);
        } else {
            userPrefs.putBoolean(ASYNC_EXECUTION, false);
        }
    }

    public void loadPreferences() {
        Preferences userPrefs = Preferences.userNodeForPackage(OnlineManager.class);
        boolean isAsync = userPrefs.getBoolean(ASYNC_EXECUTION, false);
        if (isAsync) {
            executionMode = ExecutionMode.Asynchronous;
        } else {
            executionMode = ExecutionMode.Realtime;
        }
    }

    private void performGoOnline() {
        onlineStatus = OnlineStatus.goingOnline;
        // save the offline edits, if any, in case the online operation gets stuck
        CommandRecorder cr = CommandRecorder.getInstance();
        cr.store();

        int serviceCount = onlineServices.size();
        if (statusReporter != null) {
            statusReporter.startTask(0, serviceCount - 1, true, false);
        }

        if (!LoginGUI.isLoggedInOnline()) {
            // if user started ML in offline mode, we first need to login
            if (statusReporter != null) {
                statusReporter.setMessage("Logging in ..");
            }
            boolean success = LoginGUI.onlineLogin();
            if (!success) {
                statusReporter.done();
                onlineStatus = OnlineStatus.offline;
                return;
            }
        }

        boolean oneFailed = false;
        String serviceName = "";
        Collections.sort(onlineServices, new OnlineServicePriorityComparator());
        for (int i = 0; i < serviceCount; i++) {
            try {
                OnlineService service = (OnlineService) onlineServices.get(i);
                serviceName = service.getServiceName();
                if (statusReporter != null) {
                    statusReporter.setMessage("connecting " + service.getServiceName(), i);
                }
                service.goOnline();
                if (!service.getStatus().equals(OnlineStatus.online)) {
                    oneFailed = true;
                }
            } catch (MlCacheException ex) {
                if (serviceName.isEmpty()) {
                    serviceName = "Going Online";
                }
                JOptionPane.showMessageDialog(null, ex.getMessage(), serviceName, JOptionPane.ERROR_MESSAGE);
            }
        }

        try {
            if (statusReporter != null) {
                statusReporter.setMessage("performing cache maintenance ...");
            }
            CacheEngineStatic.performMaintenance(false);
        } catch (MlCacheException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Automatic Cache Maintenance", JOptionPane.WARNING_MESSAGE);
        }

        if (!oneFailed) {
            setOnlineLabelStatus(true);
            onlineStatus = OnlineStatus.online;
        } else {
            onlineStatus = OnlineStatus.goingOnline;
        }

        if (statusReporter != null) {
            statusReporter.done();
        }

        cr.setExecutionMode(executionMode);
        if (executionMode.equals(ExecutionMode.Realtime)) {
            // do a one-time execution of the command queue
            cr.startCommandProcessor(false, statusReporter);
        }
    }

    class OnlinerTask extends SimpleSwingWorker<Void, Void> {

        @Override
        protected Void doInBackground() throws Exception {
            performGoOnline();
            return null;
        }
    }
}
