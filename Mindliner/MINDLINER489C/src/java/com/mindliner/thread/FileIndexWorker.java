/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.thread;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.commands.ObjectCreationCommand.ProgressListener;
import com.mindliner.managers.ObjectManagerRemote;
import com.mindliner.serveraccess.RemoteLookupAgent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * Worker thread to upload a file to the Mindliner server for indexing in SOLR.
 * @author Dominic Plangger
 */
public class FileIndexWorker extends SimpleSwingWorker<Void, Object> implements ProgressListener {
    
    private static final long MAX_FILE_SIZE = 1024 * 1024 * 15; // 15MB max file size for uploading
    private final File file;
    private mlcObject obj;
    private boolean error;
    // List of supported mime types. Other files are not indexed. Solr may support more formats but we focus on the reasonable ones
    private static final String[] MIMES = {"text/html","text/plain","text/richtext","text/css","application/xml","text/x-java-source","text/xml",
    "application/xhtml+xml","application/msword","application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .doc, .docx
    "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xls, .xlsx
    "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation", // .ppt, .pptx
    "application/pdf", "application/rtf", // .pdf, .rtf
    "application/vnd.oasis.opendocument.text", "application/vnd.oasis.opendocument.text-template", // .odt, .ott
    "application/vnd.oasis.opendocument.text-master",  // .oth
    "application/vnd.oasis.opendocument.graphics", "application/vnd.oasis.opendocument.presentation", // .odg, .odp
    "application/vnd.oasis.opendocument.spreadsheet", "application/vnd.oasis.opendocument.spreadsheet-template", // .ods, .ots
    "application/vnd.sun.xml.writer", "application/vnd.sun.xml.calc", "application/vnd.sun.xml.draw", "application/vnd.sun.xml.impress" // .sxw, .sxc, .sxd, .sxi
    }; 
    

    public FileIndexWorker(File file) {
        this.file = file;
        if (file.length() > MAX_FILE_SIZE) {
            JOptionPane.showMessageDialog(null, "File " + file.getName() + " is too big and will not be indexed (Limit: 15MB).", "File too big", JOptionPane.WARNING_MESSAGE);
            error = true;
        }
    }
    
    
    @Override
    protected Void doInBackground() throws Exception {
        String mime = Files.probeContentType(file.toPath());
        if (!Arrays.asList(MIMES).contains(mime)) {
            Logger.getLogger(FileIndexWorker.class.getName()).log(Level.INFO, "File mime type {0} not supported. File will not be inexed.", mime);
            return null;
        }
        
        // NOTE: the file is serialized and sent to the server using EJB. 
        // this is not suitable for bigger files as RMI was not intended to transfer big data (no streaming, all data in memory, no traffic enhancement). 
        // Therefore we introduce a maximum file size. 
        // If we want to transfer big files (> 100MB) one solution would be to use servlets (HTTP).
        ObjectManagerRemote omr = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
        byte[] bytes = convertToByteArray(new FileInputStream(file));
        Logger.getLogger(FileIndexWorker.class.getName()).log(Level.INFO, "Uploading file of size {0} for indexing.", bytes.length);
        omr.addSolrFile(bytes, obj.getId());
        
        return null;
    }

    private byte[] convertToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[2048];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        inputStream.close();
        return buffer.toByteArray();
    }

    @Override
    public void creationFinished(mlcObject obj) {
        this.obj = obj;
        if (!error) {
            execute();
        }
    }
}
