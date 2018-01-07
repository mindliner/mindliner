/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.containermap;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.commands.CommandRecorder;
import com.mindliner.commands.FileUploadCommand;
import com.mindliner.connector.CloudConnector;
import com.mindliner.serveraccess.OnlineManager;
import com.mindliner.view.containermap.CmFileUploadThread;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * Takes a list of files and handles them according to the current cloud connector settings
 * 
 * @author Marius Messerli
 */
public class FileUploadHelper {

    /**
     * Takes a list of files and uploads them, if neeed, to a shared location.
     * The content of some files are analyzed and additional objects may be
     * created (as in the case of PDF files with annotations and highlights).
     * 
     * @param droppedFiles
     * @param target The object to which any newly created objects are to be linked
     * @return true if the file was uploaded, false otherwise
     * @throws FileNotFoundException 
     */
    public static boolean uploadFiles(List<File> droppedFiles, mlcObject target) throws FileNotFoundException {
        CloudConnector connector = CloudConnector.getCurrentConnector();

        if (!OnlineManager.isOnline()) {
            // if we are offline, create an upload command that will be executed later when going online
            FileUploadCommand cmd = new FileUploadCommand(droppedFiles, target, connector);
            CommandRecorder.getInstance().scheduleCommand(cmd);
            return false;
        }

        // authentication needs to be done only once
        if (!connector.authenticate()) {
            return false;
        }
        // start asynchronous file uploader
        CmFileUploadThread uploader = new CmFileUploadThread(droppedFiles, target, connector);
        uploader.execute();
        return true;
    }
}
