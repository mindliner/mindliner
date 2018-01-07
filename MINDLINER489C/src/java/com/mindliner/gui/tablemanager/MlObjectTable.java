/*
 * MlTableModuleUI.java
 *
 * Created on 7. Juli 2007, 22:50
 */
package com.mindliner.gui.tablemanager;

import com.mindliner.analysis.MlClassHandler.MindlinerObjectType;
import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.cache.MlCacheException;
import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.categories.mlsPriority;
import com.mindliner.clientobjects.MlClientClassHandler;
import com.mindliner.clientobjects.MlcContainerMap;
import com.mindliner.clientobjects.mlcNews;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.entities.Colorizer.ColorDriverAttribute;
import com.mindliner.entities.SoftwareFeature;
import com.mindliner.events.MlEventLogger;
import com.mindliner.events.ObjectChangeObserver;
import com.mindliner.events.SearchTermManager;
import com.mindliner.events.SelectionManager;
import com.mindliner.events.SelectionObserver;
import com.mindliner.exporter.MindlinerTransferHandler;
import com.mindliner.gui.ObjectEditorLauncher;
import com.mindliner.gui.MindlinerObjectDeletionHandler;
import com.mindliner.gui.ObjectMapperLauncher;
import com.mindliner.gui.color.BaseColorizer;
import com.mindliner.gui.color.ColorManager;
import com.mindliner.gui.color.FixedKeyColorizer;
import com.mindliner.gui.font.FontPreferences;
import com.mindliner.gui.tablemodels.MlCellEditorFactory;
import com.mindliner.gui.tablemodels.MlPopupListener;
import com.mindliner.gui.tablemodels.MlTableCellRenderer;
import com.mindliner.gui.tablemodels.MlTableColumn;
import com.mindliner.gui.tablemodels.MlTableModel;
import com.mindliner.gui.tablemodels.MlTableModelFactory;
import com.mindliner.img.icons.MlIconManager;
import com.mindliner.main.SearchPanel;
import com.mindliner.managers.ObjectManagerRemote;
import com.mindliner.styles.MlStyler;
import com.mindliner.prefs.TableColorPreferences;
import com.mindliner.serveraccess.OnlineService;
import com.mindliner.serveraccess.RemoteLookupAgent;
import com.mindliner.thread.SearchWorker;
import com.mindliner.view.connectors.NodeConnection;
import com.mindliner.view.dispatch.MlObjectViewer;
import com.mindliner.view.dispatch.MlViewDispatcherImpl;
import com.mindliner.weekplanner.JTableRowHeightAdjuster;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.prefs.Preferences;
import javax.naming.NamingException;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

/**
 * This class represents the GUI the mindliner table composit. It consists of a
 * search string text field, a label displaying the number of elements in the
 * table, the title of the module, and the table itself.
 *
 *
 * @author Marius Messerli
 */
public class MlObjectTable extends JPanel implements Comparable, OnlineService, ObjectChangeObserver, SelectionObserver, MlObjectViewer {

    private MindlinerObjectType type = MindlinerObjectType.Any;
    private int displayColumnCount = 1;
    private List<SortKey> sortKeyList = new ArrayList<>();
    private List<MlTableColumn> columns = new ArrayList<>();
    private List<mlcObject> records = new ArrayList<>();
    private final Stack<List<mlcObject>> recordsHistory = new Stack<>();
    private boolean showsPrimaryRecords = true;
    private boolean editorsInitialized = false;
    private final Map<Integer, Integer> columnMovePositionPairs = new HashMap<>();
    protected DecoratedTable table = null;
    private String identifyer = null;
    private BaseColorizer colorizer;
    private MlTableCellRenderer cellRenderer = null;
    // the following are preference strings
    private static final String COLUMN_COUNT_EXTENSION = "-colcount";
    private static final String SORTING_COLUMN_NUMBER_EXTENSION = "-sortcolumn";
    private static final String SORTING_ORDER_EXTENSION = "-sortorder";
    private static final String SORTING_KEY_COUNT_EXTENSION = "-keycount";
    private static final String COLUMN_TO_MODEL_EXTENSION = "-ViewToModel-";
    private static final String COLUMN_WIDTH_EXTENSION = "-col-";
    public static final String TYPE_EXTENSION = "-type";
    private static final String NAME_EXTENSION = "-name";
    private static final String COLORIZER_TYPE_EXTENSION = "-coltype";
    private static final String OPTIONAL_CONTROLS_COLLAPSED_KEY = "-optcontrols";
    private static final int DEFAULT_COLUMN_WIDTH = 50;
    private static final int MAX_ROW_HEIGHT = 100;
    private OnlineStatus onlineStatus = OnlineStatus.offline;
    private ObjectManagerRemote objectManager = null;
    private int connectionPriority = 0;
    private static final int IMG_ROW_HEIGHT = 36;
    private static final int DEFAULT_ROW_HEIGHT = 16;
    private boolean active = true;
    public static final String FONT_PREFERENCE_KEY = "searchtable";

    /**
     * The SearchTable is a function rich table to display mindliner objects.
     * The SearchTable supports sorting, column switching, attribute-based color
     * coding and in-cell editing.
     *
     * @param type The type of object this table is supposed to accept and
     * display
     * @param identifyer An identifyer that is used to persist values between
     * the session
     * @param colorizerAttribute Which attribute of objects should be used to
     * drive the color
     */
    public MlObjectTable(MindlinerObjectType type, String identifyer, ColorDriverAttribute colorizerAttribute) {
        this.type = type;
        this.identifyer = identifyer;
        colorizer = ColorManager.getColorizerForType(colorizerAttribute);
        initComponents();
        configureComponents();
        adaptRowHeight(type);

        /**
         * this is a dual-call; setupRenderer will be called again as part of
         * the updateConfiguration call below but is needed for loading the
         * prefs
         */
        setupRenderer();
        loadPreferences();
        updateConfiguration();
    }

    @Override
    public void objectChanged(mlcObject o) {
        if (o != null) {
            if (getType().equals(MindlinerObjectType.Any) || type.equals(MlClientClassHandler.getTypeByClass(o.getClass()))) {
                updateObject(o);
            }
        }
    }

    @Override
    public void objectDeleted(mlcObject o) {
        if (records.contains(o)) {
            records.remove(o);
            redisplay();
        }
    }

    @Override
    public void objectCreated(mlcObject o) {
        if (o.isOwnedByCurrentUser()
                && (type.equals(MlClientClassHandler.getTypeByClass(o.getClass())) || getType().equals(MindlinerObjectType.Any))
                && !(o instanceof mlcNews)) {
            addObject(o);
            SearchPanel.applySelectedSorting(records);
            redisplay();
        }
    }

    @Override
    public void objectReplaced(int oldId, mlcObject o) {
        boolean redisplay = false;
        Iterator<mlcObject> it = records.iterator();
        while (it.hasNext()) {
            mlcObject obj = it.next();
            if (obj.getId() == oldId) {
                it.remove();
                redisplay = true;
            }
        }
        if (type.equals(MlClientClassHandler.getTypeByClass(o.getClass())) || getType().equals(MindlinerObjectType.Any)) {
            addObject(o);
            redisplay = true;
        }
        if (redisplay) {
            redisplay();
        }
    }

    @Override
    public void selectionChanged(List<mlcObject> newSelection) {
        if (newSelection.isEmpty()) {
            return;
        }
        boolean needSelectionUpdate = false;
        MlTableModel tm = (MlTableModel) table.getModel();
        int[] selectedRows = table.getSelectedRows();

        if (selectedRows.length == 0) {
            needSelectionUpdate = true;
        } else {
            for (int i : selectedRows) {
                if (!newSelection.contains(tm.getSourceObject(i))) {
                    needSelectionUpdate = true;
                }
            }
        }

        if (needSelectionUpdate) {
            clearSelections();
            mlcObject lastSelection = newSelection.get(newSelection.size() - 1);

            // highlight the last selection
            for (int i = 0; i < tm.getRowCount(); i++) {
                if (lastSelection.equals(tm.getSourceObject(i))) {
                    RowSorter trs = table.getRowSorter();
                    int viewRowIndex = trs.convertRowIndexToView(i);
                    table.setRowSelectionInterval(viewRowIndex, viewRowIndex);
                    return;
                }
            }
        }
    }

    @Override
    public void connectionSelectionChanged(List<NodeConnection> selection) {
        // do nothing. The table doesn't care whether a connection is selected or not
    }

    @Override
    public void clearConnectionSelections() {

    }

    @Override
    public void clearSelections() {
        getJTable().clearSelection();
    }

    @Override
    public final void goOffline() {
        objectManager = null;
        onlineStatus = OnlineStatus.offline;
    }

    @Override
    public final void goOnline() throws MlCacheException {
        try {
            objectManager = (ObjectManagerRemote) RemoteLookupAgent.getManagerForClass(ObjectManagerRemote.class);
            onlineStatus = OnlineStatus.online;
        } catch (NamingException ex) {
            throw new MlCacheException(ex.getMessage());
        }
    }

    @Override
    public OnlineStatus getStatus() {
        return onlineStatus;
    }

    @Override
    public String getServiceName() {
        return "Search Table";
    }

    @Override
    public int getConnectionPriority() {
        return connectionPriority;
    }

    @Override
    public void setConnectionPriority(int priority) {
        connectionPriority = priority;
    }

    public void setIsRestricted(boolean isRestricted) {
        RestrictionsApplyLabel.setVisible(isRestricted);
    }

    private void adaptToType(MindlinerObjectType type) {
        this.type = type;
        int currentColumnCount = getDisplayColumnCount();
        adaptRowHeight(type);
        setName(type.toString());
        ObjectTypeIconLabel.setIcon(MlIconManager.getIconForType(type));
        setColumns(TableColumnManager.getAvailableColumns(type));
        setDisplayColumnCount(Math.min(currentColumnCount, columns.size()));
        updateConfiguration();
    }

    public void setTableName(String n) {
        setName(n);
    }

    protected void installTransferHandler(DecoratedTable t) {
        t.setTransferHandler(new MindlinerTransferHandler());
    }

    private void configureComponents() {
        setName(type.toString());
        SearchField.setFont(FontPreferences.getFont(FONT_PREFERENCE_KEY));
        table = new DecoratedTable(this);
        table.setDragEnabled(true);
        table.setOpaque(true);
        ObjectTypeIconLabel.setIcon(MlIconManager.getIconForType(type));
        setColumns(TableColumnManager.getAvailableColumns(type));

        JTableHeader th = table.getTableHeader();
        th.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getButton() == MouseEvent.BUTTON3) {
                    table.getRowSorter().setSortKeys(null);
                }
            }
        });
        th.setToolTipText("Use left mouse button to set and right mouse button to clear sorting");

        TableRowSorter trs = new TableRowSorter();
        trs.setMaxSortKeys(1);
        table.setRowSorter(trs);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                showSelectedRow(evt);
            }
        });
        table.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                showSelectedRow(evt);
            }
        });
        TableScrollPane.setViewportView(table);

        updateTableSupportColors();
        installTransferHandler(table);
        List<MindlinerObjectType> types = compileEligibleTypes();
        for (MindlinerObjectType value : types) {
            final MindlinerObjectType t = (MindlinerObjectType) value;
            JMenuItem m = new JMenuItem();
            m.setText(t.toString());
            m.addActionListener((ActionEvent e) -> {
                adaptToType(t);
            });
            TableTypeMenu.add(m);
        }

        for (int i = 1; i < 7; i++) {
            JMenuItem m = new JMenuItem();
            final int ccount = i;
            m.setText(Integer.toString(i));
            m.addActionListener((ActionEvent e) -> {
                setDisplayColumnCount(ccount);
                updateConfiguration();
            });
            TableColumnMenu.add(m);
        }
        // JUST IN CASE WE BRING THE CUSTOM TABLES BACK SOMEHOW
        MlPopupListener popup = new MlPopupListener(TableTypePopup, false);
        ObjectTypeIconLabel.addMouseListener(popup);

        // ensure that we have at least two columns in the default case
        if (type == MindlinerObjectType.Any && getDisplayColumnCount() == 1) {
            setDisplayColumnCount(2);
            updateConfiguration();
        }

        if (!CacheEngineStatic.getCurrentUser().isAuthorizedForFeature(SoftwareFeature.CurrentFeatures.CUSTOMIZATION)) {
            OptionExpansionLabel.setVisible(false);
        }
    }

    private List<MindlinerObjectType> compileEligibleTypes() {
        List<MindlinerObjectType> types = new ArrayList<>(Arrays.asList(MindlinerObjectType.values()));
        if (!CacheEngineStatic.getCurrentUser().isAuthorizedForFeature(SoftwareFeature.CurrentFeatures.WORKSPHEREMAP)) {
            types.remove(MindlinerObjectType.Map);
            types.remove(MindlinerObjectType.Container);
        }
        return types;
    }

    public void applyColors(BaseColorizer colorizer) {
        Color bg = colorizer.getColorForKey(FixedKeyColorizer.FixedKeys.MAIN_DEFAULT_BACKGROUND);
        Color fg = colorizer.getColorForKey(FixedKeyColorizer.FixedKeys.MAIN_DEFAULT_TEXT);
        setBackground(bg);
        SearchField.setForeground(fg);
        SearchField.setBackground(bg);
        TablePanel.setForeground(fg);
        MlStyler.colorizeButton(ColorsButton, colorizer);
        BaseColorizer fkc = ColorManager.getColorizerForType(ColorDriverAttribute.FixedKey);
        MlStyler.colorizeTableHeader(table,
                fkc.getColorForKey(FixedKeyColorizer.FixedKeys.TABLE_HEADER_TEXT),
                fkc.getColorForKey(FixedKeyColorizer.FixedKeys.TABLE_HEADER_BACKGROUND));
    }

    public void updateTableSupportColors() {
        BaseColorizer fkc = ColorManager.getColorizerForType(ColorDriverAttribute.FixedKey);
        TableScrollPane.getViewport().setBackground(fkc.getColorForObject(FixedKeyColorizer.FixedKeys.TABLE_BACKGROUND));
        table.setGridColor(fkc.getColorForObject(FixedKeyColorizer.FixedKeys.TABLE_GRID));
        ColorPanel.setBackground(fkc.getColorForObject(FixedKeyColorizer.FixedKeys.TABLE_HEADER_BACKGROUND));
        RestrictionsApplyLabel.setForeground(fkc.getColorForKey(FixedKeyColorizer.FixedKeys.TABLE_HEADER_TEXT));
    }

    public List<mlcObject> getSelectedSourceObjects() {
        int[] selectedViewRows = table.getSelectedRows();
        MlTableModel mtm = (MlTableModel) table.getModel();
        List<mlcObject> selectedObjects = new ArrayList<>();
        for (int i = 0; i < selectedViewRows.length; i++) {
            int selectedModelRow = table.convertRowIndexToModel(selectedViewRows[i]);
            selectedObjects.add(mtm.getSourceObject(selectedModelRow));
        }
        return selectedObjects;
    }

    private void editSelectedObjects() {
        List<mlcObject> selectedSourceObjects = getSelectedSourceObjects();
        if (selectedSourceObjects.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please select at least one object to edit", "Text Editor", JOptionPane.ERROR_MESSAGE);
        } else {
            ObjectEditorLauncher.showEditor(selectedSourceObjects);
        }
    }

    /**
     * Function to return the source object for the selected table row.
     *
     * @return The mindliner object that contains the source data for the
     * selected row in the specified table.
     */
    public mlcObject getSelectedSourceObject() {
        int i = table.getSelectedRow();
        if (i != -1) {
            int selectedModelRow = table.getRowSorter().convertRowIndexToModel(i);
            MlTableModel mtm = (MlTableModel) table.getModel();
            return mtm.getSourceObject(selectedModelRow);
        } else {
            return null;
        }
    }

    protected void handleRowDoubleClick() {
        mlcObject source = getSelectedSourceObject();
        table.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SearchTermManager.setSearchTerm("");
        List<mlcObject> relatives = CacheEngineStatic.getLinkedObjects(source);
        if (!relatives.isEmpty()) {
            MlEventLogger.logReadEvent(source);
            MlViewDispatcherImpl.getInstance().display(relatives, ViewType.GenericTable);
        }
        table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    private void showSelectedRow(InputEvent e) {
        mlcObject source = getSelectedSourceObject();
        if (source == null) {
            return;
        }

        if (e instanceof MouseEvent) {
            MouseEvent evt = (MouseEvent) e;
            if (evt.getButton() == java.awt.event.MouseEvent.BUTTON1) {
                int clickCount = evt.getClickCount();
                switch (clickCount) {
                    case 1:
                        List<mlcObject> selectedSourceObjects = getSelectedSourceObjects();
                        SelectionManager.setSelection(selectedSourceObjects);
                        break;

                    case 2:
                        handleRowDoubleClick();
                        break;
                }
            } else {
                TablePopupFactory pf = new TablePopupFactory(getSelectedSourceObjects());
                JPopupMenu popup = pf.createPopup();
                popup.show(evt.getComponent(), evt.getX(), evt.getY());
            }
        } else if (e instanceof KeyEvent) {
            KeyEvent evt = (KeyEvent) e;
            switch (evt.getKeyCode()) {
                case KeyEvent.VK_UP:
                case KeyEvent.VK_DOWN:
                    SelectionManager.setSelection(getSelectedSourceObjects());
                    break;
                case KeyEvent.VK_RIGHT:
                    table.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    MlViewDispatcherImpl.getInstance().display(source, ViewType.GenericTable);
                    table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    break;
                case KeyEvent.VK_LEFT:
                    pop();
                    break;
                case KeyEvent.VK_E:
                    editSelectedObjects();
                    break;
                case KeyEvent.VK_V:
                    mapSelectedObjects();
                    break;
                case KeyEvent.VK_W:
                    if (source instanceof MlcContainerMap && CacheEngineStatic.getCurrentUser().isAuthorizedForFeature(SoftwareFeature.CurrentFeatures.WORKSPHEREMAP)) {
                        MlViewDispatcherImpl.getInstance().display(source, MlObjectViewer.ViewType.ContainerMap);
                    }   break;
                case KeyEvent.VK_DELETE:
                    MindlinerObjectDeletionHandler.delete(getSelectedSourceObjects());
                    displayTableData();
                    break;
                default:
                    break;
            }
        }
    }

    public String getSearchString() {
        return SearchField.getText();
    }
    
    /**
     * This member sets the string field only. It does not carry out a search operation.
     */
    public void setSearchStringOnly(String s){
        SearchField.setText(s);
    }

    public JTable getJTable() {
        return table;
    }

    public int getDisplayColumnCount() {
        return displayColumnCount;
    }

    private void removeIllegalSortKeys() {
        // in case the table layout has change, remove keys for non-existing columns
        Iterator it = sortKeyList.iterator();
        for (; it.hasNext();) {
            SortKey s = (SortKey) it.next();
            if (s.getColumn() >= displayColumnCount) {
                it.remove();
            }
        }
    }

    public void setSortKey(List<SortKey> list) {
        sortKeyList = list;
        removeIllegalSortKeys();
    }

    public BaseColorizer getColorizer() {
        return colorizer;
    }

    public void setColorizer(BaseColorizer colorizer) {
        this.colorizer = colorizer;
        cellRenderer.setColorizer(colorizer);
    }

    /**
     * Defines a new column count. Don't forget to call initializeTable after
     * changing this value and before using the table again.
     *
     * @param count The number of columns.
     */
    protected void setDisplayColumnCount(int count) {
        if (count > columns.size()) {
            System.err.println("Attemp to set column count higher than max column count for table " + getName());
            count = columns.size();
        }
        displayColumnCount = count;
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * This method defines the renderers and the editors for all Mindliner
     * tables. It can only be called after data has been loaded into the table
     * as the renderer definitions depend on the objct classes.
     */
    public final void setupRenderer() {

        cellRenderer = new MlTableCellRenderer(colorizer);
        for (int i = 0; i < table.getModel().getColumnCount(); i++) {
            TableColumn col = table.getColumnModel().getColumn(i);
            col.setCellRenderer(cellRenderer);
        }
        if (!editorsInitialized) {
            table.setDefaultEditor(mlsPriority.class, MlCellEditorFactory.createEditor(mlsPriority.class));
            table.setDefaultEditor(mlsConfidentiality.class, MlCellEditorFactory.createEditor(mlsConfidentiality.class));
            editorsInitialized = true;
        }
    }

    @Override
    public int compareTo(Object o) {
        return getName().compareTo(((MlObjectTable) o).getName());
    }

    public void setColumnWidth(int index, int width) {
        MlTableColumn c = columns.get(index);
        if (c != null) {
            c.setWidth(width);
        }
    }

    public Integer getColumnWidth(int index) {
        MlTableColumn c = columns.get(index);
        if (c != null) {
            return c.getWidth();
        }
        throw new IllegalArgumentException("warning: index out of range.");
    }

    public void displayTableData() {
        table.getRowSorter().setSortKeys(null);
        MlTableModel model = (MlTableModel) table.getModel();
        model.clear();
        if (records.isEmpty()) {
            model.fireTableDataChanged();
            return;
        }

        int currentRow = 0;
        for (mlcObject mbo : records) {
            model.setRow(currentRow, mbo);
            currentRow++;
        }
        model.fireTableDataChanged();
        JTableRowHeightAdjuster.updateRowHeights(table, MAX_ROW_HEIGHT);
        ColumnSizeManager.checkColumnSizes(this);
    }

    /**
     * Updates various table config setting according to the type and name of
     * the table. This function shoudl be called after key attributes have been
     * changed, there is no harm in calling this function too many times.
     *
     * Specifically it updates the table model according to the (new) table
     * type, it loads the new headers, it (re-)reads the column widths from the
     * prefrences it sets up the renderer.
     */
    protected final void updateConfiguration() {
        MlTableModel itm = MlTableModelFactory.createModel(this);
        table.setModel(itm);
        TableRowSorter trs = (TableRowSorter) table.getRowSorter();
        trs.setModel(itm);
        removeIllegalSortKeys();
        trs.setSortKeys(sortKeyList);

        // rearrange call must preceed the initialization of the column widths
        rearrangeColumnSequence();

        // now specify the column widths
        for (int i = 0; i < table.getColumnCount(); i++) {
            TableColumn tc = table.getColumnModel().getColumn(i);
            tc.setPreferredWidth(getColumnWidth(i));
        }
        setupRenderer();
        RevertToLastList.setEnabled(false);
    }

    public void setShowsPrimaryRecords(boolean b) {
        showsPrimaryRecords = b;
    }

    public boolean isShowsPrimaryRecords() {
        return showsPrimaryRecords;
    }

    private void push() {
        if (records.size() > 0) {
            recordsHistory.push(records);
            RevertToLastList.setEnabled(true);
        }
    }

    private void pop() {
        if (recordsHistory.size() > 0) {
            records = recordsHistory.pop();
        }
        if (recordsHistory.isEmpty()) {
            RevertToLastList.setEnabled(false);
        }
        displayTableData();
        SearchField.setText("");
    }

    /**
     * clear all elements from this table
     */
    public void clear() {
        push();
        records.clear();
        displayTableData();
    }

    public void addObject(mlcObject o) {
        if (!records.contains(o) && isQualified(o)) {
            records.add(o);
        }
    }

    public void redisplay() {
        displayTableData();
    }

    private boolean isQualified(mlcObject o) {
        return ((type.equals(MlClientClassHandler.getTypeByClass(o.getClass())) 
                || getType().equals(MindlinerObjectType.Any)) && !(o instanceof mlcNews));
    }

    /**
     * Some tables are type-pure tables so I need to filter out all other
     * objects.
     *
     * @param input The object list potentially containing objects that are of a
     * type/class not displayed here.
     * @return A list of objects that are of the proper type/class.
     */
    private List<mlcObject> getFactorizableObjects(List<mlcObject> input) {
        List<mlcObject> result = new ArrayList<>();
        input.stream().filter((o) -> (isQualified(o))).forEach((o) -> {
            result.add(o);
        });
        return result;
    }

    @Override
    public void display(List<mlcObject> objectList, ViewType type) {
        if (active && isSupported(type)) {
            push();
            SearchPanel.applySelectedSorting(objectList);
            records = getFactorizableObjects(objectList);
            redisplay();
        }
    }

    @Override
    public void display(mlcObject object, ViewType type) {
        if (active && isSupported(type)) {
            ArrayList<mlcObject> list = new ArrayList<>();
            list.add(object);
            display(list, type);
        }
    }

    @Override
    public boolean isSupported(ViewType type) {
        return type == ViewType.GenericTable;
    }

    @Override
    public void back() {
        // ignores as this table implements its own back function
    }

    public int getMaxColumnCount() {
        return columns.size();
    }

    public MindlinerObjectType getType() {
        return type;
    }

    public void runSearch(String searchString) {
        SearchField.setText(searchString);
        runSearch();
    }

    private void runSearch() {
        SearchField.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SearchWorker sw = new SearchWorker(SearchField.getText(), getType(), ViewType.GenericTable);
        sw.execute();
        SearchField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    }

    /**
     * Used by the ActionItemTable who does not need these functions
     */
    protected void hideSearchViewEditControls() {
        SearchField.setVisible(false);
        RevertToLastList.setVisible(false);
    }

    public void addColumnMovePositionPair(int model, int view) {
        columnMovePositionPairs.put(model, view);
    }

    private void rearrangeColumnSequence() {
        Set<Integer> modelPositions = columnMovePositionPairs.keySet();
        modelPositions.stream().forEach((pos) -> {
            table.moveColumn(pos, columnMovePositionPairs.get(pos));
        });
    }

    public void setColumns(List<MlTableColumn> clist) {
        columns = clist;
    }

    public List<MlTableColumn> getColumns() {
        return columns;
    }

    public MlTableCellRenderer getCellRenderer() {
        return cellRenderer;
    }

    /**
     * This function updates the specified object in all the containers of this
     * class.
     *
     * @param o The object to be updated.
     * @return True if an older version of this object existed and was updated
     * false otherwise.
     */
    private boolean updateObject(mlcObject newObject) {
        MlTableModel mtm = (MlTableModel) getJTable().getModel();
        int row = -1;
        for (int i = 0; row == -1 && i < mtm.getRowCount(); i++) {
            mlcObject o = (mlcObject) mtm.getSourceObject(i);
            if (o.equals(newObject)) {
                records.remove(o);
                records.add(newObject);
                mtm.setRow(i, newObject);
                row = i;
            }
        }
        if (row != -1) {
            mtm.fireTableRowsUpdated(row, row);
//            mtm.fireTableDataChanged();
        }
        return row != -1;
    }

    public DecoratedTable getTable() {
        return table;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MlObjectTable other = (MlObjectTable) obj;
        if (this.type != other.type) {
            return false;
        }
        if ((this.getName() == null) ? (other.getName() != null) : !this.getName().equals(other.getName())) {
            return false;
        }
        if (this.displayColumnCount != other.displayColumnCount) {
            return false;
        }
        return this.colorizer == other.colorizer || (this.colorizer != null);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 37 * hash + (this.getName() != null ? this.getName().hashCode() : 0);
        hash = 37 * hash + this.displayColumnCount;
        hash = 37 * hash + (this.colorizer != null ? this.colorizer.hashCode() : 0);
        return hash;
    }

    private void loadPreferences() {

        Preferences userPrefs = Preferences.userNodeForPackage(MlObjectTable.class);
        String prefix = TableManager.getTablePreferencePrefix() + identifyer;

        String colorizerAttributeString = userPrefs.get(prefix + COLORIZER_TYPE_EXTENSION, ColorDriverAttribute.Confidentiality.toString());
        ColorDriverAttribute colordriverAttribute;
        try {
            colordriverAttribute = ColorDriverAttribute.valueOf(colorizerAttributeString);
        } catch (IllegalArgumentException ex) {
            colordriverAttribute = ColorDriverAttribute.Confidentiality;
        }
        colorizer = ColorManager.getColorizerForType(colordriverAttribute);
        setDisplayColumnCount(userPrefs.getInt(prefix + COLUMN_COUNT_EXTENSION, displayColumnCount));
        int keyCount = userPrefs.getInt(prefix + SORTING_KEY_COUNT_EXTENSION, 0);
        if (keyCount > 0) {
            SortOrder so;
            try {
                String orderName = userPrefs.get(prefix + SORTING_ORDER_EXTENSION, "UNSORTED");
                so = SortOrder.valueOf(orderName);
            } catch (IllegalArgumentException e) {
                so = SortOrder.UNSORTED;
            }
            int sortColumn = userPrefs.getInt(prefix + SORTING_COLUMN_NUMBER_EXTENSION, -1);
            if (sortColumn >= 0 && sortColumn < displayColumnCount) {
                SortKey sk = new SortKey(sortColumn, so);
                List<SortKey> skl = new ArrayList<>();
                skl.add(sk);
                setSortKey(skl);
            }
        }

        for (int i = 0; i < getDisplayColumnCount(); i++) {
            int cWidth = userPrefs.getInt(prefix + COLUMN_WIDTH_EXTENSION + Integer.toString(i),
                    DEFAULT_COLUMN_WIDTH);
            setColumnWidth(i, cWidth);
        }
        boolean optcontrols = userPrefs.getBoolean(prefix + OPTIONAL_CONTROLS_COLLAPSED_KEY, true);
        OptionalControls.setCollapsed(optcontrols);
        loadColumnSequence(prefix);
    }

    private void loadColumnSequence(String prefix) {
        Preferences userPrefs = Preferences.userNodeForPackage(MlObjectTable.class);
        List<Integer> modelPositions = new ArrayList<>();
        for (int viewPosition = 0; viewPosition < getDisplayColumnCount(); viewPosition++) {
            int modelPosition = userPrefs.getInt(prefix + COLUMN_TO_MODEL_EXTENSION + viewPosition, viewPosition);
            modelPositions.add(modelPosition);
        }
        for (int viewPosition = 0; viewPosition < getDisplayColumnCount(); viewPosition++) {
            int modelPos = modelPositions.get(viewPosition);
            if (modelPos != viewPosition) {
                addColumnMovePositionPair(modelPos, viewPosition);
                // now correct column positions to the right
                for (int j = viewPosition + 1; j <= modelPos; j++) {
                    modelPositions.set(j, modelPositions.get(j) + 1);
                }
            }
        }
    }

    public void storePreferences(String root) {
        Preferences userPrefs = Preferences.userNodeForPackage(MlObjectTable.class);
        String prefix = root + identifyer;
        JTable jtable = getJTable();
        int columnCount = jtable.getColumnCount();
        if (table.getColumnCount() != columnCount) {
            throw new IllegalStateException("column count inconsistency");
        }
        userPrefs.putInt(prefix + COLUMN_COUNT_EXTENSION, columnCount);
        for (int i = 0; i < columnCount; i++) {
            TableColumn tc = jtable.getColumnModel().getColumn(i);
            userPrefs.putInt(prefix + COLUMN_WIDTH_EXTENSION + Integer.toString(i), tc.getWidth());
        }
        List<SortKey> sortingKeys = (List<SortKey>) jtable.getRowSorter().getSortKeys();
        userPrefs.putInt(prefix + SORTING_KEY_COUNT_EXTENSION, sortingKeys.size());
        if (sortingKeys.size() == 1) {
            SortKey sk = sortingKeys.get(0);
            userPrefs.putInt(prefix + SORTING_COLUMN_NUMBER_EXTENSION, sk.getColumn());
            userPrefs.put(prefix + SORTING_ORDER_EXTENSION, sk.getSortOrder().toString());
        } else if (sortingKeys.size() > 1) {
            System.err.println("More than one sorting key found but only one is supported here. Ignoring extra keys.");
        }
        userPrefs.put(prefix + TYPE_EXTENSION, getType().toString());
        userPrefs.put(prefix + NAME_EXTENSION, getName());
        userPrefs.put(prefix + COLORIZER_TYPE_EXTENSION, colorizer.getDriverAttribute().toString());
        userPrefs.putBoolean(prefix + OPTIONAL_CONTROLS_COLLAPSED_KEY, OptionalControls.isCollapsed());
        storeColumnSequence(jtable, prefix);
    }

    private static void storeColumnSequence(JTable table, String prefix) {
        Preferences userPrefs = Preferences.userNodeForPackage(MlObjectTable.class);
        for (int i = 0; i < table.getColumnCount(); i++) {
            userPrefs.putInt(prefix + COLUMN_TO_MODEL_EXTENSION + i, table.convertColumnIndexToModel(i));
        }
    }

    private void mapSelectedObjects() {
        table.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        List<mlcObject> selectedSourceObjects = getSelectedSourceObjects();
        ObjectMapperLauncher.map(selectedSourceObjects);
        table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }



    private void adaptRowHeight(MindlinerObjectType type) {
        if (type.equals(MindlinerObjectType.Image)) {
            table.setRowHeight(IMG_ROW_HEIGHT);
        } else {
            table.setRowHeight(DEFAULT_ROW_HEIGHT);
        }
    }

    @Override
    public void setActive(boolean state) {
        active = state;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        TableTypePopup = new javax.swing.JPopupMenu();
        TableTypeMenu = new javax.swing.JMenu();
        TableColumnMenu = new javax.swing.JMenu();
        ColorPanel = new javax.swing.JPanel();
        LeftControlsPanel = new javax.swing.JPanel();
        SearchEssentialsPanel = new javax.swing.JPanel();
        RevertToLastList = new javax.swing.JButton();
        SearchField = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        RestrictionsApplyLabel = new javax.swing.JLabel();
        OptionExpansionLabel = new javax.swing.JLabel();
        OptionalControls = new org.jdesktop.swingx.JXCollapsiblePane();
        ColorsButton = new javax.swing.JButton();
        ObjectTypeIconLabel = new javax.swing.JLabel();
        SpacerPanel = new javax.swing.JPanel();
        TablePanel = new javax.swing.JPanel();
        TableScrollPane = new javax.swing.JScrollPane();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/mindliner/resources/Tables"); // NOI18N
        TableTypeMenu.setText(bundle.getString("SearchTable_QuickAssignmentPopup")); // NOI18N
        TableTypePopup.add(TableTypeMenu);

        TableColumnMenu.setText(bundle.getString("SearchTable_TableColumnPopup")); // NOI18N
        TableTypePopup.add(TableColumnMenu);

        setPreferredSize(new java.awt.Dimension(250, 27));
        setLayout(new java.awt.BorderLayout());

        ColorPanel.setBackground(new java.awt.Color(201, 201, 201));
        ColorPanel.setOpaque(false);
        ColorPanel.setLayout(new java.awt.BorderLayout());

        LeftControlsPanel.setOpaque(false);
        LeftControlsPanel.setLayout(new java.awt.BorderLayout());

        SearchEssentialsPanel.setOpaque(false);
        SearchEssentialsPanel.setLayout(new java.awt.BorderLayout());

        RevertToLastList.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/3232/arrow2_left_blue.png"))); // NOI18N
        RevertToLastList.setToolTipText(bundle.getString("SearchTableNavigateBack_TT")); // NOI18N
        RevertToLastList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RevertToLastListActionPerformed(evt);
            }
        });
        SearchEssentialsPanel.add(RevertToLastList, java.awt.BorderLayout.WEST);

        SearchField.setColumns(12);
        SearchField.setFont(SearchField.getFont().deriveFont(SearchField.getFont().getSize()+1f));
        SearchField.setToolTipText(bundle.getString("TableSearchField_TT")); // NOI18N
        SearchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SearchFieldKeyPressed(evt);
            }
        });
        SearchEssentialsPanel.add(SearchField, java.awt.BorderLayout.CENTER);

        jPanel1.setOpaque(false);
        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0));

        RestrictionsApplyLabel.setForeground(new java.awt.Color(255, 0, 0));
        RestrictionsApplyLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        RestrictionsApplyLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/3232/led_red.png"))); // NOI18N
        RestrictionsApplyLabel.setToolTipText(bundle.getString("RestrictionsApplyTooltip")); // NOI18N
        RestrictionsApplyLabel.setAlignmentY(0.0F);
        RestrictionsApplyLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                RestrictionsApplyLabelMouseClicked(evt);
            }
        });
        jPanel1.add(RestrictionsApplyLabel);

        OptionExpansionLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/3232/bullet_triangle_glass_grey.png"))); // NOI18N
        OptionExpansionLabel.setToolTipText(bundle.getString("SearchPanelOptionalParameter_TT")); // NOI18N
        OptionExpansionLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                OptionExpansionLabelMouseClicked(evt);
            }
        });
        jPanel1.add(OptionExpansionLabel);

        SearchEssentialsPanel.add(jPanel1, java.awt.BorderLayout.EAST);

        LeftControlsPanel.add(SearchEssentialsPanel, java.awt.BorderLayout.CENTER);

        OptionalControls.setDirection(org.jdesktop.swingx.JXCollapsiblePane.Direction.LEFT);
        OptionalControls.setOpaque(false);
        OptionalControls.getContentPane().setLayout(new org.jdesktop.swingx.HorizontalLayout());

        ColorsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/2424/paint_bucket_blue.png"))); // NOI18N
        ColorsButton.setToolTipText(bundle.getString("TableColorWheel_TT")); // NOI18N
        ColorsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ColorsButtonActionPerformed(evt);
            }
        });
        OptionalControls.getContentPane().add(ColorsButton);

        ObjectTypeIconLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        ObjectTypeIconLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/3232/preferences.png"))); // NOI18N
        ObjectTypeIconLabel.setToolTipText(bundle.getString("ObjectTableTypeIconLabel_TT")); // NOI18N
        OptionalControls.getContentPane().add(ObjectTypeIconLabel);

        LeftControlsPanel.add(OptionalControls, java.awt.BorderLayout.EAST);

        ColorPanel.add(LeftControlsPanel, java.awt.BorderLayout.CENTER);

        add(ColorPanel, java.awt.BorderLayout.NORTH);

        SpacerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 0, 0, 0));
        SpacerPanel.setOpaque(false);
        SpacerPanel.setLayout(new java.awt.BorderLayout());

        TablePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        TablePanel.setOpaque(false);
        TablePanel.setLayout(new javax.swing.BoxLayout(TablePanel, javax.swing.BoxLayout.LINE_AXIS));

        TableScrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        TableScrollPane.setOpaque(false);
        TablePanel.add(TableScrollPane);

        SpacerPanel.add(TablePanel, java.awt.BorderLayout.CENTER);

        add(SpacerPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

private void SearchFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SearchFieldKeyPressed
    if (SearchField.getText().length() > 254) {
        JOptionPane.showMessageDialog(null, "Search string cannot be larger than 255 characters; truncating...", "Search String Too Long", JOptionPane.ERROR_MESSAGE);
        SearchField.setText(SearchField.getText().substring(0, 254));
    }
    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
        runSearch();
    }
}//GEN-LAST:event_SearchFieldKeyPressed
private void RevertToLastListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RevertToLastListActionPerformed
    pop();
}//GEN-LAST:event_RevertToLastListActionPerformed

    private void ColorsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ColorsButtonActionPerformed
        TableColorPreferences tcp = new TableColorPreferences(null, true, this);
        tcp.setLocationRelativeTo(this);
        tcp.setVisible(true);
    }//GEN-LAST:event_ColorsButtonActionPerformed

    private void OptionExpansionLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_OptionExpansionLabelMouseClicked
        if (OptionalControls.isCollapsed()) {
            OptionalControls.setCollapsed(false);
        } else {
            OptionalControls.setCollapsed(true);
        }
    }//GEN-LAST:event_OptionExpansionLabelMouseClicked

    private void RestrictionsApplyLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RestrictionsApplyLabelMouseClicked
        SearchPanel.clearSearchRestrictions();
    }//GEN-LAST:event_RestrictionsApplyLabelMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ColorPanel;
    private javax.swing.JButton ColorsButton;
    private javax.swing.JPanel LeftControlsPanel;
    private javax.swing.JLabel ObjectTypeIconLabel;
    private javax.swing.JLabel OptionExpansionLabel;
    private org.jdesktop.swingx.JXCollapsiblePane OptionalControls;
    private javax.swing.JLabel RestrictionsApplyLabel;
    private javax.swing.JButton RevertToLastList;
    private javax.swing.JPanel SearchEssentialsPanel;
    private javax.swing.JTextField SearchField;
    private javax.swing.JPanel SpacerPanel;
    private javax.swing.JMenu TableColumnMenu;
    private javax.swing.JPanel TablePanel;
    private javax.swing.JScrollPane TableScrollPane;
    private javax.swing.JMenu TableTypeMenu;
    private javax.swing.JPopupMenu TableTypePopup;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
