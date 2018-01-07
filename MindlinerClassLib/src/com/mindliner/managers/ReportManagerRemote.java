/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.cal.ReportingPeriod;
import com.mindliner.cal.ReportingPeriod.Period;
import com.mindliner.contentfilter.TimeFilter;
import com.mindliner.entities.mlsUser;
import java.util.List;
import javax.ejb.Remote;

/**
 * This interface allows data pool managers to create reports on the current
 * client (data pool).
 *
 * @author Marius Messerli
 */
@Remote
public interface ReportManagerRemote {

    /**
     * Get a JSON formatted set of user statistics.
     * 
     * @param user The user for whom the stats are requested
     * @param period The time period for which the stats are requested
     * @return 
     */
    public String getUserActivityReport(int user, ReportingPeriod.Period period);  
    
    /**
     * Reports for each user the number of links he created between own and foreign objects.
     * @param period
     * @return JSON formatted string representing the links
     */
    public String getUserLinksReport(Period period);
    
    
    /**
     * Fetches all users that share at least one data pool with the current user
     * @return 
     */
    public List<mlsUser> getVisibleUsers();
    
}
