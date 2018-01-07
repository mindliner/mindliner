/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.web.backbeans;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mindliner.entities.mlsKnowlet;
import com.mindliner.entities.mlsObject;
import com.mindliner.enums.LinkRelativeType;
import com.mindliner.exceptions.ForeignOwnerException;
import com.mindliner.exceptions.IsOwnerException;
import com.mindliner.exceptions.MlLinkException;
import com.mindliner.exceptions.NonExistingObjectException;
import com.mindliner.json.DFGRelativesJson;
import com.mindliner.json.TreeRelativesJson;
import com.mindliner.managers.LinkManagerRemote;
import com.mindliner.managers.ObjectManagerRemote;
import com.mindliner.managers.SearchManagerRemote;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

/**
 *
 * @author Dominic Plangger
 */
@ManagedBean
@ViewScoped
public class MapBB {

    private static final String REDIRECT = "associations?faces-redirect=true";

    @EJB
    private SearchManagerRemote searchManager;
    @EJB
    private ObjectManagerRemote objectManager;
    @EJB
    private LinkManagerRemote linker;
    @ManagedProperty(value = "#{searchBB}")
    private SearchBB searchBean;

    private String currentNodes = "";
    private String treeNodes = "";
    private String treeNodesToBeAdded = "";
    private String searchResults = "";
    private int rootId;
    private int deleteId;
    private int deleteResult;
    private mlsObject selectedObject = null;
    private boolean editMode = false;
    private final static String JSON_ROOT_TITLE = "{\"rootTreeNode\":";

    public void setSearchBean(SearchBB searchBean) {
        this.searchBean = searchBean;
    }

    public void fetchNewNode() {
        currentNodes = searchManager.fetchRelativesJson(rootId, searchBean.getLevelWS(), new DFGRelativesJson());
    }

    public String fetchRootNode(int rootId) {
        this.rootId = rootId;
        currentNodes = searchManager.fetchRelativesJson(rootId, searchBean.getLevelWS(), new DFGRelativesJson());
        return REDIRECT;
    }

    public void fetchRelatives(int parentId) {
        treeNodesToBeAdded = searchManager.fetchRelativesJson(parentId, 2, new TreeRelativesJson(), searchBean.isIncludeArchivedWS(), searchBean.isIncludePrivateWS());
        //remove outer node
        if (treeNodesToBeAdded.length() > 2) {
            treeNodesToBeAdded = treeNodesToBeAdded.replace(JSON_ROOT_TITLE, "");
            treeNodesToBeAdded = treeNodesToBeAdded.substring(0, treeNodesToBeAdded.length() - 2) + treeNodesToBeAdded.charAt(treeNodesToBeAdded.length() - 1);
        }
    }

    public String fetchTreeRootNode(int rootId) {
        // Filter has been applied, reset object relatives
        if (rootId == -1) {
            searchBean.setLastRelationCacheUpdate(null);
        }
        //root id might get lost due to validation error or submit request
        if (rootId <= 0) {
            rootId = this.rootId;
        } else {
            this.rootId = rootId;
        }
        treeNodes = searchManager.fetchRelativesJson(rootId, searchBean.getLevelWS(), new TreeRelativesJson(), searchBean.isIncludeArchivedWS(), searchBean.isIncludePrivateWS());
        //remove outer node
        if (treeNodes.length() > 2) {
            treeNodes = treeNodes.replace(JSON_ROOT_TITLE, "");
            treeNodes = treeNodes.substring(0, treeNodes.length() - 2) + treeNodes.charAt(treeNodes.length() - 1);
        }

        return "workspace?faces-redirect=true&id=" + rootId;
    }

    public void deleteNode(int nodeId) {
        try {
            objectManager.remove(nodeId);
            deleteResult = 1;
        } catch (ForeignOwnerException | IsOwnerException ex) {
            Logger.getLogger(MapBB.class.getName()).log(Level.SEVERE, null, ex);
            deleteResult = 0;
        }
    }

    public void deleteNode() {
        deleteNode(getDeleteId());
    }

    public void relink(int nodeId, int oldRelativeId, int newRelativeId) throws ForeignOwnerException, MlLinkException, NonExistingObjectException {
        // always two-way updates
        linker.unlink(nodeId, oldRelativeId, false, LinkRelativeType.OBJECT);
        linker.link(nodeId, newRelativeId, false, LinkRelativeType.OBJECT);
    }

    public int getDeleteId() {
        return deleteId;
    }

    public void setDeleteId(int deleteId) {
        this.deleteId = deleteId;
    }

    public int getRootId() {
        return rootId;
    }

    public void setRootId(int rootId) {
        this.rootId = rootId;
    }

    public String getCurrentNodes() {
        return currentNodes;
    }

    public void setCurrentNodes(String currentNodes) {
        this.currentNodes = currentNodes;
    }

    public int getDeleteResult() {
        return deleteResult;
    }

    public void setDeleteResult(int deleteResult) {
        this.deleteResult = deleteResult;
    }

    public String getTreeNodes() {
        return treeNodes;
    }

    public void setTreeNodes(String treeNodes) {
        this.treeNodes = treeNodes;
    }

    public String getTreeNodesToBeAdded() {
        return treeNodesToBeAdded;
    }

    public void setTreeNodesToBeAdded(String treeNodesToBeAdded) {
        this.treeNodesToBeAdded = treeNodesToBeAdded;
    }

    /**
     * These are the results of the current search. They are added to the map,
     * under a temporary collection, so that the drag and drop mechanism can be
     * used to link them to other nodes in the tree.
     *
     * @return
     */
    public String getSearchResults() {
        int maxRowsToShow = 8;

        List<mlsObject> sr = searchBean.getSearchResults();
        if (sr.isEmpty()) {
            return "";
        }
        int maxRows = Math.min(maxRowsToShow, sr.size());

        // create a temporary parent node for all the search hits
        TreeRelativesJson searchTree = new TreeRelativesJson();
        mlsKnowlet searchRootObject = new mlsKnowlet();
        searchRootObject.setHeadline("(search results)");
        searchRootObject.setId(-1);
        // I need to add the search hits to the getRelatives() - they will
        // be used only to to determine if the search hit node has has children
        for (int i = 0; i < maxRows; i++) {
            mlsObject searchHit = sr.get(i);
            searchRootObject.getRelatives().add(searchHit);
        }
        searchTree.addNode(null, searchRootObject);

        for (int i = 0; i < maxRows; i++) {
            mlsObject searchHit = sr.get(i);
            searchTree.addNode(searchRootObject, searchHit);
        }

        // convert to JSON
        Gson gson = new GsonBuilder().create();
        String searchResultsJson = gson.toJson(searchTree);
        System.out.println("Json search results: " + searchResultsJson);
        return searchResultsJson;
    }

    public void setSearchResults(String searchResults) {
        this.searchResults = searchResults;
    }

    public mlsObject getSelectedObject() {
        return selectedObject;
    }

    public void setSelectedObject(int key) {
        mlsObject o = objectManager.find(key);
        this.selectedObject = o;
    }

    public boolean getEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

}
