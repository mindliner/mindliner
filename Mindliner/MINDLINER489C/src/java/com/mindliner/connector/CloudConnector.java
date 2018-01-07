/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.connector;

import com.mindliner.cache.CacheEngineStatic;
import java.awt.Dialog;
import java.util.List;

/**
 * Abstract class that represents a connector to a cloud storage. 
 * Available implementations are for example for Google Drive: GoogleDriveConnector, or SFTP: SftpConnector.
 * <p>
 * Intended usage: call 'getCurrentConnector' and then 'authenticate' in the GUI thread. Then use an
 * AbstractUploadThread to asynchronously upload the files.
 * <p>
 * It saves the current chosen connector implementation such that the user
 * doesn't have to chose the connector each time he wants to upload something.
 * <p>
 * @author Dominic Plangger
 */
public abstract class CloudConnector {
    
    public static final String DOWNLOAD_LINK = "Download: ";
    protected static final String OPEN_LINK = "Open: ";
    private static CloudConnector currentConnector = null;
    private static boolean isConnectorSet = false;
    
    protected static final int MAX_FILES_PER_FOLDER = 100; 
    protected static final String DATA_FOLDER = "Files_";
    private static String rootFolder = "Mindliner_Files_";
    private static boolean isRootSet;
    
    /**
     * Returns the currently selected connector. If the user hasn't select one, a dialog will be shown
     * where the user can select the desired cloud connector. Needs to be executed in the GUI thread.
     * 
     * @return the selected connector or null if the user canceled the connector selection dialog. 
     */
    public static CloudConnector getCurrentConnector() {
        if (!isConnectorSet) {
            ConnectorSelectionPanel panel = new ConnectorSelectionPanel();
            ConnectorSelectionDialog dialog = new ConnectorSelectionDialog(panel);
            dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setVisible(true);

            if (dialog.isNoAction()) {
                return null;
            }
            
            currentConnector = panel.getConnector();
            isConnectorSet = panel.isRememberSelection();
        }
        
        return currentConnector;
    }
    
    protected static String getRootFolder() {
        if (!isRootSet) {
            rootFolder = rootFolder + CacheEngineStatic.getCurrentUser().getLoginName();
            isRootSet = true;
        }
        return rootFolder;
    }
    
    /**
     * Uploads the files to the implemented Cloud service. 
     * @param fileStreams
     * @return
     * @throws ConnectorException 
     */
    public abstract List<UploadResult> uploadFiles(List<UploadSource> fileStreams) throws ConnectorException;
    
    /**
     * Authenticates the user to the desired Cloud service. The first time a dialog is shown, afterwards the credentials are remembered. 
     * Needs to be executed in GUI thread. 
     * @return 
     */
    public abstract boolean authenticate();
    
    public abstract boolean isAuthenticated();

    public abstract void clear();
}
