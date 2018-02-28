package com.mindliner.main;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.cache.CacheMonitor;
import com.mindliner.cache.DefaultObjectAttributes;
import com.mindliner.cache.MlCacheException;
import com.mindliner.cal.WeekNumbering;
import com.mindliner.categories.mlsPriority;
import com.mindliner.clientobjects.MlcContainerMap;
import com.mindliner.clientobjects.mlcContact;
import com.mindliner.clientobjects.mlcKnowlet;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcObjectCollection;
import com.mindliner.clientobjects.mlcTask;
import com.mindliner.clientobjects.mlcUser;
import com.mindliner.commands.CommandRecorder;
import com.mindliner.commands.UndoObserver;
import com.mindliner.common.UserAuthenticationDialog;
import com.mindliner.connector.GoogleDriveConnector;
import com.mindliner.connector.SftpConnector;
import com.mindliner.entities.Colorizer;
import com.mindliner.entities.Release;
import com.mindliner.entities.SoftwareFeature;
import com.mindliner.enums.ObjectReviewStatus;
import com.mindliner.events.MindlinerMessageListener;
import com.mindliner.events.ObjectChangeManager;
import com.mindliner.events.SelectionManager;
import com.mindliner.events.SelectionObserver;
import com.mindliner.gui.ContactEditor;
import com.mindliner.gui.StatusBar;
import com.mindliner.gui.ObjectEditor;
import com.mindliner.gui.UserAccountDialog;
import com.mindliner.gui.color.ColorManager;
import com.mindliner.gui.color.FixedKeyColorizer;
import com.mindliner.gui.color.FixedKeyColorizer.FixedKeys;
import com.mindliner.gui.tablemanager.TableManager;
import com.mindliner.img.icons.MlIconLoader;
import com.mindliner.img.icons.MlIconManager;
import com.mindliner.managers.UserManagerRemote;
import com.mindliner.prefs.DividerLocationPreferences;
import com.mindliner.prefs.MlMainPreferenceEditor;
import com.mindliner.prefs.MlPreferenceManager;
import com.mindliner.styles.MlStyler;
import com.mindliner.prefs.SearchPreferences;
import com.mindliner.prefs.file.FilePreferencesFactory;
import com.mindliner.serveraccess.BandwidthTester;
import com.mindliner.serveraccess.OnlineManager;
import com.mindliner.serveraccess.OnlineManager.ExecutionMode;
import com.mindliner.serveraccess.OnlineService;
import com.mindliner.serveraccess.RemoteLookupAgent;
import com.mindliner.synch.SynchConfigurator;
import com.mindliner.synch.SynchronizationManager;
import com.mindliner.view.Mindliner2DViewer;
import com.mindliner.view.connectors.NodeConnection;
import com.mindliner.view.dispatch.MlObjectViewer;
import com.mindliner.view.dispatch.MlViewDispatcherImpl;
import com.mindliner.view.containermap.ContainerMap;
import static com.mindliner.view.dispatch.MlObjectViewer.ViewType.Map;
import com.mindliner.view.news.NewsContainer;
import com.mindliner.weekplanner.WeekPlanner;
import com.mindliner.weekplanner.WorkTracker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.naming.NamingException;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import mindlinerstarter.OSValidator;
import com.mindliner.events.HeartBeatTask;
import com.mindliner.gui.ObjectEditorLauncher;
import com.mindliner.weekplanner.WeekPlanChangeManager;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.TimerTask;

/**
 * The main class of the Mindliner desktop client application.
 *
 * @author Marius Messerli
 */
public class MindlinerMain extends javax.swing.JFrame implements OnlineService, UndoObserver, SelectionObserver, MlObjectViewer {

    private static final int DEFAULT_TABLE_PANE_HEIGHT = 200;
    private static final String LOOKANDFEEL_KEY = "lookandfeelname";
    private static final String POWERPOINT_IMPORT_DIRECTORY_KEY = "pptimportloc";
    private static final String FREEMIND_IMPORT_DIRECTORY_KEY = "freemindimportloc";

    private static final long serialVersionUID = 500L;
    private static MindlinerMain instance = null;
    private static final Logger LOGGER = Logger.getLogger(MindlinerMain.class.getName());
    private static boolean initialized = false;

    private StatusBar statusBar = null;
    private String lookAndFeelName = "undefined";

    private OnlineStatus onlineStatus = OnlineStatus.offline;
    private int connectionPriority = 0;
    private java.util.Timer timer = null;
    private HeartBeatTask heartBeatTask = null;
    private final ContactEditor contactEditor;
    private final BulkUpdater bulkUpdater = new BulkUpdater();
    private Mindliner2DViewer mindmapView = null;
    private ContainerMap worksphereMapView = null;
    private WeekPlanner weekPlanView = null;
    private NewsContainer newsView = null;
    private final List<MlOverlayPane> overlayViews = new ArrayList<>();
    private JPanel currentOverlayPanel = null;
    private String hostname = "";
    private Date lastIncomingUpdateMessage = new Date();
    private static final int NO_UPDATE_RECEIVED_INTERVAL = 500; // ms - indicates that no updates have been received in indicated ms

    // debugging
    private int startupYieldPeriod = 1500;
    private boolean ignoreNewsFeature = false;

    static {
        // read in configuration for java util logging
        final InputStream inputStream = MindlinerMain.class.getResourceAsStream("/META-INF/logging.properties");
        try {
            LogManager.getLogManager().readConfiguration(inputStream);
        } catch (final IOException e) {
            Logger.getLogger(MindlinerMain.class.getName()).log(Level.SEVERE, "Could not load logging properties", e);
        }
    }

    private void setBugReportText(mlcKnowlet k) {
        k.setHeadline("dbg: ");
        StringBuilder descr = new StringBuilder();
        descr.append("Bug description:").append("\n\n\n");
        descr.append("Reproduction steps:").append("\n\n\n");
        descr.append("Version: ").append(Release.VERSION_STRING).append(", ").append(Release.VERSION_NUMBER);
        k.setDescription(descr.toString());
    }

    private static enum ApplicationViewType {

        Editor, Sidebar
    }

    public static void main(String args[]) throws URISyntaxException {

        // we are using a file as preferences backing store rather than the registry in the case of Windows
        System.setProperty("java.util.prefs.PreferencesFactory", FilePreferencesFactory.class.getName());

        // specify absolute path in case of mac environment (relative paths are searched for in users directory)
        
        if (OSValidator.isMac()) {
            String fs = System.getProperty("file.separator");
            String jarPath = MindlinerMain.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            String basePath = null;
            // remove the jar file itself from the path
            if (jarPath != null && jarPath.length() > 0) {
                int endIndex = jarPath.lastIndexOf(fs);
                if (endIndex != -1) {
                    basePath = jarPath.substring(0, endIndex); // not forgot to put check if(endIndex != -1)
                }
            }
            if (basePath == null) {
                throw new IllegalStateException("Cannot determine base path for login resources");
            }
            String conf = basePath.concat(fs).concat("config").concat(fs);
            String login = conf.concat("login.conf");
            String cacerts = conf.concat("cacerts");
            System.getProperties().setProperty("javax.net.ssl.trustStore", cacerts);
            System.getProperties().setProperty("java.security.auth.login.config", login);
        }

        instance = new MindlinerMain();
        MlViewDispatcherImpl.getInstance().registerViewer(instance);

        SwingUtilities.invokeLater(() -> {
            LoginGUI.initialize(instance);
        });
    }

    public static void createNewObject(Class c, mlcObject relative) {
        instance.editNewMindlinerObject(c, relative, "", "");
    }

    public static MindlinerMain getInstance() {
        return instance;
    }

    public static StatusBar getStatusBar() {
        return instance.statusBar;
    }

    public MindlinerMain() {
        System.out.println("Start: " + new Date());
        System.out.println("Starting " + Release.VERSION_STRING);
        updateLookAndFeel();
        loadPreferences();
        updateLookAndFeel();
        initComponents();
        AboutDialog.setUndecorated(true);
        // the view menu gets reactivated later if necessary
        ViewMenu.setVisible(false);
        ButtonGroup bg = new ButtonGroup();
        bg.add(PerformanceAsynchExecMode);
        bg.add(PerformanceRealtimeExecMode);
        contactEditor = new ContactEditor(this, true);
        loadGuiInitPreferences();

        this.addWindowListener(
                new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e
            ) {
                terminateClientApplication();
            }
        }
        );

        addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                final Dimension size = e.getComponent().getSize();
                if (currentOverlayPanel != null) {
                    expandView(currentOverlayPanel);
                }
                invalidate();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });

    }

    @Override
    public void goOffline() {
        if (heartBeatTask != null && !heartBeatTask.isForcedOfflineMode()) {
            if (timer != null) {
                timer.cancel();
            }
        }
        PerformCacheMaintenance.setEnabled(false);
        FileSynchronizeMenuItem.setEnabled(false);
        ExecutionMenu.setEnabled(false);
        ChangePasswordButton.setEnabled(false);
        onlineStatus = OnlineStatus.offline;
    }

    /**
     * @todo The fact that this class "wires up" the cache engine by providing a
     * search filter is not nice.
     * @todo The fact that this class takes care of the SynchManagers online
     * status is not nice - move to SynchManager
     */
    @Override
    public void goOnline() {
        PerformCacheMaintenance.setEnabled(true);
        FileSynchronizeMenuItem.setEnabled(true);
        ChangePasswordButton.setEnabled(true);
        ExecutionMenu.setEnabled(true);
        if (CacheEngineStatic.getCurrentUser().isAuthorizedForFeature(SoftwareFeature.CurrentFeatures.SYNCH_BASICS)) {
            SynchronizationManager.loadSynchActors();
            // the following call requires the SynchronizationManager initialized
            SynchConfigurator.configure();
        } else {
            FileSynchronizeMenuItem.setVisible(false);
        }
        onlineStatus = OnlineStatus.online;
        if (heartBeatTask == null || !heartBeatTask.isForcedOfflineMode()) {
            if (timer != null) {
                timer.cancel();
            }
            timer = new java.util.Timer("Mindliner Heartbeat Timer");
            heartBeatTask = new HeartBeatTask();
            timer.schedule(heartBeatTask, 0, UserManagerRemote.HEARTBEAT_INTERVALL);
        } else {
            // heart beat task forced mindliner into offline mode. Therefore the heartbeat task also initiated the goOnline (-> heartbeat task is still running)
            heartBeatTask.setForcedOfflineMode(false);
        }
    }

    @Override
    public OnlineStatus getStatus() {
        return onlineStatus;
    }

    @Override
    public String getServiceName() {
        return "Mindliner Main";
    }

    @Override
    public void setUndoControlItemText(String text) {
        UndoMenuItem.setText("Undo " + text);
    }

    @Override
    public int getConnectionPriority() {
        return connectionPriority;
    }

    @Override
    public void setConnectionPriority(int priority) {
        connectionPriority = priority;
    }

    private void adaptToType(ViewType type) {

        switch (type) {
            case Map:
                expandView(mindmapView);
                break;

            case ContainerMap:
                if (worksphereMapView != null) {
                    expandView(worksphereMapView);
                }
                break;

            default:
                ;
        }
    }

    @Override
    public void display(mlcObject object, ViewType type) {
        adaptToType(type);
    }

    @Override
    public void display(List<mlcObject> objects, ViewType type) {
        adaptToType(type);
    }

    @Override
    public boolean isSupported(ViewType type) {
        /**
         * Here the action that is required for display is different than for
         * otehr viewers. The only thing THIS needs to do is to change the
         * viewing tabs so that either the map or the spreadsheet is showing.
         */
        return (type == ViewType.Map || type == ViewType.Spreadsheet || type == ViewType.ContainerMap);
    }

    @Override
    public void back() {
    }

    @Override
    public void selectionChanged(List<mlcObject> selection) {

        if (!selection.isEmpty() && selection.get(selection.size() - 1).getStatus().equals(ObjectReviewStatus.REVIEWED)) {
            ObjectReviewed.setSelected(true);
        } else {
            ObjectReviewed.setSelected(false);
        }
    }

    @Override
    public void clearSelections() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void connectionSelectionChanged(List<NodeConnection> selection) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clearConnectionSelections() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void updateLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            if (OSValidator.isMac()) {
                // put the main menu on the screen menu bar 
                System.setProperty("apple.laf.useScreenMenuBar", "true");
            }
        } catch (UnsupportedLookAndFeelException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(MindlinerMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadPreferences() {
        Preferences userPrefs = Preferences.userNodeForPackage(MindlinerMain.class);
        startupYieldPeriod = userPrefs.getInt("STARTUP_YIELD_PERIOD", startupYieldPeriod);
        System.out.println("Startup yield period is " + startupYieldPeriod + " millis.");
        ignoreNewsFeature = userPrefs.getBoolean("IGNORE_NEWS_FEATURE", ignoreNewsFeature);
        // debug: write is just so I can find it easily in the prefs for updating
        userPrefs.putInt("STARTUP_YIELD_PERIOD", startupYieldPeriod);
        userPrefs.putBoolean("IGNORE_NEWS_FEATURE", ignoreNewsFeature);
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("config/server.properties"));
            hostname = props.getProperty("org.omg.CORBA.ORBInitialHost");
        } catch (IOException ex) {
            // keep quite no harm done - hostname is simply missing from the window title bar
        }
        MlPreferenceManager.initializePreferences();
    }

    private void loadGuiInitPreferences() {
        Preferences userPrefs = Preferences.userNodeForPackage(MindlinerMain.class);
        lookAndFeelName = userPrefs.get(LOOKANDFEEL_KEY, lookAndFeelName);
    }

    public void mainStartupSequence() {
        mainStartupSequenceInvokedLater();
    }

    /**
     *********************************************************************
     * The Main Mindliner Startup Sequence
     *
     * This sequence is quite critical and moving code blocks around often
     * result in NPEs.
     *
     ********************************************************************
     */
    public void mainStartupSequenceInvokedLater() {

        System.out.println(new Date() + "Loading Mindliner modules ....");
        if (LoginGUI.isAutoLoginPreference()) {
            ForceLoginScreen.setVisible(true);
        } else {
            ForceLoginScreen.setVisible(false);
        }
        setStartupConfiguration(true);
        statusBar = new StatusBar();
        CacheEngineStatic.setStatusReporter(statusBar);
        StatusPanel.add(statusBar);
        DividerLocationPreferences.loadPreferences();
        this.setSize(MlPreferenceManager.desktopPrefs.getWidth(), MlPreferenceManager.desktopPrefs.getHeight());
        this.setLocation(MlPreferenceManager.desktopPrefs.getLocationX(), MlPreferenceManager.desktopPrefs.getLocationY());
        applyFrameAndSplitPanePreferences();
        LoginGUI.closeDialog();
        setVisible(true);
        MyMenu.setText(CacheEngineStatic.getCurrentUser().getFirstName());
        statusBar.setMessage("performing background startup...", StatusBar.INDETERMINATE);
        DefaultObjectAttributes.loadPreferences();

        statusBar.setMessage("starting online manager ...");
        OnlineManager.setStatusReporter(statusBar);
        OnlineManager.setOnlineLabel(OnlineLabel, hostname);
        OnlineManager.getInstance().loadPreferences();
        if (OnlineManager.waitForServerMessages()) {
            PerformanceAsynchExecMode.setSelected(false);
            PerformanceRealtimeExecMode.setSelected(true);
        } else {
            PerformanceAsynchExecMode.setSelected(true);
            PerformanceRealtimeExecMode.setSelected(false);
        }

        statusBar.setMessage("starting search subsystem ...");
        SearchPreferences.loadPreferences();
        ColorManager.initialize();

        // the following call depends on SearchPreferences.loadPreferences() having been issued
        SearchPanel searchFilter = SearchPanel.getUniqueInstance();

        ControlPane.add(searchFilter.getToolBarAdditionsPane(), BorderLayout.CENTER);
        mlcUser u = CacheEngineStatic.getCurrentUser();

        setTitle(Release.VERSION_STRING + " (".concat(u.getFirstName()).concat(" ").concat(u.getLastName()).concat(")"));

        statusBar.setMessage("initializing tables ...");

        // the TablePane is to  be deleted; not used anymore
        TablePane.setVisible(false);

        // THE MIND MAPPER AND SCATTER PLOTTER
        statusBar.setMessage("initialize mindmap viewer ...");
        mindmapView = new Mindliner2DViewer(statusBar);
        mindmapView.setName("Mindmap");
        MlViewDispatcherImpl.getInstance().registerViewer(mindmapView);
        if (OnlineManager.isOnline()) {
            mindmapView.goOnline();
        } else {
            mindmapView.goOffline();
        }
        mindmapView.setConnectionPriority(OnlineService.LOW_PRIORITY);
        OnlineManager.getInstance().registerService(mindmapView);
        ObjectChangeManager.registerObserver(mindmapView);

        // HIDE CUSTOMIZATION FUNCTIONS
        if (!CacheEngineStatic.getCurrentUser().isAuthorizedForFeature(SoftwareFeature.CurrentFeatures.CUSTOMIZATION)) {
            MiscellaneousMenu.setVisible(false);
            PerformanceMenu.setVisible(false);
            PreferenceMenu.setVisible(false);
            CacheMenu.setVisible(false);
            EditMenuSeparator.setVisible(false);

            // if the defaults dialog is not visible we switch to auto-updating the defaults with the last edit
            CreationDefaultsMenuItem.setVisible(false);
            DefaultObjectAttributes.setAdoptLastEdit(true);

        }

        // THE WEEKPLAN AND TIME MANAGEMENT
        statusBar.setMessage("initialize week plans...");

        if (CacheEngineStatic.getCurrentUser().isAuthorizedForFeature(SoftwareFeature.CurrentFeatures.TIME_MANAGEMENT)) {
            weekPlanView = new WeekPlanner();
            weekPlanView.setName("Week Plan");
            WeekPlanChangeManager.addObserver(weekPlanView);
            ObjectChangeManager.registerObserver(weekPlanView);
        } else {
            WeekPlanMenu.setVisible(false);
        }

        statusBar.setMessage("initializing preference dialog ...");

        MlMainPreferenceEditor.getInstance(mindmapView);

        Date now = new Date();
        WeekPlanChangeManager.weekChanged(WeekNumbering.getYear(now), WeekNumbering.getWeek(now));

        // for now make sure to register this service as last due to
        // the re-initialization of CacheEngine's search filter after an off-line session
        setConnectionPriority(OnlineService.LOW_PRIORITY);
        OnlineManager.getInstance().registerService(this);

        statusBar.setMessage("initializing command recorder ...");
        CommandRecorder.getInstance().registerUndoObserver(this);

        statusBar.setMessage("adapting to online status ...");
        // hide all features related to online/offline management for users who are not authorized for this feature
        if (CacheEngineStatic.getCurrentUser().isAuthorizedForFeature(SoftwareFeature.CurrentFeatures.OFFLINE_MODE)) {
            if (OnlineManager.isOnline()) {
                //goOnline(); OnlineManager.goOnline is called at the end, so why goOnlin already here?
            } else {
                goOffline();
            }
        } else {
            OnlineManager.goOnline();
            OnlineLabel.setVisible(false);
            OfflineMenu.setVisible(false);
            ConnectionLabel.setVisible(false);
            FileSaveMenu.setVisible(false);
        }

        if (CacheEngineStatic.getCurrentUser().isAuthorizedForFeature(SoftwareFeature.CurrentFeatures.WORKSPHEREMAP)) {
            statusBar.setMessage("initializing worksphere map...");
            worksphereMapView = new ContainerMap();
            worksphereMapView.setName("Worksphere Map");
            worksphereMapView.initialize();;
            MlViewDispatcherImpl.getInstance().registerViewer(worksphereMapView);
        } else {
            insertWsm.setVisible(false);
            WorkSphereMapMenu.setVisible(false);
        }

        statusBar.setMessage("initializing object change listener...");
        MindlinerMessageListener messageListener = new MindlinerMessageListener();
        messageListener.setConnectionPriority(OnlineService.LOW_PRIORITY);
        OnlineManager.getInstance().registerService(messageListener);
        CommandRecorder cr = CommandRecorder.getInstance();
        cr.setTrafficControl(messageListener); // need this to turn listening off during synching of offline commands when coming online
        Thread changeListener = new Thread(messageListener);
        changeListener.start();

        if (OnlineManager.isOnline() && !ignoreNewsFeature && CacheEngineStatic.getCurrentUser().isAuthorizedForFeature(SoftwareFeature.CurrentFeatures.SUBSCRIPTION)) {
            newsView = new NewsContainer();
            newsView.setName("News");
            newsView.initialize();
        } else {
            NewsMenu.setVisible(false);
        }

        try {
            statusBar.setMessage("configuring view overlay pane ...");
            Thread.sleep(startupYieldPeriod);
            assembleOverlayPanes();
            statusBar.setMessage("done re-arranging overlay pane!");
            // colorize only now that all tabbed panes are populated
            colorizeComponents();

            /**
             * The following statement looks useless and unnecessary but it is
             * not. Now that we have started and registered all OnlineServices
             * we need another goOnline pass through all of them because some
             * dependencies could not be satisfied before.
             */
            if (OnlineManager.isOnline()) {
                Thread.sleep(startupYieldPeriod);
            }
            if (OnlineManager.isOnline()) {
                OnlineManager.goOnline();
            } else {
                statusBar.done();
            }
//            if (OnlineManager.isOnline()) Thread.sleep(startupYieldPeriod);
        } catch (InterruptedException ex) {
            Logger.getAnonymousLogger().warning(ex.getMessage());
        }
        setStartupConfiguration(false);

        // if a new version is run for the first time we have to force cache maintenance
        if (MlPreferenceManager.isRunCacheMaintenance()) {
            try {
                CacheEngineStatic.performMaintenance(true);
                persistApplicationState(false);
            } catch (MlCacheException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "New Version First-Time Startup", JOptionPane.ERROR_MESSAGE);
            }
        }

        /**
         * The following lines are necessary for users who have been upgraded to
         * custom tables and who's custom table pane is zero pixels high. They
         * lose their search table in the toolbar but would not see (yet) the
         * custom tables.
         */
        if (TablePane.getHeight() == 0) {
            int defaultHeight = Math.max(MainSplitPane.getHeight() - DEFAULT_TABLE_PANE_HEIGHT, MainSplitPane.getHeight() / 2);
            MainSplitPane.setDividerLocation(defaultHeight);
        }

        EditorLabelUpdateTask elut = new EditorLabelUpdateTask();
        new Timer("EditorLabelUpdateTask").schedule(elut, 0, 1000);

        // this task checks if there were incoming update messages recveived from the server
        IncomingTrafficIndicatorTask intrin = new IncomingTrafficIndicatorTask();
        new Timer("IncomingMessageIndicator").schedule(intrin, 0, 1000);

        setupAboutScreen(Release.VERSION_STRING);
        setIconImage(MlIconLoader.getApplicationIcon().getImage());
        initialized = true;
    }

    private void setStartupConfiguration(boolean startup) {
        MainSplitPane.setVisible(!startup);
    }

    /**
     * This function re-sets the winddow size, position as well as the position
     * of the various split pane dividers to their position when the software
     * was started.
     */
    protected void applyFrameAndSplitPanePreferences() {
        MainSplitPane.setDividerLocation(DividerLocationPreferences.getDividerLocationMain());
        ToolbarContentSplitPane.setDividerLocation(DividerLocationPreferences.getToolbarContent());
    }

    /**
     * stores data and preferences to persistent storage
     * @param skipDataCache If true data cache is not written (mainly to speed up exit)
     */
    public void persistApplicationState(boolean skipDataCache) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Preferences userPrefs = Preferences.userNodeForPackage(MindlinerMain.class
        );
        userPrefs.put(LOOKANDFEEL_KEY, lookAndFeelName);

        TableManager.storePreferences();
        MlStyler.storePreferences();

        DividerLocationPreferences.setDividerLocationMain(MainSplitPane.getDividerLocation());
        // store the divider location only if the control panel is not collapsed
        if (!ViewControlPaneCollapsed.isSelected()) {
            DividerLocationPreferences.setToolbarContent(ToolbarContentSplitPane.getDividerLocation());
        }
        MlPreferenceManager.desktopPrefs.setWidth(this.getWidth());
        MlPreferenceManager.desktopPrefs.setHeight(this.getHeight());
        MlPreferenceManager.desktopPrefs.setLocationX((int) this.getLocation().getX());
        MlPreferenceManager.desktopPrefs.setLocationY((int) this.getLocation().getY());

        SearchPanel.storePreferences();

        MlPreferenceManager.storePreferences();
        if (weekPlanView != null) {
            weekPlanView.storePreferences();
        }
        
        if (!skipDataCache) {
            statusBar.setMessage("writing data cache ...");
            CacheEngineStatic.storeCacheData();
        }

        OnlineManager.getInstance().storePreferences();
        statusBar.setMessage("saving offline commands ...");
        CommandRecorder.getInstance().store();
        statusBar.setMessage("storing colors ...");
        ColorManager.storeColorDefinitions();

        statusBar.setMessage("storing synchronization actors ...");
        SynchronizationManager.storeSynchActors();

        // Connector saves refreshToken for authentication such that next time we don't need to authenticate again
        GoogleDriveConnector.persistState();

        // Connector saves username and server for easier authentication
        SftpConnector.persistState();

        statusBar.done();

        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     * Creates a new object, loads it into the text editor and starts the
     * editing session.
     *
     * @param c The class of object requested.
     * @param relative A relative that will automatically be linked to the new
     * object
     * @param initialHeadline The headline with which the editor will be
     * pre-loaded
     * @param initialDescription The description with which the editor will be
     * preloaded
     */
    public void editNewMindlinerObject(Class c, mlcObject relative, String initialHeadline, String initialDescription) {
        if (c == mlcContact.class) {
            contactEditor.showEditor(null);
            ObjectEditorLauncher.registerAsOpenEditor(contactEditor);
        } else {
            try {
                mlcObject newObject = createNewObject(c, initialHeadline, initialDescription);
                showEditDialog(newObject, relative);
            } catch (InstantiationException | IllegalAccessException ex) {
                Logger.getLogger(MindlinerMain.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private mlcObject createNewObject(Class c, String initialHeadline, String initialDescription) throws InstantiationException, IllegalAccessException {
        mlcObject newObject;
        newObject = (mlcObject) c.newInstance();
        if (initialHeadline != null) {
            newObject.setHeadline(initialHeadline);
        }
        if (initialDescription != null) {
            newObject.setDescription(initialDescription);
        }
        newObject.setOwner(CacheEngineStatic.getCurrentUser());
        newObject.setClient(CacheEngineStatic.getClient(DefaultObjectAttributes.getDataPoolId()));
        newObject.setConfidentiality(DefaultObjectAttributes.getConfidentialityForDefaultClient());
        if (newObject instanceof mlcTask) {
            mlcTask t = (mlcTask) newObject;
            t.setPriority(CacheEngineStatic.getPriority(mlsPriority.PRIORITY_NORMAL));
        }
        return newObject;
    }

    private void showEditDialog(mlcObject newObject, mlcObject relative) {
        List<mlcObject> objects = new ArrayList<>();
        objects.add(newObject);
        ObjectEditor editor = new ObjectEditor(objects);
        editor.setRelative(relative);
        editor.setVisible(true);
    }

    public static void quit() {
        instance.terminateClientApplication();
    }

    private void setupAboutScreen(String versionString) {
        AboutScreenImageLabel.setIcon(MlIconLoader.getAboutScreenIcon());
        AboutDialog.setLocationRelativeTo(this);
        AboutDialog.pack();
        AboutScreenImageLabel.setText(versionString);
        AboutScreenImageLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                AboutDialog.setVisible(false);
            }

        });
    }

    @Override
    public void setActive(boolean state) {
        // this view is always active
    }

    private void performExitTasks() {
        statusBar.setMessage("persisting work tracker ");
        WorkTracker.endTracking();
        persistApplicationState(true);
        // go offline after persisting state or else the online status would always be offline
        OnlineManager.goOffline();
        System.out.println("MindlinerDesktop terminated normally.");
        System.exit(0);
    }

    private void terminateClientApplication() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        long graceTime = 30000;
        Timer forceTerminationTimer = new Timer();
        forceTerminationTimer.schedule(new TerminationTask(), graceTime, graceTime); // the third argument is irrelevant
        EventQueue.invokeLater(() -> {
            performExitTasks();
        });
    }

    /**
     * This function tries to minimize complexity. If the user is licensed to
     * just one of the overlay views then we directly link it to the top of the
     * main split pane. If its two then we insert another JSplitPane, otherwise
     * a JTabbedPane.
     */
    private void assembleOverlayPanes() {

        overlayViews.clear();

        // always present
        overlayViews.add(new MlOverlayPane(mindmapView, MlIconLoader.getImageIcon(MlIconManager.IconSize.thirtyTwo, "cube_molecule.png")));

        if (weekPlanView != null) {
            overlayViews.add(new MlOverlayPane(weekPlanView, MlIconLoader.getImageIcon(MlIconManager.IconSize.thirtyTwo, "table2_selection_row.png")));
        }
        if (worksphereMapView != null) {
            overlayViews.add(new MlOverlayPane(worksphereMapView, MlIconLoader.getImageIcon(MlIconManager.IconSize.thirtyTwo, "chart_dot.png")));
        }
        if (newsView != null) {
            overlayViews.add(new MlOverlayPane(newsView, MlIconLoader.getImageIcon(MlIconManager.IconSize.thirtyTwo, "about.png")));
        }

        FixedKeyColorizer fkc = (FixedKeyColorizer) ColorManager.getColorizerForType(Colorizer.ColorDriverAttribute.FixedKey);
        Color bg = fkc.getColorForKey(FixedKeys.MAIN_DEFAULT_BACKGROUND);
        // otherwise let's use a tabbed pane
        for (int i = 0; i < overlayViews.size(); i++) {
            OverlayPane.addTab(
                    "",
                    overlayViews.get(i).getIcon(),
                    overlayViews.get(i).getPanel(),
                    overlayViews.get(i).getPanel().getName());
            OverlayPane.setBackgroundAt(i, bg);
        }
        OverlayPane.addChangeListener((ChangeEvent e) -> {
            currentOverlayPanel = (JPanel) OverlayPane.getSelectedComponent();
            // have to do this to activate the new view
            expandView(currentOverlayPanel);
        });
        ViewMenu.setVisible(overlayViews.size() > 1);
        expandView(mindmapView);
    }

    /**
     * Adjust the split pane stack so that the target panel gets the full size
     * of the root split pane.
     *
     * @param newTarget The new panel to receive the full size
     */
    private void expandView(JPanel newTarget) {

        if (overlayViews.size() == 1) {
            return;
        }
        // de-activate all viewers that are de-activatable
        overlayViews.stream().filter((p) -> (p.panel instanceof MlObjectViewer)).forEach((p) -> {
            ((MlObjectViewer) p.panel).setActive(false);
        });

        // activate the new view
        if (newTarget instanceof MlObjectViewer) {
            ((MlObjectViewer) newTarget).setActive(true);
        }
        OverlayPane.setSelectedComponent(newTarget);
        currentOverlayPanel = newTarget;
        newTarget.repaint();
    }

    public static boolean isInitialized() {
        return initialized;
    }

    private void colorizeComponents() {
        FixedKeyColorizer fkc = (FixedKeyColorizer) ColorManager.getColorizerForType(Colorizer.ColorDriverAttribute.FixedKey);
        Color bg = fkc.getColorForKey(FixedKeys.MAIN_DEFAULT_BACKGROUND);
        Color fg = fkc.getColorForKey(FixedKeys.MAIN_DEFAULT_TEXT);
        Color border = fkc.getColorForKey(FixedKeyColorizer.FixedKeys.TABLE_GRID);
        setBackground(bg);
        MainSplitPane.setBackground(bg);
        MlStyler.colorSplitPane(MainSplitPane, bg, border);
        ToolbarContentSplitPane.setBackground(bg);
        MlStyler.colorSplitPane(ToolbarContentSplitPane, bg, border);
        StatusPanel.setBackground(bg);
        TablePane.setBackground(bg);
        statusBar.colorizeComponents();
        ConnectionLabel.setForeground(fg);
        OnlineLabel.setForeground(fg);
        EditorLabel.setForeground(fg);
        MlStyler.colorTabbedPane(OverlayPane, fg, bg);
    }

    public void incomingMessageReceived() {
        lastIncomingUpdateMessage = new Date();
    }


    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        AboutDialog = new javax.swing.JDialog();
        AboutScreenImageLabel = new javax.swing.JLabel();
        CacheDetailsDialog = new javax.swing.JDialog();
        ToolbarContentSplitPane = new javax.swing.JSplitPane();
        ControlPane = new javax.swing.JPanel();
        MainPanel = new javax.swing.JPanel();
        MainSplitPane = new javax.swing.JSplitPane();
        TablePane = new javax.swing.JPanel();
        LeftPanel = new javax.swing.JPanel();
        MiddlePanel = new javax.swing.JPanel();
        RightPanel = new javax.swing.JPanel();
        OverlayPane = new javax.swing.JTabbedPane();
        StatusPanel = new javax.swing.JPanel();
        ConnectionPanel = new javax.swing.JPanel();
        ConnectionLabel = new javax.swing.JLabel();
        OnlineLabel = new javax.swing.JLabel();
        IncomingMessageLabel = new javax.swing.JLabel();
        EditorLabel = new javax.swing.JLabel();
        MainMenuBar = new javax.swing.JMenuBar();
        FileMenu = new javax.swing.JMenu();
        FileSynchronizeMenuItem = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        CacheMenu = new javax.swing.JMenu();
        FileSaveMenu = new javax.swing.JMenuItem();
        CacheShowDetails = new javax.swing.JMenuItem();
        PerformCacheMaintenance = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JPopupMenu.Separator();
        ClearObjectAndLinkCache = new javax.swing.JMenuItem();
        ForceLoginScreen = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        AboutMenuItem = new javax.swing.JMenuItem();
        FileMenuQuit = new javax.swing.JMenuItem();
        EditMenu = new javax.swing.JMenu();
        UndoMenuItem = new javax.swing.JMenuItem();
        EditMenuSeparator = new javax.swing.JSeparator();
        PerformanceMenu = new javax.swing.JMenu();
        OfflineMenu = new javax.swing.JMenu();
        GoOnlineMenu = new javax.swing.JMenuItem();
        GoOfflineMenu = new javax.swing.JMenuItem();
        ExecutionMenu = new javax.swing.JMenu();
        PerformanceAsynchExecMode = new javax.swing.JRadioButtonMenuItem();
        PerformanceRealtimeExecMode = new javax.swing.JRadioButtonMenuItem();
        PerformanceTestBandwidth = new javax.swing.JMenuItem();
        MiscellaneousMenu = new javax.swing.JMenu();
        ObjectReviewed = new javax.swing.JCheckBoxMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        PreferenceMenu = new javax.swing.JMenuItem();
        InsertMenu = new javax.swing.JMenu();
        InsertCollection = new javax.swing.JMenuItem();
        InsertKnowlet = new javax.swing.JMenuItem();
        InsertContact = new javax.swing.JMenuItem();
        InsertTask = new javax.swing.JMenuItem();
        insertWsm = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        CreationDefaultsMenuItem = new javax.swing.JMenuItem();
        ViewMenu = new javax.swing.JMenu();
        MindmapMenu = new javax.swing.JMenuItem();
        WeekPlanMenu = new javax.swing.JMenuItem();
        WorkSphereMapMenu = new javax.swing.JMenuItem();
        NewsMenu = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        ViewControlPaneCollapsed = new javax.swing.JCheckBoxMenuItem();
        MyMenu = new javax.swing.JMenu();
        AccountInfoButton = new javax.swing.JMenuItem();
        BugReportMenuItem = new javax.swing.JMenuItem();
        ChangePasswordButton = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        AccountAdmin = new javax.swing.JMenuItem();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/mindliner/resources/General"); // NOI18N
        AboutDialog.setTitle(bundle.getString("MindlinerAboutTitle")); // NOI18N
        AboutDialog.setAlwaysOnTop(true);
        AboutDialog.setModal(true);

        AboutScreenImageLabel.setFont(AboutScreenImageLabel.getFont().deriveFont(AboutScreenImageLabel.getFont().getSize()+2f));
        AboutScreenImageLabel.setForeground(new java.awt.Color(125, 182, 182));
        AboutScreenImageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        AboutScreenImageLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/About.png"))); // NOI18N
        AboutScreenImageLabel.setText("Version 10.2");
        AboutScreenImageLabel.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        AboutScreenImageLabel.setIconTextGap(-350);
        AboutScreenImageLabel.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        AboutDialog.getContentPane().add(AboutScreenImageLabel, java.awt.BorderLayout.CENTER);

        CacheDetailsDialog.setTitle(bundle.getString("CacheDetailsDialogTitle")); // NOI18N

        org.jdesktop.layout.GroupLayout CacheDetailsDialogLayout = new org.jdesktop.layout.GroupLayout(CacheDetailsDialog.getContentPane());
        CacheDetailsDialog.getContentPane().setLayout(CacheDetailsDialogLayout);
        CacheDetailsDialogLayout.setHorizontalGroup(
            CacheDetailsDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 412, Short.MAX_VALUE)
        );
        CacheDetailsDialogLayout.setVerticalGroup(
            CacheDetailsDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 114, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(bundle.getString("Application_Titlebar")); // NOI18N
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        ToolbarContentSplitPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        ControlPane.setLayout(new java.awt.BorderLayout());
        ToolbarContentSplitPane.setLeftComponent(ControlPane);

        MainPanel.setLayout(new java.awt.BorderLayout());

        MainSplitPane.setBorder(null);
        MainSplitPane.setDividerLocation(300);
        MainSplitPane.setDividerSize(4);
        MainSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        MainSplitPane.setResizeWeight(1.0);

        TablePane.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        TablePane.setLayout(new java.awt.GridLayout(1, 3));

        LeftPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        LeftPanel.setLayout(new java.awt.BorderLayout());
        TablePane.add(LeftPanel);

        MiddlePanel.setLayout(new java.awt.BorderLayout());
        TablePane.add(MiddlePanel);

        RightPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        RightPanel.setLayout(new java.awt.BorderLayout());
        TablePane.add(RightPanel);

        MainSplitPane.setBottomComponent(TablePane);

        OverlayPane.setTabPlacement(javax.swing.JTabbedPane.LEFT);
        MainSplitPane.setLeftComponent(OverlayPane);

        MainPanel.add(MainSplitPane, java.awt.BorderLayout.CENTER);

        ToolbarContentSplitPane.setRightComponent(MainPanel);

        getContentPane().add(ToolbarContentSplitPane, java.awt.BorderLayout.CENTER);

        StatusPanel.setLayout(new javax.swing.BoxLayout(StatusPanel, javax.swing.BoxLayout.LINE_AXIS));

        ConnectionPanel.setOpaque(false);
        ConnectionPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 15, 5));

        ConnectionLabel.setText(bundle.getString("ConnectionStatusLabel")); // NOI18N
        ConnectionPanel.add(ConnectionLabel);

        OnlineLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/3232/hard_drive_network_ok.png"))); // NOI18N
        OnlineLabel.setText("Online");
        OnlineLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                OnlineLabelMouseClicked(evt);
            }
        });
        ConnectionPanel.add(OnlineLabel);

        IncomingMessageLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/3232/cloud_computing_network.png"))); // NOI18N
        IncomingMessageLabel.setToolTipText(bundle.getString("ConnectionIncomingTraffic_TT")); // NOI18N
        ConnectionPanel.add(IncomingMessageLabel);

        EditorLabel.setText(bundle.getString("MainWindowShowEditorLabel")); // NOI18N
        EditorLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                EditorLabelMouseClicked(evt);
            }
        });
        ConnectionPanel.add(EditorLabel);

        StatusPanel.add(ConnectionPanel);

        getContentPane().add(StatusPanel, java.awt.BorderLayout.SOUTH);

        FileMenu.setMnemonic('f');
        FileMenu.setText(bundle.getString("FileMenu")); // NOI18N

        FileSynchronizeMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/documents_exchange.png"))); // NOI18N
        FileSynchronizeMenuItem.setText(bundle.getString("FileMenuSynchronizeItems")); // NOI18N
        FileSynchronizeMenuItem.setToolTipText(bundle.getString("FileMenuSynchItems_TT")); // NOI18N
        FileSynchronizeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FileSynchronizeMenuItemActionPerformed(evt);
            }
        });
        FileMenu.add(FileSynchronizeMenuItem);
        FileMenu.add(jSeparator4);

        CacheMenu.setText(bundle.getString("CacheMenuName")); // NOI18N

        FileSaveMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        FileSaveMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/floppy_disk_blue.png"))); // NOI18N
        FileSaveMenu.setText(bundle.getString("FileSaveMenuItem")); // NOI18N
        FileSaveMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FileSaveMenuActionPerformed(evt);
            }
        });
        CacheMenu.add(FileSaveMenu);

        CacheShowDetails.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/information.png"))); // NOI18N
        CacheShowDetails.setText(bundle.getString("CacheShowDetails")); // NOI18N
        CacheShowDetails.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CacheShowDetailsActionPerformed(evt);
            }
        });
        CacheMenu.add(CacheShowDetails);

        PerformCacheMaintenance.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        PerformCacheMaintenance.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/replace2.png"))); // NOI18N
        PerformCacheMaintenance.setText(bundle.getString("CachePerformMaintenance")); // NOI18N
        PerformCacheMaintenance.setToolTipText(bundle.getString("CachePerformMaintenance_TT")); // NOI18N
        PerformCacheMaintenance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PerformCacheMaintenanceActionPerformed(evt);
            }
        });
        CacheMenu.add(PerformCacheMaintenance);
        CacheMenu.add(jSeparator8);

        ClearObjectAndLinkCache.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/delete.png"))); // NOI18N
        ClearObjectAndLinkCache.setText(bundle.getString("ClearObjectCache")); // NOI18N
        ClearObjectAndLinkCache.setToolTipText(bundle.getString("FileMenuClearObjectAndLinkCache_TT")); // NOI18N
        ClearObjectAndLinkCache.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ClearObjectAndLinkCacheActionPerformed(evt);
            }
        });
        CacheMenu.add(ClearObjectAndLinkCache);

        FileMenu.add(CacheMenu);

        ForceLoginScreen.setText(bundle.getString("ShowLoginScreen")); // NOI18N
        ForceLoginScreen.setToolTipText(bundle.getString("ShowLoginScreen_TT")); // NOI18N
        ForceLoginScreen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ForceLoginScreenActionPerformed(evt);
            }
        });
        FileMenu.add(ForceLoginScreen);
        FileMenu.add(jSeparator3);

        AboutMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/about-3.png"))); // NOI18N
        AboutMenuItem.setText(bundle.getString("MindlinerAbout")); // NOI18N
        AboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AboutMenuItemActionPerformed(evt);
            }
        });
        FileMenu.add(AboutMenuItem);

        FileMenuQuit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        FileMenuQuit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/sign_stop.png"))); // NOI18N
        FileMenuQuit.setMnemonic('x');
        FileMenuQuit.setText(bundle.getString("FileMenuQuit")); // NOI18N
        FileMenuQuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FileMenuQuitActionPerformed(evt);
            }
        });
        FileMenu.add(FileMenuQuit);

        MainMenuBar.add(FileMenu);

        EditMenu.setMnemonic('e');
        EditMenu.setText(bundle.getString("EditMenuName")); // NOI18N

        UndoMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        UndoMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/undo.png"))); // NOI18N
        UndoMenuItem.setText(bundle.getString("EditMenuUndo")); // NOI18N
        UndoMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UndoMenuItemActionPerformed(evt);
            }
        });
        EditMenu.add(UndoMenuItem);
        EditMenu.add(EditMenuSeparator);

        PerformanceMenu.setText(bundle.getString("EditMenuPerformance")); // NOI18N

        OfflineMenu.setText(bundle.getString("FileMenuOffline")); // NOI18N

        GoOnlineMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/hard_drive_network_ok.png"))); // NOI18N
        GoOnlineMenu.setText(bundle.getString("MenuGoOnline")); // NOI18N
        GoOnlineMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                GoOnlineMenuActionPerformed(evt);
            }
        });
        OfflineMenu.add(GoOnlineMenu);

        GoOfflineMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/hard_drive_network_error.png"))); // NOI18N
        GoOfflineMenu.setText(bundle.getString("MenuGoOffline")); // NOI18N
        GoOfflineMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                GoOfflineMenuActionPerformed(evt);
            }
        });
        OfflineMenu.add(GoOfflineMenu);

        PerformanceMenu.add(OfflineMenu);

        ExecutionMenu.setText(bundle.getString("PerformanceMenuExecMode")); // NOI18N

        PerformanceAsynchExecMode.setSelected(true);
        PerformanceAsynchExecMode.setText(bundle.getString("PerformanceAsynchExecMode")); // NOI18N
        PerformanceAsynchExecMode.setToolTipText(bundle.getString("ExecutionModeServerUpdateBackground")); // NOI18N
        PerformanceAsynchExecMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PerformanceAsynchExecModeActionPerformed(evt);
            }
        });
        ExecutionMenu.add(PerformanceAsynchExecMode);

        PerformanceRealtimeExecMode.setText(bundle.getString("PerformanceRealtimeExecMode")); // NOI18N
        PerformanceRealtimeExecMode.setToolTipText(bundle.getString("ExecutionModeWaitOnServer_TT")); // NOI18N
        PerformanceRealtimeExecMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PerformanceRealtimeExecModeActionPerformed(evt);
            }
        });
        ExecutionMenu.add(PerformanceRealtimeExecMode);

        PerformanceMenu.add(ExecutionMenu);

        PerformanceTestBandwidth.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/stopwatch.png"))); // NOI18N
        PerformanceTestBandwidth.setText(bundle.getString("PerformanceMenuTestBandwidth")); // NOI18N
        PerformanceTestBandwidth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PerformanceTestBandwidthActionPerformed(evt);
            }
        });
        PerformanceMenu.add(PerformanceTestBandwidth);

        EditMenu.add(PerformanceMenu);

        MiscellaneousMenu.setText(bundle.getString("MainMiscMenu")); // NOI18N

        ObjectReviewed.setSelected(true);
        ObjectReviewed.setText(bundle.getString("EditMenuReviewed")); // NOI18N
        ObjectReviewed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ObjectReviewedActionPerformed(evt);
            }
        });
        MiscellaneousMenu.add(ObjectReviewed);

        EditMenu.add(MiscellaneousMenu);
        EditMenu.add(jSeparator6);

        PreferenceMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_COMMA, java.awt.event.InputEvent.CTRL_MASK));
        PreferenceMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/preferences.png"))); // NOI18N
        PreferenceMenu.setText(bundle.getString("EditPreferencesMenuItem")); // NOI18N
        PreferenceMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PreferenceMenuActionPerformed(evt);
            }
        });
        EditMenu.add(PreferenceMenu);

        MainMenuBar.add(EditMenu);

        InsertMenu.setMnemonic('i');
        InsertMenu.setText(bundle.getString("InsertMenu")); // NOI18N

        InsertCollection.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        InsertCollection.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/folder2_blue.png"))); // NOI18N
        InsertCollection.setText(bundle.getString("InsertCollection")); // NOI18N
        InsertCollection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                InsertCollectionActionPerformed(evt);
            }
        });
        InsertMenu.add(InsertCollection);

        InsertKnowlet.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        InsertKnowlet.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/information2.png"))); // NOI18N
        InsertKnowlet.setText(bundle.getString("InsertKnowlet")); // NOI18N
        InsertKnowlet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                InsertKnowletActionPerformed(evt);
            }
        });
        InsertMenu.add(InsertKnowlet);

        InsertContact.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/users4_add.png"))); // NOI18N
        InsertContact.setText(bundle.getString("InsertContact")); // NOI18N
        InsertContact.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                InsertContactActionPerformed(evt);
            }
        });
        InsertMenu.add(InsertContact);

        InsertTask.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_MASK));
        InsertTask.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/checkbox.png"))); // NOI18N
        InsertTask.setText(bundle.getString("InsertTask")); // NOI18N
        InsertTask.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                InsertTaskActionPerformed(evt);
            }
        });
        InsertMenu.add(InsertTask);

        insertWsm.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/chart_dot.png"))); // NOI18N
        insertWsm.setText(bundle.getString("InsertWorkSphereMap")); // NOI18N
        insertWsm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertWsmActionPerformed(evt);
            }
        });
        InsertMenu.add(insertWsm);

        jMenuItem1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/photo_landscape2.png"))); // NOI18N
        jMenuItem1.setText(bundle.getString("CreateMenuUrlImage")); // NOI18N
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        InsertMenu.add(jMenuItem1);
        InsertMenu.add(jSeparator5);

        CreationDefaultsMenuItem.setText(bundle.getString("CreateMenuViewDefaults")); // NOI18N
        CreationDefaultsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CreationDefaultsMenuItemActionPerformed(evt);
            }
        });
        InsertMenu.add(CreationDefaultsMenuItem);

        MainMenuBar.add(InsertMenu);

        ViewMenu.setText(bundle.getString("ViewMenu")); // NOI18N
        ViewMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ViewMenuActionPerformed(evt);
            }
        });

        MindmapMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_1, java.awt.event.InputEvent.CTRL_MASK));
        MindmapMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/cube_molecule.png"))); // NOI18N
        MindmapMenu.setText(bundle.getString("ViewMenuMindmap")); // NOI18N
        MindmapMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MindmapMenuActionPerformed(evt);
            }
        });
        ViewMenu.add(MindmapMenu);

        WeekPlanMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_2, java.awt.event.InputEvent.CTRL_MASK));
        WeekPlanMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/table2_selection_row.png"))); // NOI18N
        WeekPlanMenu.setText(bundle.getString("ViewMenuWorkPlan")); // NOI18N
        WeekPlanMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                WeekPlanMenuActionPerformed(evt);
            }
        });
        ViewMenu.add(WeekPlanMenu);

        WorkSphereMapMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_3, java.awt.event.InputEvent.CTRL_MASK));
        WorkSphereMapMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/chart_dot.png"))); // NOI18N
        WorkSphereMapMenu.setText(bundle.getString("WorkSphereMapMenu")); // NOI18N
        WorkSphereMapMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                WorkSphereMapMenuActionPerformed(evt);
            }
        });
        ViewMenu.add(WorkSphereMapMenu);

        NewsMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_4, java.awt.event.InputEvent.CTRL_MASK));
        NewsMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/about-3.png"))); // NOI18N
        NewsMenu.setText(bundle.getString("ViewMenuNews")); // NOI18N
        NewsMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NewsMenuActionPerformed(evt);
            }
        });
        ViewMenu.add(NewsMenu);
        ViewMenu.add(jSeparator7);

        ViewControlPaneCollapsed.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SPACE, java.awt.event.InputEvent.CTRL_MASK));
        ViewControlPaneCollapsed.setText(bundle.getString("MainLeftControlPaneCollapsed")); // NOI18N
        ViewControlPaneCollapsed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ViewControlPaneCollapsedActionPerformed(evt);
            }
        });
        ViewMenu.add(ViewControlPaneCollapsed);

        MainMenuBar.add(ViewMenu);

        MyMenu.setText(bundle.getString("UsernameMenu")); // NOI18N

        AccountInfoButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/safe_edit.png"))); // NOI18N
        AccountInfoButton.setText(bundle.getString("MyMenuAccountInfo")); // NOI18N
        AccountInfoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AccountInfoButtonActionPerformed(evt);
            }
        });
        MyMenu.add(AccountInfoButton);

        BugReportMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/bug_red.png"))); // NOI18N
        BugReportMenuItem.setText(bundle.getString("BugReportMenu")); // NOI18N
        BugReportMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BugReportMenuItemActionPerformed(evt);
            }
        });
        MyMenu.add(BugReportMenuItem);

        ChangePasswordButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/text_lock.png"))); // NOI18N
        ChangePasswordButton.setText(bundle.getString("ChangePasswordMenu")); // NOI18N
        ChangePasswordButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ChangePasswordButtonActionPerformed(evt);
            }
        });
        MyMenu.add(ChangePasswordButton);
        MyMenu.add(jSeparator2);

        AccountAdmin.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/window_key.png"))); // NOI18N
        AccountAdmin.setText(bundle.getString("ClientAdminMenu")); // NOI18N
        AccountAdmin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AccountAdminActionPerformed(evt);
            }
        });
        MyMenu.add(AccountAdmin);

        MainMenuBar.add(MyMenu);

        setJMenuBar(MainMenuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void PreferenceMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PreferenceMenuActionPerformed
        MlMainPreferenceEditor.showDialog();
    }//GEN-LAST:event_PreferenceMenuActionPerformed

    private void FileMenuQuitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FileMenuQuitActionPerformed
        terminateClientApplication();
    }//GEN-LAST:event_FileMenuQuitActionPerformed

private void CacheShowDetailsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CacheShowDetailsActionPerformed
    CacheMonitor cm = new CacheMonitor();
    cm.setVisible(true);
}//GEN-LAST:event_CacheShowDetailsActionPerformed

private void FileSynchronizeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FileSynchronizeMenuItemActionPerformed
    if (SynchronizationManager.getSynchActors().isEmpty()) {
        int answer = JOptionPane.showConfirmDialog(MindlinerMain.getInstance(), "Synchronization is not initialized. Want to do this now?", "Synchronizatoin", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            MlMainPreferenceEditor.showDialog();
        } else {
            JOptionPane.showMessageDialog(null, "Choose Edit Preferences to initialize synchronization", "Synchronization", JOptionPane.INFORMATION_MESSAGE);
        }
    } else {
        ExecutionMode previousExecutionMode = OnlineManager.getInstance().getExecutionMode();
        if (previousExecutionMode.equals(OnlineManager.ExecutionMode.Asynchronous)) {
            OnlineManager.getInstance().setExecutionMode(ExecutionMode.Realtime);
        }
        SynchronizationManager.synchronize();
        if (!previousExecutionMode.equals(OnlineManager.getInstance().getExecutionMode())) {
            OnlineManager.getInstance().setExecutionMode(previousExecutionMode);

        }
    }
}//GEN-LAST:event_FileSynchronizeMenuItemActionPerformed

private void InsertKnowletActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_InsertKnowletActionPerformed
    editNewMindlinerObject(mlcKnowlet.class, SelectionManager.getLastSelection(), "", "");
}//GEN-LAST:event_InsertKnowletActionPerformed

private void InsertTaskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_InsertTaskActionPerformed
    editNewMindlinerObject(mlcTask.class, SelectionManager.getLastSelection(), "", "");
}//GEN-LAST:event_InsertTaskActionPerformed

private void InsertCollectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_InsertCollectionActionPerformed
    editNewMindlinerObject(mlcObjectCollection.class, SelectionManager.getLastSelection(), "", "");
}//GEN-LAST:event_InsertCollectionActionPerformed

private void InsertContactActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_InsertContactActionPerformed
    editNewMindlinerObject(mlcContact.class, SelectionManager.getLastSelection(), "", "");
}//GEN-LAST:event_InsertContactActionPerformed

private void ForceLoginScreenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ForceLoginScreenActionPerformed
    Preferences userPrefs = Preferences.userNodeForPackage(LoginGUI.class);
    userPrefs.put("password", "");
    userPrefs.putBoolean("autoLogin", false);
    OnlineManager.goOnline();
}//GEN-LAST:event_ForceLoginScreenActionPerformed

private void OnlineLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_OnlineLabelMouseClicked
    if (OnlineManager.isOnline()) {
        OnlineManager.goOffline();
    } else {
        OnlineManager.goOnline();
    }
}//GEN-LAST:event_OnlineLabelMouseClicked

private void FileSaveMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FileSaveMenuActionPerformed
    persistApplicationState(false);
}//GEN-LAST:event_FileSaveMenuActionPerformed

private void PerformCacheMaintenanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PerformCacheMaintenanceActionPerformed
    try {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        CacheEngineStatic.performMaintenance(true);
    } catch (MlCacheException ex) {
        JOptionPane.showMessageDialog(null, ex.getMessage(), "Cache Maintenance", JOptionPane.ERROR_MESSAGE);
    } finally {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
}//GEN-LAST:event_PerformCacheMaintenanceActionPerformed

private void UndoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UndoMenuItemActionPerformed
    CommandRecorder cr = CommandRecorder.getInstance();
    cr.undoLastCommand();
}//GEN-LAST:event_UndoMenuItemActionPerformed

    private void AboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AboutMenuItemActionPerformed
        AboutDialog.setVisible(true);
    }//GEN-LAST:event_AboutMenuItemActionPerformed

    private void GoOnlineMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_GoOnlineMenuActionPerformed
        OnlineManager.goOnline();
    }//GEN-LAST:event_GoOnlineMenuActionPerformed

    private void GoOfflineMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_GoOfflineMenuActionPerformed
        OnlineManager.goOffline();
    }//GEN-LAST:event_GoOfflineMenuActionPerformed

    private void PerformanceAsynchExecModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PerformanceAsynchExecModeActionPerformed
        OnlineManager.getInstance().setExecutionMode(ExecutionMode.Asynchronous);
    }//GEN-LAST:event_PerformanceAsynchExecModeActionPerformed

    private void PerformanceRealtimeExecModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PerformanceRealtimeExecModeActionPerformed
        OnlineManager.getInstance().setExecutionMode(ExecutionMode.Realtime);
    }//GEN-LAST:event_PerformanceRealtimeExecModeActionPerformed

    private void PerformanceTestBandwidthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PerformanceTestBandwidthActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        String testBandwidth = BandwidthTester.testBandwidth();
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        System.out.println(testBandwidth);
        JDialog d = new JDialog();
        d.setLayout(new BorderLayout());
        JTextArea ta = new JTextArea(testBandwidth);
        d.add(ta, BorderLayout.CENTER);
        d.setSize(new Dimension(600, 400));
        d.setLocationRelativeTo(this);
        d.setModal(true);
        d.setVisible(true);
    }//GEN-LAST:event_PerformanceTestBandwidthActionPerformed

    private void ChangePasswordButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChangePasswordButtonActionPerformed
        try {
            UserManagerRemote umr = (UserManagerRemote) RemoteLookupAgent.getManagerForClass(UserManagerRemote.class
            );
            UserAuthenticationDialog uad = new UserAuthenticationDialog(CacheEngineStatic.getCurrentUser().getId(), umr, null, true, false);

            uad.setVisible(
                    true);
            if (uad.isPasswordChanged()) {
                LoginGUI.clearPassword();
                System.exit(0);
            }
        } catch (NamingException ex) {
            Logger.getLogger(MindlinerMain.class
                    .getName()).log(Level.SEVERE, ex.getMessage());
        }
    }//GEN-LAST:event_ChangePasswordButtonActionPerformed

    private void AccountInfoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AccountInfoButtonActionPerformed
        UserAccountDialog uad = new UserAccountDialog(this, false);
        uad.setVisible(true);
    }//GEN-LAST:event_AccountInfoButtonActionPerformed

    private void BugReportMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BugReportMenuItemActionPerformed
        try {
            mlcKnowlet k = (mlcKnowlet) createNewObject(mlcKnowlet.class, "", "");
            setBugReportText(k);
            showEditDialog(k, SelectionManager.getLastSelection());
        } catch (InstantiationException | IllegalAccessException ex) {
            JOptionPane.showMessageDialog(null, ex, "Cannot Create Bug Report", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_BugReportMenuItemActionPerformed

    private void ViewMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ViewMenuActionPerformed

    }//GEN-LAST:event_ViewMenuActionPerformed

    private void AccountAdminActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AccountAdminActionPerformed
        try {
            BrowserLauncher.openWebpage(new URL(Release.DEFAULT_CLIENT_ADMIN_PAGE));
        } catch (MalformedURLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Sorry: Cannot Open Mindliner Web", JOptionPane.ERROR_MESSAGE);

        }
    }//GEN-LAST:event_AccountAdminActionPerformed

    private void insertWsmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertWsmActionPerformed
        editNewMindlinerObject(MlcContainerMap.class, SelectionManager.getLastSelection(), "", "");
    }//GEN-LAST:event_insertWsmActionPerformed

    private void CreationDefaultsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CreationDefaultsMenuItemActionPerformed
        ObjectDefaultsDialog odd = new ObjectDefaultsDialog(null, true);
        odd.pack();
        odd.setLocationRelativeTo(this);
        odd.setVisible(true);
    }//GEN-LAST:event_CreationDefaultsMenuItemActionPerformed

    private void ObjectReviewedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ObjectReviewedActionPerformed
        if (SelectionManager.getLastSelection() != null) {
            bulkUpdater.updateObjectStatus(SelectionManager.getSelection(), ObjectReviewed.isSelected() ? ObjectReviewStatus.REVIEWED : ObjectReviewStatus.IMPORTED);
        }
    }//GEN-LAST:event_ObjectReviewedActionPerformed

    private void MindmapMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MindmapMenuActionPerformed
        expandView(mindmapView);
    }//GEN-LAST:event_MindmapMenuActionPerformed

    private void WeekPlanMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_WeekPlanMenuActionPerformed
        expandView(weekPlanView);
    }//GEN-LAST:event_WeekPlanMenuActionPerformed

    private void WorkSphereMapMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_WorkSphereMapMenuActionPerformed
        expandView(worksphereMapView);
    }//GEN-LAST:event_WorkSphereMapMenuActionPerformed

    private void NewsMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NewsMenuActionPerformed
        expandView(newsView);
    }//GEN-LAST:event_NewsMenuActionPerformed

    private void ClearObjectAndLinkCacheActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ClearObjectAndLinkCacheActionPerformed
        int answer = JOptionPane.showConfirmDialog(MindlinerMain.getInstance(), "Improves startup/shutdown speed and may limit object availability in offline mode. Continue", "Cache Object Removal", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            CacheEngineStatic.clearObjectAndLinkCache();
        }
    }//GEN-LAST:event_ClearObjectAndLinkCacheActionPerformed

    private void ViewControlPaneCollapsedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ViewControlPaneCollapsedActionPerformed
        if (ViewControlPaneCollapsed.isSelected()) {
            ControlPane.setVisible(false);
        } else {
            ControlPane.setVisible(true);
            ToolbarContentSplitPane.setDividerLocation(300);
        }
    }//GEN-LAST:event_ViewControlPaneCollapsedActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        JOptionPane.showMessageDialog(this, "To create an image please paste the URL of an image into the Mindmap view", "Image Creation", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void EditorLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_EditorLabelMouseClicked
        ObjectEditorLauncher.popEditors();
    }//GEN-LAST:event_EditorLabelMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDialog AboutDialog;
    private javax.swing.JMenuItem AboutMenuItem;
    private javax.swing.JLabel AboutScreenImageLabel;
    private javax.swing.JMenuItem AccountAdmin;
    private javax.swing.JMenuItem AccountInfoButton;
    private javax.swing.JMenuItem BugReportMenuItem;
    private javax.swing.JDialog CacheDetailsDialog;
    private javax.swing.JMenu CacheMenu;
    private javax.swing.JMenuItem CacheShowDetails;
    private javax.swing.JMenuItem ChangePasswordButton;
    private javax.swing.JMenuItem ClearObjectAndLinkCache;
    private javax.swing.JLabel ConnectionLabel;
    private javax.swing.JPanel ConnectionPanel;
    private javax.swing.JPanel ControlPane;
    private javax.swing.JMenuItem CreationDefaultsMenuItem;
    private javax.swing.JMenu EditMenu;
    private javax.swing.JSeparator EditMenuSeparator;
    private javax.swing.JLabel EditorLabel;
    private javax.swing.JMenu ExecutionMenu;
    private javax.swing.JMenu FileMenu;
    private javax.swing.JMenuItem FileMenuQuit;
    private javax.swing.JMenuItem FileSaveMenu;
    private javax.swing.JMenuItem FileSynchronizeMenuItem;
    private javax.swing.JMenuItem ForceLoginScreen;
    private javax.swing.JMenuItem GoOfflineMenu;
    private javax.swing.JMenuItem GoOnlineMenu;
    private javax.swing.JLabel IncomingMessageLabel;
    private javax.swing.JMenuItem InsertCollection;
    private javax.swing.JMenuItem InsertContact;
    private javax.swing.JMenuItem InsertKnowlet;
    private javax.swing.JMenu InsertMenu;
    private javax.swing.JMenuItem InsertTask;
    private javax.swing.JPanel LeftPanel;
    private javax.swing.JMenuBar MainMenuBar;
    private javax.swing.JPanel MainPanel;
    private javax.swing.JSplitPane MainSplitPane;
    private javax.swing.JPanel MiddlePanel;
    private javax.swing.JMenuItem MindmapMenu;
    private javax.swing.JMenu MiscellaneousMenu;
    private javax.swing.JMenu MyMenu;
    private javax.swing.JMenuItem NewsMenu;
    private javax.swing.JCheckBoxMenuItem ObjectReviewed;
    private javax.swing.JMenu OfflineMenu;
    private javax.swing.JLabel OnlineLabel;
    private javax.swing.JTabbedPane OverlayPane;
    private javax.swing.JMenuItem PerformCacheMaintenance;
    private javax.swing.JRadioButtonMenuItem PerformanceAsynchExecMode;
    private javax.swing.JMenu PerformanceMenu;
    private javax.swing.JRadioButtonMenuItem PerformanceRealtimeExecMode;
    private javax.swing.JMenuItem PerformanceTestBandwidth;
    private javax.swing.JMenuItem PreferenceMenu;
    private javax.swing.JPanel RightPanel;
    private javax.swing.JPanel StatusPanel;
    private javax.swing.JPanel TablePane;
    private javax.swing.JSplitPane ToolbarContentSplitPane;
    private javax.swing.JMenuItem UndoMenuItem;
    private javax.swing.JCheckBoxMenuItem ViewControlPaneCollapsed;
    private javax.swing.JMenu ViewMenu;
    private javax.swing.JMenuItem WeekPlanMenu;
    private javax.swing.JMenuItem WorkSphereMapMenu;
    private javax.swing.JMenuItem insertWsm;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JPopupMenu.Separator jSeparator8;
    // End of variables declaration//GEN-END:variables

    class EditorLabelUpdateTask extends TimerTask {

        @Override
        public void run() {
            SwingUtilities.invokeLater(() -> {
                EditorLabel.setVisible(ObjectEditorLauncher.getEditorCount() > 0);
            });

        }

    }

    class IncomingTrafficIndicatorTask extends TimerTask {

        @Override
        public void run() {

            if ((new Date()).getTime() - lastIncomingUpdateMessage.getTime() > NO_UPDATE_RECEIVED_INTERVAL) {
                IncomingMessageLabel.setVisible(false);
            } else {
                IncomingMessageLabel.setVisible(true);
            }
        }

    }
}
