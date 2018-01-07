/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.mlcNews;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.contentfilter.BaseFilter.SortingMode;
import com.mindliner.gui.tablemanager.ObjectViewer;
import com.mindliner.gui.tablemanager.TableManager;
import com.mindliner.main.SearchPanel;
import com.mindliner.serveraccess.StatusReporter;
import com.mindliner.thread.SimpleSwingWorker;
import com.mindliner.view.dispatch.MlObjectViewer;
import com.mindliner.view.dispatch.MlViewDispatcherImpl;
import java.util.ArrayList;
import java.util.List;

/**
 * Class loads related object to the specified root object using all the filter
 * settings that are currently active with the SearchManager bean.
 *
 * @author Marius Messerli
 */
public class RelationLoader extends SimpleSwingWorker {

    private mlcObject sourceObject = null;
    private StatusReporter statusReporter = null;
    private SortingMode sorting = null;

    public RelationLoader(StatusReporter sr, ObjectViewer viewer) {
        statusReporter = sr;
    }

    public void load() {
        statusReporter.startTask(0, 100, false, true);
        statusReporter.setMessage("loading relatives ...");
        List<mlcObject> relatives = null;

        /**
         * if the rootObject is an action item it may have ID of -1. For these
         * objects we show the userObject.
         */
        if (sourceObject.getId() == -1 && sourceObject instanceof mlcNews) {
            mlcObject userObject = null;
            mlcNews ai = (mlcNews) sourceObject;
            if (ai.getUserObjectId() != -1) {
                userObject = CacheEngineStatic.getObject(ai.getUserObjectId());
            }

            if (userObject != null) {
                relatives = new ArrayList<>();
                relatives.add(userObject);
            }
        } else {
            relatives = CacheEngineStatic.getLinkedObjects(sourceObject);
        }
        if (relatives != null && !relatives.isEmpty()) {
            if (!sourceObject.isRelativesOrdered()) {
                SearchPanel.applySelectedSorting(relatives);
            }
            MlViewDispatcherImpl.getInstance().display(relatives, MlObjectViewer.ViewType.GenericTable);
        }
        endWork();
    }

    private void endWork() {
        statusReporter.done();
    }

    @Override
    protected Object doInBackground() throws Exception {
        load();
        return null;
    }

    public void setObject(mlcObject o) {
        if (o == null) {
            throw new IllegalArgumentException("Source object cannot be null");
        }
        sourceObject = o;
    }

    public void setSorting(SortingMode sorting) {
        this.sorting = sorting;
    }

    public SortingMode getSorting() {
        return sorting;
    }
}
