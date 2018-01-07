/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.analysis;

import java.util.Date;

/**
 *
 * @author Marius Messerli
 */
public class DueDateAssessor {
    
    /**
     * Returns the rate of the due date.
     * 
     * @param d
     * @return
     */
    public double getRate(Date date, int lookAheadDays, double maxrate){
       
        double ratio = (double) maxrate / (double) lookAheadDays;
        
        // if not scheduled then don't add to the object's rating
        if (date == null) return 0D;
        
        // if already past due date then give maximum rating
        if (date.compareTo(new Date()) <= 0) return maxrate;

        long diffInMillis = date.getTime() - new Date().getTime();
        int diffDays = (int) (diffInMillis / 1000 / 60 / 60 / 24);
        return ratio * Math.max(0, lookAheadDays - diffDays);
    }

}
