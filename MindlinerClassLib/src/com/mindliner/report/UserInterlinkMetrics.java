/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.report;

import com.mindliner.cal.ReportingPeriod;

/**
 * This report evaluates the number of links between each pair of users.
 * 
 * @author Marius Messerli
 */
public class UserInterlinkMetrics extends UserReport{

    /**
     * This vector holds the number of connections to team members including
     * the link count to self.
     */
    private long[] teamLinks;
    
    public UserInterlinkMetrics(ReportingPeriod.Period period) {
        super(period);
    }

    public long[] getTeamLinks() {
        return teamLinks;
    }

    public void setTeamLinks(long[] teamLinks) {
        this.teamLinks = teamLinks;
    }

        
}
