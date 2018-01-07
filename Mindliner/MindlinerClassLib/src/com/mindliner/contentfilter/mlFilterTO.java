/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.contentfilter;

import com.mindliner.analysis.MlClassHandler;
import com.mindliner.analysis.MlClassHandler.MindlinerObjectType;
import com.mindliner.contentfilter.BaseFilter.SortingMode;
import com.mindliner.contentfilter.TimeFilter.TimePeriod;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class represents the transport object for a filter setting. This is more the
 * most light weight version of communicating to the server and has been
 * implemented in response to performance problems with version of the filter
 * that were representing the objects instead of the ids.
 *
 * @author Marius Messerli
 */
public class mlFilterTO implements Serializable {

    private Date lastLogin = null;
    private String textConstraint = "";
    private int maxNumberOfElements = 100;
    private boolean showArchived = false;
    private boolean showPrivate = false;
    private boolean includeFiles = false;
    private MindlinerObjectType objectType = MindlinerObjectType.Any;
    private TimePeriod maxModificationAge = TimePeriod.All;
    private SortingMode defaultSorting = SortingMode.Modification;
    // if > 0, then the search term(s) can appear in related objects, too
    private int relationLevel = 0;
    private static final long serialVersionUID = 19640205L;
    private List<Integer> ownerIds = new ArrayList<>();
    private Set<Integer> dataPoolIds = new HashSet<>();

    public mlFilterTO() {
    }

    public mlFilterTO(BaseFilter f) {
        lastLogin = f.getLastLogin();
        textConstraint = f.getTextConstraint();
        maxNumberOfElements = f.getMaximumNumberOfDisplayElements();
        showArchived = f.isShowArchived();
        showPrivate = f.isShowPrivateElements();
        maxModificationAge = f.getMaxModificationAge();
        defaultSorting = f.getDefaultSorting();
        relationLevel = f.getRelationLevel();
        ownerIds = f.getOwnerIds();
        dataPoolIds = f.getDataPoolIds();
        objectType = f.getObjectType();
    }

    public List<Integer> getOwnerIds() {
        return ownerIds;
    }

    public void setOwnerIds(List<Integer> ownerIds) {
        this.ownerIds = ownerIds;
    }

    public Set<Integer> getDataPoolIds() {
        return dataPoolIds;
    }

    public void setDataPoolIds(Set<Integer> clientIds) {
        this.dataPoolIds = clientIds;
    }

    public void setMaxNumberOfElements(int max) {
        maxNumberOfElements = max;
    }

    public int getMaxNumberOfElements() {
        return maxNumberOfElements;
    }

    public boolean isShowArchived() {
        return showArchived;
    }

    public void setShowArchived(boolean showArchived) {
        this.showArchived = showArchived;
    }

    public void setShowPrivate(boolean show) {
        showPrivate = show;
    }

    public boolean getShowPrivate() {
        return showPrivate;
    }

    public void setTextConstraint(String t) {
        textConstraint = t;
    }

    public String getTextConstraint() {
        return textConstraint;
    }

    public void setDefaultSorting(SortingMode d) {
        defaultSorting = d;
    }

    public SortingMode getDefaultSorting() {
        return defaultSorting;
    }

    public int getRelationLevel() {
        return relationLevel;
    }

    public void setRelationLevel(int relationLevel) {
        this.relationLevel = relationLevel;
    }

    public void setLastLogin(Date llt) {
        lastLogin = llt;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public TimePeriod getMaxModificationAge() {
        return maxModificationAge;
    }

    public void setMaxModificationAge(TimePeriod maxModificationAge) {
        this.maxModificationAge = maxModificationAge;
    }

    public boolean isIncludeFiles() {
        return includeFiles;
    }

    public void setIncludeFiles(boolean includeFiles) {
        this.includeFiles = includeFiles;
    }

    public MindlinerObjectType getObjectType() {
        return objectType;
    }

    public void setObjectType(MindlinerObjectType objectType) {
        this.objectType = objectType;
    }

}
