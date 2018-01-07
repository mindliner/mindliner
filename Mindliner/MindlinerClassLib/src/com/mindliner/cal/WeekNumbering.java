/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.cal;

import java.util.Calendar;
import java.util.Date;

/**
 * A utility class used by the week planner.
 *
 * @author Marius Messerli
 */
public class WeekNumbering {

    // A new year is started if the week contains January 4th (http://en.wikipedia.org/wiki/Seven-day_week)
    private static final int MINIMUM_DAYS_IN_FIRST_WEEK = 4;

    public static int getWeek(Date d) {
        Calendar cal = Calendar.getInstance();
        cal.setMinimalDaysInFirstWeek(MINIMUM_DAYS_IN_FIRST_WEEK);
        cal.setTime(d);
        int week = cal.get(Calendar.WEEK_OF_YEAR);
        return week;
    }

    public static int getYear(Date d) {
        Calendar cal = Calendar.getInstance();
        cal.setMinimalDaysInFirstWeek(MINIMUM_DAYS_IN_FIRST_WEEK);
        cal.setTime(d);
        int week = cal.get(Calendar.WEEK_OF_YEAR);
        int year = cal.get(Calendar.YEAR);
        if (week == 53) {
            year--;
        }
        return year;
    }

}
