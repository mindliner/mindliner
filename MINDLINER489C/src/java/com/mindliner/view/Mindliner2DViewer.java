/*
 * Mindliner2DViewer.java
 * 
 * @author Marius Messerli
 * Created on Jun 9, 2010, 10:45:02 PM
 */
package com.mindliner.view;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.MlcImage;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.entities.Colorizer;
import com.mindliner.entities.SoftwareFeature;
import com.mindliner.events.ObjectChangeManager;
import com.mindliner.events.ObjectChangeObserver;
import com.mindliner.events.SelectionManager;
import com.mindliner.gui.color.BaseColorizer;
import com.mindliner.gui.color.ColorManager;
import com.mindliner.gui.color.FixedKeyColorizer;
import com.mindliner.gui.font.FontPreferences;
import com.mindliner.image.IconLoader;
import com.mindliner.prefs.MapPreferencesPane;
import com.mindliner.styles.MlStyler;
import com.mindliner.serveraccess.OnlineService;
import com.mindliner.serveraccess.StatusReporter;
import com.mindliner.thread.SimpleSwingWorker;
import com.mindliner.view.colorizers.ConfidentialityColorizer;
import com.mindliner.view.colorizers.HierarchicalColorizer;
import com.mindliner.view.colorizers.ModificationAgeColorizer;
import com.mindliner.view.colorizers.NodeColorizerBase;
import com.mindliner.view.colorizers.NodeColorizerBase.ColorDriverType;
import com.mindliner.view.colorizers.NodeDataPoolColorizer;
import com.mindliner.view.colorizers.NodeOwnerColorizer;
import com.mindliner.view.colorizers.RatingColorizer;
import com.mindliner.view.dispatch.MlObjectViewer;
import com.mindliner.view.dispatch.MlViewDispatcherImpl;
import com.mindliner.view.interaction.MindlinerDragGestureRecognizer;
import com.mindliner.view.interaction.RebuildMapKeyListener;
import com.mindliner.view.nodebuilder.MindmapTextNodeBuilder;
import com.mindliner.view.nodebuilder.NodeBuilderImpl;
import com.mindliner.view.positioner.MindmapPositioner;
import com.mindliner.view.positioner.ObjectPositioner;
import com.mindliner.view.positioner.ObjectPositioner.PositionLayout;
import com.mindliner.view.positioner.SymetricMindmapPositioner;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Timer;
import com.mindliner.clientobjects.MlMapNode;
import com.mindliner.view.colorizers.BranchColorizer;
import com.mindliner.view.connectors.NodeConnection.ConnectorType;
import com.mindliner.view.positioner.SectorPositioner;
import java.awt.Dimension;
import java.util.Date;
import javax.swing.ButtonGroup;

public class Mindliner2DViewer extends javax.swing.JPanel implements OnlineService, ObjectChangeObserver, MlObjectViewer {

    private static final String COLOR_DRIVER_KEY = "colorDriver";
    private static final int INITIAL_MAP_BUILD_LEVEL = 2;
    private List<mlcObject> objects = new ArrayList<>();
    private MindlinerMapper mapper = null;
    private StatusReporter statusReporter = null;
    private final MapBuildWorker mapBuildWorker = new MapBuildWorker();
    private OnlineStatus onlineStatus = OnlineStatus.offline;
    private int mapBuildLevels = INITIAL_MAP_BUILD_LEVEL;
    private MapPreferencesPane mapPreferences;
    // strings for preferences
    private int connectionPriority = 0;
    private boolean active = false;

    public Mindliner2DViewer(StatusReporter statusReporter) {
        this.statusReporter = statusReporter;
        initComponents();
        configureComponents();
        loadPreferences();
        installComboListeners();
        active = true;
    }

    /**
     * This call is used so that I can populate the combos without action events
     * being process while the system may not have been initialized properly.
     */
    private void installComboListeners() {
        ColorDriverCombo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mapper.setColorizer(getSelectedColorizer());
                mapper.reAssignNodeColors();
                mapper.repaint();
            }
        });
    }

    public void exportToPdf() {
        mapper.exportToPdf();
    }

    public void exportToSvg() {
        mapper.exportToSvg();
    }

    /**
     * THESE CALLS HAVE BEEN REMOVED WITH REMOVING THE ACTION LISTENERS. SEE
     * THAT THESE FUNCTIONS ARE CALLED ELSEWHERE IF REQUIRED
     *
     * updateConnectors((ConnectorType) ConnectorTypeCombo.getSelectedItem());
     * maxWordLength = Integer.parseInt(WordMaxLength.getText());
     * updateAtomizer(mapper.getNodeBuilder());
     * mapper.setNodes(mapper.getNodeBuilder().buildNodes(objects));
     *
     *
     * GridFrame.FrameType frameType = (GridFrame.FrameType)
     * MapPrefsFrameCombobox.getSelectedItem(); switch (frameType) { case
     * SceneFrame: mapper.setFrame(new SceneFrame()); break;
     *
     * case FixedPositionFrame: mapper.setFrame(new
     * FixedPositionFrame(mapper.getZoomAndPanListener(), mapper)); break;
     *
     * default: throw new IllegalStateException("Unknown selection made in the
     * frame type combobox."); }
     *
     * @return The name of this online service.
     */
    @Override
    public String getServiceName() {
        return "Map Viewer";
    }

    @Override
    public void goOffline() {
        storePreferences();
        mapPreferences.storePreferences();
        onlineStatus = OnlineStatus.offline;
    }

    @Override
    public void goOnline() {
        List<MlMapNode> nodes = mapper.getNodes();
        for (MlMapNode n : nodes) {
            // images that were added in offline mode need to be loaded now
            invalidateMapImages(n);
        }
        onlineStatus = OnlineStatus.online;
    }

    @Override
    public OnlineStatus getStatus() {
        return onlineStatus;
    }

    @Override
    public int getConnectionPriority() {
        return connectionPriority;
    }

    @Override
    public void setConnectionPriority(int priority) {
        connectionPriority = priority;
    }

    @Override
    public void objectChanged(mlcObject o) {
        // handled by the mapper implementation
    }

    @Override
    public void objectDeleted(mlcObject o) {
        // handled by the mapper implementation
    }

    @Override
    public void objectReplaced(int oldId, mlcObject o) {
        // handled by the mapper implementation
    }

    /**
     * Gets called when a new object is created by any of Mindliner's
     * mechanisms. This call checks if the new object is related to any of the
     * node's underlying objects. If so it will generate a new node with the
     * newObject as underlyer for each relationship.
     *
     * @todo - This call is actually taking care of all creations whether or not
     * they were initiated in the map as the editor is shown whether or not the
     * new node is linked to any existing nodes....
     *
     * @param newObject
     */
    @Override
    public void objectCreated(mlcObject newObject) {
        // only show unlinked objects created by the current user
        if (mapper.getNodes().isEmpty() && newObject.isOwnedByCurrentUser()) {
            List<mlcObject> root = new ArrayList<>();
            root.add(newObject);
            display(root, ViewType.Map);
        } else {
            // case is handled by MindlinerMapperImpl
        }
    }

    @Override
    public boolean isSupported(ViewType type) {
        return type == ViewType.Map;
    }

    @Override
    public void back() {
    }

    public MapPreferencesPane getMapPreferences() {
        return mapPreferences;
    }

    public List<MlMapNode> getNodes() {
        return mapper.getNodes();
    }

    private void storePreferences() {
        Preferences userPrefs = Preferences.userNodeForPackage(Mindliner2DViewer.class
        );
        userPrefs.put(COLOR_DRIVER_KEY,
                (String) ColorDriverCombo.getSelectedItem().toString());
    }

    private void loadPreferences() {
        Preferences userPrefs = Preferences.userNodeForPackage(Mindliner2DViewer.class
        );

        String colorDriverString = userPrefs.get(COLOR_DRIVER_KEY, ColorDriverType.Level.toString());
        ColorDriverType colorType;

        try {
            colorType = ColorDriverType.valueOf(colorDriverString);
        } catch (IllegalArgumentException ex) {
            colorType = ColorDriverType.Level;
        }

        ColorDriverCombo.setSelectedItem(colorType);

    }

    @Override
    public void display(List<mlcObject> objects, ViewType type) {
        if (!active) {
            return;
        }
        this.objects = objects;
        mapper.pushCurrentNodes();
        EventQueue.invokeLater(() -> {
            // this statement is slightly unefficient as it would need to be called only when the background chanages
            ControlsPanel.setBackground(mapper.getBackgroundPainter().getBackground());
            // also this call needs to be done every time a new map is displayed simply because we don't have a font change notification system
            mapper.getNodeBuilder().setFont(FontPreferences.getFont(mapPreferences.FONT_PREFERENCE_KEY));
            changeMapBuildLevel(INITIAL_MAP_BUILD_LEVEL);
            // I need to call ensureNodeInView separately before I call initializeView (even though the latter contains a call to the former)
//            mapper.ensureNodeInView(false);
            initializeView();
        });
    }

    @Override
    public void display(mlcObject object, ViewType type) {
        if (!active) {
            return;
        }
        ArrayList<mlcObject> list = new ArrayList<>();
        list.add(object);
        display(list, type);
    }

    /**
     * @todo The action events should not be fired when defining the combo
     * boxes. Everytime I call setSelectedItem the action listener is called and
     * may not be ready because multiple combos depend on each other.
     */
    private void configureComponents() {
        DefaultComboBoxModel dcm = new DefaultComboBoxModel();
        dcm.addElement(ColorDriverType.Level);
        dcm.addElement(ColorDriverType.Branch);
        dcm.addElement(ColorDriverType.ModificationAge);
        dcm.addElement(ColorDriverType.Owner);
        dcm.addElement(ColorDriverType.DataPool);
        if (CacheEngineStatic.getCurrentUser().isAuthorizedForFeature(SoftwareFeature.CurrentFeatures.CONFIDENTIALITY_LEVELS)) {
            dcm.addElement(ColorDriverType.Confidentiality);
        }
        if (CacheEngineStatic.getCurrentUser().isAuthorizedForFeature(SoftwareFeature.CurrentFeatures.OBJECT_RATING)) {
            dcm.addElement(ColorDriverType.Rating);
        }
        dcm.setSelectedItem(ColorDriverType.Level);
        ColorDriverCombo.setModel(dcm);

        mapper = new MindlinerMapperImpl();
        mapper.addKeyListener(new RebuildMapKeyListener());
        SelectionManager.registerObserver((MindlinerMapperImpl) mapper);
        mapper.setTransferHandler(new MapTransferHandler(mapper));
        DragSource dataSource = new DragSource();
        dataSource.createDefaultDragGestureRecognizer(mapper, DnDConstants.ACTION_MOVE, new MindlinerDragGestureRecognizer(mapper));

        // the following call must be after the construction of mapper but before creation of createNewRelationshipTextNodeBuilder
        mapPreferences = new MapPreferencesPane(mapper);
        // mapPreferences must be initialized before creating the node builder
        MindmapTextNodeBuilder rb = createNewRelationshipTextNodeBuilder();
        mapper.setNodeBuilder(rb);

        add(mapper, BorderLayout.CENTER);
        mapper.setParentFrame(this);
        ObjectChangeManager.registerObserver((ObjectChangeObserver) mapper);
        MlViewDispatcherImpl.getInstance().registerViewer(this);

        IconLoader.getInstance().setMapper(mapper);
        URL helpURL = Mindliner2DViewer.class
                .getResource("QuickHelpContent.html");
        try {
            QuickHelpTextPane.setPage(helpURL);
        } catch (IOException ex) {
            Logger.getLogger(Mindliner2DViewer.class.getName()).log(Level.SEVERE, null, ex);
        }
        // call every 500 ms
        Timer nodeCountReporter = new Timer(500, new MapRoutineTasks());
        nodeCountReporter.start();
        updateLayoutComboMindmapOnly();

        ButtonGroup b = new ButtonGroup();
        b.add(MindmapMode);
        b.add(BrizwalkMode);
        MindmapMode.setSelected(true);

        // style various components
        FixedKeyColorizer fkc = (FixedKeyColorizer) ColorManager.getColorizerForType(Colorizer.ColorDriverAttribute.FixedKey);
        Color bg = fkc.getColorForKey(FixedKeyColorizer.FixedKeys.MAIN_DEFAULT_BACKGROUND);
        Color fg = fkc.getColorForKey(FixedKeyColorizer.FixedKeys.MAIN_DEFAULT_TEXT);
        setBackground(bg);
        ZoomLabel.setForeground(fg);
        MlStyler.colorizeButton(BackButton, fkc);
        MlStyler.colorizeButton(BuildLevel3Button, fkc);
        MlStyler.colorizeButton(BuildLevel4Button, fkc);
        MlStyler.colorizeButton(ResetZoomPan, fkc);
        MlStyler.colorizeComboBox(ColorDriverCombo, fkc);
    }

    private MindmapTextNodeBuilder createNewRelationshipTextNodeBuilder() {
        MindmapTextNodeBuilder rb = new MindmapTextNodeBuilder(mapper.getBackgroundPainter());
        rb.setConnectorType(ConnectorType.CurvedTextClear);
        rb.setMaxLevel(mapBuildLevels);
        return rb;
    }

    private ObjectPositioner getPositionerForType(PositionLayout type) {
        ObjectPositioner p = null;
        switch (type) {

            case MindMap:
                p = new MindmapPositioner();
                break;

            case SymmetricMindMap:
                p = new SymetricMindmapPositioner();
                break;

            case Brizwalk:
                SectorPositioner sp = new SectorPositioner();
                sp.setDrawingArea(
                        new Dimension(
                                (int) (mapper.getSize().width / mapper.getZoomAndPanListener().getScale()),
                                (int) (mapper.getSize().height / mapper.getZoomAndPanListener().getScale())
                        ));
                p = sp;
                break;

            default:
                throw new IllegalStateException("Unknown Positioner Requested");
        }
        if (p != null) {
            mapper.setColorizer(getSelectedColorizer());
            p.setNodes(mapper.getNodes());
            Graphics2D g = (Graphics2D) getGraphics();
            if (g != null) {
                p.setFontRenderContext(g.getFontRenderContext());
            }
            return p;
        } else {
            return null;
        }
    }

    private void assignMapperPositioner() {
        Graphics2D g2 = (Graphics2D) getGraphics();
        // the following reads wrong but the action performed listener is called before the state is really changed, hence it still shows the old state
        ObjectPositioner p = getPositionerForType(MindmapMode.isSelected() ? PositionLayout.MindMap : PositionLayout.Brizwalk);
        if (p != null && g2 != null) {
            p.setNodes(mapper.getNodes());
            p.setFontRenderContext(g2.getFontRenderContext());
            mapper.setPositioner(p);
            p.arrangePositions();
        }
    }

    private void updateAtomizer(NodeBuilderImpl tnb) {
        tnb.setMaxCharacterCount(mapPreferences.getWordMaxLength());
    }

    private void changeMapBuildLevel(int newValue) {
        mapBuildLevels = newValue;
        mapBuildWorker.buildMap();
        mapper.repaint();
    }

    public void buildView() {
        mapBuildWorker.buildMap();
    }

    private NodeColorizerBase getSelectedColorizer() {

        switch ((ColorDriverType) ColorDriverCombo.getSelectedItem()) {

            case ModificationAge:
                return new ModificationAgeColorizer();

            case Owner:
                return new NodeOwnerColorizer();

            case DataPool:
                return new NodeDataPoolColorizer();

            case Confidentiality:
                return new ConfidentialityColorizer();

            case Rating:
                return new RatingColorizer();

            case Branch:
                return new BranchColorizer();

            default:
                return new HierarchicalColorizer();
        }
    }

    private void updateConnectors(ConnectorType connectorType) {
        if (connectorType == null) {
            return;
        }

        if (mapper != null && mapper.getNodeBuilder() instanceof MindmapTextNodeBuilder) {
            MindmapTextNodeBuilder b = (MindmapTextNodeBuilder) mapper.getNodeBuilder();
            b.setConnectorType(ConnectorType.CurvedTextClear);

            // now update the connectors if there is already a map
            List<MlMapNode> nodes = mapper.getNodes();
            if (nodes.size() > 0) {
                MlMapNode firstNode = nodes.get(0);
                NodeConnectorUpdater.updateConnectorsForTree(firstNode, connectorType);
                mapper.repaint();
            }
        }
    }

    /**
     * Updates the layout combobox.
     *
     * @param mindmap If true only mindmap layouts are loaded, otherwise
     * individual-node layouts are loaded.
     */
    private void updateLayoutComboMindmapOnly() {
        updateConnectors(ConnectorType.CurvedTextClear);
    }

    /**
     * redraws the map down to the specified branch level
     *
     * @param level The depth of the tree to show wiht root node being level 1
     */
    private void adaptLevel(int level) {
        if (objects != null) {
            changeMapBuildLevel(level);
        }
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    private void initializeView() {
        // clear the selection to center on the root node and not the selected node
        SelectionManager.clearSelection();
        mapper.getZoomAndPanListener().resetZoomAndPan();
        mapper.repaint();
        mapper.ensureNodeInView(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        QuickHelp = new javax.swing.JDialog();
        jScrollPane3 = new javax.swing.JScrollPane();
        QuickHelpTextPane = new javax.swing.JTextPane();
        MapNodePopup = new javax.swing.JPopupMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        ControlsPanel = new javax.swing.JPanel();
        BuildPanel = new javax.swing.JPanel();
        BackButton = new javax.swing.JButton();
        BuildLevel3Button = new javax.swing.JButton();
        BuildLevel4Button = new javax.swing.JButton();
        ColorPanel = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        ColorDriverCombo = new javax.swing.JComboBox();
        ModePanel = new javax.swing.JPanel();
        MindmapMode = new javax.swing.JRadioButton();
        BrizwalkMode = new javax.swing.JRadioButton();
        ZoomPanel = new javax.swing.JPanel();
        ResetZoomPan = new javax.swing.JButton();
        ZoomLabel = new javax.swing.JLabel();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/mindliner/resources/Mapper"); // NOI18N
        QuickHelp.setTitle(bundle.getString("ViewPanelQuickHelpTitle")); // NOI18N

        jScrollPane3.setPreferredSize(new java.awt.Dimension(400, 600));

        QuickHelpTextPane.setToolTipText(bundle.getString("ViewPanelQuickHelpTitle")); // NOI18N
        jScrollPane3.setViewportView(QuickHelpTextPane);

        javax.swing.GroupLayout QuickHelpLayout = new javax.swing.GroupLayout(QuickHelp.getContentPane());
        QuickHelp.getContentPane().setLayout(QuickHelpLayout);
        QuickHelpLayout.setHorizontalGroup(
            QuickHelpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(QuickHelpLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                .addContainerGap())
        );
        QuickHelpLayout.setVerticalGroup(
            QuickHelpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(QuickHelpLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE)
                .addContainerGap())
        );

        jMenuItem1.setText("jMenuItem1");
        MapNodePopup.add(jMenuItem1);

        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setLayout(new java.awt.BorderLayout());

        ControlsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 15, 5));

        BuildPanel.setOpaque(false);
        BuildPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 3, 0));

        BackButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/3232/arrow2_left_blue.png"))); // NOI18N
        BackButton.setToolTipText(bundle.getString("MindmapBackBtn")); // NOI18N
        BackButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BackButtonActionPerformed(evt);
            }
        });
        BuildPanel.add(BackButton);

        BuildLevel3Button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/3232/keyboard_key_3.png"))); // NOI18N
        BuildLevel3Button.setToolTipText(bundle.getString("MalLevel3Button_TT")); // NOI18N
        BuildLevel3Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BuildLevel3ButtonActionPerformed(evt);
            }
        });
        BuildPanel.add(BuildLevel3Button);

        BuildLevel4Button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/3232/keyboard_key_4.png"))); // NOI18N
        BuildLevel4Button.setToolTipText(bundle.getString("BuildLevel4Button_TT")); // NOI18N
        BuildLevel4Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BuildLevel4ButtonActionPerformed(evt);
            }
        });
        BuildPanel.add(BuildLevel4Button);

        ControlsPanel.add(BuildPanel);

        ColorPanel.setOpaque(false);
        ColorPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 3, 0));

        jLabel16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/3232/paint_bucket_blue.png"))); // NOI18N
        jLabel16.setLabelFor(ColorDriverCombo);
        ColorPanel.add(jLabel16);

        ColorDriverCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        ColorDriverCombo.setToolTipText(bundle.getString("ColorDriver_TT")); // NOI18N
        ColorDriverCombo.setOpaque(false);
        ColorPanel.add(ColorDriverCombo);

        ControlsPanel.add(ColorPanel);

        ModePanel.setOpaque(false);
        ModePanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 3, 0));

        MindmapMode.setText(bundle.getString("ViewerMindmapModeCheckbox")); // NOI18N
        MindmapMode.setToolTipText(bundle.getString("MindmapMode_TT")); // NOI18N
        MindmapMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MindmapModeActionPerformed(evt);
            }
        });
        ModePanel.add(MindmapMode);

        BrizwalkMode.setText(bundle.getString("ViewerBrizwalkModeCheckbox")); // NOI18N
        BrizwalkMode.setToolTipText(bundle.getString("BrizwalkMode_TT")); // NOI18N
        BrizwalkMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BrizwalkModeActionPerformed(evt);
            }
        });
        ModePanel.add(BrizwalkMode);

        ControlsPanel.add(ModePanel);

        ZoomPanel.setOpaque(false);
        ZoomPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 3, 0));

        ResetZoomPan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/3232/fit_to_size.png"))); // NOI18N
        ResetZoomPan.setToolTipText(bundle.getString("MindmapResetZoomPan")); // NOI18N
        ResetZoomPan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ResetZoomPanActionPerformed(evt);
            }
        });
        ZoomPanel.add(ResetZoomPan);

        ZoomLabel.setForeground(new java.awt.Color(153, 153, 153));
        ZoomLabel.setText("zoom: 1.0");
        ZoomPanel.add(ZoomLabel);

        ControlsPanel.add(ZoomPanel);

        add(ControlsPanel, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void BuildLevel3ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BuildLevel3ButtonActionPerformed
        adaptLevel(3);
    }//GEN-LAST:event_BuildLevel3ButtonActionPerformed

    private void BackButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BackButtonActionPerformed
        mapper.popNodes();
        repaint();
    }//GEN-LAST:event_BackButtonActionPerformed

    private void ResetZoomPanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ResetZoomPanActionPerformed
        initializeView();
    }//GEN-LAST:event_ResetZoomPanActionPerformed

    private void BuildLevel4ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BuildLevel4ButtonActionPerformed
        adaptLevel(4);
    }//GEN-LAST:event_BuildLevel4ButtonActionPerformed

    private void MindmapModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MindmapModeActionPerformed
        mapper.getNodeBuilder().setConnectorType(ConnectorType.CurvedTextClear);
        buildView();
    }//GEN-LAST:event_MindmapModeActionPerformed

    private void BrizwalkModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BrizwalkModeActionPerformed
        mapper.getNodeBuilder().setConnectorType(ConnectorType.ZeroConnector);
        buildView();
    }//GEN-LAST:event_BrizwalkModeActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BackButton;
    private javax.swing.JRadioButton BrizwalkMode;
    private javax.swing.JButton BuildLevel3Button;
    private javax.swing.JButton BuildLevel4Button;
    private javax.swing.JPanel BuildPanel;
    private javax.swing.JComboBox ColorDriverCombo;
    private javax.swing.JPanel ColorPanel;
    private javax.swing.JPanel ControlsPanel;
    private javax.swing.JPopupMenu MapNodePopup;
    private javax.swing.JRadioButton MindmapMode;
    private javax.swing.JPanel ModePanel;
    private javax.swing.JDialog QuickHelp;
    private javax.swing.JTextPane QuickHelpTextPane;
    private javax.swing.JButton ResetZoomPan;
    private javax.swing.JLabel ZoomLabel;
    private javax.swing.JPanel ZoomPanel;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JScrollPane jScrollPane3;
    // End of variables declaration//GEN-END:variables

    private void invalidateMapImages(MlMapNode n) {
        if (n.getObject() instanceof MlcImage) {
            CacheEngineStatic.invalidateImage(n.getObject().getId());
        }
        if (n.getObject().getIcons() == null) {
            // initiates background download, returns immediately
            // use case: object has been loaded from server, but not yet displayed. Then user goes offline, displays it on the map and goes online again.
            // then we need to load the icons
            IconLoader.getInstance().loadIcons(n.getObject());
        }
        if (n.getChildren() != null) {
            for (MlMapNode c : n.getChildren()) {
                invalidateMapImages(c);
            }
        }
    }

    class MapBuildWorker extends SimpleSwingWorker {

        public void buildMap() {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            BaseColorizer fkc = ColorManager.getColorizerForType(Colorizer.ColorDriverAttribute.FixedKey);
            mapper.getBackgroundPainter().setBackground(fkc.getColorForKey(FixedKeyColorizer.FixedKeys.MAP_BACKGROUND));

            if (!(mapper.getNodeBuilder() instanceof MindmapTextNodeBuilder)) {
                MindmapTextNodeBuilder rb = createNewRelationshipTextNodeBuilder();
                updateAtomizer(rb);
                mapper.setNodeBuilder(rb);
            } else {
                MindmapTextNodeBuilder nodeBuilder = (MindmapTextNodeBuilder) mapper.getNodeBuilder();
//                nodeBuilder.setConnectorType(ConnectorType.CurvedTextClear);
                updateAtomizer(nodeBuilder);
            }
            mapper.getNodeBuilder().setMaxCharacterCount(mapPreferences.getWordMaxLength());
            mapper.getNodeBuilder().setShowAttributes(mapPreferences.isShowAttributes());
            mapper.getNodeBuilder().setShowDescription(mapPreferences.isShowDescription());
            mapper.getNodeBuilder().setShowImages(mapPreferences.isShowImages());
            MindmapTextNodeBuilder rtnb = (MindmapTextNodeBuilder) mapper.getNodeBuilder();
            rtnb.setMaxLevel(mapBuildLevels);
            if (objects != null) {
                List<MlMapNode> nodes = mapper.getNodeBuilder().buildNodes(objects);
                mapper.setNodes(nodes);
            }

            // initialize positioner
            assignMapperPositioner();
            mapper.reAssignNodeColors();
            // when building a new map we need to force the positioner
            if (statusReporter != null) {
                //statusReporter.done();
            }
            mapper.ensureNodeInView(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }

        @Override
        protected Object doInBackground() throws Exception {
            buildMap();
            return null;
        }
    }

    class MapRoutineTasks implements ActionListener {

        private final long beautificationLag = 1000;
        private double lastScale = -1;
        private Date lastInspection = new Date();

        @Override
        public void actionPerformed(ActionEvent e) {
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(1);
            nf.setMinimumFractionDigits(1);

            // run a re-positioning of all nodes only after the user stop zooming
            ZoomLabel.setText("zoom: " + nf.format(mapper.getZoomAndPanListener().getScale()));
            if ((new Date()).getTime() - lastInspection.getTime() > beautificationLag
                    && lastScale == mapper.getZoomAndPanListener().getScale()) {
                mapper.repaint();
            } else {
                lastScale = mapper.getZoomAndPanListener().getScale();
                lastInspection = new Date();
            }
        }
    }

}
