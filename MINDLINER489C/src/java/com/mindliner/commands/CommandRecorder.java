/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.commands;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.serveraccess.OnlineManager;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.UndoAlreadyUndoneException;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.importer.MlTextTransfer;
import com.mindliner.main.MindlinerMain;
import com.mindliner.serveraccess.MessageTrafficControl;
import com.mindliner.serveraccess.OnlineManager.ExecutionMode;
import com.mindliner.serveraccess.StatusReporter;
import com.mindliner.system.MlSessionClientParams;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.swing.JOptionPane;

/**
 * Note: This class does not implement the interface OnlineService and is
 * therefore not registered with the OnlineManager because we need more precise
 * control over when commands are synched back to the server.
 *
 * @author Marius Messerli
 */
public class CommandRecorder {

    private static CommandRecorder INSTANCE = null;
    private final ConcurrentLinkedQueue<MindlinerCommand> commandQueue = new ConcurrentLinkedQueue<>();
    private final List<MindlinerCommand> sessionUndoCommands = new ArrayList<>();
    private final List<UndoObserver> undoObservers = new ArrayList<>();
    private CommandEditor commandEditor = null;
    private Integer nextTemporaryId;
    private AsynchCommandProcessor commandProcessor = null;
    private final Map<Integer, Integer> idmap = new HashMap<>();
    private MessageTrafficControl trafficControl = null;
    private ExecutionMode executionMode = ExecutionMode.Asynchronous;
    private Timer timer = null;
    private int transactionId = 0;
    private final Map<Integer, List<MindlinerCommand>> transactions = new HashMap<>();

    public static CommandRecorder getInstance() {
        synchronized (CommandRecorder.class) {
            if (INSTANCE == null) {
                INSTANCE = new CommandRecorder();
            }
        }
        return INSTANCE;
    }

    /**
     * Creates a new transaction, i.e. a sequence of commands that are committed
     * when committTransaction() is called
     *
     * @return The transaction Id
     */
    public int beginTransaction() {
        List<MindlinerCommand> transactionCommands = new ArrayList<>();
        transactions.put(++transactionId, transactionCommands);
        return transactionId;
    }

    public void committTransaction(int transactionId) {
        List<MindlinerCommand> cmds = transactions.get(transactionId);
        if (cmds != null) {
            for (MindlinerCommand c : cmds) {
                scheduleCommand(c);
            }
            transactions.remove(transactionId);
        }
    }

    public void addToTransaction(int transactionId, MindlinerCommand c) {
        List<MindlinerCommand> cmds = transactions.get(transactionId);
        if (cmds != null) {
            cmds.add(c);
        }
    }

    private CommandRecorder() {
        assert (mlcObject.NEW_OBJECT_ID < 0);
        nextTemporaryId = mlcObject.NEW_OBJECT_ID - 1;
        loadPreviousCommandList();
    }

    public void setCommandEditor(CommandEditor commandEditor) {
        this.commandEditor = commandEditor;
        updateCommandEditor();
    }

    public void cancelCommandProcessor() {
        if (commandProcessor != null) {
            commandProcessor.cancelNow();
        }
    }

    public void startCommandProcessor(boolean periodically, StatusReporter statusBar) {
        if (timer == null) {
            timer = new Timer("Mindliner Async Command Processor");
        }
        cancelCommandProcessor();
        commandProcessor = new AsynchCommandProcessor(statusBar);
        if (periodically) {
            timer.schedule(commandProcessor, 0, 500);
        } else {
            timer.schedule(commandProcessor, 0);
        }
    }

    public void setExecutionMode(ExecutionMode newExecutionMode) {
        this.executionMode = newExecutionMode;
        switch (newExecutionMode) {
            case Realtime:
                cancelCommandProcessor();
                break;
            case Asynchronous:
                startCommandProcessor(true, null);
                break;
            default:
                throw new AssertionError();
        }
    }

    /**
     * While in offline mode this command adds the specified command to the
     * list. In online mode the command is executed immediately.
     *
     * @param c
     */
    public void scheduleCommand(MindlinerCommand c) {
        if (OnlineManager.isOnline() == false) {
            commandQueue.add(c);
            addSessionUndoCommand(c);
            updateCommandEditor();
        } else {
            switch (executionMode) {
                case Realtime:
                    try {
                        c.execute();
                        addSessionUndoCommand(c);
                    } catch (mlModifiedException ex) {
                        MlTextTransfer mtt = new MlTextTransfer();
                        mtt.setClipboardContents("Headline : " + c.getObject().getHeadline() + ". Description : " + c.getObject().getDescription());
                        JOptionPane.showMessageDialog(MindlinerMain.getInstance(),
                                "The object was modified in the meantime. Your version is in the clipboard, please merge manually.",
                                "Storage Error", JOptionPane.ERROR_MESSAGE);
                    } catch (NamingException ex) {
                        JOptionPane.showMessageDialog(
                                MindlinerMain.getInstance(),
                                "Could not transmit command to server. Try again later.",
                                "Server Update Error",
                                JOptionPane.ERROR_MESSAGE);
                    } catch (MlAuthorizationException ex) {
                        JOptionPane.showMessageDialog(MindlinerMain.getInstance(),ex.getMessage(),"Command Execution Error",JOptionPane.ERROR_MESSAGE);

                    }
                    break;
                case Asynchronous:
                    commandQueue.add(c);
                    addSessionUndoCommand(c);
                    updateCommandEditor();
                    break;
                default:
                    throw new AssertionError();
            }
        }
    }

    public void scheduleCommands(List<MindlinerCommand> commands) {
        for (MindlinerCommand c : commands) {
            scheduleCommand(c);
        }
    }

    private File getFile() {
        return new File(CacheEngineStatic.getDataCacheFilePath("Commands"));
    }

    public void addSessionUndoCommand(MindlinerCommand c) {
        sessionUndoCommands.add(c);
        updateUndoObserverText();
    }

    public void removeCommand(MindlinerCommand c) {
        commandQueue.remove(c);
    }

    /**
     * Stores the current command list to a local file
     */
    public void store() {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            File f = getFile();
            if (f == null) {
                return;
            }
            fos = new FileOutputStream(f);
            oos = new ObjectOutputStream(fos);
            oos.writeInt(MlSessionClientParams.DATA_CACHE_FILE_VERSION);
            oos.writeInt(commandQueue.size());
            for (MindlinerCommand c : commandQueue) {
                oos.writeObject(c);
            }
            oos.writeObject(nextTemporaryId);

        } catch (IOException ex) {
            Logger.getLogger(CacheEngineStatic.class
                    .getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(CacheEngineStatic.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            try {
                if (oos != null) {
                    oos.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(CacheEngineStatic.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Loads previously stored and unexecuted command lists (if any).
     *
     * @return True if previous commands exist and were loaded successfully,
     * false otherwise.
     */
    private void loadPreviousCommandList() {

        try {
            File f = getFile();
            if (f.exists()) {
                FileInputStream fis = new FileInputStream(f);
                ObjectInputStream ois = new ObjectInputStream(fis);
                int version = ois.readInt();
                if (version == MlSessionClientParams.DATA_CACHE_FILE_VERSION) {
                    int commandCount = ois.readInt();
                    for (int i = 0; i < commandCount; i++) {
                        MindlinerCommand c = (MindlinerCommand) ois.readObject();
                        commandQueue.add(c);
                    }
                    nextTemporaryId = (Integer) ois.readObject();
                } else {
                    JOptionPane.showMessageDialog(MindlinerMain.getInstance(), "Command file structure changed. Could not load", "Command File Version Change", JOptionPane.INFORMATION_MESSAGE);

                }
            }
        } catch (IOException ex) {
            Logger.getLogger(CommandRecorder.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(MindlinerMain.getInstance(),
                    ex.getMessage(),
                    "Cannot load previous commands.",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * This function removes all calls from the stack that would not have had an
     * affect on the server if all commands in the stack had been completed.
     *
     * Examples: it removes commands that are overridden by other commands later
     * in the editing sequence or removes commands for objects that were later
     * deleted before ever created on the server.
     */
    public synchronized void consolidateCommandStack() {
        ArrayList<MindlinerCommand> outlist = new ArrayList<>();
        List<MindlinerCommand> inlist = new ArrayList<>(commandQueue.size());
        inlist.addAll(commandQueue);
        // remove redudant overriding calls for same object
        for (int i = inlist.size() - 1; i >= 0; i--) {
            MindlinerCommand c = inlist.get(i);
            boolean remove = false;
            for (MindlinerCommand c2 : outlist) {
                if (c.isOverriding() && c.equals(c2)) {
                    remove = true;
                }
            }
            if (remove == false) {
                outlist.add(0, c);
            }
        }
        commandQueue.clear();
        commandQueue.addAll(outlist);
    }

    public boolean hasCommands() {
        return !commandQueue.isEmpty();
    }

    private void removeCommandFromLists(MindlinerCommand c) {
        commandQueue.remove(c);
        sessionUndoCommands.remove(c);
    }

    public void undoLastCommand() {
        MindlinerCommand lastSessionCommand = null;
        try {
            if (sessionUndoCommands.isEmpty()) {
                return;
            }
            lastSessionCommand = sessionUndoCommands.get(sessionUndoCommands.size() - 1);
            lastSessionCommand.undo();
            removeCommandFromLists(lastSessionCommand);
            updateCommandEditor();
            updateUndoObserverText();
        } catch (mlModifiedException | UndoAlreadyUndoneException ex) {
            JOptionPane.showMessageDialog(MindlinerMain.getInstance(), ex.getMessage(), "Undo", JOptionPane.ERROR_MESSAGE);
            removeCommandFromLists(lastSessionCommand);
        } catch (NamingException | MlAuthorizationException ex) {
            JOptionPane.showMessageDialog(MindlinerMain.getInstance(), ex.getMessage(), "Undo", JOptionPane.ERROR_MESSAGE);
            // here we keep the command in the list so we can try later again when the network is back
        }
    }

    private String getLastSessionCommandHeadline() {
        if (sessionUndoCommands.size() > 0) {
            MindlinerCommand lastCommand = sessionUndoCommands.get(sessionUndoCommands.size() - 1);
            return lastCommand.toString();
        }
        return "(no command in session)";
    }

    public void updateCommandEditor() {
        if (commandEditor != null) {
            commandEditor.updateCommandList();
        }
    }

    /**
     * Deletes all commands from the recorder's list.
     */
    public void clearQueue() {
        commandQueue.clear();
        updateCommandEditor();
    }

    public Queue<MindlinerCommand> getQueue() {
        return commandQueue;
    }

    public int getTemporaryId() {
        return nextTemporaryId--;

    }

    /**
     * Maps the specified Id to the server id. This function is used for objects
     * that were generated in offline mode and that are now uploaded to the
     * server together with possible edit and linking operations.
     *
     * @param id The id to be mapped. If >= 0 then the id is simply returned. If
     * less than 0 the ID is mapped.
     * @return The new server Id or 0 if mapping failed.
     */
    public int mapId(int id) {
        if (id >= 0) {
            return id;
        }
        Integer objectOnServerId = idmap.get(id);
        if (objectOnServerId == null) {
            return 0;
        } else {
            return objectOnServerId;
        }
    }

    /**
     * This command stores a pair of IDs in the map. Once IDs are added to the
     * map then every occurance of the temporaryKey will be substituted for the
     * serverKey in all the registered commands.
     *
     * @param temporaryKey The temporary ID used in some of the commands (or
     * none in which case the pair is ignored)
     * @param serverKey The permanent server key for the object.
     */
    public void addIdPair(int temporaryKey, int serverKey) {
        idmap.put(temporaryKey, serverKey);
    }

    public void registerUndoObserver(UndoObserver o) {
        undoObservers.add(o);
    }

    public void unregisterUndoObserver(UndoObserver o) {
        undoObservers.remove(o);
    }

    private void updateUndoObserverText() {
        for (UndoObserver u : undoObservers) {
            u.setUndoControlItemText(getLastSessionCommandHeadline());
        }
    }

    public MessageTrafficControl getTrafficControl() {
        return trafficControl;
    }

    public void setTrafficControl(MessageTrafficControl trafficControl) {
        this.trafficControl = trafficControl;
    }

    private class AsynchCommandProcessor extends TimerTask {

        private boolean isCancelled = false;
        private final StatusReporter statusBar;

        public AsynchCommandProcessor(StatusReporter statusBar) {
            this.statusBar = statusBar;
        }

        @Override
        @SuppressWarnings("SleepWhileInLoop")
        public void run() {
            try {
                executeCommands();
            } catch (Exception ex) {
                Logger.getLogger(CommandRecorder.class.getName()).log(Level.SEVERE, "Unexpected error while executing command queue", ex);
            }
        }

        public void cancelNow() {
            isCancelled = true;
            cancel();
        }

        private void executeCommands() {
            if (commandQueue.isEmpty()) {
                return;
            }
            try {
                consolidateCommandStack();
            } catch (Exception ex) {
                Logger.getLogger(CommandRecorder.class.getName()).log(Level.SEVERE, "Error while consolidating command stack", ex);
            }

            if (statusBar != null) {
                statusBar.startTask(0, commandQueue.size(), true, false);
                statusBar.setMessage("Synchronizing offline work", 0);
            }

            int i = 0;
            while (!commandQueue.isEmpty()) {
                if (isCancelled) {
                    break;
                }
                MindlinerCommand c = commandQueue.poll();
                try {
                    c.execute();
                    updateCommandEditor();
                } catch (mlModifiedException ex) {
                    Logger.getLogger(CommandRecorder.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(MindlinerMain.getInstance(), ex.getMessage() + ". Merge manually and delete first command in editor.", "Server Update Error", JOptionPane.ERROR_MESSAGE);
                    mlcObject updatedObject = CacheEngineStatic.getObject(c.getObject().getId());
                    c.setObject(updatedObject);
                } catch (NamingException ex) {
                    Logger.getLogger(CommandRecorder.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(MindlinerMain.getInstance(), "Could not transmit command to server. Try again later.", "Server Update Error", JOptionPane.ERROR_MESSAGE);
                } catch (MlAuthorizationException ex) {
                    Logger.getLogger(CommandRecorder.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(MindlinerMain.getInstance(), "Unexpected error while doing " + c + ". Skipping and continuing with remaining commands.", "Command execution error", JOptionPane.ERROR_MESSAGE);
                }
                if (statusBar != null) {
                    i++;
                    statusBar.setProgress(i);
                    statusBar.setMaximum(i + commandQueue.size());
                }
            }
            if (statusBar != null) {
                statusBar.done();
            }
            store();
            updateCommandEditor();
        }
    }
}
