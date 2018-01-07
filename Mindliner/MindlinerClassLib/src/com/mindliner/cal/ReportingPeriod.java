/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.cal;

import java.util.Calendar;
import java.util.Date;

/**
 * This class provides calendar functions to specify the tyical business
 * reporting periods.
 *
 * @author Marius Messerli
 */
public class ReportingPeriod {

    public static enum Period {

        LastWeek("LastWeek"),
        LastMonth("LastMonth"),
        LastQuarter("LastQuarter"),
        LastHalfYear("LastHalfYear"),
        LastYear("LastYear"),
        YearToDate("YearToDate");
        
        private String key;
        
        Period(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public Date getStartDate() {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.HOUR_OF_DAY, 0);

            switch (this) {
                case LastWeek:
                    int dayInWeek = c.get(Calendar.DAY_OF_WEEK);
                    c.add(Calendar.DAY_OF_MONTH, -7 - dayInWeek);
                    break;
                case LastMonth:
                    c.add(Calendar.MONTH, -1);
                    c.set(Calendar.DAY_OF_MONTH, 0);
                    break;
                case LastQuarter:
                    int monthInCurrentQuarter = c.get(Calendar.MONTH) % 3;
                    c.add(Calendar.MONTH, -(3 + monthInCurrentQuarter));
                    c.set(Calendar.DAY_OF_MONTH, 0);
                    break;
                case LastHalfYear:
                    int monthsInCurrentHalf = c.get(Calendar.MONTH) % 6;
                    c.add(Calendar.MONTH, -(6 + monthsInCurrentHalf));
                    c.set(Calendar.DAY_OF_MONTH, 0);
                    break;
                case YearToDate:
                    c.set(Calendar.MONTH, 0);
                    c.set(Calendar.DAY_OF_MONTH, 1);

                    break;
                case LastYear:
                    c.add(Calendar.YEAR, -1);
                    c.set(Calendar.MONTH, 0);
                    c.set(Calendar.DAY_OF_MONTH, 1);
                    break;
            }
            return c.getTime();
        }

        public Date getEndDate() {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.HOUR_OF_DAY, 0);

            switch (this) {
                case LastWeek:
                    int dayInWeek = c.get(Calendar.DAY_OF_WEEK);
                    c.add(Calendar.DAY_OF_MONTH, -dayInWeek);
                    c.set(Calendar.HOUR_OF_DAY, 0);
                    break;
                case LastMonth:
                    c.set(Calendar.DAY_OF_MONTH, 0);
                    break;
                case LastQuarter:
                    int monthInCurrentQuarter = c.get(Calendar.MONTH) % 3;
                    c.add(Calendar.MONTH, -monthInCurrentQuarter);
                    c.set(Calendar.DAY_OF_MONTH, 0);
                    break;
                case LastHalfYear:
                    int monthsInCurrentHalf = c.get(Calendar.MONTH) % 6;
                    c.add(Calendar.MONTH, - monthsInCurrentHalf);
                    c.set(Calendar.DAY_OF_MONTH, 0);
                    break;
                case YearToDate:
                    c.setTime(new Date());
                    break;
                case LastYear:
                    c.set(Calendar.MONTH, 0);
                    c.set(Calendar.DAY_OF_MONTH, 1);
                    break;
            }
            return c.getTime();
        }
    }
}
