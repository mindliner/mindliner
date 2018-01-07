/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.connector;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.awt.Dialog;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;

/**
 * Connector for any SFTP server. Handlers authentication and file upload. Files are uploaded into the same structure
 * as documented in GoogleDriveConnector
 * @author Dominic Plangger
 */
public class SftpConnector extends CloudConnector {
    
    private static final String USERNAME_KEY = "username";
    private static final String SERVER_KEY = "server";
    
    private boolean isAuthenticated = false;
    private Session session = null;
    private String username;
    private String password;
    private String server;
    
    private static final int SSH_PORT = 22;
    
    private static SftpConnector instance;
    
    private SftpConnector() {
    }

    public static SftpConnector getInstance() {
        if (instance == null) {
            instance = new SftpConnector();
        }
        return instance;
    }

    @Override
    public List<UploadResult> uploadFiles(List<UploadSource> fileStreams) throws ConnectorException {
        if (fileStreams == null || fileStreams.isEmpty()) {
            return null;
        }
        // The first time users authentication is needed. 
        if (!isAuthenticated || !session.isConnected()) {
            throw new ConnectorException("Not yet authenticated");
        }
        // create new sftp channel
        ChannelSftp sftp;
        try {
            sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.connect();
        } catch (JSchException ex) {
            clear();
            Logger.getLogger(SftpConnector.class.getName()).log(Level.SEVERE, null, ex);
            throw new ConnectorException("Failed to open new sftp channel");
        }
        
        // change working directory to mindliner root folder, create one if not exists yet
        moveToRoot(sftp);
        
        try {
            // find next data folder where to insert new files (i.e. data folder with highest index)
            int folderNum = findNextDataFolder(sftp);
            
            int folderSize = 0;
            if (folderNum == -1) {
                folderNum = 1;
                sftp.mkdir(DATA_FOLDER + folderNum);
            }
            else {
                folderSize = sftp.ls(DATA_FOLDER + folderNum).size(); 
            }
            sftp.cd(DATA_FOLDER + folderNum);
            
            List<UploadResult> result = new ArrayList<>();
            for (UploadSource file : fileStreams) {
                // if we exceed the max amount of files per data folder, create a new one
                if (folderSize >= MAX_FILES_PER_FOLDER) {
                    folderNum++;
                    sftp.cd("..");
                    sftp.mkdir(DATA_FOLDER + folderNum);
                    sftp.cd(DATA_FOLDER + folderNum);
                    folderSize = 0;
                }
                
                // We support both files from disk and from memory.
                // depending on use case, only one of both is vailable (i.e. PowerPoint import (byte array) vs file import)
                InputStream is;
                long size;
                if (file.getBytes() != null) {
                    is = new ByteArrayInputStream(file.getBytes());
                    size = file.getBytes().length;
                }
                else {
                    is = new FileInputStream(file.getFile());
                    size = file.getFile().length();
                }
                String filename = file.getName();
                // URLs are expected to be without whitespaces
                if (filename != null) {
                    filename = filename.replaceAll(" ", "_");
                }
                sftp.put(is ,filename , new SftpProgressListener(size));
                folderSize++;
                
                UploadResult ur = new UploadResult();
                // build link to the new uploaded file
                
                String link = buildLink(sftp, folderNum, filename); 
                ur.setDownloadUrl(link);
                result.add(ur);
            }
            return result;
        } catch (SftpException ex) {
            throw new ConnectorException("Unexpected failure while uploading files", ex);
        } catch (FileNotFoundException ex) {
            throw new ConnectorException("Given file from UploadSource does not exist", ex);
        }
    }

    private String buildLink(ChannelSftp sftp, int folderNum, String filename) throws SftpException {
        StringBuilder b = new StringBuilder(DOWNLOAD_LINK);
        b.append("sftp://").append(username)
                .append("@").append(server)
                .append(":").append(SSH_PORT)
                .append(sftp.getHome())
                .append("/").append(getRootFolder())
                .append("/").append(DATA_FOLDER).append(folderNum)
                .append("/").append(filename);
        return b.toString();
    }

    private int findNextDataFolder(ChannelSftp sftp) throws SftpException {
        Vector<ChannelSftp.LsEntry> files;
        files = sftp.ls(DATA_FOLDER + "*");
        int maxNum = -1;
        for (ChannelSftp.LsEntry file : files) {
            String fname = file.getFilename();
            if (fname != null && !fname.isEmpty() && fname.length() > DATA_FOLDER.length()) {
                String num = fname.substring(DATA_FOLDER.length());
                try {
                    int curr = Integer.valueOf(num);
                    maxNum = Math.max(maxNum, curr);
                } catch (NumberFormatException ex) {
                    Logger.getLogger(SftpConnector.class.getName()).log(Level.INFO, "SFTP File folder has invalid name: {0}", fname);
                }
            }
        }
        return maxNum;
    }

    private void moveToRoot(ChannelSftp sftp) throws ConnectorException {
        String rootFolder = getRootFolder();
        try {
            sftp.ls(rootFolder);
        } catch (SftpException ex) {
            Logger.getLogger(SftpConnector.class.getName()).log(Level.INFO, "No Root foler found at remote SFTP server, will create one.", ex);
            try {
                sftp.mkdir(rootFolder);
            } catch (SftpException ex1) {
                throw new ConnectorException("Failed to create root folder", ex);
            }
        }
        try {
            sftp.cd(rootFolder);
        } catch (SftpException ex) {
            throw new ConnectorException("Failed to change directory to root folder", ex);
        }
    }

    @Override
    public boolean authenticate() {
        if (!isAuthenticated) {
            // We only need the credentials once
            SftpAuthenticationPanel p = new SftpAuthenticationPanel();
            // Take username and server from last session if possible
            Preferences userPrefs = Preferences.userNodeForPackage(SftpConnector.class);
            p.setUsername(userPrefs.get(USERNAME_KEY, ""));
            p.setServer(userPrefs.get(SERVER_KEY, ""));
            SftpAuthenticationDialog dialog = new SftpAuthenticationDialog(p);
            dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setVisible(true);
            if (dialog.isNoAction()) {
                return false;
            }
            username = p.getUsername();
            password = p.getPassword();
            server = p.getServer();
            if (server.startsWith("sftp://")) {
                server = server.substring(7); // JSch cannot handle the case where the protocol is given before the server name
            }

            session = createSession(username, server, password);
            if (session != null) {
                isAuthenticated = true;
            }
            else {
                return false;
            }
        }
        else {
            // session might expire
            if (!session.isConnected()) {
                try {
                    session.connect();
                } catch (JSchException ex) {
                    clear();
                    JOptionPane.showMessageDialog(null, "Failed to reconnect to SFTP Server. Please try again with new credentials.", "Reconnect failed", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(SftpConnector.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
            }
        }
        return true;
    }
    
    private Session createSession(String usr, String srv, String pwd) {
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        try {
            JSch ssh = new JSch();
            Session s = ssh.getSession(usr, srv, SSH_PORT);
            s.setConfig(config);
            s.setPassword(pwd);
            s.connect();
            return s;
        } catch (JSchException ex) {
            JOptionPane.showMessageDialog(null, "Failed to connect to SFTP Server. Please try again with new credentials.", "Connection failed", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(SftpConnector.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public void clear() {
        isAuthenticated = false;
        Preferences userPrefs = Preferences.userNodeForPackage(SftpConnector.class);
        userPrefs.remove(USERNAME_KEY);
        userPrefs.remove(SERVER_KEY);
    }

    @Override
    public boolean isAuthenticated() {
        return isAuthenticated;
    }
    
    public static void persistState() {
        if (instance != null && instance.isAuthenticated) {
            Preferences userPrefs = Preferences.userNodeForPackage(SftpConnector.class);
            // for security reasons we do not save the password in registry (would be clear text)
            userPrefs.put(USERNAME_KEY, instance.username);
            userPrefs.put(SERVER_KEY, instance.server);
        }
    }
    
    /**
     * Removes the file specified by the uri. It reuses the session if we already are 
     * connected to the right SFTP server.
     * Either way, it first spans a dialog where the user can cancel the deletion. 
     * @param uri 
     */
    public void removeFile(URI uri) {
        String srv = uri.getHost();
        String usr = uri.getUserInfo();
        
        SftpAuthenticationPanel p = new SftpAuthenticationDeletionPanel(uri.getPath());
        p.setUsername(usr);
        p.setServer(srv);
        if (isAuthenticated && server.equals(srv) && username.equals(usr)) {
            p.setPassword(password);
        }
        SftpAuthenticationDialog dialog = new SftpAuthenticationDialog(p);
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setVisible(true);
        if (dialog.isNoAction()) {
            return;
        }
        String path = uri.getPath();
        try {
            if (isAuthenticated && server.equals(srv) && username.equals(usr)) {
                // if the file to delete is on a server that we currently are authenticated on, we already have an opened session
                try {
                    ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
                    sftp.connect();
                    sftp.rm(path);
                    return;
                } catch (JSchException ex) {
                    Logger.getLogger(SftpConnector.class.getName()).log(Level.WARNING, "Could not open channel for file deletion", ex);
                    clear();
                    // Maybe connection expired, continue with normal authentication
                }
            }
            String pwd = p.getPassword();
            Session s = createSession(usr, srv, pwd);
            if (s == null) {
                return;
            }
            ChannelSftp sftp = (ChannelSftp) s.openChannel("sftp");
            sftp.connect();
            sftp.rm(path);
        } catch (JSchException | SftpException ex) {
            Logger.getLogger(SftpConnector.class.getName()).log(Level.WARNING, "Failed to remove file from remote SFTP server [" + uri.toString() + "]", ex);
            JOptionPane.showMessageDialog(null, "We could not remove the file from the remote SFTP server. Does it even exist?", "Deletion failed", JOptionPane.ERROR_MESSAGE);
        }
    }

}
