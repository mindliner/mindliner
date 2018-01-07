/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.commands;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.MlcImage;
import com.mindliner.clientobjects.mlcKnowlet;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.connector.CloudConnector;
import com.mindliner.connector.StreamUploadThread;
import com.mindliner.connector.UploadSource;
import com.mindliner.entities.SoftwareFeature;
import com.mindliner.events.ObjectChangeManager;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.mlModifiedException;
import com.mindliner.serveraccess.OnlineManager;
import com.mindliner.thread.FileIndexWorker;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;

/**
 * Command for uploading a file list to a given cloud connector. 
 * Is only used in case the user wants to upload files in offline mode. 
 * Otherwise the upload is done directly without a command.
 * 
 * @author Dominic Plangger
 */
public class FileUploadCommand extends MindlinerOnlineCommand {
    
    private final List<File> files;
    private final mlcObject target;
    private final List<mlcObject> createdObjects;
    /**
     * Commands might be serialized. But we do not want to force all CloudConnectors to
     * be serializable (because they in turn might have non-serializable members that
     * would result in an uncontrolled state or runtime exception).
     * 
     * Therefore we only serialize the connector's class and use reflection later on to
     * call getInstance on the connector's class to get a clean instance.
     */
    transient private CloudConnector connector; 
    private final Class connectorClass;

    public FileUploadCommand(List<File> files, mlcObject target, CloudConnector connector) {
        super(null, false);
        if (connector == null || files == null || files.isEmpty()) {
            throw new IllegalArgumentException("Empty file list or null connector not allowed.");
        }
        this.files = files;
        this.target = target;
        this.connector = connector;
        connectorClass = connector.getClass();
        createdObjects = new ArrayList<>();
        createObjects();
    }
    
    private void createObjects() {
        CommandRecorder cr = CommandRecorder.getInstance();
        for (File f : files) {
            String mime = "";
            try {
                mime = Files.probeContentType(f.toPath());
            } catch (IOException ex) {
                Logger.getLogger(FileUploadCommand.class.getName()).log(Level.WARNING, "Could not probe content type of file " + f, ex);
            }
            Class c = mime.contains("image") ? MlcImage.class : mlcKnowlet.class;
            
            ObjectCreationCommand cmd = new ObjectCreationCommand(target, c, f.getName(), "");
            if (CacheEngineStatic.getCurrentUser().isAuthorizedForFeature(SoftwareFeature.CurrentFeatures.FILE_INDEXING)) {
                cmd.addProgressListener(new FileIndexWorker(f));
            }
            cr.scheduleCommand(cmd);
            createdObjects.add(cmd.getObject());
            if (!OnlineManager.waitForServerMessages()) {
                ObjectChangeManager.objectCreated(cmd.getObject());
            }
        }
    }

    @Override
    public void execute() throws mlModifiedException, NamingException, MlAuthorizationException {
        super.execute(); 
        if (connector == null) {
            try {
                // use reflection to get a clean connector instance (see above why) when  the connector is not set
                // (happens when the command has been serialized)
                connector = (CloudConnector) connectorClass.getMethod("getInstance").invoke(null);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(FileUploadCommand.class.getName()).log(Level.SEVERE, "Unexpected error while using reflection to get cloud connector", ex);
            }
        }
        
        List<UploadSource> srcs = new ArrayList<>();
        for (File f : files) {
            try {
                srcs.add(new UploadSource(f));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(FileUploadCommand.class.getName()).log(Level.SEVERE, "Given file for upload not found", ex);
            }
        }
        
        if (!connector.authenticate()) {
            return;
        }
        // start file uploader SYNCHRONOUSLY because we can only be in a background thread (FileUploadCommand is only triggered when being offline)
        // and consecutive commands may rely on this one to be finished
        StreamUploadThread uploader = new StreamUploadThread(connector, srcs, createdObjects);
        try {
            uploader.runSynchronously();
        } catch (Exception ex) {
            Logger.getLogger(FileUploadCommand.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    

    @Override
    public boolean equals(Object obj) {
        if (!(obj.getClass().equals(this.getClass()))) {
            return false;
        }
        FileUploadCommand other = (FileUploadCommand) obj;
        return files.equals(other.files) && connector.equals(other.connector);
    }

    @Override
    public String toString() {
        return "File upload to cloud storage";
    }
    
    

    @Override
    public boolean isVersionChecking() {
        return false;
    }

    
}
