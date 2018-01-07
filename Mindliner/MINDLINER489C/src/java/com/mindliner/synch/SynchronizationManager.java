/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.synch;

import com.mindliner.exceptions.synch.SynchConnectionException;
import com.mindliner.managers.SynchManagerRemote;
import com.mindliner.objects.transfer.mltSyncher;
import com.mindliner.serveraccess.RemoteLookupAgent;
import com.mindliner.synchronization.MlSynchProgressReporter;
import com.mindliner.synchronization.SynchActor;
import com.mindliner.thread.SimpleSwingWorker;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.naming.NamingException;
import javax.swing.JOptionPane;

/**
 * This class is the top synch manager that loads and launches the
 * synchronization opeation(s).
 *
 * @author Marius Messerli
 */
public class SynchronizationManager {

    private static final List<SynchActor> synchActors = new ArrayList<>();
    private static SynchronizationSwingWorker worker;
    private static final MlSynchProgressReporter progressDialog = new SynchProgressDialog(null, false);

    /**
     * This call stores all registered synch workers with their configuration
     * and their synch history to disk.
     */
    public static void storeSynchActors() {
        for (SynchActor sa : synchActors) {
            sa.store();
        }
    }

    /**
     * This call reconstructs, initializes, and registers previously configured
     * synch workers
     */
    public static void loadSynchActors() {
        try {
            SynchManagerRemote smr = (SynchManagerRemote) RemoteLookupAgent.getManagerForClass(SynchManagerRemote.class);
            Collection<mltSyncher> synchers = smr.getSynchers();
            for (mltSyncher s : synchers) {
                SynchActor sa = SynchActorFactory.createSynchActor(s.getType(), s.getBrand());
                sa.setSyncher(s);
                registerSyncher(sa);
            }
        } catch (NamingException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Loading Synch Config and History", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Adds a new syncher to be executed upon synchronization. This call also
     * ensures that the syncher's state is persisted between sessions.
     *
     * @param synchActor The new actor to be registered.
     */
    public static void registerSyncher(SynchActor synchActor) {
        if (!synchActors.contains(synchActor)) {
            synchActors.add(synchActor);
        }
    }

    public static void deleteSyncher(SynchActor synchActor) {
        try {
            SynchManagerRemote smr = (SynchManagerRemote) RemoteLookupAgent.getManagerForClass(SynchManagerRemote.class);
            smr.deleteSyncher(synchActor.getSyncher());
            synchActors.remove(synchActor);
        } catch (NamingException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Deleting Syncher", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static List<SynchActor> getSynchActors() {
        return synchActors;
    }

    public static void synchronize() {
        // if worker is not null then it is already synching and we don't start another synch run
        if (worker == null) {
            worker = new SynchronizationSwingWorker();
            worker.execute();
//            worker.go();
            worker = null;
        }
    }

    static class SynchronizationSwingWorker extends SimpleSwingWorker {

        // this is singled out so that I can call it directly in the main thread for debugging
        public static void go() {
            progressDialog.setVisible(true);
            progressDialog.clear();
            progressDialog.printLine("SYNCH START: " + new SimpleDateFormat().format(new Date()));
            for (SynchActor w : synchActors) {
                try {
                    w.connect();
                    w.setProgressReporter(progressDialog);
                    w.synchronizeElements();
                    // if we don't need immeidate synchronization then close the connection to free resources
                    if (!w.getSyncher().isImmediateForeignUpdate()) {
                        w.disconnect();
                    }
                } catch (SynchConnectionException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Error in " + w.getClass().getName(), JOptionPane.ERROR_MESSAGE);
                }
            }
            progressDialog.printLine("FINISHED");
        }

        @Override
        protected Object doInBackground() throws Exception {
            go();
            return null;
        }
    }
}
