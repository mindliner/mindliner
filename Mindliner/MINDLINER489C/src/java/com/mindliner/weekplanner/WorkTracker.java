 /*
 * WorkTracker.java
 * 
 * This class implements the user interface to measure the length of time
 * spent working on an item.
 * 
 * @author Marius Messerli
 *
 * Created on Mar 18, 2009, 12:21:07 PM
 */
package com.mindliner.weekplanner;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.cal.LapseTimeFormatter;
import com.mindliner.cal.WeekNumbering;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcTask;
import com.mindliner.clientobjects.mlcWeekPlan;
import com.mindliner.clientobjects.mlcWorkUnit;
import com.mindliner.commands.CommandRecorder;
import com.mindliner.commands.WeekplanAddTaskCommand;
import com.mindliner.entities.Colorizer;
import com.mindliner.events.ObjectChangeManager;
import com.mindliner.events.ObjectChangeObserver;
import com.mindliner.gui.MlDialogUtils;
import com.mindliner.gui.color.BaseColorizer;
import com.mindliner.gui.color.ColorManager;
import com.mindliner.gui.color.FixedKeyColorizer;
import com.mindliner.main.MindlinerMain;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.DefaultListModel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

/**
 * This class take a task and lets the user capture the work spent on it.
 *
 * @author Marius Messerli
 */
public class WorkTracker extends javax.swing.JPanel implements WeekPlanChangeObserver, ObjectChangeObserver {

    private mlcTask currentTimerTask = null;
    private mlcTask pastWorkUnitTask = null;
    private Date startTime = null;
    int activeWeek = -1;
    int activeYear = -1;
    private Timer lapseTimer = null;
    private static WorkTracker INSTANCE = null;
    ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/mindliner/resources/WeekPlan");

    public static WorkTracker getUniqueInstance() {
        synchronized (WorkTracker.class) {
            if (INSTANCE == null) {
                INSTANCE = new WorkTracker();
                ObjectChangeManager.registerObserver(INSTANCE);
                WeekPlanChangeManager.addObserver(INSTANCE);
            }
        }
        return INSTANCE;
    }

    public static void endTracking() {
        if (INSTANCE != null) {
            INSTANCE.endWork();
        }
    }

    /**
     * Creates new form WorkTracker
     */
    private WorkTracker() {
        initComponents();
        configureComponents();
    }

    private void configureComponents() {
        WorkUnitList.setModel(new DefaultListModel());
        Date now = new Date();
        activeWeek = WeekNumbering.getWeek(now);
        activeYear = WeekNumbering.getYear(now);
        WeekPlanChangeManager.weekChanged(activeYear, activeWeek);
        MlDialogUtils.addEscapeListener(WorkUnitEditor);
        MlDialogUtils.addEscapeListener(AddWorkUnitDialog);
    }

    /**
     * Adds the specified object to the current weekplan.
     *
     * Note: I don't need to update the CacheEngine specifically as we are
     * working in one and the same JVM - hence the object is automatically
     * removed from the cache.
     *
     * @param t The task to be added to the weekplan
     */
    public void addToWeekplanL(mlcTask t) {
        mlcWeekPlan weekPlan = CacheEngineStatic.getWeekPlan(activeYear, activeWeek);
        if (weekPlan != null) {
            if (!weekPlan.getTasksIds().contains(t.getId())) {
                CommandRecorder cr = CommandRecorder.getInstance();
                cr.scheduleCommand(new WeekplanAddTaskCommand(t, weekPlan));
                WeekPlanChangeManager.weekChanged(activeYear, activeWeek);
            }
        }
    }

    private void resetTimer() {
        if (lapseTimer != null) {
            lapseTimer.cancel();
        }
        lapseTimer = null;
        LapseLabel.setText(bundle.getString("TimerNotRunning"));
        updateTimerGUI(false);
    }

    private void endWork() {
        if (currentTimerTask != null && startTime != null && lapseTimer != null) {
            resetTimer();
            Calendar cal = Calendar.getInstance();
            CacheEngineStatic.createWorkUnit(currentTimerTask, startTime, new Date(), cal.getTimeZone().getID(), false);
        }
        // the following access the server even in asynch mode - needs to change
        CacheEngineStatic.setCurrentWorkTask(null);
    }

    private void updateTimerGUI(boolean timeron) {
        LapseLabel.setOpaque(timeron);
        if (timeron) {
            BaseColorizer colorizer = ColorManager.getColorizerForType(Colorizer.ColorDriverAttribute.FixedKey);
            LapseLabel.setBackground(colorizer.getColorForObject(FixedKeyColorizer.FixedKeys.WEEKPLAN_CURRENT_TASK_BACKGROUND));
            LapseLabel.setForeground(colorizer.getColorForObject(FixedKeyColorizer.FixedKeys.MAIN_DEFAULT_TEXT));
        } else {
            LapseLabel.setBackground(Color.LIGHT_GRAY);
        }
    }

    /**
     * Starts a new timer now.
     */
    private void startWork() {
        startWork(new Date());
    }

    /**
     * Starts the timer at the specified time.
     *
     * @param start The time at which the timer starts.
     */
    private void startWork(Date start) {
        startTime = start;
        lapseTimer = new Timer("Mindliner Work Timer");
        TimerTask tt = new LapseTimerTask();
        lapseTimer.schedule(tt, new Date(), 1000);
        CacheEngineStatic.setCurrentWorkTask(currentTimerTask);
        updateTimerGUI(true);
    }

    /**
     * Load all workunits for the specified task and the specified week into the
     * list
     *
     * @param task The task for which workunits are to be loaded
     * @param weekPlan The week for which work units are to be loaded, specify
     * null if all units are to be loaded
     */
    private void updateWorkBlockList(mlcTask task, mlcWeekPlan weekPlan) {
        DefaultListModel dlm = (DefaultListModel) WorkUnitList.getModel();
        dlm.clear();
        if (weekPlan != null) {
            for (mlcWorkUnit wu : weekPlan.getWorkUnitsForWeek(CacheEngineStatic.getCurrentUser(), task)) {
                dlm.addElement(wu);
            }
        } else {
            task.getWorkUnits().stream()
                    .filter((wu) -> (wu.isPlan() == false)).forEach((wu) -> {
                dlm.addElement(wu);
            });
        }
        WorkUnitsTaskHeadline.setText(task.getHeadline());
    }

    @Override
    public void weekOrPlanChanged(int newYear, int newWeek) {
        activeYear = newYear;
        activeWeek = newWeek;
    }

    @Override
    public void objectSelectionChanged(mlcTask t) {
        assert (t.equals(CacheEngineStatic.getObject(t.getId()))) : "Task is not up to date";
        if (lapseTimer == null) {
            currentTimerTask = t;
        }
    }

    @Override
    public void taskUpdated(mlcTask task) {
    }

    @Override
    public void objectChanged(mlcObject o) {
        if (!(o instanceof mlcTask)) {
            return;
        }
        mlcTask t = (mlcTask) o;
        if (currentTimerTask != null && t.getId() == currentTimerTask.getId()) {
            currentTimerTask = t;
        }
    }

    @Override
    public void objectDeleted(mlcObject o) {
        if (currentTimerTask != null && o.getId() == currentTimerTask.getId()) {
            resetTimer();
            DefaultListModel dlm = (DefaultListModel) WorkUnitList.getModel();
            dlm.clear();
        }
    }

    @Override
    public void objectCreated(mlcObject o) {
    }

    @Override
    public void objectReplaced(int oldId, mlcObject o) {
    }

    class LapseTimerTask extends TimerTask {

        @Override
        public void run() {
            long duration = (new Date()).getTime() - startTime.getTime();
            StringBuilder sb = new StringBuilder();
            sb.append(bundle.getString("TimerRunningSentenceStart"))
                    .append(LapseTimeFormatter.formatMinutesAndSeconds(duration))
                    .append(bundle.getString("TimerRunningSentenceDuration"));
            if (currentTimerTask != null) {
                sb.append(currentTimerTask.getHeadline()).append("'").append(". ").append(bundle.getString("TimerClickToStop"));
            }
            LapseLabel.setText(sb.toString());
        }
    }

    public void stopPerformed() {
        endWork();
    }

    private void startPerformed(mlcTask newTask) {
        if (currentTimerTask != newTask) {
            currentTimerTask = newTask;
        }
        startWork();
    }

    public void startTracking(mlcTask task) {
        stopPerformed();
        // check if the task is on the current week plan
        mlcWeekPlan weekPlan = CacheEngineStatic.getWeekPlan(activeYear, activeWeek);
        if (!weekPlan.getTasksIds().contains(task.getId())) {
            addToWeekplanL(task);
        }
        startPerformed(task);
    }

    /**
     * Returns the item the timer is running on.
     *
     * @return The task the timer is running on or null if the timer isnt
     * running.
     */
    public mlcTask getCurrentWorkItem() {
        if (lapseTimer == null) {
            return null;
        } else {
            return currentTimerTask;
        }
    }

    private mlcWeekPlan getCurrentWeekPlan() {
        return CacheEngineStatic.getWeekPlan(activeYear, activeWeek);
    }

    private void deleteSelectedWorkUnit() {
        mlcWorkUnit w = (mlcWorkUnit) WorkUnitList.getSelectedValue();
        if (w != null) {
            CacheEngineStatic.removeWorkUnit(w);
            DefaultListModel dlm = (DefaultListModel) WorkUnitList.getModel();
            dlm.removeElement(w);
        }
    }

    public void addPastWork(mlcTask task) {
        if (task == null) {
            System.err.println("Error: Specified task is null - cannot add work");
            return;
        }
        pastWorkUnitTask = task;
        Date previousEnd = CacheEngineStatic.getLastWorkUnitEnd();
        if (previousEnd != null) {
            Calendar cal = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal.setTime(previousEnd);
            cal2.setTime(new Date());
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int day2 = cal2.get(Calendar.DAY_OF_MONTH);
            if (day == day2) {
                cal.add(Calendar.MINUTE, 1);
                previousEnd = cal.getTime();
            } else {
                previousEnd = new Date();
            }
        } else {
            previousEnd = new Date();
        }
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        AWUStartTime.setText(df.format(previousEnd));
        AWUEndTime.setText(df.format(new Date()));
        AddWorkObjectLabel.setText(bundle.getString("AWU_TaskLabel") + ": " + pastWorkUnitTask.getHeadline());
        AddWorkUnitDialog.setLocationRelativeTo(MindlinerMain.getInstance());
        AddWorkUnitDialog.pack();
        AddWorkUnitDialog.setVisible(true);
    }

    public void startTimerBackFilling() {
        stopPerformed();
        Date previousEnd = CacheEngineStatic.getLastWorkUnitEnd();
        Calendar instance = Calendar.getInstance();
        instance.setTime(previousEnd);
        instance.add(Calendar.MINUTE, 1);
        startWork(instance.getTime());
    }

    public void showWorkUnits(mlcTask task, boolean filterForCurrentWeek) {
        mlcWeekPlan wp = filterForCurrentWeek ? getCurrentWeekPlan() : null;
        updateWorkBlockList(task, wp);
        WorkUnitEditor.setLocationRelativeTo(MindlinerMain.getInstance());
        WorkUnitEditor.pack();
        WorkUnitEditor.setVisible(true);
    }

    public mlcTask getCurrentTask() {
        if (lapseTimer == null) {
            return null;
        }
        return currentTimerTask;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        WorkStateGroup = new javax.swing.ButtonGroup();
        AddWorkUnitDialog = new javax.swing.JDialog();
        AddWorkObjectLabel = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        AWUStartTime = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        AWUEndTime = new javax.swing.JTextField();
        AWUAddButton = new javax.swing.JButton();
        AWUCancelButton = new javax.swing.JButton();
        WorkUnitEditor = new javax.swing.JDialog();
        WorkUnitsScroller = new javax.swing.JScrollPane();
        WorkUnitList = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        WorkUnitsTaskHeadline = new javax.swing.JLabel();
        LapseLabel = new javax.swing.JLabel();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/mindliner/resources/WeekPlan"); // NOI18N
        AddWorkUnitDialog.setTitle(bundle.getString("AWU_Title")); // NOI18N

        AddWorkObjectLabel.setFont(AddWorkObjectLabel.getFont());
        AddWorkObjectLabel.setText("title");

        jLabel10.setFont(jLabel10.getFont());
        jLabel10.setText(bundle.getString("AWU_StartTimeLabel")); // NOI18N

        AWUStartTime.setFont(AWUStartTime.getFont());
        AWUStartTime.setText("jTextField1");

        jLabel11.setFont(jLabel11.getFont());
        jLabel11.setText(bundle.getString("AWU_EndTimeLabel")); // NOI18N

        AWUEndTime.setFont(AWUEndTime.getFont());
        AWUEndTime.setText("jTextField2");

        AWUAddButton.setFont(AWUAddButton.getFont());
        AWUAddButton.setText(bundle.getString("AWU_OKButton")); // NOI18N
        AWUAddButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AWUAddButtonActionPerformed(evt);
            }
        });

        AWUCancelButton.setFont(AWUCancelButton.getFont());
        AWUCancelButton.setText(bundle.getString("AWU_CancelButton")); // NOI18N
        AWUCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AWUCancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout AddWorkUnitDialogLayout = new javax.swing.GroupLayout(AddWorkUnitDialog.getContentPane());
        AddWorkUnitDialog.getContentPane().setLayout(AddWorkUnitDialogLayout);
        AddWorkUnitDialogLayout.setHorizontalGroup(
            AddWorkUnitDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AddWorkUnitDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(AddWorkUnitDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(AddWorkUnitDialogLayout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addGap(0, 284, Short.MAX_VALUE))
                    .addGroup(AddWorkUnitDialogLayout.createSequentialGroup()
                        .addGroup(AddWorkUnitDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(AddWorkObjectLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, AddWorkUnitDialogLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(AWUAddButton)
                                .addGap(6, 6, 6)
                                .addComponent(AWUCancelButton))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, AddWorkUnitDialogLayout.createSequentialGroup()
                                .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, 77, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE)
                                .addGroup(AddWorkUnitDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(AWUEndTime, javax.swing.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
                                    .addComponent(AWUStartTime))))
                        .addContainerGap())))
        );
        AddWorkUnitDialogLayout.setVerticalGroup(
            AddWorkUnitDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AddWorkUnitDialogLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(AddWorkObjectLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(AddWorkUnitDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(AWUStartTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(AddWorkUnitDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(AWUEndTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(AddWorkUnitDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(AWUAddButton)
                    .addComponent(AWUCancelButton))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        WorkUnitEditor.setTitle(bundle.getString("WorkUnitListTitle")); // NOI18N
        WorkUnitEditor.setModal(true);

        WorkUnitsScroller.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        WorkUnitsScroller.setPreferredSize(new java.awt.Dimension(300, 200));

        WorkUnitList.setToolTipText(bundle.getString("WorkTracker_WorkBlockList_TT")); // NOI18N
        WorkUnitList.setPreferredSize(new java.awt.Dimension(360, 130));
        WorkUnitList.setVisibleRowCount(3);
        WorkUnitList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                WorkUnitListMouseClicked(evt);
            }
        });
        WorkUnitList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                WorkUnitListKeyPressed(evt);
            }
        });
        WorkUnitsScroller.setViewportView(WorkUnitList);

        jLabel1.setText(bundle.getString("DeleteSelectedWorkUnitLabel")); // NOI18N

        WorkUnitsTaskHeadline.setText("jLabel2");

        javax.swing.GroupLayout WorkUnitEditorLayout = new javax.swing.GroupLayout(WorkUnitEditor.getContentPane());
        WorkUnitEditor.getContentPane().setLayout(WorkUnitEditorLayout);
        WorkUnitEditorLayout.setHorizontalGroup(
            WorkUnitEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(WorkUnitEditorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(WorkUnitEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(WorkUnitsTaskHeadline, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(WorkUnitsScroller, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        WorkUnitEditorLayout.setVerticalGroup(
            WorkUnitEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, WorkUnitEditorLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(WorkUnitsTaskHeadline, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(WorkUnitsScroller, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addContainerGap())
        );

        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setLayout(new java.awt.BorderLayout(2, 2));

        LapseLabel.setBackground(java.awt.Color.lightGray);
        LapseLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LapseLabel.setText(bundle.getString("TrackerTimerNotRunning")); // NOI18N
        LapseLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                LapseLabelMouseClicked(evt);
            }
        });
        add(LapseLabel, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void AWUAddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AWUAddButtonActionPerformed
        if (pastWorkUnitTask != null) {
            try {
                Calendar cal = Calendar.getInstance();
                DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
                Date start = df.parse(AWUStartTime.getText());
                Date end = df.parse(AWUEndTime.getText());
                mlcWeekPlan weekPlan = CacheEngineStatic.getWeekPlan(start);
                if (!weekPlan.getTasksIds().contains(pastWorkUnitTask.getId())) {
                    CommandRecorder.getInstance().scheduleCommand(new WeekplanAddTaskCommand(pastWorkUnitTask, weekPlan));
                }
                CacheEngineStatic.createWorkUnit(pastWorkUnitTask, start, end, cal.getTimeZone().getID(), false);
                AddWorkUnitDialog.setVisible(false);
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Date Format Problem", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_AWUAddButtonActionPerformed

    private void AWUCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AWUCancelButtonActionPerformed
        AddWorkUnitDialog.setVisible(false);
    }//GEN-LAST:event_AWUCancelButtonActionPerformed

    private void WorkUnitListKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_WorkUnitListKeyPressed
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_DELETE:
                deleteSelectedWorkUnit();
                break;
        }
    }//GEN-LAST:event_WorkUnitListKeyPressed

    private void WorkUnitListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_WorkUnitListMouseClicked
        switch (evt.getButton()) {
            case MouseEvent.BUTTON3:
                java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/mindliner/gui/GuiElements");
                JPopupMenu m = new JPopupMenu("Work Units");
                JMenuItem mi = new JMenuItem("Delete Selected");
                mi.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        deleteSelectedWorkUnit();
                    }
                });
                m.add(mi);
                m.show(evt.getComponent(), evt.getX(), evt.getY());
                break;
        }
    }//GEN-LAST:event_WorkUnitListMouseClicked

    private void LapseLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_LapseLabelMouseClicked
        int ans = JOptionPane.showConfirmDialog(MindlinerMain.getInstance(), "Do you want to stop the current timer?", "Time Tracker", JOptionPane.YES_NO_OPTION);
        if (ans == JOptionPane.YES_OPTION) {
            stopPerformed();
        }
    }//GEN-LAST:event_LapseLabelMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton AWUAddButton;
    private javax.swing.JButton AWUCancelButton;
    private javax.swing.JTextField AWUEndTime;
    private javax.swing.JTextField AWUStartTime;
    private javax.swing.JLabel AddWorkObjectLabel;
    private javax.swing.JDialog AddWorkUnitDialog;
    private javax.swing.JLabel LapseLabel;
    private javax.swing.ButtonGroup WorkStateGroup;
    private javax.swing.JDialog WorkUnitEditor;
    private javax.swing.JList WorkUnitList;
    private javax.swing.JScrollPane WorkUnitsScroller;
    private javax.swing.JLabel WorkUnitsTaskHeadline;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    // End of variables declaration//GEN-END:variables
    // End of variables declaration
}
