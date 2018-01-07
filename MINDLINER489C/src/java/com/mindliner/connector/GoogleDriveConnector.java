/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.connector;

import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files.Insert;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.Permission;
import java.awt.Dialog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;

/**
 * Connector for Google Drive. Handles authentication and asynchronous file upload. Files are uploaded into numbered 
 * 'Files x' folders that reside in the 'Mindliner Files username' root folder
 * @author Dominic Plangger
 */
public class GoogleDriveConnector extends CloudConnector {

    private static GoogleDriveConnector instance = null;
    // @todo REMEMBER IMPORTANT: client id and client secret can be obtained by creating a new google drive project in a
    // google account. currently these two values are from a project created in the google account from Dominic Plangger (plangger.dominic@gmail.com)
    private static final String CLIENT_ID = "218111956324-7vbr7q7141p2jojd5g65qvf2lec1vrca.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "Oq4efAj6nzVRQ1jU3G8k6Q_o";
    private static final String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
    private static final String DEFAULT_NAME = "Mindliner_file";
    private static final String REFRESH_TOKEN = "refreshtoken";
    private static final int UNAUTHORIZED_ERROR = 401;
    private static final String INVALID_GRANT_ERROR = "invalid_grant";
    private static final long EXPIRATION_MARGIN = 600; // safety margin for refreshing access access token

    private boolean authenticated = false;
    private GoogleCredential credential = null;
    private Drive drive = null;
    private long tokenExpirationTime;
    private Date lastRefresh = null;
    private String rootFolderId = null;
    private String dataFolderId = null;
    private String refreshToken = null;
    private int dataFolderCnt;
    private int dataFolderSize;
    private final HttpTransport httpTransport = new NetHttpTransport();
    private final JsonFactory jsonFactory = new JacksonFactory();

    private GoogleDriveConnector() {
    }

    public static GoogleDriveConnector getInstance() {
        if (instance == null) {
            instance = new GoogleDriveConnector();
        }
        return instance;
    }
      
    public static void persistState() {
        if (instance != null && instance.authenticated) {
            Preferences userPrefs = Preferences.userNodeForPackage(GoogleDriveConnector.class);
            userPrefs.put(REFRESH_TOKEN, instance.refreshToken);
        }
    }
    
    @Override
    public List<UploadResult> uploadFiles(List<UploadSource> fileStreams) throws ConnectorException {
        if (fileStreams == null || fileStreams.isEmpty()) {
            return null;
        }
        try {
            // The first time users authorisation is needed. 
            if (!authenticated) {
                throw new ConnectorException("Not yet authenticated");
            }
            
            Date now = new Date();
            if ((now.getTime() - lastRefresh.getTime()) > (tokenExpirationTime + EXPIRATION_MARGIN) * 1000) {
                // need to refresh access token
                createNewCredentials();
                lastRefresh = new Date();
            }
            
            List<UploadResult> result = new ArrayList<>();
            for (UploadSource us : fileStreams) {
                if (us == null) {
                    Logger.getLogger(GoogleDriveConnector.class.getName()).log(Level.WARNING, "Passed null file stream for upload to google drive");
                    continue;
                }
                
                if (dataFolderSize >= MAX_FILES_PER_FOLDER) {
                    createDataFolder();
                }

                com.google.api.services.drive.model.File body = new com.google.api.services.drive.model.File();

                // Set title, either default or use file name if exists
                if (us.getName() == null || us.getName().isEmpty()) {
                    body.setTitle(DEFAULT_NAME);
                } else {
                    body.setTitle(us.getName());
                }
                body.setDescription("Uploaded by Mindliner");
                body.setParents(Arrays.asList(new ParentReference().setId(dataFolderId)));

                AbstractInputStreamContent mediaContent;
                if (us.getBytes() != null) {
                    mediaContent = new ByteArrayContent(us.getMime(), us.getBytes());
                }
                else {
                    mediaContent = new FileContent(us.getMime(), us.getFile());
                }
                

                // Upload file
                Insert insert = drive.files().insert(body, mediaContent);
                MediaHttpUploader uploader = insert.getMediaHttpUploader();
                // upload files in chunks such that progress listener can be used
                uploader.setDirectUploadEnabled(false);
                uploader.setChunkSize(MediaHttpUploader.MINIMUM_CHUNK_SIZE);
                uploader.setProgressListener(new GoogleDriveProgressListener());
                com.google.api.services.drive.model.File file = insert.execute();
                dataFolderSize++;
                        
                UploadResult ur = new UploadResult();
                ur.setDownloadUrl(DOWNLOAD_LINK + file.getWebContentLink());
                ur.setOpenUrl(OPEN_LINK + file.getAlternateLink());
                result.add(ur);
                Logger.getLogger(GoogleDriveConnector.class.getName()).log(Level.INFO, "Uploaded file {0}", file.getId());
            }
            return result;
        } catch (TokenResponseException ex) {
            // can happen when user revokes access rights for mindliner
            if (INVALID_GRANT_ERROR.equals(ex.getDetails().getError())) {
                catchAuthorisationError(ex);
            }
            throw new ConnectorException("Unexpected upload error", ex);
        } catch (GoogleJsonResponseException ex) {
            // can happen when user revokes access rights for mindliner
            if (ex.getStatusCode() == UNAUTHORIZED_ERROR) {
                catchAuthorisationError(ex);
            }
            throw new ConnectorException("Unexpected upload error", ex);
        } catch (Exception ex) {
            // @todo add retry mechanism (with exponential backoff) in case of an internal google api error
            throw new ConnectorException("Unexpected upload error", ex);
        } 

    }

    private void catchAuthorisationError(Exception ex) throws ConnectorException {
        clear();
        throw new ConnectorException("Authorisation error", ex, true);
    }
    
    
    private void initRootFolder() throws IOException {
        String rootFolder = getRootFolder();
        if (rootFolderId != null && dataFolderId != null) {
            return;
        }
        List<File> files = drive.files().list().setQ("mimeType = 'application/vnd.google-apps.folder' and title = '" + rootFolder + "'").execute().getItems();
        File mlRoot = null;
        for (File f : files) {
            if (rootFolder.equals(f.getTitle())) {
                mlRoot = f;
                break;
            }
        }
        // Create Mindliner Root folder if not exists
        if (mlRoot == null) {
            mlRoot = new File();
            mlRoot.setTitle(rootFolder);
            mlRoot.setMimeType("application/vnd.google-apps.folder");
            Permission p = new Permission();
            p.setValue("");
            p.setType("anyone");
            p.setRole("reader");
            mlRoot = drive.files().insert(mlRoot).execute();
            drive.permissions().insert(mlRoot.getId(), p).execute();
        }
        rootFolderId = mlRoot.getId();
        
        // inside the root folder there are data folder containing the uploaded files
        initDataFolder();
    }
    
    private void initDataFolder() throws IOException {
        if (dataFolderId != null) {
            return;
        }
        // the data folders are numbered, we need to retrieve the last used data folder
        // with the highest index
        List<File> files = drive.files().list().setQ(""
                + "mimeType = 'application/vnd.google-apps.folder' "
                + "and '" + rootFolderId + "' in parents "
                + "and title contains '" + DATA_FOLDER + "'").execute().getItems();
        File mlData = null;
        int max = -1;
        for (File f : files) {
            String num = f.getTitle().replace(DATA_FOLDER, "").trim();
            try {
                int curr = Integer.parseInt(num);
                if (curr > max) {
                    max = curr;
                    mlData = f;
                }
            } catch(NumberFormatException ex){
            }
        }
        
        if (mlData != null) {
            // if a data folder already exists we reuse it as long as it does not contain too many files
            dataFolderCnt = max;
            int size = drive.files().list().setQ("'" + mlData.getId() + "' in parents ").execute().getItems().size();
            if (size < MAX_FILES_PER_FOLDER) {
                dataFolderId = mlData.getId();
                dataFolderSize = size;
                return;
            }
        }
        // if no data folder exists we create a new one
        createDataFolder();
    }
    
    private void createDataFolder() throws IOException {
        dataFolderCnt++;
        dataFolderSize = 0;
        File body = new File();
        body.setTitle(DATA_FOLDER + dataFolderCnt);
        body.setMimeType("application/vnd.google-apps.folder");
        body.setParents(Arrays.asList(new ParentReference().setId(rootFolderId)));
        File file = drive.files().insert(body).execute();
        dataFolderId = file.getId();
        
    }

    @Override
    public boolean authenticate() {
        if (authenticated) {
            return true;
        }
        
        // Maybe user already authorised Mindliner in last session then we reuse the refreshToken
        Preferences userPrefs = Preferences.userNodeForPackage(GoogleDriveConnector.class);
        instance.refreshToken = userPrefs.get(REFRESH_TOKEN, null);

        try {
            
            if (refreshToken == null) {
                // Retrieves an authorization url to which the user will be redirected in the browser
                // for user consent. Only needed once.
                // See https://developers.google.com/accounts/docs/OAuth2
                if (!doAuthentication()) {
                    return false;
                }
            }
            
            createNewCredentials();
            drive = new Drive.Builder(httpTransport, jsonFactory, credential).build();
            lastRefresh = new Date();
            // Create root Mindliner Folder if not yet exists
            initRootFolder();
            authenticated = true;
            Logger.getLogger(GoogleDriveConnector.class.getName()).log(Level.INFO, "Successfully authenticated Mindliner to Google Drive");
            return true;
        } catch (Exception ex) {
            Logger.getLogger(GoogleDriveConnector.class.getName()).log(Level.SEVERE, null, ex);
            // for safety. maybe saved refreshToken isn't valid anymore
            userPrefs.remove(REFRESH_TOKEN);
            JOptionPane.showMessageDialog(null, "Failed to authenticate Mindliner for Google Drive. Please try again.", "Authentication failed", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    
    private boolean doAuthentication() throws IOException {
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, CLIENT_ID, CLIENT_SECRET, Arrays.asList(DriveScopes.DRIVE))
                .setAccessType("offline") // indicates that we need a refresh token for long-term authorisation
                .setApprovalPrompt("auto").build();
        
        // show dialog where the user has to copy paste the authorisation code
        String url = flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).build();
        GoogleDriveAuthenticationPanel p = new GoogleDriveAuthenticationPanel();
        p.setUrl(url);
        GoogleDriveAuthenticationDialog dialog = new GoogleDriveAuthenticationDialog(p);
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setVisible(true);
        if (dialog.isNoAction()) {
            return false;
        }
        String code = p.getCode();
        
        // get refresh token
        GoogleTokenResponse tokenResponse = flow.newTokenRequest(code).setRedirectUri(REDIRECT_URI).execute();
        refreshToken = tokenResponse.getRefreshToken();
        tokenExpirationTime = tokenResponse.getExpiresInSeconds();
        return true;
    }

    // refreshes credentials (access token expires after one hour)
    private void createNewCredentials() {
        credential = new GoogleCredential.Builder().setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .setClientSecrets(CLIENT_ID, CLIENT_SECRET)
                .build()
                .setRefreshToken(refreshToken);
    }
    
    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void clear() {
        // force the user to re-authorise mindliner
        authenticated = false;
        Preferences userPrefs = Preferences.userNodeForPackage(GoogleDriveConnector.class);
        userPrefs.remove(REFRESH_TOKEN);
    }

}
