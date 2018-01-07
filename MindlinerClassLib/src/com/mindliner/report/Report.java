/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.report;

import com.mindliner.cal.ReportingPeriod.Period;

/**
 * The base class for all stat reports.
 *
 * @author Marius Messerli
 */
public abstract class Report {

    Period period;

    public Report(Period period) {
        this.period = period;
    }

    public Period getPeriod() {
        return period;
    }
}
