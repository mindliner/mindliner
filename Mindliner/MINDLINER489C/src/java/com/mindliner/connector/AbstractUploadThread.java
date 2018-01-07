/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.connector;

import com.mindliner.gui.StatusBar;
import com.mindliner.main.MindlinerMain;
import com.mindliner.serveraccess.StatusReporter;
import com.mindliner.thread.SimpleSwingWorker;
import com.mindliner.view.MapTransferHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * Abstract worker thread that calls the desired cloud connector to upload the objects
 * specified through the <code>setSource</code> method. Provides an abstract method afterUpload that can be used by implementations to
 * specify the behavior after the upload has finished.
 * @author Dominic Plangger
 */
abstract public class AbstractUploadThread extends SimpleSwingWorker<List<UploadResult>, Object> {
    

    private final StatusReporter statusReporter = MindlinerMain.getStatusBar();
    private final CloudConnector connector;
    
    private List<UploadSource> source = new ArrayList<>();

    public AbstractUploadThread(CloudConnector connector) {
        this.connector = connector;
    }
    
    public void setSource(List<UploadSource> source) {
        this.source = source;
    }
    
    public void runSynchronously() throws Exception {
        List<UploadResult> res = doInBackground();
        afterUpload(res);
    }
    
    @Override
    protected List<UploadResult> doInBackground() throws Exception {
        // No connector means user doesn't want to upload the files
        if (connector == null) {
            return null;
        }
        // upload files to desired cloud service
        try {
            return connector.uploadFiles(source);
        } catch (ConnectorException ex) {
            Logger.getLogger(MapTransferHandler.class.getName()).log(Level.SEVERE, "Failed to upload files to cloud", ex);
            if (ex.isAuthorisationError) {
                JOptionPane.showMessageDialog(null, "Mindliner is not authorised for file upload anymore. Please try again and an authorisation dialog will be triggered.", "Authorisation error", JOptionPane.OK_OPTION);
                cancel(false);
            } else {
                int res = JOptionPane.showConfirmDialog(MindlinerMain.getInstance(), "Failed to upload files to the desired cloud service. Do you still want to create Knowlets for the files?", "Upload Error", JOptionPane.YES_NO_OPTION);
                if (res == JOptionPane.NO_OPTION) {
                    cancel(false);
                }
            }
            return null;
        }
    }
    
    @Override
    protected void done() {
        statusReporter.done();
        if (isCancelled()) {
            return;
        }
        try {
            List<UploadResult> result = get();
            afterUpload(result);
        } catch (Exception ex) {
            Logger.getLogger(GoogleDriveConnector.class.getName()).log(Level.SEVERE, "Upload error", ex);
        }
    }
    
    abstract protected void afterUpload(List<UploadResult> result);
}
