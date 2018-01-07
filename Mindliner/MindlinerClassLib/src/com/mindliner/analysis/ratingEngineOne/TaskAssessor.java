/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.analysis.ratingEngineOne;

import com.mindliner.analysis.DueDateAssessor;
import com.mindliner.analysis.MlsObjectRatingAssessor;
import com.mindliner.entities.mlsObject;
import com.mindliner.entities.mlsTask;

/**
 *
 * @author Marius Messerli
 */
public class TaskAssessor extends MlsObjectRatingAssessor {

    private static final int DUEDATE_LOOKAHEAD = 10;

    /**
     * Constructor
     *
     * @param inheritance The rate at which the rating of relatives are adopted
     * @param expirationDiscount The discount (a factor by which rating is
     * divided) applied to expired objects
     */
    public TaskAssessor(double inheritance, double expirationDiscount) {
        super(inheritance, expirationDiscount);
    }

    /**
     * Formula is 2 * priority + (10 - number of days to due date) +
     * inheritanceRate * neighbor rating
     *
     * @param object The object that is to be rated
     * @return The new rating value for this object
     */
    @Override
    public double getSpecificRatingComponent(mlsObject object) {
        if (!(object instanceof mlsTask)) {
            throw new IllegalArgumentException("Only tasks are allowed as arguments");
        }
        mlsTask t = (mlsTask) object;
        double rate = 0D;
        if (t.isCompleted() == false) {
            rate += t.getPriority().getImportance();
            DueDateAssessor dda = new DueDateAssessor();
            double dueDateRate = dda.getRate(t.getDueDate(), DUEDATE_LOOKAHEAD, 5); // max additional 5 points for urgency
            rate += dueDateRate;
            if (getInheritanceRate() > 0) {
                for (mlsObject o : t.getRelatives()) {
                    if (o.getRatingDetail() == null) {
                        System.err.println("Object skipped as rating details are null for object id " + o.getId());
                    } else {
                        rate += o.getRatingDetail().getRating() * getInheritanceRate();
                    }
                }
            }
        }
        return rate;
    }
}
