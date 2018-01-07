/*
 * TableManager.java
 *
 * Created on 18. Juli 2006, 13:25
 *
 * This class manages a list of all the JTables used in the application. For
 * each table a description of the columns which are used to assign colors to the
 * table rows is stored together with a description of the values and value types
 * that these column can have.
 *
 */
package com.mindliner.gui.tablemanager;

import com.mindliner.analysis.MlClassHandler.MindlinerObjectType;
import com.mindliner.clientobjects.MlClientClassHandler;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.entities.Colorizer.ColorDriverAttribute;
import com.mindliner.events.ObjectChangeManager;
import com.mindliner.events.SelectionManager;
import com.mindliner.gui.tablemodels.MlTableColumn;
import com.mindliner.gui.tablemodels.MlTableModel;
import com.mindliner.main.SearchPanel;
import com.mindliner.view.dispatch.MlViewDispatcherImpl;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JTable;

/**
 * This class manages the configured instances of MindlienrObjectTable.
 *
 * @author Marius Messerli
 */
public abstract class TableManager {

    private static final ArrayList<MlObjectTable> tables = new ArrayList<>();
    public static final String ANY_CONTENT_CLASS = "AnyClass";
    public static Color TableStandardColor = new Color(240, 240, 240);
    private static final String TablePreferencePrefix = "Table-";
    private static final String TableCountExtension = "count";
    private static int actionItemUpdateIntervallMinutes = 10;

    public static void updateTableColors() {
        for (MlObjectTable t : tables) {
            t.updateTableSupportColors();
        }
    }

    private static MindlinerObjectType getTypePreference(MindlinerObjectType defaultType, String identifyer) {
        Preferences userPrefs = Preferences.userNodeForPackage(MlObjectTable.class);
        String prefix = TablePreferencePrefix + identifyer;
        String typeString = userPrefs.get(prefix + MlObjectTable.TYPE_EXTENSION, defaultType.toString());
        MindlinerObjectType type;
        try {
            type = MindlinerObjectType.valueOf(typeString);
        } catch (IllegalArgumentException ex) {
            type = defaultType;
        }
        return type;
    }

    /**
     * Create a new table and registers it with the view dispather, the
     * selection manager, the table manager, and the object change manager.
     *
     * @param type The type of object to be displayed
     * @param identifyer A unique string to persist some values between sessions
     * @param colorDriver The driver for the table row colors
     * @return A new table
     */
    public static MlObjectTable createTypeTable(MindlinerObjectType type, String identifyer, ColorDriverAttribute colorDriver) {
        MindlinerObjectType typePrefs = getTypePreference(type, identifyer);
        MlObjectTable mot = new MlObjectTable(typePrefs, identifyer, colorDriver);
        MlViewDispatcherImpl.getInstance().registerViewer(mot);
        SelectionManager.registerObserver(mot);
        TableManager.registerTable(mot);
        ObjectChangeManager.registerObserver(mot);
        SearchPanel.registerTable(mot);
        return mot;
    }

    /**
     * Displays a single Mindliner object.
     *
     * Finds all tables which can display the specified object. The objects will
     * first be displayed in tables specific for the type. If no such table
     * exists and if a table of the type "Other Objects" exists then the object
     * will be display in the OtherObjectsTable.
     *
     * @param mbo The object to be displayed.
     * @param clearTable If true the current table content is clear, otherwise
     * the new object is added at the bottom of the table.
     */
    public static void displayObject(mlcObject mbo, boolean clearTable) {
        boolean specificTableExists = false;
        List<MlObjectTable> anyObjectTables = new ArrayList<>();

        for (MlObjectTable mot : TableManager.getTableList()) {
            if (mot.getType().equals(MindlinerObjectType.Any)) {
                anyObjectTables.add(mot);
            } else if (mot.getType().equals(MlClientClassHandler.getTypeByClass(mbo.getClass()))) {
                if (clearTable) {
                    mot.clear();
                }
                mot.addObject(mbo);
                mot.redisplay();
                specificTableExists = true;
            }
        }
        if (specificTableExists == false && !anyObjectTables.isEmpty()) {
            for (MlObjectTable t : anyObjectTables) {
                if (clearTable) {
                    t.clear();
                }
                t.addObject(mbo);
                t.redisplay();
            }
        }
    }

    /**
     * @todo implement loading of the tables
     */
    public static void loadPreferences() {
        Preferences userPrefs = Preferences.userNodeForPackage(TableManager.class);
        actionItemUpdateIntervallMinutes = userPrefs.getInt("actionitemupdateintervall", actionItemUpdateIntervallMinutes);
        updateTableColors();
    }

    /**
     * Saves color and sorting preferences for each table to persistet storage.
     */
    public static void storePreferences() {
        Preferences userPrefs = Preferences.userNodeForPackage(TableManager.class);
        userPrefs.putInt(TablePreferencePrefix + TableCountExtension, tables.size());
        userPrefs.putInt("actionitemupdateintervall", actionItemUpdateIntervallMinutes);
        for (MlObjectTable st : tables) {
            st.storePreferences(TablePreferencePrefix);
        }
    }

    /**
     * Returns the table with the specified name.
     */
    public static MlObjectTable getTable(String name) {
        for (MlObjectTable tui : tables) {
            if (name.compareTo(tui.getName()) == 0) {
                return tui;
            }
        }
        return null;
    }

    /**
     * Returns the actual JTable inside the mindliner table object.
     *
     * @param name The name of the mindliner table
     * @return The JTable reference of the mindliner table object.
     */
    public static JTable getJTable(String name) {
        return getTable(name).getJTable();
    }

    public static List<MlObjectTable> getTableList() {
        return tables;
    }

    /**
     * Returns a table definition object for the specified JTable.
     *
     * @param table The JTable for which the definitions are to be obtained.
     * @return The TableDefinition for the specified table or null if not in
     * list.
     */
    public static MlObjectTable getMindlinerTable(DecoratedTable table) {
        for (MlObjectTable td : tables) {
            if (td.getJTable() == table) {
                return td;
            }
        }
        return null;
    }

    public static String[] getHeadersForType(MindlinerObjectType type) {
        List<MlTableColumn> list = TableColumnManager.getAvailableColumns(type);
        String[] stringvector = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            stringvector[i] = list.get(i).getHeader();
        }
        return stringvector;
    }

    public static void registerTable(MlObjectTable mot) {
        tables.add(mot);
    }

    /**
     * If the application has one of several tables which accept any object as
     * content this function will return the first of these tables. If none of
     * the tables accept all objects the function returns null.
     *
     * @param protectIfPossible If possible find another table that is capable
     * of acceping any type of object. Specify null if you don't need to protect
     * any table in which case the first AllObjectTable is returned.
     * @return The first table accepting all objects (optinally observing the
     * projection request) or null if none exists.
     */
    public static MlObjectTable getFirstAllObjectsTable(MlObjectTable protectIfPossible) {
        MlObjectTable defaultResult = null;
        for (MlObjectTable st : tables) {
            if (st.getType().equals(MindlinerObjectType.Any)) {
                if (st.equals(protectIfPossible)) {
                    // keep it as default, perhaps there is another table without type constraint to come...
                    defaultResult = st;
                } else {
                    return st;
                }
            }
        }
        return defaultResult;
    }

    /**
     * This function returns a mindliner table for the specified class. If more
     * than one table is available for that class it makes sure it does not
     * return the specified table.
     *
     * @param c The source class displayed by the requested table.
     * @param table The table for which another one is to be found if possible.
     * @return
     */
    public static MlObjectTable getOtherTable(Class c, MlObjectTable table) {
        MlObjectTable one = null;
        MlObjectTable two = null;

        for (MlObjectTable st : tables) {
            if (st.getType().equals(MlClientClassHandler.getTypeByClass(c))) {
                if (one == null) {
                    one = st;
                } else if (two == null) {
                    two = st;
                }
            }
        }
        if (two == null) {
            return one;
        }
        if (one == table) {
            return two;
        }
        return one;
    }

    public static void clearTables() {
        for (MlObjectTable t : tables) {
            t.clear();
        }
    }

    public static void setTableEditing(boolean state) {
        for (MlObjectTable st : tables) {
            MlTableModel tm = (MlTableModel) st.getJTable().getModel();
            tm.setCellEditing(state);
        }
    }

    /**
     * Runs a search for the specified string in each of the tables.
     *
     * @param searchString The text to be found
     * @todo special treatement of ActionItemTable is not clean - think of
     * re-writing
     */
    public static void runSearch(String searchString) {
        for (MlObjectTable t : tables) {
            t.runSearch(searchString);
        }
    }

    public static String getTablePreferencePrefix() {
        return TablePreferencePrefix;
    }
}
