/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.report;

import com.mindliner.cal.ReportingPeriod.Period;

/**
 * This class provides statistics on the number of changes
 *
 * @author Marius Messerli
 */
public class UserActivityMetrics extends UserReport {

    private Long objectModificationCount;
    private Long objectCreationCount;
    private Long objectDeletionCount;
    private Long own2OwnLinkCreationCount;
    private Long own2ForeignLinkCreationCount;

    public UserActivityMetrics(Period period) {
        super(period);
    }

    public Long getObjectModificationCount() {
        return objectModificationCount;
    }

    public void setObjectModificationCount(Long objectModificationCount) {
        this.objectModificationCount = objectModificationCount;
    }

    public Long getObjectCreationCount() {
        return objectCreationCount;
    }

    public void setObjectCreationCount(Long objectCreationCount) {
        this.objectCreationCount = objectCreationCount;
    }

    public Long getObjectDeletionCount() {
        return objectDeletionCount;
    }

    public void setObjectDeletionCount(Long objectDeletionCount) {
        this.objectDeletionCount = objectDeletionCount;
    }

    public Long getOwn2OwnLinkCreationCount() {
        return own2OwnLinkCreationCount;
    }

    public void setOwn2OwnLinkCreationCount(Long own2OwnLinkCreationCount) {
        this.own2OwnLinkCreationCount = own2OwnLinkCreationCount;
    }

    public Long getOwn2ForeignLinkCreationCount() {
        return own2ForeignLinkCreationCount;
    }

    public void setOwn2ForeignLinkCreationCount(Long own2ForeignLinkCreationCount) {
        this.own2ForeignLinkCreationCount = own2ForeignLinkCreationCount;
    }
}
