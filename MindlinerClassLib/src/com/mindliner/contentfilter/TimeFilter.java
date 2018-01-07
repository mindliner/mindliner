/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.contentfilter;

import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Marius Messerli
 */
public class TimeFilter {

    public enum TimePeriod {
        SinceLastLogout,
        Hour,
        Day,
        Week,
        Fortnight,
        Month,
        Year,
        All
    }

    private static void applyPeriod(Calendar cal, boolean future, int field, int amount) {
        int signedAmount = future ? amount : -amount;
        cal.add(field, signedAmount);
    }

    /**
     * This function takes a start date and adds or removes the specified time
     * period.
     *
     * @param base The date to which the period is to be added or subtracted
     * @param future If true the period is added otherwise the period is
     * subtracted
     * @return The date that is 'period' in the future or the past depending on
     * the 'future' parameter
     */
    public static Date applyOffset(Date base, boolean future, TimePeriod period) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(base);
        switch (period) {
            case Year:
                applyPeriod(cal, future, Calendar.YEAR, 1);
                break;

            case Month:
                applyPeriod(cal, future, Calendar.MONTH, 1);
                break;

            case Fortnight:
                applyPeriod(cal, future, Calendar.DAY_OF_YEAR, 14);
                break;

            case Day:
                applyPeriod(cal, future, Calendar.DAY_OF_YEAR, 1);
                break;

            case Week:
                applyPeriod(cal, future, Calendar.WEEK_OF_YEAR, 1);
                break;

            case All:
                applyPeriod(cal, future, Calendar.YEAR, 100);
                break;
        }
        return cal.getTime();
    }

}
