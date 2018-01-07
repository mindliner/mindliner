/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.common;

import java.util.Calendar;
import java.util.Date;

/**
 * A helper class for the weekplan.
 *
 * @author Marius Messerli
 */
public class WeekUtil {

    /**
     * Returns the start date for the specified week
     * @param year
     * @param week
     * @return 
     */
    public static Date getWeekStart(int year, int week) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.WEEK_OF_YEAR, week - 1);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }

    /**
     * Returns the end date for the specified week
     * 
     * @param year
     * @param week
     * @return 
     */
    public static Date getWeekEnd(int year, int week) {
        Date start = getWeekStart(year, week);
        Calendar cal = Calendar.getInstance();
        cal.setTime(start);
        cal.add(Calendar.WEEK_OF_YEAR, 1);
        return cal.getTime();
    }
}
