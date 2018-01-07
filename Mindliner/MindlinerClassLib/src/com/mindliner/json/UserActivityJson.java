/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.json;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Dominic Plangger
 */
public class UserActivityJson {
    private final String name;
    private final List<Entry> values;

    public UserActivityJson(String name) {
        this.name = name;
        this.values = new ArrayList<>();
    }
    
    public void addEntry(int amount, Date date) {
        if (!values.isEmpty()) {
            Entry e = values.get(values.size() - 1);
            boolean sameDay = isSameDay(e.date, date);
            if (sameDay) {
                e.amount += amount;
                return;
            }
        }
        values.add(new Entry(amount, date));
    }

    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
        return sameDay;
    }
    
    public void computeDailyIncreases() {
        if (values.size() < 2) {
            return;
        }
        int dayBefore = values.remove(0).amount;
        int currDay;
        for (Entry e : values) {
            currDay = e.amount;
            e.amount -= dayBefore;
            dayBefore = currDay;
        }
    }

    private static class Entry {
        public int amount;
        public final Date date;

        public Entry(int amount, Date date) {
            this.amount = amount;
            this.date = date;
        }
    }
}
