/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.connector;

import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.mindliner.gui.StatusBar;
import com.mindliner.main.MindlinerMain;
import com.mindliner.serveraccess.OnlineService;
import com.mindliner.serveraccess.StatusReporter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/**
 * Simple progress listener to update the status bar during file upload to Google Drive
 * @author Dominic Plangger
 */
public class GoogleDriveProgressListener implements MediaHttpUploaderProgressListener {
    
    private final StatusReporter statusReporter;

    public GoogleDriveProgressListener() {
        this.statusReporter = MindlinerMain.getStatusBar();
    }

    @Override
    public void progressChanged(final MediaHttpUploader uploader) throws IOException {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                switch (uploader.getUploadState()) {
                    case INITIATION_STARTED:
                        statusReporter.setMessage("Uploading...");
                        statusReporter.startTask(0, 100, true, true);
                        break;
                    case INITIATION_COMPLETE:
                        break;
                    case MEDIA_IN_PROGRESS:
                        try {
                            statusReporter.setProgress((int) (uploader.getProgress() * 100));
                        } catch (IOException ex) {
                            Logger.getLogger(GoogleDriveProgressListener.class.getName()).log(Level.SEVERE, "Failed to retrieve upload progress", ex);
                        }
                        break;
                    case MEDIA_COMPLETE:
                        statusReporter.done();
                        break;
                }
            }
        });
    }

}
