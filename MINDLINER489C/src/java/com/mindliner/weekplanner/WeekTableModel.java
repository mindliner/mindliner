/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.weekplanner;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcTask;
import com.mindliner.clientobjects.mlcWeekPlan;
import com.mindliner.clientobjects.mlcWorkUnit;
import com.mindliner.main.SearchPanel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Marius Messerli
 */
public class WeekTableModel extends AbstractTableModel {

    private final List<WeekTableRow> rows = new ArrayList<>();
    int weekPlanId = -1;
    private boolean showAverages = false;
    private static final int DAY_COLUMN_OFFSET = 2; // the number of columns used by one day
    private static final int PLAN_COLUMN_OFFSET = 0; // for each day there is a plan and an actual column
    private static final int ACTUAL_COLUMN_OFFSET = 1;
    private static final int TASK_COLUMN_INDEX = 0;
    private static final int ALLOCATED_TOTAL_COLUMN_INDEX = 1;
    private static final int ACTUAL_TOTAL_COLUMN_INDEX = 2;
    private static final int ROLLING_AVERAGE_COLUMN_INDEX = 3;
    private static final int TO_GO_COLUMN_INDEX = 4;
    private static final int MONDAY_COLUMN_INDEX = 5;
    private static final int TUESDAY_COLUMN_INDEX = MONDAY_COLUMN_INDEX + DAY_COLUMN_OFFSET;
    private static final int WEDNESDAY_COLUMN_INDEX = TUESDAY_COLUMN_INDEX + DAY_COLUMN_OFFSET;
    private static final int THURSDAY_COLUMN_INDEX = WEDNESDAY_COLUMN_INDEX + DAY_COLUMN_OFFSET;
    private static final int FRIDAY_COLUMN_INDEX = THURSDAY_COLUMN_INDEX + DAY_COLUMN_OFFSET;
    private static final int SATURDAY_COLUMN_INDEX = FRIDAY_COLUMN_INDEX + DAY_COLUMN_OFFSET;
    private static final int SUNDAY_COLUMN_INDEX = SATURDAY_COLUMN_INDEX + DAY_COLUMN_OFFSET;
    private static final int COLUMN_COUNT = SUNDAY_COLUMN_INDEX + 2;

    public static enum RowType {

        ObjectRow, TotalRow
    };

    public void setWeekPlan(mlcWeekPlan wp) {
        weekPlanId = wp.getId();
        rows.clear();
        for (Integer id : wp.getTasksIds()) {
            mlcObject o = CacheEngineStatic.getObject(id);
            if (o instanceof mlcTask) {
                mlcTask task = (mlcTask) o;
                if (SearchPanel.evaluateObject(task)) {
                    addRow(task);
                }
            }
        }
        if (rows.isEmpty()) {
            addRow("Drop a task here to add to plan");

        } else {
            addRow("Totals");
        }
        fireTableDataChanged();
    }

    public mlcWeekPlan getWeekPlan() {
        return CacheEngineStatic.getWeekPlan(weekPlanId);
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_COUNT;
    }

    @Override
    public String getColumnName(int column) {

        switch (column) {
            case TASK_COLUMN_INDEX:
                return "Headline";

            case ALLOCATED_TOTAL_COLUMN_INDEX:
                return "∑P";

            case ACTUAL_TOTAL_COLUMN_INDEX:
                return "∑A";

            case ROLLING_AVERAGE_COLUMN_INDEX:
                return "Avg";

            case TO_GO_COLUMN_INDEX:
                return "2Go";

            case MONDAY_COLUMN_INDEX + ACTUAL_COLUMN_OFFSET:
                return "MA";

            case MONDAY_COLUMN_INDEX + PLAN_COLUMN_OFFSET:
                return "MP";

            case TUESDAY_COLUMN_INDEX + ACTUAL_COLUMN_OFFSET:
                return "TA";

            case TUESDAY_COLUMN_INDEX + PLAN_COLUMN_OFFSET:
                return "TP";

            case WEDNESDAY_COLUMN_INDEX + ACTUAL_COLUMN_OFFSET:
                return "WA";

            case WEDNESDAY_COLUMN_INDEX + PLAN_COLUMN_OFFSET:
                return "WP";

            case THURSDAY_COLUMN_INDEX + ACTUAL_COLUMN_OFFSET:
                return "TA";

            case THURSDAY_COLUMN_INDEX + PLAN_COLUMN_OFFSET:
                return "TP";

            case FRIDAY_COLUMN_INDEX + ACTUAL_COLUMN_OFFSET:
                return "FA";

            case FRIDAY_COLUMN_INDEX + PLAN_COLUMN_OFFSET:
                return "FP";

            case SATURDAY_COLUMN_INDEX + ACTUAL_COLUMN_OFFSET:
                return "SAA";

            case SATURDAY_COLUMN_INDEX + PLAN_COLUMN_OFFSET:
                return "SAP";

            case SUNDAY_COLUMN_INDEX + ACTUAL_COLUMN_OFFSET:
                return "SUA";

            case SUNDAY_COLUMN_INDEX + PLAN_COLUMN_OFFSET:
                return "SUP";

            default:
                throw new IllegalArgumentException("Illegal column argument: " + column);
        }
    }

    /**
     * Adds a row to the table.
     *
     * @param o The object which was worked on.
     */
    public final void addRow(Object o) {
        if (o instanceof mlcTask) {
            mlcTask t = (mlcTask) o;
            rows.add(new WeekTableRow(t, RowType.ObjectRow));
        } else if (o instanceof String) {
            rows.add(new WeekTableRow((String) o, RowType.TotalRow));
        }
    }

    public void clear() {
        rows.clear();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex >= rows.size()) {
            throw new IllegalArgumentException("Row index out of range: >= " + rows.size());
        }
        if (columnIndex >= COLUMN_COUNT) {
            throw new IllegalArgumentException("Column index out of range: >= " + COLUMN_COUNT);
        }

        WeekTableRow r = rows.get(rowIndex);

        if (null != r.getType()) switch (r.getType()) {
            case ObjectRow:
                switch (columnIndex) {
                    
                    case TASK_COLUMN_INDEX:
                        return r.getTask();
                        
                    case ALLOCATED_TOTAL_COLUMN_INDEX:
                        return r.getIntegratedEffortEstimate(true);
                        
                    case ACTUAL_TOTAL_COLUMN_INDEX:
                        return r.getIntegratedEffortEstimate(false);
                        
                    case ROLLING_AVERAGE_COLUMN_INDEX:
                        return r.getActualPastWeekAverages();
                        
                    case TO_GO_COLUMN_INDEX:
                        return Math.max(r.getIntegratedEffortEstimate(true) - r.getIntegratedEffortEstimate(false), 0);
                        
                    case MONDAY_COLUMN_INDEX + ACTUAL_COLUMN_OFFSET:
                        return r.getDayTotal(Calendar.MONDAY, false);
                        
                    case MONDAY_COLUMN_INDEX + PLAN_COLUMN_OFFSET:
                        return r.getDayTotal(Calendar.MONDAY, true);
                        
                    case TUESDAY_COLUMN_INDEX + ACTUAL_COLUMN_OFFSET:
                        return r.getDayTotal(Calendar.TUESDAY, false);
                        
                    case TUESDAY_COLUMN_INDEX + PLAN_COLUMN_OFFSET:
                        return r.getDayTotal(Calendar.TUESDAY, true);
                        
                    case WEDNESDAY_COLUMN_INDEX + ACTUAL_COLUMN_OFFSET:
                        return r.getDayTotal(Calendar.WEDNESDAY, false);
                        
                    case WEDNESDAY_COLUMN_INDEX + PLAN_COLUMN_OFFSET:
                        return r.getDayTotal(Calendar.WEDNESDAY, true);
                        
                    case THURSDAY_COLUMN_INDEX + ACTUAL_COLUMN_OFFSET:
                        return r.getDayTotal(Calendar.THURSDAY, false);
                        
                    case THURSDAY_COLUMN_INDEX + PLAN_COLUMN_OFFSET:
                        return r.getDayTotal(Calendar.THURSDAY, true);
                        
                    case FRIDAY_COLUMN_INDEX + ACTUAL_COLUMN_OFFSET:
                        return r.getDayTotal(Calendar.FRIDAY, false);
                        
                    case FRIDAY_COLUMN_INDEX + PLAN_COLUMN_OFFSET:
                        return r.getDayTotal(Calendar.FRIDAY, true);
                        
                    case SATURDAY_COLUMN_INDEX + ACTUAL_COLUMN_OFFSET:
                        return r.getDayTotal(Calendar.SATURDAY, false);
                        
                    case SATURDAY_COLUMN_INDEX + PLAN_COLUMN_OFFSET:
                        return r.getDayTotal(Calendar.SATURDAY, true);
                        
                    case SUNDAY_COLUMN_INDEX + ACTUAL_COLUMN_OFFSET:
                        return r.getDayTotal(Calendar.SUNDAY, false);
                        
                    case SUNDAY_COLUMN_INDEX + PLAN_COLUMN_OFFSET:
                        return r.getDayTotal(Calendar.SUNDAY, true);
                        
                    default:
                        throw new IllegalArgumentException("Column index out of range.");
                }
            case TotalRow:
                switch (columnIndex) {
                    case TASK_COLUMN_INDEX:
                        return r.getTotalsRowTitle();
                        
                    default:
                        int sum = 0;
                        for (int i = 0; i < rows.size() - 1; i++) {
                            sum += (Integer) getValueAt(i, columnIndex);
                        }
                        return sum;
                }
            default:
                return -1; // should never get here
        }
        return -1;
    }

    /*
     * Find all work unit for object and day and delete them. There must only
    ever be one plan unit per day and task.
     */
    private void deleteAllPlanWorkUnits(int day, mlcObject o) {
        Calendar c = Calendar.getInstance();
        // temp list to avoid concurrent modification exceptions
        List<mlcWorkUnit> toBeDeleted = new ArrayList<>();
        mlcWeekPlan weekPlan = CacheEngineStatic.getWeekPlan(weekPlanId);
        if (weekPlan == null) {
            return;
        }
        for (Integer id : weekPlan.getTasksIds()) {
            mlcTask task = (mlcTask) CacheEngineStatic.getObject(id);
            if (task.equals(o)) {
                Iterator it = task.getWorkUnits().iterator();
                for (; it.hasNext();) {
                    mlcWorkUnit wu = (mlcWorkUnit) it.next();
                    c.setTime(wu.getStart());
                    if (c.get(Calendar.DAY_OF_WEEK) == day && wu.isPlan()) {
                        toBeDeleted.add(wu);
                    }
                }
            }
        }
        for (mlcWorkUnit d : toBeDeleted) {
            CacheEngineStatic.removeWorkUnit(d);
        }
    }

    /**
     * @param day One of Calendar.SUNDAY through Calendar.SATRUDAY
     * @param o The object for which the daily estimate is to be set.
     */
    private void updatePlanEstimate(int day, mlcTask task, int minutes) {
        Calendar c = Calendar.getInstance();
        mlcWeekPlan weekPlan = CacheEngineStatic.getWeekPlan(weekPlanId);
        if (weekPlan == null) {
            return;
        }
        c.set(Calendar.YEAR, weekPlan.getYear());
        c.set(Calendar.WEEK_OF_YEAR, weekPlan.getWeek());
        c.set(Calendar.DAY_OF_WEEK, day);
        c.set(Calendar.HOUR_OF_DAY, 1);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        Date start = c.getTime();
        c.add(Calendar.MINUTE, minutes);
        Date end = c.getTime();

        deleteAllPlanWorkUnits(day, task);
        if (minutes > 0) {
            CacheEngineStatic.createWorkUnit(task, start, end, c.getTimeZone().getID(), true);
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch (columnIndex - PLAN_COLUMN_OFFSET) {
            case MONDAY_COLUMN_INDEX:
            case TUESDAY_COLUMN_INDEX:
            case WEDNESDAY_COLUMN_INDEX:
            case THURSDAY_COLUMN_INDEX:
            case FRIDAY_COLUMN_INDEX:
            case SATURDAY_COLUMN_INDEX:
            case SUNDAY_COLUMN_INDEX:
                return true;
            default:
                return false;
        }
    }

    /**
     *
     * @param aValue
     * @param rowIndex
     * @param columnIndex
     */
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

        try {
            String input = (String) aValue;
            Integer minutes = input.isEmpty() ? 0 : Integer.parseInt(input);
            mlcTask task = getRowObject(rowIndex);

            switch (columnIndex) {
                case MONDAY_COLUMN_INDEX + PLAN_COLUMN_OFFSET:
                    updatePlanEstimate(Calendar.MONDAY, task, minutes);
                    break;

                case TUESDAY_COLUMN_INDEX + PLAN_COLUMN_OFFSET:
                    updatePlanEstimate(Calendar.TUESDAY, task, minutes);
                    break;

                case WEDNESDAY_COLUMN_INDEX + PLAN_COLUMN_OFFSET:
                    updatePlanEstimate(Calendar.WEDNESDAY, task, minutes);
                    break;

                case THURSDAY_COLUMN_INDEX + PLAN_COLUMN_OFFSET:
                    updatePlanEstimate(Calendar.THURSDAY, task, minutes);
                    break;

                case FRIDAY_COLUMN_INDEX + PLAN_COLUMN_OFFSET:
                    updatePlanEstimate(Calendar.FRIDAY, task, minutes);
                    break;

                case SATURDAY_COLUMN_INDEX + PLAN_COLUMN_OFFSET:
                    updatePlanEstimate(Calendar.SATURDAY, task, minutes);
                    break;

                case SUNDAY_COLUMN_INDEX + PLAN_COLUMN_OFFSET:
                    updatePlanEstimate(Calendar.SUNDAY, task, minutes);
                    break;
            }
//            fireTableDataChanged();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Cell Value Not A Number", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static int getToGoColumnIndex() {
        return TO_GO_COLUMN_INDEX;
    }

    /**
     * Sunday : 1 Monday : 2 -> 0 Tuesday : 3 -> 6 Wednesday: 4 -> 4 (day - 2) *
     * 2 + Dayoff + PlanColOff
     *
     * @return
     */
    public static int getTodayColumnIndex() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int index = 2 * (cal.get(Calendar.DAY_OF_WEEK) - 2) + PLAN_COLUMN_OFFSET + MONDAY_COLUMN_INDEX;
        return index;
    }

    public boolean isShowAverages() {
        return showAverages;
    }

    public void setShowAverages(boolean showAverages) {
        this.showAverages = showAverages;
    }

    public mlcTask getRowObject(int index) {
        if (rows.size() > index && index >= 0) {
            WeekTableRow row = rows.get(index);
            if (row != null) {
                return row.getTask();
            }
        }
        return null;
    }

    private class WeekTableRow {

        private int taskId = -1;
        private int actualPastWeekAverage = 0;
        private RowType type = RowType.ObjectRow;
        private String totalsRowTitle = "";

        /**
         * The constructor computes the average weekly actual for the specified
         * number of weeks in the past.
         *
         * @param numWeeks The number of past weeks to form the average with.
         * @return The average number of minutes spent on this task in the last
         * numWeeks weeks.
         */
        public WeekTableRow(Object o, RowType type) {
            switch (type) {
                case ObjectRow:
                    taskId = ((mlcTask) o).getId();
                    if (isShowAverages()) {
                        actualPastWeekAverage = CacheEngineStatic.getActualPastWeekAverages(taskId, weekPlanId, 3);
                    } else {
                        actualPastWeekAverage = 0;
                    }
                    break;

                case TotalRow:
                    if (o instanceof String) {
                        totalsRowTitle = (String) o;
                    }
                    break;

                default:
                    throw new AssertionError();
            }
            this.type = type;
        }

        public mlcTask getTask() {
            mlcObject o = CacheEngineStatic.getObject(taskId);
            if (o instanceof mlcTask) {
                return (mlcTask) o;
            } else {
                return null;
            }
        }

        public String getTotalsRowTitle() {
            return totalsRowTitle;
        }

        public int getDayTotal(int dayIndex, boolean isPlan) {
            mlcWeekPlan weekPlan = CacheEngineStatic.getWeekPlan(weekPlanId);
            mlcTask task = (mlcTask) CacheEngineStatic.getObject(taskId);
            if (weekPlan == null || task == null) {
                return 0;
            }
            return CacheEngineStatic.getDailyWorkMinutes(weekPlan.getUser(), task, weekPlan, dayIndex, isPlan);
        }

        /**
         * @todo The hardcoding 0..7 should be replaced by Calendar constants.
         *
         * @param plan Defines where the return value is requested for plan
         * items (true) or actual items (false).
         */
        public int getIntegratedEffortEstimate(boolean plan) {
            int total = 0;
            for (int i = 0; i < 7; i++) {
                total += getDayTotal(i, plan);
            }
            return total;
        }

        public RowType getType() {
            return type;
        }

        public int getActualPastWeekAverages() {
            return actualPastWeekAverage;
        }
    }
}
