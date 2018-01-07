/*
 * ModificationEvaluator.java
 * 
 * Created on 19.06.2007, 23:13:45
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.mindliner.contentfilter.evaluator;
import com.mindliner.contentfilter.ObjectEvaluator;
import com.mindliner.contentfilter.TimeFilter.TimePeriod;
import com.mindliner.entities.mlsContact;
import com.mindliner.entities.mlsObject;
import com.mindliner.entities.mlsUser;
import java.io.Serializable;
import java.util.Date;

/**
 * Checks when the object was last modified.
 *
 * @author Marius Messerli
 */
public class ModificationEvaluator implements ObjectEvaluator, Serializable {

    private static final long serialVersionUID = 19640205L;
    private TimePeriod maxModificationAge = TimePeriod.All;
    private mlsUser currentUser;

    /**
     * Evalutes objects for the time period since the last modification.
     *
     * @param user
     * @param modAge
     */
    public ModificationEvaluator(mlsUser user, TimePeriod modAge) {
        maxModificationAge = modAge;
        currentUser = user;
    }

    /**
     * Makes sure that only objects who's modification time is sooner than the
     * specified time frame pass the filter.
     *
     * @param o The object to be evaluated.
     * @return True if the object's last modification is sooner than the
     * filter's max time frame.
     */
    @Override
    public boolean passesEvaluation(mlsObject o) {
        if (o instanceof mlsContact) {
            return true;
        }
        Date now = new Date();
        mlsObject mbo = (mlsObject) o;
        long millis = getMillis(maxModificationAge);
        if (now.getTime() - mbo.getModificationDate().getTime() <= millis) {
            return true;
        }
        return false;
    }

    private long getMillis(TimePeriod modAge) {
        long hour = 60 * 60 * 1000;
        long day = 24 * hour;
        long week = 7 * day;
        long fortneight = 2 * week;
        long month = 30 * day;
        long year = 360 * day;

        switch (modAge) {
            case All:
                throw new IllegalArgumentException("A modification age qualifyer All is not allowed here - remove the evaluator from the filter.");

            case Hour:
                return hour;

            case Day:
                return day;

            case Week:
                return week;

            case Fortnight:
                return fortneight;

            case Month:
                return month;
                
            case Year:
                return year;

            case SinceLastLogout:
                return (new Date()).getTime() - currentUser.getLastLogout().getTime();

            default:
                throw new IllegalArgumentException("Unindentified age qualifyer not allowed.");
        }
    }

    @Override
    public boolean isMultipleInstancesAllowed() {
        return false;
    }
}
