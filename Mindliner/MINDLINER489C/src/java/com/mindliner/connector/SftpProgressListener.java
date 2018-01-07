/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.connector;

import com.jcraft.jsch.SftpProgressMonitor;
import com.mindliner.gui.StatusBar;
import com.mindliner.main.MindlinerMain;
import com.mindliner.serveraccess.StatusReporter;
import javax.swing.SwingUtilities;

/**
 * Simple progress listener to update the status bar during file upload to an SFTP server
 * @author Dominic Plangger
 */
public class SftpProgressListener implements SftpProgressMonitor {
    
    private final StatusReporter statusReporter;
    private final long fileSize;
    private long current;

    public SftpProgressListener(long size) {
        this.fileSize = size;
        this.statusReporter = MindlinerMain.getStatusBar();
    }

    @Override
    public void init(int i, String string, String string1, long max) {
        // note: parameter 'max' should give the size of the file, but it does not (JSch bug)
        statusReporter.setMessage("Uploading...");
        statusReporter.startTask(0, 100, true, true);
    }

    @Override
    public boolean count(long count) {
        current += count;
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                statusReporter.setProgress((int)((current * 100) / fileSize));
            }
        });
        
        return true;
    }

    @Override
    public void end() {
        statusReporter.done();
    }
    
}
