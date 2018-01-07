/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.web.backbeans;

import com.mindliner.analysis.SearchStringAnalyzer;
import com.mindliner.analysis.SearchStringAnalyzer.SearchType;
import com.mindliner.contentfilter.BaseFilter;
import com.mindliner.contentfilter.BaseFilter.SortingMode;
import com.mindliner.contentfilter.TimeFilter.TimePeriod;
import com.mindliner.contentfilter.mlFilterTO;
import com.mindliner.entities.mlsClient;
import com.mindliner.entities.mlsObject;
import com.mindliner.entities.mlsUser;
import com.mindliner.managers.ObjectManagerRemote;
import com.mindliner.managers.SearchManagerLocal;
import com.mindliner.managers.UserManagerLocal;
import com.mindliner.managers.UserManagerRemote;
import com.mindliner.managers.ObjectManagerLocal;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

/**
 *
 * @author Marius Messerli
 */
@ManagedBean
@SessionScoped
@DeclareRoles(value = {"Admin", "User", "MasterAdmin"})
@RolesAllowed(value = {"Admin", "User", "MasterAdmin"})
public class SearchBB implements Serializable {

    private final List<mlsObject> searchResults = new ArrayList<>();
    private String searchString = "";
    private int maxHits;
    private int rowsToShow;
    private int relationLevel;
    private boolean includeArchived;
    private boolean includePrivate;
    private boolean includeFiles;
    private boolean showFilter;
    private final static String REDIRECT_EXPLORE_URL = "workspace?faces-redirect=true";
    private List<Integer> selectedObjectOwners;
    private List<mlsUser> objectOwners;
    private List<mlsClient> selectedClients;
    private TimePeriod maxModificationAge;
    private SortingMode defaultSorting;

    //Filter options for Workspace
    private boolean includeArchivedWS = true;
    private boolean includePrivateWS = true;
    private int levelWS = 2;

    // the jsf mechanism calls the getRelatedObjects functions multiple times which
    // in turn generates multiple (unwanted) log records, so I cache it for a minute 
    private List<mlsObject> shortTermRelatedObjectsCache = new ArrayList<>();
    private Date lastRelationCacheUpdate = null;
    private mlsObject lastRootObject = null;

    @EJB
    private SearchManagerLocal searchManager;
    @EJB
    private ObjectManagerRemote objectManager;
    @EJB
    private ObjectManagerLocal objectManagerLocal;
    @EJB
    private UserManagerLocal userManager;
    @EJB
    private UserManagerRemote userManagerRemote;

    @PostConstruct
    public void init() {
        if (FacesContext.getCurrentInstance().getExternalContext().getRemoteUser() != null) {
            initUserRelated(); //TODO when to init this later?
        }

        showFilter = false;
        rowsToShow = 5;
        includeArchived = false;
        includePrivate = true;
        includeFiles = false;
        maxModificationAge = TimePeriod.All;
        maxHits = 30;
        defaultSorting = BaseFilter.SortingMode.Modification;
    }

    private void initUserRelated() {
        mlsUser currentUser = userManager.getCurrentUser();
        selectedClients = currentUser.getClients();
        objectOwners = userManagerRemote.getUsersWithSharedDatapool(currentUser.getId());
        selectedObjectOwners = new ArrayList<>();
        for (mlsUser o : objectOwners) {
            selectedObjectOwners.add(o.getId());
        }
    }

    public List<mlsUser> getObjectOwners() {
        return objectOwners;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    /**
     * This function is called by the object list page.
     *
     * @return
     */
    public List<mlsObject> getSearchResults() {
        return searchResults;
    }

    /**
     * Removes the specified object from the current search hit list.
     *
     * @param o The object to be removed from the search hit list.
     */
    public void removeFromResults(mlsObject o) {
        searchResults.remove(o);
    }

    /**
     * Adds the specified object to the current search results.
     *
     * @param o
     */
    public void addToResults(mlsObject o) {
        searchResults.add(o);
    }

    public List<mlsObject> getLimitedSearchResults(int limit) {
        if (limit < searchResults.size()) {
            return searchResults.subList(0, limit - 1);
        }
        return searchResults;
    }

    private mlFilterTO createFilter() {
        mlFilterTO fto = new mlFilterTO();

        fto.setOwnerIds(selectedObjectOwners);

        //Filter clients
        Set<Integer> clientIds = new HashSet<>();
        for (mlsClient client : selectedClients) {
            clientIds.add(client.getId());
        }
        fto.setDataPoolIds(clientIds);

        //Filter other options
        fto.setMaxNumberOfElements(maxHits);
        fto.setShowArchived(includeArchived);
        fto.setShowPrivate(includePrivate);
        fto.setIncludeFiles(includeFiles);
        fto.setMaxModificationAge(maxModificationAge);
        fto.setDefaultSorting(defaultSorting);
        fto.setRelationLevel(relationLevel);
        return fto;
    }

    public String loadObjectsMatchingSearchString() {
        List<mlsObject> searchHits = new ArrayList<>();
        SearchStringAnalyzer ssa = new SearchStringAnalyzer();
        SearchStringAnalyzer.SearchType searchType = ssa.analyze(searchString);

        // handle the ID case first and fall back to text search if it fails to find an object
        if (searchType == SearchType.SearchById) {

            mlsObject result = objectManager.find(ssa.getId());
            if (result != null) {
                searchHits.add(result);
            } else {
                searchType = SearchType.PlainTextSearch;
            }
        }
        mlFilterTO filter = createFilter();
        if (searchType == SearchType.SearchInClassOnly) {
            filter.setObjectType(ssa.getTargetType());
            searchHits = searchManager.getTextSearchResults(ssa.getPlainSearchString(), filter);
        } else if (searchType == SearchType.PlainTextSearch) {
            searchHits = searchManager.getTextSearchResults(ssa.getPlainSearchString(), filter);
        }
        searchResults.clear();

        for (mlsObject searchHit : searchHits) {
            if (objectManagerLocal.isAuthorizedForCurrentUser(searchHit)
                    && searchResults.size() < maxHits) {
                searchResults.add(searchHit);
            }
        }
        return REDIRECT_EXPLORE_URL;
    }

    public List<mlsObject> getRelatedObjects(mlsObject o) {
        // caching is only required because the jsf mechanism calls this function once for every element in the list
        if (lastRelationCacheUpdate == null
                || ((new Date()).getTime() - lastRelationCacheUpdate.getTime()) > 1000 * 60
                || lastRootObject == null || !lastRootObject.equals(o)) {
            shortTermRelatedObjectsCache = searchManager.loadRelatives(o.getId(), includeArchivedWS, includePrivateWS);
            lastRelationCacheUpdate = new Date();
            lastRootObject = o;
        }
        return shortTermRelatedObjectsCache;
    }

    public TimePeriod[] getModificationAges() {
        return TimePeriod.values();
    }

    public BaseFilter.SortingMode[] getDefaultSortingModes() {
        return BaseFilter.SortingMode.values();
    }

    public int getMaxHits() {
        return maxHits;
    }

    public void setMaxHits(int maxHits) {
        this.maxHits = maxHits;
    }

    public boolean isIncludeArchived() {
        return includeArchived;
    }

    public void setIncludeArchived(boolean includeArchived) {
        this.includeArchived = includeArchived;
    }

    public List<mlsClient> getSelectedClients() {
        return selectedClients;
    }

    public void setSelectedClients(List<mlsClient> selectedClients) {
        this.selectedClients = selectedClients;
    }

    public List<Integer> getSelectedObjectOwners() {
        return selectedObjectOwners;
    }

    public void setSelectedObjectOwners(List<Integer> selectedObjectOwners) {
        this.selectedObjectOwners = selectedObjectOwners;
    }

    public boolean isIncludePrivate() {
        return includePrivate;
    }

    public void setIncludePrivate(boolean includePrivate) {
        this.includePrivate = includePrivate;
    }

    public TimePeriod getMaxModificationAge() {
        return maxModificationAge;
    }

    public void setMaxModificationAge(TimePeriod maxModificationAge) {
        this.maxModificationAge = maxModificationAge;
    }

    public BaseFilter.SortingMode getDefaultSorting() {
        return defaultSorting;
    }

    public void setDefaultSorting(BaseFilter.SortingMode defaultSorting) {
        this.defaultSorting = defaultSorting;
    }

    public boolean isIncludeFiles() {
        return includeFiles;
    }

    public void setIncludeFiles(boolean includeFiles) {
        this.includeFiles = includeFiles;
    }

    public int getRelationLevel() {
        return relationLevel;
    }

    public void setRelationLevel(int relationLevel) {
        this.relationLevel = relationLevel;
    }

    public int getRowsToShow() {
        return rowsToShow;
    }

    public void setRowsToShow(int rowsToShow) {
        this.rowsToShow = rowsToShow;
    }

    public boolean getShowFilter() {
        return showFilter;
    }

    public void setShowFilter(boolean showFilter) {
        this.showFilter = showFilter;
    }

    public boolean isIncludeArchivedWS() {
        return includeArchivedWS;
    }

    public void setIncludeArchivedWS(boolean includeArchivedWS) {
        this.includeArchivedWS = includeArchivedWS;
    }

    public boolean isIncludePrivateWS() {
        return includePrivateWS;
    }

    public void setIncludePrivateWS(boolean includePrivateWS) {
        this.includePrivateWS = includePrivateWS;
    }

    public int getLevelWS() {
        return levelWS;
    }

    public void setLevelWS(int levelWS) {
        this.levelWS = levelWS;
    }

    public Date getLastRelationCacheUpdate() {
        return lastRelationCacheUpdate;
    }

    public void setLastRelationCacheUpdate(Date lastRelationCacheUpdate) {
        this.lastRelationCacheUpdate = lastRelationCacheUpdate;
    }
}
