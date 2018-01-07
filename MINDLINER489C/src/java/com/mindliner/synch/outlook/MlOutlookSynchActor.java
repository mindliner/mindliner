/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.synch.outlook;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.commands.CommandRecorder;
import com.mindliner.commands.DataPoolUpdateCommand;
import com.mindliner.commands.ObjectCreationCommand;
import com.mindliner.exceptions.NonExistingObjectException;
import com.mindliner.exceptions.synch.SynchConnectionException;
import com.mindliner.managers.SynchManagerRemote;
import com.mindliner.objects.transfer.mltSyncher;
import com.mindliner.serveraccess.OnlineManager;
import com.mindliner.serveraccess.RemoteLookupAgent;
import com.mindliner.synch.SynchSettingChangeManager;
import com.mindliner.synch.SynchWorkflowGeneric;
import com.mindliner.synch.outlook.folderchooser.FolderChooser;
import com.mindliner.synchronization.MlSynchProgressReporter;
import com.mindliner.synchronization.SynchActor;
import com.mindliner.synchronization.foreign.ForeignObject;
import com.moyosoft.connector.com.ComponentObjectModelException;
import com.moyosoft.connector.exception.LibraryNotFoundException;
import com.moyosoft.connector.ms.outlook.Outlook;
import com.moyosoft.connector.ms.outlook.folder.FolderType;
import com.moyosoft.connector.ms.outlook.folder.FoldersCollection;
import com.moyosoft.connector.ms.outlook.folder.OutlookFolder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.swing.JOptionPane;

/**
 * This class provides a wrapper for the Synchers that are stored on the server.
 * While the Synchers provide settings and a link to previously synched items
 * the SynchActor adds some functionality to it which is required on the client.
 *
 * @author Marius Messerli
 */
public abstract class MlOutlookSynchActor extends SynchActor {

    private static final String DLL_PATH = "\\lib\\dll\\moyocore.dll";
    private static final String DLL_PATH_64 = "\\lib\\dll\\moyocore_x64.dll";
    private static final String LOCAL_START_BIN_PREFIX = "\\bin";
    private static boolean libPathSet = false;

    protected Outlook outlookHandle = null;
    protected MlSynchProgressReporter progressReporter = null;

    /**
     * Returns the default Outlook folder for the worker. Implement this when
     * things get real and concrete and the type can be determined.
     *
     * @return
     */
    protected abstract FolderType getFolderType();

    protected OutlookFolder getOutlookFolder() {
        OutlookFolder folder;
        String foreignSourceURL = syncher.getSourceFolder();
        if (foreignSourceURL == null || foreignSourceURL.isEmpty() || foreignSourceURL.equals(DEFAULT_FOLDER_PATH)) {
            folder = outlookHandle.getDefaultFolder(getFolderType());
        } else {
            folder = outlookHandle.getFolder(syncher.getSourceFolder());
        }
        return folder;

    }

    @Override
    public void chooseSourceFolder() {
        boolean disconnectHandle = false;
        if (outlookHandle == null) {
            try {
                connect();
                disconnectHandle = true;
            } catch (SynchConnectionException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Folder Chooser", JOptionPane.ERROR_MESSAGE);
            }
        }
        if (outlookHandle != null) {
            FoldersCollection folders = outlookHandle.getFolders();
            // a pointer to this syncher is used to feed back the selected folder source path
            OutlookFolder folder = FolderChooser.open(folders);
            if (folder != null) {
                syncher.setSourceFolder(folder.getFullFolderPath());
                SynchSettingChangeManager.sourcePathChanged(folder.getFullFolderPath());
            }
            if (disconnectHandle) {
                disconnect();
            }
        }
    }

    @Override
    public void connect() throws SynchConnectionException {
        if (outlookHandle == null) {
            try {
                if (!libPathSet) {
                    setLibPath();
                }
                outlookHandle = new Outlook();
            } catch (ComponentObjectModelException ex) {
                Logger.getLogger(MlOutlookSynchActor.class.getName()).log(Level.SEVERE, "Cannot open connection to Outlook", ex);
                throw new SynchConnectionException("Cannot open connection to Outlook: " + ex.getMessage());
            } catch (LibraryNotFoundException ex) {
                Logger.getLogger(MlOutlookSynchActor.class.getName()).log(Level.SEVERE, "Cannot load moyocore DLL", ex);
                System.err.println(ex.getMessage());
                throw new SynchConnectionException("The Java Outlook Library hasn't been found.");
            }
        }
    }

    private void setLibPath() throws ComponentObjectModelException {
        String arch = System.getProperty("os.arch");
        String user_dir = System.getProperty("user.dir");
        if (RemoteLookupAgent.isLocalStart()) {
            // When starting ML locally, the lib directory is inside the bin directory of the current working directory
            user_dir = user_dir.concat(LOCAL_START_BIN_PREFIX);
        }
        String path;
        // load different DLL depending on architecture (64bit -> os.arch = amd64, 32bit -> os.arch = ia32)
        if (arch != null && arch.contains("64")) {
            path = user_dir + DLL_PATH_64;
        } else {
            path = user_dir + DLL_PATH;
        }
        Outlook.setLibraryPath(path);
        libPathSet = true;
    }

    @Override
    public void disconnect() {
        if (outlookHandle != null) {
            outlookHandle.dispose();
            outlookHandle = null;
        }
    }

    @Override
    public boolean safeDeleteForeignObject(ForeignObject fo) {
        if (syncher.isDeleteOnMissingCounterpart() == true) {
            String msg;
            msg = "Mindliner object was deleted. " + "Deleting outlook task: " + fo.getHeadline();
            if (progressReporter != null) {
                progressReporter.printLine(msg);
            } else {
                Logger.getAnonymousLogger(SynchWorkflowGeneric.class.getName()).info(msg);
            }
            fo.delete();
            return true;
        } else {
            String msg = "Mindliner object was deleted. Outlook object left unchanged "
                    + "as specified in synch preferences: "
                    + fo.getHeadline();
            if (progressReporter != null) {
                progressReporter.printLine(msg);
            } else {
                Logger.getAnonymousLogger(SynchWorkflowGeneric.class.getName()).info(msg);
            }
            return false;
        }
    }

    @Override
    public int createMindlinerObject() {
        CommandRecorder cr = CommandRecorder.getInstance();
        ObjectCreationCommand occ = new ObjectCreationCommand(null, getMindlinerObjectClass(), "", "");
        cr.scheduleCommand(occ);
        mlcObject newObject = occ.getObject();
        cr.scheduleCommand(new DataPoolUpdateCommand(newObject, CacheEngineStatic.getClient(syncher.getClientId())));
        return occ.getObject().getId();
    }

    /**
     * Returns the class matching the particular syncher instance.
     *
     * @return The class matching the syncher.
     */
    protected abstract Class getMindlinerObjectClass();

    @Override
    public void setProgressReporter(MlSynchProgressReporter reporter) {
        progressReporter = reporter;
    }

    @Override
    public boolean isEqual(ForeignObject foreignObject, Object mo) {
        boolean headlineMatch;
        boolean descriptionMatch;
        if (!(mo instanceof mlcObject)) {
            return false;
        }
        mlcObject mindlinerObject = (mlcObject) mo;

        if (mindlinerObject.getHeadline().isEmpty() || foreignObject.getHeadline().isEmpty()) {
            return false;
        }
        headlineMatch = mindlinerObject.getHeadline().equals(foreignObject.getHeadline());

        if (!mindlinerObject.getDescription().isEmpty() && !foreignObject.getDescription().isEmpty()) {
            descriptionMatch = mindlinerObject.getDescription().equals(foreignObject.getDescription());
        } else {
            descriptionMatch = true;
        }
        return (headlineMatch && descriptionMatch);
    }

    @Override
    public void store() {
        try {
            if (OnlineManager.isOnline()) {
                SynchManagerRemote sr = (SynchManagerRemote) RemoteLookupAgent.getManagerForClass(SynchManagerRemote.class);
                if (syncher.getId() == mltSyncher.UNPERSISTED_SYNCHER_ID) {
                    syncher = sr.storeNewSyncher(syncher);
                } else {
                    syncher = sr.updateSyncher(syncher);
                }
            }
        } catch (NonExistingObjectException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Storing Synchronization History", JOptionPane.ERROR_MESSAGE);
        } catch (NamingException ex) {
            JOptionPane.showMessageDialog(null, "Could not contact server to store for previous synch operations", "Contacting Server", JOptionPane.ERROR_MESSAGE);
        }
    }
}
