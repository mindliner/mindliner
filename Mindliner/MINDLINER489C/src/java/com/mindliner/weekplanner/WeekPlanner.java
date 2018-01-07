/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.mindliner.weekplanner;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.cal.WeekNumbering;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcTask;
import com.mindliner.clientobjects.mlcUser;
import com.mindliner.clientobjects.mlcWeekPlan;
import com.mindliner.commands.CommandRecorder;
import com.mindliner.commands.WeekPlanRemoveTaskCommand;
import com.mindliner.entities.Colorizer;
import com.mindliner.events.ObjectChangeObserver;
import com.mindliner.events.SelectionManager;
import com.mindliner.gui.ObjectEditor;
import com.mindliner.gui.color.BaseColorizer;
import com.mindliner.gui.color.ColorManager;
import com.mindliner.gui.color.FixedKeyColorizer;
import com.mindliner.styles.MlStyler;
import com.mindliner.view.dispatch.MlObjectViewer;
import com.mindliner.view.dispatch.MlViewDispatcherImpl;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumn;

/**
 *
 * @author Marius Messerli
 */
public class WeekPlanner extends javax.swing.JPanel implements WeekPlanChangeObserver, ObjectChangeObserver {

    private static final int DEFAULT_TEAM_TABLE_HEIGHT = 200;
    private int year = 0;
    private int week = 0;
    private WeekplannerRowColoringTable myWeekPlanTable = null;
    private WeekplannerRowColoringTable teamWeekPlan = null;
    private mlcWeekPlan currentPlan = null;
    private WeekPlanTableCellRenderer renderer;
    private final String PLAN_SPLIT_PANE_KEY = "plansplitpane";
    public static final String FONT_PREFERENCE_KEY = "weekplan";

    public WeekPlanner() {
        initComponents();
        configureComponents();
    }

    @Override
    public void weekOrPlanChanged(int newYear, int newWeek) {
        setWeekLabel(newYear, newWeek);
        year = newYear;
        week = newWeek;
        updateTables();
    }

    @Override
    public void objectSelectionChanged(mlcTask t) {
        // make the currently selected object also the current object in the rest of the application
        SelectionManager.setSelection(t);
    }

    @Override
    public void taskUpdated(mlcTask t) {
        repaintTables();
    }

    @Override
    public void objectChanged(mlcObject o) {
        repaintTables();
    }

    @Override
    public void objectDeleted(mlcObject o) {
    }

    @Override
    public void objectCreated(mlcObject o) {
    }

    @Override
    public void objectReplaced(int oldId, mlcObject o) {
    }

    private void configureComponents() {
        Preferences userPrefs = Preferences.userNodeForPackage(WeekPlanner.class);
        int defaultUserDefinedDividerPosition = userPrefs.getInt(PLAN_SPLIT_PANE_KEY, PlanSplitPane.getPreferredSize().height - DEFAULT_TEAM_TABLE_HEIGHT);
        BaseColorizer fkc = applyColoring();

        PlanSplitPane.setDividerLocation(defaultUserDefinedDividerPosition);
        PlanSplitPane.setResizeWeight(0.7);
        renderer = new WeekPlanTableCellRenderer(WeekTableModel.getToGoColumnIndex(), WeekTableModel.getTodayColumnIndex());

        myWeekPlanTable = createWeekplanTable(true);
        MlStyler.colorizeTableHeader(myWeekPlanTable,
                fkc.getColorForKey(FixedKeyColorizer.FixedKeys.TABLE_HEADER_TEXT),
                fkc.getColorForKey(FixedKeyColorizer.FixedKeys.TABLE_HEADER_BACKGROUND));
        MyTableScroller.setViewportView(myWeekPlanTable);

        MyTableScroller.getViewport().setBackground(fkc.getColorForObject(FixedKeyColorizer.FixedKeys.WEEKPLAN_BACKGROUND));

        teamWeekPlan = createWeekplanTable(false);
        MlStyler.colorizeTableHeader(teamWeekPlan,
                fkc.getColorForKey(FixedKeyColorizer.FixedKeys.TABLE_HEADER_TEXT),
                fkc.getColorForKey(FixedKeyColorizer.FixedKeys.TABLE_HEADER_BACKGROUND));

        TeamTableScroller.setViewportView(teamWeekPlan);
        TeamTableScroller.getViewport().setBackground(fkc.getColorForObject(FixedKeyColorizer.FixedKeys.WEEKPLAN_BACKGROUND));

        Date now = new Date();
        int activeWeek = WeekNumbering.getWeek(now);
        int activeYear = WeekNumbering.getYear(now);

        WeekplanHeaderPane.add(WorkTracker.getUniqueInstance(), BorderLayout.SOUTH);
        WeekPlanChangeManager.weekChanged(activeYear, activeWeek);

        SimpleDateFormat a = new SimpleDateFormat("'Week' w',' yyyy");
        WeekPicker.setFormats(a);
        WeekPicker.getEditor().setText("Week");
    }

    public BaseColorizer applyColoring() {
        BaseColorizer fkc = ColorManager.getColorizerForType(Colorizer.ColorDriverAttribute.FixedKey);
        Color bg = fkc.getColorForKey(FixedKeyColorizer.FixedKeys.WEEKPLAN_CALENDER_PANEL);
        Color fg = fkc.getColorForKey(FixedKeyColorizer.FixedKeys.MAIN_DEFAULT_TEXT);
        Color border = fkc.getColorForKey(FixedKeyColorizer.FixedKeys.TABLE_GRID);
        setBackground(bg);
        MyTablePanel.setBackground(bg);
        PlanSplitPane.setBackground(bg);
        MlStyler.colorSplitPane(PlanSplitPane, bg, border);
        WeekplanHeaderPane.setBackground(bg);
        TeamTableControlPanel.setBackground(bg);
        CalendarPanel.setBackground(bg);
        ShowAverages.setForeground(fg);
        WeekLabel.setForeground(fg);
        MyPlanLabel.setForeground(fg);
        TeamComboLabel.setForeground(fg);

        // the color legend labels
        ColorLegendPanel.setBackground(bg);
        CurrentDayColorLabel.setForeground(fkc.getColorForKey(FixedKeyColorizer.FixedKeys.WEEKPLAN_CURRENT_DAY));
        CurrentTaskColorLabel.setBackground(fkc.getColorForKey(FixedKeyColorizer.FixedKeys.WEEKPLAN_CURRENT_TASK_BACKGROUND));
        TeamTaskColorLabel.setBackground(fkc.getColorForKey(FixedKeyColorizer.FixedKeys.WEEKPLAN_CURRENT_TEAM_TASK_BACKGROUND));
        ToGoColorLabel.setForeground(fkc.getColorForKey(FixedKeyColorizer.FixedKeys.WEEKPLAN_TOGO));
        ColorLegendLabel.setForeground(fg);

        MlStyler.colorizeButton(PreviousWeek, fkc);
        MlStyler.colorizeButton(ReloadWeekplan, fkc);
        MlStyler.colorizeButton(PlanCurrentWeek, fkc);
        MlStyler.colorizeButton(WorkTrackerMapItems, fkc);
        MlStyler.colorizeButton(NextWeek, fkc);
        WeekPicker.getEditor().setBackground(bg);
        return fkc;
    }

    public void storePreferences() {
        Preferences userPrefs = Preferences.userNodeForPackage(WeekPlanner.class);
        userPrefs.putInt(PLAN_SPLIT_PANE_KEY, PlanSplitPane.getDividerLocation());
    }

    /**
     * This function takes the specified date and "floors" it to the beginning
     * of the week.
     *
     * @param d
     */
    private void setWeekLabel(int year, int week) {

        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.WEEK_OF_YEAR, week);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());

        Date weekStart = cal.getTime();

        WeekPicker.setDate(weekStart);

        cal.add(Calendar.DAY_OF_MONTH, 6);
        cal.set(Calendar.HOUR, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date weekEnd = cal.getTime();

        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        WeekLabel.setText(df.format(weekStart) + " through " + df.format(weekEnd));
    }

    private WeekplannerRowColoringTable createWeekplanTable(boolean myPlan) {
        BaseColorizer fkc = ColorManager.getColorizerForType(Colorizer.ColorDriverAttribute.FixedKey);

        final WeekplannerRowColoringTable table = new WeekplannerRowColoringTable();
        table.initialize();
        table.setBorder(new EmptyBorder(0, 0, 0, 0));
        table.setGridColor(fkc.getColorForKey(FixedKeyColorizer.FixedKeys.TABLE_GRID));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        if (myPlan) {
            table.setTransferHandler(new WorkPlanTransferHandler());
            table.setDragEnabled(true);

            table.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    int row = table.getSelectedRow();
                    WeekTableModel wtm = (WeekTableModel) myWeekPlanTable.getModel();
                    // the last row is a total's row and must not trigger a selection update
                    if (row < table.getRowCount()) {
                        mlcTask rowObject = wtm.getRowObject(row);
                        WeekPlanChangeManager.selectionChanged(rowObject);
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        showPopup(e);
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        showPopup(e);
                    }
                }

            });

            table.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyReleased(java.awt.event.KeyEvent evt) {
                    WeekTableModel wtm = (WeekTableModel) myWeekPlanTable.getModel();
                    switch (evt.getKeyCode()) {
                        case KeyEvent.VK_DOWN:
                        case KeyEvent.VK_UP:
                            int row = myWeekPlanTable.getSelectedRow();
                            // the last row is a total's row and must not trigger a selection update
                            if (row < myWeekPlanTable.getRowCount() - 1) {
                                mlcTask rowObject = wtm.getRowObject(row);
                                WeekPlanChangeManager.selectionChanged(rowObject);
                            }
                            break;

                        case KeyEvent.VK_DELETE:
                            int selectedRow = myWeekPlanTable.getSelectedRow();
                            if (selectedRow != -1 && selectedRow < myWeekPlanTable.getRowCount() - 1) {
                                mlcTask task = (mlcTask) wtm.getRowObject(selectedRow);
                                if (task != null) {
                                    CommandRecorder cr = CommandRecorder.getInstance();
                                    mlcWeekPlan weekPlan = CacheEngineStatic.getWeekPlan(year, week);
                                    if (weekPlan != null && CacheEngineStatic.hasWorkInWeek(CacheEngineStatic.getCurrentUser(), task, weekPlan)) {
                                        JOptionPane.showMessageDialog(null,
                                                "Object has been worked on this week; cannot delete it.",
                                                "Work Object Deletion",
                                                JOptionPane.ERROR_MESSAGE);
                                    } else if (weekPlan != null) {
                                        cr.scheduleCommand(new WeekPlanRemoveTaskCommand(task, weekPlan));
                                        WeekPlanChangeManager.weekChanged(year, week);
                                    }
                                }
                            }
                            break;
                        case KeyEvent.VK_V:
                            MlViewDispatcherImpl.getInstance().display(SelectionManager.getSelection(), MlObjectViewer.ViewType.Map);
                            break;

                        case KeyEvent.VK_E:
                            selectedRow = myWeekPlanTable.getSelectedRow();
                            if (selectedRow != -1 && selectedRow < myWeekPlanTable.getRowCount() - 1) {
                                mlcTask task = (mlcTask) wtm.getRowObject(selectedRow);
                                if (task != null) {
                                    List<mlcObject> objects = new ArrayList<>();
                                    objects.add(task);
                                    ObjectEditor editor = new ObjectEditor(objects);
                                    editor.setRelative(SelectionManager.getLastSelection());
                                    editor.setVisible(true);
                                }
                            }
                            break;
                    }
                }
            });
        }
        table.setModel(new WeekTableModel());
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
        // we cannot interact with the team plans
        table.setEnabled(myPlan);
        return table;
    }

    private void showPopup(MouseEvent e) {
        WeekplanPopupBuilder wpb = new WeekplanPopupBuilder();
        JPopupMenu pup = wpb.createWeekplanPopupMenu(currentPlan);
        pup.show(e.getComponent(), e.getX(), e.getY());
    }

    private void showForeignPlan(mlcUser u) {
        List<mlcWeekPlan> foreignWeekPlans = CacheEngineStatic.getForeignWeekPlans(year, week);
        for (mlcWeekPlan w : foreignWeekPlans) {
            if (w.getUser().equals(u)) {
                WeekTableModel fwtm = (WeekTableModel) teamWeekPlan.getModel();
                fwtm.setWeekPlan(w);
                setColumnWidths(teamWeekPlan);
            }
        }
    }

    private void updateTables() {
        if (year != 0) {
            // updating the current day column in case the day has changed
            renderer.setTodayPlanColumnIndex(WeekTableModel.getTodayColumnIndex());

            int oldPlanId = -1;
            if (currentPlan != null) {
                oldPlanId = currentPlan.getId();
            }
            currentPlan = CacheEngineStatic.getWeekPlan(year, week);
            System.out.println("updateTables(): updating for year = " + year + ", week = " + week);
            if (currentPlan != null) {
//                CacheEngineStatic.ensureMyTasksDueInWeekAreOnPlan(currentPlan.getYear(), currentPlan.getWeek());
                int oldRowIndex = myWeekPlanTable.getSelectedRow();
                WeekTableModel wtm = (WeekTableModel) myWeekPlanTable.getModel();
                wtm.setWeekPlan(currentPlan);
                setColumnWidths(myWeekPlanTable);
                if (currentPlan.getId() == oldPlanId && oldRowIndex != -1) {
                    myWeekPlanTable.setRowSelectionInterval(oldRowIndex, oldRowIndex);
                }
                JTableRowHeightAdjuster.updateRowHeights(myWeekPlanTable, -1);
            }
            List<mlcWeekPlan> foreignWeekPlans = CacheEngineStatic.getForeignWeekPlans(year, week);
            DefaultComboBoxModel dcm = new DefaultComboBoxModel();
            foreignWeekPlans.stream().forEach((w) -> {
                dcm.addElement(w.getUser());
            });
            WeekplanForeignUserCombo.setModel(dcm);
            java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/mindliner/resources/WeekPlan");
            if (dcm.getSize() > 0) {
                TeamComboLabel.setText(bundle.getString("TeamPlanSelectorLabelPlanFor"));
                WeekplanForeignUserCombo.setVisible(true);
                WeekplanForeignUserCombo.setSelectedIndex(0);
                mlcUser foreignPlanUser = (mlcUser) WeekplanForeignUserCombo.getSelectedItem();
                showForeignPlan(foreignPlanUser);
            } else {
                WeekTableModel fwtm = (WeekTableModel) teamWeekPlan.getModel();
                fwtm.clear();
                TeamComboLabel.setText(bundle.getString("TeamPlanSelectorLabelNoPlansAvailable"));
                WeekplanForeignUserCombo.setVisible(false);
            }
            JTableRowHeightAdjuster.updateRowHeights(myWeekPlanTable, -1);
        }
    }

    private void setColumnWidths(WeekplannerRowColoringTable t) {
        TableColumn tc = t.getColumnModel().getColumn(0);
        tc.setPreferredWidth(260);
        for (int i = 1; i < t.getModel().getColumnCount(); i++) {
            tc = t.getColumnModel().getColumn(i);
            tc.setPreferredWidth(40);
        }
    }

    private void incrementWeek() {
        if (week < 53) {
            week++;
        } else {
            week = 1;
            year++;
        }
        // @todo this call and the entire WeekPlanChangeManager may not be necessary anymore after having merged tracker and plan
        WeekPlanChangeManager.weekChanged(year, week);
    }

    private void decrementWeek() {
        if (week > 1) {
            week--;
        } else {
            week = 53;
            year--;
        }
        repaintTables();
        WeekPlanChangeManager.weekChanged(year, week);
    }

    private void repaintTables() {
        myWeekPlanTable.repaint();
        teamWeekPlan.repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        WeekplanControls = new javax.swing.JPanel();
        CalendarPanel = new javax.swing.JPanel();
        ShowAverages = new javax.swing.JCheckBox();
        WeekSelectorPanel = new javax.swing.JPanel();
        PreviousWeek = new javax.swing.JButton();
        ReloadWeekplan = new javax.swing.JButton();
        PlanCurrentWeek = new javax.swing.JButton();
        WorkTrackerMapItems = new javax.swing.JButton();
        NextWeek = new javax.swing.JButton();
        WeekPicker = new org.jdesktop.swingx.JXDatePicker();
        WeekLabel = new javax.swing.JLabel();
        PlanTablesPanel = new javax.swing.JPanel();
        PlanSplitPane = new javax.swing.JSplitPane();
        MyTablePanel = new javax.swing.JPanel();
        WeekplanHeaderPane = new javax.swing.JPanel();
        MyPlanLabel = new javax.swing.JLabel();
        MyTableScroller = new javax.swing.JScrollPane();
        TeamTablesPanel = new javax.swing.JPanel();
        TeamTableControlPanel = new javax.swing.JPanel();
        TeamComboLabel = new javax.swing.JLabel();
        WeekplanForeignUserCombo = new javax.swing.JComboBox();
        TeamTableScroller = new javax.swing.JScrollPane();
        ColorLegendPanel = new javax.swing.JPanel();
        ColorLegendLabel = new javax.swing.JLabel();
        CurrentTaskColorLabel = new javax.swing.JLabel();
        TeamTaskColorLabel = new javax.swing.JLabel();
        CurrentDayColorLabel = new javax.swing.JLabel();
        ToGoColorLabel = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setLayout(new java.awt.BorderLayout());

        WeekplanControls.setLayout(new java.awt.BorderLayout());

        CalendarPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("InternalFrame.activeTitleBackground"));
        CalendarPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 0));

        ShowAverages.setFont(ShowAverages.getFont());
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/mindliner/resources/WeekPlan"); // NOI18N
        ShowAverages.setText(bundle.getString("WeekplanShowAverages")); // NOI18N
        ShowAverages.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ShowAveragesActionPerformed(evt);
            }
        });
        CalendarPanel.add(ShowAverages);

        WeekSelectorPanel.setOpaque(false);
        WeekSelectorPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 1));

        PreviousWeek.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/3232/arrow2_left_blue.png"))); // NOI18N
        PreviousWeek.setToolTipText(bundle.getString("WorkTracker_ShowPreviousWeek")); // NOI18N
        PreviousWeek.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PreviousWeekActionPerformed(evt);
            }
        });
        WeekSelectorPanel.add(PreviousWeek);

        ReloadWeekplan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/3232/cloud_computing_refresh.png"))); // NOI18N
        ReloadWeekplan.setToolTipText(bundle.getString("WorkTracker_RefreshWeekplan_TT")); // NOI18N
        ReloadWeekplan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ReloadWeekplanActionPerformed(evt);
            }
        });
        WeekSelectorPanel.add(ReloadWeekplan);

        PlanCurrentWeek.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/3232/calendar_1.png"))); // NOI18N
        PlanCurrentWeek.setToolTipText(bundle.getString("WorkTracker_WeekplanToday")); // NOI18N
        PlanCurrentWeek.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PlanCurrentWeekActionPerformed(evt);
            }
        });
        WeekSelectorPanel.add(PlanCurrentWeek);

        WorkTrackerMapItems.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/3232/star2_blue.png"))); // NOI18N
        WorkTrackerMapItems.setToolTipText(bundle.getString("WorkTracker_MapWeek")); // NOI18N
        WorkTrackerMapItems.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                WorkTrackerMapItemsActionPerformed(evt);
            }
        });
        WeekSelectorPanel.add(WorkTrackerMapItems);

        NextWeek.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mindliner/img/icons/3232/arrow2_right_blue.png"))); // NOI18N
        NextWeek.setToolTipText(bundle.getString("WorkTracker_ShowNextWeek")); // NOI18N
        NextWeek.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NextWeekActionPerformed(evt);
            }
        });
        WeekSelectorPanel.add(NextWeek);

        CalendarPanel.add(WeekSelectorPanel);

        WeekPicker.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                WeekPickerActionPerformed(evt);
            }
        });
        CalendarPanel.add(WeekPicker);

        WeekLabel.setFont(WeekLabel.getFont());
        WeekLabel.setText("13 (3/23 - 3/29)");
        CalendarPanel.add(WeekLabel);

        WeekplanControls.add(CalendarPanel, java.awt.BorderLayout.SOUTH);

        add(WeekplanControls, java.awt.BorderLayout.NORTH);

        PlanTablesPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        PlanTablesPanel.setLayout(new java.awt.BorderLayout());

        PlanSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        PlanSplitPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        PlanSplitPane.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        MyTablePanel.setBackground(new java.awt.Color(207, 173, 139));
        MyTablePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        MyTablePanel.setMinimumSize(new java.awt.Dimension(329, 200));
        MyTablePanel.setLayout(new java.awt.BorderLayout());

        WeekplanHeaderPane.setLayout(new java.awt.BorderLayout());

        MyPlanLabel.setText(bundle.getString("WeekplanMyPlanTitle")); // NOI18N
        WeekplanHeaderPane.add(MyPlanLabel, java.awt.BorderLayout.NORTH);

        MyTablePanel.add(WeekplanHeaderPane, java.awt.BorderLayout.NORTH);

        MyTableScroller.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        MyTableScroller.setOpaque(false);
        MyTableScroller.setPreferredSize(new java.awt.Dimension(100, 200));
        MyTablePanel.add(MyTableScroller, java.awt.BorderLayout.CENTER);

        PlanSplitPane.setTopComponent(MyTablePanel);

        TeamTablesPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        TeamTablesPanel.setLayout(new java.awt.BorderLayout());

        TeamTableControlPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        TeamComboLabel.setText(bundle.getString("WeekpanelOtherUserComboLabel")); // NOI18N
        TeamTableControlPanel.add(TeamComboLabel);

        WeekplanForeignUserCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        WeekplanForeignUserCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                WeekplanForeignUserComboActionPerformed(evt);
            }
        });
        TeamTableControlPanel.add(WeekplanForeignUserCombo);

        TeamTablesPanel.add(TeamTableControlPanel, java.awt.BorderLayout.NORTH);

        TeamTableScroller.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        TeamTableScroller.setOpaque(false);
        TeamTablesPanel.add(TeamTableScroller, java.awt.BorderLayout.CENTER);

        ColorLegendPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 20, 5));

        ColorLegendLabel.setText(bundle.getString("WeekplanerColorLegend")); // NOI18N
        ColorLegendPanel.add(ColorLegendLabel);

        CurrentTaskColorLabel.setText(bundle.getString("WeekplanerCurrentTask")); // NOI18N
        CurrentTaskColorLabel.setOpaque(true);
        ColorLegendPanel.add(CurrentTaskColorLabel);

        TeamTaskColorLabel.setText(bundle.getString("WeekplannerTeamTaskLabel")); // NOI18N
        TeamTaskColorLabel.setOpaque(true);
        TeamTaskColorLabel.setToolTipText(bundle.getString("WeekplanerTeamTaskLabel_TT")); // NOI18N
        ColorLegendPanel.add(TeamTaskColorLabel);

        CurrentDayColorLabel.setText(bundle.getString("WeekplanerCurrentDayColorLabel")); // NOI18N
        ColorLegendPanel.add(CurrentDayColorLabel);

        ToGoColorLabel.setText(bundle.getString("WeekplanerToGoLabel")); // NOI18N
        ToGoColorLabel.setToolTipText(bundle.getString("WeekplanerToGo_TT")); // NOI18N
        ColorLegendPanel.add(ToGoColorLabel);

        TeamTablesPanel.add(ColorLegendPanel, java.awt.BorderLayout.SOUTH);

        PlanSplitPane.setBottomComponent(TeamTablesPanel);

        PlanTablesPanel.add(PlanSplitPane, java.awt.BorderLayout.CENTER);

        add(PlanTablesPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void ShowAveragesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ShowAveragesActionPerformed
        WeekTableModel wtm = (WeekTableModel) myWeekPlanTable.getModel();
        if (wtm != null) {
            wtm.setShowAverages(ShowAverages.isSelected());
            updateTables();
        }
    }//GEN-LAST:event_ShowAveragesActionPerformed

    private void WeekplanForeignUserComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_WeekplanForeignUserComboActionPerformed
        mlcUser u = (mlcUser) WeekplanForeignUserCombo.getSelectedItem();
        showForeignPlan(u);
    }//GEN-LAST:event_WeekplanForeignUserComboActionPerformed

    private void PreviousWeekActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PreviousWeekActionPerformed
        decrementWeek();
    }//GEN-LAST:event_PreviousWeekActionPerformed

    private void ReloadWeekplanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ReloadWeekplanActionPerformed
        WeekPlanChangeManager.weekChanged(year, week);
    }//GEN-LAST:event_ReloadWeekplanActionPerformed

    private void PlanCurrentWeekActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PlanCurrentWeekActionPerformed
        Date now = new Date();
        week = WeekNumbering.getWeek(now);
        year = WeekNumbering.getYear(now);
        WeekPlanChangeManager.weekChanged(year, week);
    }//GEN-LAST:event_PlanCurrentWeekActionPerformed

    private void NextWeekActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NextWeekActionPerformed
        incrementWeek();
    }//GEN-LAST:event_NextWeekActionPerformed

    private void WorkTrackerMapItemsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_WorkTrackerMapItemsActionPerformed
        if (currentPlan != null) {
            MlViewDispatcherImpl.getInstance().display((List<mlcObject>) CacheEngineStatic.getObjects(currentPlan.getTasksIds()), MlObjectViewer.ViewType.Map);
        }
    }//GEN-LAST:event_WorkTrackerMapItemsActionPerformed

    private void WeekPickerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_WeekPickerActionPerformed
        mlcWeekPlan wp = CacheEngineStatic.getWeekPlan(WeekPicker.getDate());
        week = wp.getWeek();
        year = wp.getYear();
        WeekPlanChangeManager.weekChanged(year, week);
    }//GEN-LAST:event_WeekPickerActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel CalendarPanel;
    private javax.swing.JLabel ColorLegendLabel;
    private javax.swing.JPanel ColorLegendPanel;
    private javax.swing.JLabel CurrentDayColorLabel;
    private javax.swing.JLabel CurrentTaskColorLabel;
    private javax.swing.JLabel MyPlanLabel;
    private javax.swing.JPanel MyTablePanel;
    private javax.swing.JScrollPane MyTableScroller;
    private javax.swing.JButton NextWeek;
    private javax.swing.JButton PlanCurrentWeek;
    private javax.swing.JSplitPane PlanSplitPane;
    private javax.swing.JPanel PlanTablesPanel;
    private javax.swing.JButton PreviousWeek;
    private javax.swing.JButton ReloadWeekplan;
    private javax.swing.JCheckBox ShowAverages;
    private javax.swing.JLabel TeamComboLabel;
    private javax.swing.JPanel TeamTableControlPanel;
    private javax.swing.JScrollPane TeamTableScroller;
    private javax.swing.JPanel TeamTablesPanel;
    private javax.swing.JLabel TeamTaskColorLabel;
    private javax.swing.JLabel ToGoColorLabel;
    private javax.swing.JLabel WeekLabel;
    private org.jdesktop.swingx.JXDatePicker WeekPicker;
    private javax.swing.JPanel WeekSelectorPanel;
    private javax.swing.JPanel WeekplanControls;
    private javax.swing.JComboBox WeekplanForeignUserCombo;
    private javax.swing.JPanel WeekplanHeaderPane;
    private javax.swing.JButton WorkTrackerMapItems;
    // End of variables declaration//GEN-END:variables
}
