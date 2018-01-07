/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.connector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Container class representing data/a file that needs to be uploaded
 * to a cloud storage.
 * The data can either be set as a byte array or as a File object. Both
 * is supported by the CloudConnectors.
 * @author Dominic Plangger
 */
public class UploadSource {
    
    private File file = null;
    private String name = null;
    private String mime = null;
    private byte[] bytes = null;
    
    public UploadSource(byte[] bytes, String name, String mime) {
        this.bytes = bytes;
        this.name = name;
        this.mime = mime;
        // not all cloud services can guess the filetype from a byte array,
        // therefore we add the file type to the file name.
        // We only cover image files because only image upload from Powerpoint uses the byte array
        // constructor from UploadSource.
        if (mime != null && !mime.isEmpty()) {
            if (mime.contains("jpg") && !name.endsWith(".jpg")) {
                this.name = name.concat(".jpg");
            }
            else if (mime.contains("bmp") && !name.endsWith(".bmp")) {
                this.name = name.concat(".bmp");
            }
            else if (mime.contains("png") && !name.endsWith(".png")) {
                this.name = name.concat(".png");
            }
        }
    }
    
    public UploadSource(File f) throws FileNotFoundException {
        this.file = f;
        this.name = f.getName();
        try {
            this.mime = Files.probeContentType(f.toPath());
        } catch (IOException ex) {
            Logger.getLogger(GoogleDriveConnector.class.getName()).log(Level.WARNING, "Failed to probe content type of file", ex);
        }
    }
    
    public File getFile() {
        return file;
    }

    public byte[] getBytes() {
        return bytes;
    }
    
    public String getName() {
        return name;
    }

    public String getMime() {
        return mime;
    }
}
