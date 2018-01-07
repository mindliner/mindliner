/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */

 /*
 * LoginModule.java
 *
 * Created on Oct 12, 2008, 5:52:29 PM
 *//*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */

 /*
 * LoginModule.java
 *
 * Created on Oct 12, 2008, 5:52:29 PM
 */
package com.mindliner.main;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.cache.MainCache;
import com.mindliner.cache.MlCacheException;
import com.mindliner.clientobjects.mlcUser;
import com.mindliner.entities.Release;
import com.mindliner.entities.Release.ClientStatus;
import com.mindliner.entities.SoftwareFeature;
import com.mindliner.managers.ReleaseManagerRemote;
import com.mindliner.managers.UserManagerRemote;
import com.mindliner.styles.MlStyler;
import com.mindliner.serveraccess.OnlineManager;
import com.mindliner.serveraccess.RemoteLookupAgent;
import com.mindliner.system.MlSessionClientParams;
import com.sun.appserv.security.ProgrammaticLogin;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.naming.NamingException;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 * This class implements a login dialog that is presented the first time the
 * server requests authentication. It is registered as the custom login callback
 * class.
 *
 * @author Marius Messerli
 */
public class LoginGUI extends javax.swing.JDialog {

    private static final String USERNAME_KEY = "username";
    private static final String AUTOLOGIN_KEY = "autoLogin";
    private static final String PASSWORD_KEY = "password";
    private static final String REMEMBERPASSWORD_KEY = "rememberpassword";
    private static final String REMEMBERUSERNAME_KEY = "rememberusername";
    private static final String OFFLINE_MODE = "offlineMode";
    private static final String GREAT_IDEA_DESCRIPTION_KEY = "startupdescription";
    private static final String GREAT_IDEA_HEADLINE_KEY = "startupheadline";
    private static final int LOGIN_TIMEOUT = 20000; // ms
    private boolean passWordInitialized = false;
    private boolean isLoggedInOnline = false;
    private String password = "";
    private String userName = "";
    private static LoginGUI INSTANCE = null;
    private UserManagerRemote userManager;
    private LoginThread loginThread = null;

    public static void initialize(MindlinerMain main) {
        synchronized (LoginGUI.class) {
            if (INSTANCE == null) {
                System.out.println("Java Version: " + System.getProperty("java.version"));
                try {
                    INSTANCE = new LoginGUI();
                    INSTANCE.main = main;
                    INSTANCE.readPreferences();

                    if (INSTANCE.isAutoLogin() == false) {
                        INSTANCE.setLocationRelativeTo(null);
                        INSTANCE.setVisible(true);
                    }
                } catch (MlCacheException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Autologin", JOptionPane.ERROR_MESSAGE);
                    INSTANCE.AutoLoginCheckbox.setSelected(false);
                    INSTANCE.storeCredentialsAndPrefs();
                    INSTANCE.setLocationRelativeTo(null);
                    INSTANCE.setVisible(true);
                }
            }
        }
    }

    public static boolean onlineLogin() {
        return INSTANCE.doOnlineLogin();
    }

    public static boolean isLoggedInOnline() {
        return INSTANCE.isLoggedInOnline;
    }

    public static String getLogin() {
        return INSTANCE.UserNameField.getText();
    }

    public static char[] getPassword() {
        return INSTANCE.PasswordField.getPassword();
    }

    public static void clearPassword() {
        INSTANCE.PasswordField.setText("");
        INSTANCE.RememberPassword.setSelected(false);
        INSTANCE.AutoLoginCheckbox.setSelected(false);
        Preferences userPrefs = Preferences.userNodeForPackage(LoginGUI.class);
        userPrefs.remove(PASSWORD_KEY);
        userPrefs.putBoolean(AUTOLOGIN_KEY, false);
    }

    public static boolean isAutoLoginPreference() {
        Preferences userPrefs = Preferences.userNodeForPackage(LoginGUI.class);
        return userPrefs.getBoolean(AUTOLOGIN_KEY, false);
    }

    public static void closeDialog() {
        INSTANCE.setVisible(false);
    }

    private LoginGUI() {
        super(new Frame(), true);
        initComponents();
        configureComponents();
    }

    private void configureComponents() {
        getContentPane().setBackground(MlStyler.BACKGROUND_CYAN);
        checkboxPanel.setBackground(MlStyler.BACKGROUND_CYAN);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                System.exit(0);
            }
        });
        CompatDialog.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(WindowEvent e) {
                System.exit(0);
            }

        });        
        UserNameField.setForeground(MlStyler.DARK_GREEN_TITLE_COLOR);
        PasswordField.setForeground(MlStyler.DARK_GREEN_TITLE_COLOR);
    }

    private String encrypt(String input) {
        char[] crypted = new char[input.length()];
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            int cvalue = c;
            char ccrypt = (char) (cvalue + 10);
            crypted[i] = ccrypt;
        }
        return String.valueOf(crypted);
    }

    private String decypher(String input) {
        char[] clear = new char[input.length()];
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            int cvalue = c;
            char cclear = (char) (cvalue - 10);
            clear[i] = cclear;
        }
        return String.valueOf(clear);
    }

    private void readPreferences() {
        Preferences userPrefs = Preferences.userNodeForPackage(LoginGUI.class);

        boolean rememberUserName = userPrefs.getBoolean(REMEMBERUSERNAME_KEY, false);
        RememberUsername.setSelected(rememberUserName);
        if (rememberUserName) {
            userName = userPrefs.get(USERNAME_KEY, "");
            UserNameField.setText(userName);
        }

        AutoLoginCheckbox.setSelected(userPrefs.getBoolean(AUTOLOGIN_KEY, false));
        OfflineCheckBox.setSelected(userPrefs.getBoolean(OFFLINE_MODE, false));

        if (!isAuthorizedForOffline(userName)) {
            OfflineCheckBox.setVisible(false);
            OfflineCheckBox.setSelected(false);
        }

        boolean rememberPassword = userPrefs.getBoolean(REMEMBERPASSWORD_KEY, false);
        RememberPassword.setSelected(rememberPassword);
        if (rememberPassword) {
            password = userPrefs.get(PASSWORD_KEY, "");
            password = decypher(password);
            PasswordField.setText(password);
            passWordInitialized = true;
        }
        PasswordField.requestFocus();
    }

    private boolean isAutoLogin() throws MlCacheException {
        if (AutoLoginCheckbox.isSelected() && passWordInitialized) {
            return performLogin();
        }
        return false;
    }

    /**
     * This function initializes the cache subsystem checking if the user name
     * matches any cache files.
     *
     * In online mode the specified user name and password is validated by the
     * server who calls MindlinerLoginCallbackHandler. In offline mode the user
     * is authenticated by comparing the username in the cache file with the
     * specified user name.
     *
     * @return true for successful login
     */
    private void startApplication() throws MlCacheException {
        userName = UserNameField.getText();
        // startup the cache system
        MainCache mainCache = CacheEngineStatic.createMainCache(userName);
        SwingWorker startupWorker;
        startupWorker = new SwingWorker<Object, Object>() {
            @Override
            protected Object doInBackground() throws Exception {

                mainCache.initialize();
                main.mainStartupSequence();
                return null;
            }

        };
        
        // DEBUGGING SETUP
//        startupWorker.execute();
                mainCache.initialize();
                main.mainStartupSequence();
    }

    private String getUsername() {
        Preferences userPrefs = Preferences.userNodeForPackage(LoginGUI.class);
        String name = userPrefs.get(USERNAME_KEY, "");
        return name;
    }

    private String getPlainPassword() {
        Preferences userPrefs = Preferences.userNodeForPackage(LoginGUI.class);
        String pwd = userPrefs.get(PASSWORD_KEY, "");
        if (pwd != null) {
            pwd = decypher(pwd);
        }
        return pwd;
    }

    private boolean performLogin() {

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        // as more dialogs could now pop up we have to allow this one to go into the background
        Preferences p = Preferences.userNodeForPackage(LoginGUI.class);
        if (!isFormComplete()) {
            JOptionPane.showMessageDialog(null, "Please fill in username and password", "Form incomplete", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        boolean success;
        if (OfflineCheckBox.isSelected()) {
            success = doOfflineLogin();
        } else {
            success = doOnlineLogin();
        }
        if (!success) {
            JOptionPane.showMessageDialog(null, "Please restart Mindlner to try again.", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
        try {
            // upon successful login we don't need the temporary storage anymore
            p.remove(GREAT_IDEA_DESCRIPTION_KEY);
            p.remove(GREAT_IDEA_HEADLINE_KEY);
            storeCredentialsAndPrefs();
            startApplication();
        } catch (MlCacheException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Problem when starting Mindliner", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(LoginGUI.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    // Best effort approach. Only with an ejb lookup we can be fully sure, that the server is reachable.
    // But an ejb lookup may hang forever if no internet connectivity is present. Therefore we check
    // if there is at least a connection to the "internet" (i.e. to the swiss internet veteran switch). 
    private void checkInternetConnection() throws IOException {
        // sometimes we don't have internet but have a local server
        if (!Release.isDevelopmentState()) {
            URL url = new URL("http://www.switch.ch/");
            HttpURLConnection urlConnect = (HttpURLConnection) url.openConnection();
            //trying to retrieve data from the source. If there
            //is no connection, this line will fail
            Object objData = urlConnect.getContent();
        }
    }

    // check whether this client is compatible with the server
    // requires online mode
    private void checkCompatibility() {
        try {
            ReleaseManagerRemote releaseManager = (ReleaseManagerRemote) RemoteLookupAgent.getManagerForClass(ReleaseManagerRemote.class);
            Release currentServerRelease = releaseManager.getCurrentServerRelease();
            showCompatibilityDialog(currentServerRelease);
        } catch (NamingException ex) {
            setAlwaysOnTop(false);
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Could not check client compatibility", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void showCompatibilityDialog(Release serverRelease) {
        boolean show = false;

        ClientStatus cs = serverRelease.getClientStatus(Release.VERSION_NUMBER);
        System.out.println("Compatibility Status: Client is " + cs);

        switch (cs) {
            case Outdated:
                CompatStatusLabel.setText("Please upgrade. This software is no longer compatible with the current server.");
                makeTerminationButton();
                show = true;
                break;

            case OlderAndCompatible:
                CompatStatusLabel.setText("Please upgrade soon. This software is no longer current but still compatible.");
                makeProceedButton();
                show = true;
                break;

            case Current:
            case NewerAndCompatible:
                // no action for now
                break;

            case TooNew:
                CompatStatusLabel.setText("Sorry: This software is too new for the current server version.");
                makeTerminationButton();
                show = true;
                break;
        }
        if (serverRelease.getDistributionUrl().isEmpty()) {
            DownloadLatestVersion.setText("Download URL not available");
            DownloadLatestVersion.setEnabled(false);
        } else {
            DownloadLatestVersion.setEnabled(true);
            DownloadLatestVersion.setText(serverRelease.getDistributionUrl());
        }
        if (serverRelease.getReleaseNotesUrl().isEmpty()) {
            ViewReleaseNotes.setText("Release notes not available");
            ViewReleaseNotes.setEnabled(false);
        } else {
            ViewReleaseNotes.setEnabled(true);
            ViewReleaseNotes.setText(serverRelease.getReleaseNotesUrl());
        }
//        setVisible(false);
        if (show) {
            /**
             * Make sure the login main dialog is not blocking the modal compat
             * dialog which needs to be modal to give the user some feedback
             * before proceeding with startup or terminating
             */
            setAlwaysOnTop(false);
            CompatDialog.setLocationRelativeTo(this);
            CompatDialog.setVisible(true);
        }
    }

    private void makeTerminationButton() {
        ActionButton.setText("Quit");
        ActionButton.addActionListener((ActionEvent e) -> {
            System.exit(1);
        });
    }

    private void makeProceedButton() {
        ActionButton.setText("Continue");
        ActionButton.addActionListener((ActionEvent e) -> {
            CompatDialog.setVisible(false);
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        CompatDialog = new javax.swing.JDialog();
        jLabel3 = new javax.swing.JLabel();
        CompatStatusLabel = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        DownloadLatestVersion = new javax.swing.JButton();
        ViewReleaseNotes = new javax.swing.JButton();
        ActionButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        UserNameField = new javax.swing.JTextField();
        PasswordLabel = new javax.swing.JLabel();
        LoginButton = new javax.swing.JButton();
        PasswordField = new javax.swing.JPasswordField();
        jLabel2 = new javax.swing.JLabel();
        checkboxPanel = new javax.swing.JPanel();
        RememberUsername = new javax.swing.JCheckBox();
        RememberPassword = new javax.swing.JCheckBox();
        AutoLoginCheckbox = new javax.swing.JCheckBox();
        OfflineCheckBox = new javax.swing.JCheckBox();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/mindliner/resources/General"); // NOI18N
        CompatDialog.setTitle(bundle.getString("CompatibilityDialog")); // NOI18N
        CompatDialog.setMinimumSize(new java.awt.Dimension(700, 200));
        CompatDialog.setModal(true);

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel3.setText(bundle.getString("CompatibilityDialogStatus")); // NOI18N

        CompatStatusLabel.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        CompatStatusLabel.setText("status");

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel5.setText(bundle.getString("CompatibilityNewVersionDownloadURL")); // NOI18N

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel4.setText(bundle.getString("CompatibilityLastVersionReleaseNotes")); // NOI18N

        DownloadLatestVersion.setText(bundle.getString("CompatibilityDownloadLatestVersionBtn")); // NOI18N
        DownloadLatestVersion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DownloadLatestVersionActionPerformed(evt);
            }
        });

        ViewReleaseNotes.setText(bundle.getString("CompatibilityViewReleaseNotesBtn")); // NOI18N
        ViewReleaseNotes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ViewReleaseNotesActionPerformed(evt);
            }
        });

        ActionButton.setText(bundle.getString("CompatibilityContinueAsIs")); // NOI18N
        ActionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ActionButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout CompatDialogLayout = new javax.swing.GroupLayout(CompatDialog.getContentPane());
        CompatDialog.getContentPane().setLayout(CompatDialogLayout);
        CompatDialogLayout.setHorizontalGroup(
            CompatDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CompatDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(CompatDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(CompatDialogLayout.createSequentialGroup()
                        .addGroup(CompatDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(CompatDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jLabel4))
                        .addGap(18, 18, 18)
                        .addGroup(CompatDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(CompatStatusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(DownloadLatestVersion, javax.swing.GroupLayout.DEFAULT_SIZE, 473, Short.MAX_VALUE)
                            .addComponent(ViewReleaseNotes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, CompatDialogLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(ActionButton)))
                .addContainerGap())
        );
        CompatDialogLayout.setVerticalGroup(
            CompatDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CompatDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(CompatDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(CompatStatusLabel))
                .addGap(18, 18, 18)
                .addGroup(CompatDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(DownloadLatestVersion))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(CompatDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(ViewReleaseNotes))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ActionButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setTitle(bundle.getString("MindlinerLoginModuleTitle")); // NOI18N
        setAlwaysOnTop(true);

        jLabel1.setFont(jLabel1.getFont().deriveFont(jLabel1.getFont().getSize()+1f));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText(bundle.getString("UserNameLabel")); // NOI18N
        jLabel1.setFocusable(false);

        UserNameField.setFont(UserNameField.getFont().deriveFont(UserNameField.getFont().getSize()+4f));
        UserNameField.setForeground(new java.awt.Color(71, 114, 134));

        PasswordLabel.setFont(PasswordLabel.getFont().deriveFont(PasswordLabel.getFont().getSize()+1f));
        PasswordLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        PasswordLabel.setText(bundle.getString("PasswordLabel")); // NOI18N
        PasswordLabel.setFocusable(false);

        LoginButton.setFont(LoginButton.getFont());
        LoginButton.setText(bundle.getString("LoginOKButton")); // NOI18N
        LoginButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LoginButtonActionPerformed(evt);
            }
        });

        PasswordField.setFont(PasswordField.getFont().deriveFont(PasswordField.getFont().getSize()+4f));
        PasswordField.setForeground(new java.awt.Color(71, 114, 134));
        PasswordField.setMaximumSize(new java.awt.Dimension(180, 30));
        PasswordField.setMinimumSize(new java.awt.Dimension(180, 30));
        PasswordField.setPreferredSize(new java.awt.Dimension(180, 30));
        PasswordField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PasswordFieldKeyPressed(evt);
            }
        });

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/LoginScreenIconBlue64.jpg"))); // NOI18N
        jLabel2.setOpaque(true);

        checkboxPanel.setBackground(new java.awt.Color(255, 255, 255));

        RememberUsername.setFont(RememberUsername.getFont().deriveFont(RememberUsername.getFont().getSize()-1f));
        RememberUsername.setText(bundle.getString("LoginRemberUsername")); // NOI18N
        RememberUsername.setFocusable(false);
        RememberUsername.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RememberUsernameActionPerformed(evt);
            }
        });

        RememberPassword.setFont(RememberPassword.getFont().deriveFont(RememberPassword.getFont().getSize()-1f));
        RememberPassword.setText(bundle.getString("LoginRemeberPasswordCheckbox")); // NOI18N
        RememberPassword.setFocusable(false);
        RememberPassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RememberPasswordActionPerformed(evt);
            }
        });

        AutoLoginCheckbox.setFont(AutoLoginCheckbox.getFont().deriveFont(AutoLoginCheckbox.getFont().getSize()-1f));
        AutoLoginCheckbox.setText(bundle.getString("LoginAutoLoginCheckbox")); // NOI18N
        AutoLoginCheckbox.setToolTipText(bundle.getString("LoginAutoLogin_TT")); // NOI18N
        AutoLoginCheckbox.setFocusable(false);
        AutoLoginCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AutoLoginCheckboxActionPerformed(evt);
            }
        });

        OfflineCheckBox.setFont(OfflineCheckBox.getFont().deriveFont(OfflineCheckBox.getFont().getSize()-1f));
        OfflineCheckBox.setText(bundle.getString("LoginOfflineMode")); // NOI18N
        OfflineCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OfflineCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout checkboxPanelLayout = new javax.swing.GroupLayout(checkboxPanel);
        checkboxPanel.setLayout(checkboxPanelLayout);
        checkboxPanelLayout.setHorizontalGroup(
            checkboxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(checkboxPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(checkboxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(AutoLoginCheckbox)
                    .addComponent(OfflineCheckBox)
                    .addComponent(RememberPassword)
                    .addComponent(RememberUsername))
                .addContainerGap(27, Short.MAX_VALUE))
        );
        checkboxPanelLayout.setVerticalGroup(
            checkboxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(checkboxPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(RememberUsername)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(RememberPassword)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(AutoLoginCheckbox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(OfflineCheckBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE)
                    .addComponent(PasswordLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(LoginButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(checkboxPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(PasswordField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(UserNameField, javax.swing.GroupLayout.Alignment.LEADING))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2)))
                .addGap(21, 21, 21))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(UserNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(8, 8, 8)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addComponent(PasswordField, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE))
                            .addComponent(PasswordLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkboxPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(LoginButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void LoginButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LoginButtonActionPerformed
        performLogin();
    }//GEN-LAST:event_LoginButtonActionPerformed

    private void RememberPasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RememberPasswordActionPerformed
        if (RememberPassword.isSelected() == true) {
            int reply = JOptionPane.showConfirmDialog(MindlinerMain.getInstance(),
                    "I accept that my password will be stored on the disk", "Security Information", JOptionPane.YES_NO_OPTION);
            if (reply != JOptionPane.YES_OPTION) {
                RememberPassword.setSelected(false);
            } else {
                RememberUsername.setSelected(true);
            }
        }
    }//GEN-LAST:event_RememberPasswordActionPerformed

    private void AutoLoginCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AutoLoginCheckboxActionPerformed
        if (AutoLoginCheckbox.isSelected()) {
            if (RememberPassword.isSelected() == false || RememberUsername.isSelected() == false) {
                JOptionPane.showMessageDialog(null, "You must check to remember username and password for autologin.",
                        "Auto-Login Requirements",
                        JOptionPane.ERROR_MESSAGE);
                AutoLoginCheckbox.setSelected(false);
            }
        }
    }//GEN-LAST:event_AutoLoginCheckboxActionPerformed

    private void PasswordFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PasswordFieldKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            performLogin();
        }
    }//GEN-LAST:event_PasswordFieldKeyPressed

    private void OfflineCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OfflineCheckBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_OfflineCheckBoxActionPerformed

    private void ViewReleaseNotesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ViewReleaseNotesActionPerformed
        try {
            BrowserLauncher.openWebpage(new URL(ViewReleaseNotes.getText()));
        } catch (MalformedURLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Sorry: Cannot Open Release Notes", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_ViewReleaseNotesActionPerformed

    private void DownloadLatestVersionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DownloadLatestVersionActionPerformed
        try {
            BrowserLauncher.openWebpage(new URL(DownloadLatestVersion.getText()));
            System.exit(0);
        } catch (MalformedURLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Sorry: Cannot Open Download Page", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_DownloadLatestVersionActionPerformed

    private void ActionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ActionButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ActionButtonActionPerformed

    private void RememberUsernameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RememberUsernameActionPerformed
        if (!RememberUsername.isSelected()){
            RememberPassword.setSelected(false);
            AutoLoginCheckbox.setSelected(false);
        }
    }//GEN-LAST:event_RememberUsernameActionPerformed
    private MindlinerMain main = null;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton ActionButton;
    private javax.swing.JCheckBox AutoLoginCheckbox;
    private javax.swing.JDialog CompatDialog;
    private javax.swing.JLabel CompatStatusLabel;
    private javax.swing.JButton DownloadLatestVersion;
    private javax.swing.JButton LoginButton;
    private javax.swing.JCheckBox OfflineCheckBox;
    private javax.swing.JPasswordField PasswordField;
    private javax.swing.JLabel PasswordLabel;
    private javax.swing.JCheckBox RememberPassword;
    private javax.swing.JCheckBox RememberUsername;
    private javax.swing.JTextField UserNameField;
    private javax.swing.JButton ViewReleaseNotes;
    private javax.swing.JPanel checkboxPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    // End of variables declaration//GEN-END:variables

    private void storeCredentialsAndPrefs() {
        Preferences userPrefs = Preferences.userNodeForPackage(LoginGUI.class);
        userPrefs.putBoolean(REMEMBERUSERNAME_KEY, RememberUsername.isSelected());
        userPrefs.put(USERNAME_KEY, UserNameField.getText());

        userPrefs.putBoolean(REMEMBERPASSWORD_KEY, RememberPassword.isSelected());
        char[] pwd = PasswordField.getPassword();
        String passString = new String(pwd);
        userPrefs.put(PASSWORD_KEY, encrypt(passString));

        userPrefs.putBoolean(AUTOLOGIN_KEY, AutoLoginCheckbox.isSelected());
        userPrefs.putBoolean(OFFLINE_MODE, OfflineCheckBox.isSelected());
    }

    private boolean isFormComplete() {
        if (UserNameField.getText() == null || UserNameField.getText().isEmpty()) {
            return false;
        }
        if (PasswordField.getPassword() == null || PasswordField.getPassword().length == 0) {
            return false;
        }
        return true;
    }

    private boolean doOfflineLogin() throws HeadlessException {
        String storedName = getUsername();
        String storedPwd = getPlainPassword();
        if (storedName == null || storedPwd == null) {
            JOptionPane.showMessageDialog(null, "You need to login online at least once, before you can work offline", "Online login required", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        String enteredPwd = new String(PasswordField.getPassword());
        if (!storedName.equals(UserNameField.getText()) && !storedPwd.contentEquals(enteredPwd)) {
            JOptionPane.showMessageDialog(null, "Invalid username or password.", "Authentication failure", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        OnlineManager.goOffline();
        return true;
    }

    private boolean doOnlineLogin() throws HeadlessException {
        if (isLoggedInOnline) {
            return true;
        }
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            try {
                checkInternetConnection();
            } catch (IOException ex) {
                setAlwaysOnTop(false);
                JOptionPane.showMessageDialog(null, "No internet connection available. Use Offline Mode.", "Network failure", JOptionPane.ERROR_MESSAGE);
                Logger.getLogger(LoginGUI.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
            try {
                // Only start a new thread if there isn't already a hanging login thread,
                // because a login thread may hang until the EJB lookup is successfull. 
                if (loginThread == null || !loginThread.isAlive()) {
                    loginThread = new LoginThread();
                    loginThread.start();
                    loginThread.join(LOGIN_TIMEOUT);
                    if (loginThread.isHasError()) {
                        throw loginThread.getException();
                    } else if (loginThread.isAlive()) {
                        setAlwaysOnTop(false);
                        throw new Exception("LoginThread exceeded timeout, probably server is not reachable");
                    }
                } else {
                    setAlwaysOnTop(false);
                    throw new Exception("LoginThread is still hanging from last login try, probably server is still not reachable");
                }
                checkCompatibility();
                return true;
            } catch (Exception ex) {
                setAlwaysOnTop(false);
                String msg = "Login failed. Invalid username or password or network failure";
                JOptionPane.showMessageDialog(null, msg, "Authentication failed", JOptionPane.ERROR_MESSAGE);
                Logger.getLogger(LoginGUI.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        } finally {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    // Checks whether the entered user is authorized for offline mode
    private boolean isAuthorizedForOffline(String username) {
        MlSessionClientParams.setCurrentLoginName(username);
        FileInputStream fis = null;
        try {
            File f = new File(CacheEngineStatic.getDataCacheFilePath(MainCache.MAIN_CACHE_FILE_EXTENSION));
            fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);
            // just skipping version here as it's not needed
            Integer version = (Integer) ois.readObject();
            mlcUser cacheUser = (mlcUser) ois.readObject();
            boolean isAuthorized = cacheUser.isAuthorizedForFeature(SoftwareFeature.CurrentFeatures.OFFLINE_MODE);
            return isAuthorized;
        } catch (IOException ex) {
            Logger.getLogger(LoginGUI.class.getName()).log(Level.INFO, "No cache found. User will not be allowed for offline mode. [{0}]", ex.getMessage());
            return false;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(LoginGUI.class.getName()).log(Level.SEVERE, "Invalid cache found, user will not be allowed for offline mode", ex);
            return false;
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(LoginGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * EJB lookup call for login may hang forever. Therefore we do login in a
     * background thread.
     */
    private class LoginThread extends Thread {

        private Exception exception = null;

        public boolean isHasError() {
            return exception != null;
        }

        public Exception getException() {
            return exception;
        }

        @Override
        public void run() {
            try {
                if (RemoteLookupAgent.isLocalStart()) {
                    // in case of AAC start (i.e. webstart), no programmaticlogin is needed because a EJB lookup will automatically trigger the login callback handler
                    ProgrammaticLogin l = new ProgrammaticLogin();
                    boolean success = l.login(getLogin(), getPassword());
                    if (!success) {
                        setAlwaysOnTop(false);
                        throw new Exception("Programmatic login failed");
                    }
                }
                userManager = (UserManagerRemote) RemoteLookupAgent.getManagerForClass(UserManagerRemote.class);
                userManager.login();
                isLoggedInOnline = true;
            } catch (NamingException ex) {
                setAlwaysOnTop(false);
                Logger.getLogger(LoginGUI.class.getName()).log(Level.SEVERE, "Login failed", ex);
                exception = ex;
            } catch (Exception ex) {
                setAlwaysOnTop(false);
                Logger.getLogger(LoginGUI.class.getName()).log(Level.SEVERE, null, ex);
                exception = ex;
            }
        }

    }
}
