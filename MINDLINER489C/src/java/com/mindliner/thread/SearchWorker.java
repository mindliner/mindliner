/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.thread;

import com.mindliner.analysis.MlClassHandler.MindlinerObjectType;
import com.mindliner.analysis.SearchStringAnalyzer;
import com.mindliner.analysis.SearchStringAnalyzer.SearchType;
import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.contentfilter.mlFilterTO;
import com.mindliner.events.SearchTermManager;
import com.mindliner.main.MindlinerMain;
import com.mindliner.main.SearchPanel;
import com.mindliner.view.dispatch.MlObjectViewer.ViewType;
import com.mindliner.view.dispatch.MlViewDispatcherImpl;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * This class executes object search in a separate thread.
 *
 * @author Marius Messerli
 */
public class SearchWorker extends SimpleSwingWorker {

    private final String searchString;
    private final MindlinerObjectType targetTableObjectType;
    private final ViewType viewType;

    public SearchWorker(String searchString, MindlinerObjectType objectType, ViewType viewType) {
        this.searchString = searchString;
        this.targetTableObjectType = objectType;
        this.viewType = viewType;
        SearchTermManager.setSearchTerm(searchString);
        SearchPanel.getUniqueInstance().addQuery(searchString);
    }

    public List<mlcObject> getObjects() {
        MindlinerMain.getStatusBar().setMessage("performing search ...");
        SearchStringAnalyzer ssa = new SearchStringAnalyzer();
        MindlinerObjectType searchObjectType = MindlinerObjectType.Any;
        SearchStringAnalyzer.SearchType searchType = ssa.analyze(searchString);
        if (searchType == SearchType.SearchById) {
            mlcObject result = CacheEngineStatic.getObject(ssa.getId());
            if (result != null) {
                List<mlcObject> searchHits = new ArrayList<>();
                searchHits.add(result);
                // check according to search filter settings
                searchHits = SearchPanel.filterObjects(searchHits);
                return searchHits;
            }
            // fall-back to text search for the number
            searchType = SearchType.PlainTextSearch;
        }
        switch (searchType) {
            case SearchInClassOnly:
                if (ssa.getTargetType() != MindlinerObjectType.Any && !targetTableObjectType.equals(MindlinerObjectType.Any)) {
                    JOptionPane.showMessageDialog(null, "This table can only display one type of objects; type character is ignored.", "Leading single-character type specifyer", JOptionPane.WARNING_MESSAGE);
                    searchObjectType = targetTableObjectType;
                } else {
                    searchObjectType = ssa.getTargetType();
                }
                break;
            case PlainTextSearch:
                searchObjectType = targetTableObjectType;
                break;
        }
        mlFilterTO filter = SearchPanel.getSearchFilter();
        filter.setObjectType(searchObjectType);
        return CacheEngineStatic.getPrimarySearchHits(ssa.getPlainSearchString(), filter);
    }

    /**
     * Below we see a cascade of search expansions for users who post identical
     * searches multiple times in a row, first we try increase the search depth,
     * then we include expired objects, then private objects.
     */
    public void runSearch() {
        MlViewDispatcherImpl.getInstance().display(getObjects(), viewType);
        MindlinerMain.getStatusBar().done();
    }

    @Override
    protected Object doInBackground() throws Exception {
        runSearch();
        return null;
    }

}
