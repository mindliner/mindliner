/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.dispatch;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.view.dispatch.MlObjectViewer.ViewType;
import java.util.ArrayList;
import java.util.List;

/**
 * This singleton class handles the display of objects across different view
 * subsystems. For example: If the spreadsheet needs to show a cell as a mind
 * map it will use this class to initiate the display.
 *
 * @author Marius Messerli
 */
public class MlViewDispatcherImpl implements MlViewDispatcher {

    private ArrayList<MlObjectViewer> viewers = new ArrayList<>();

    private MlViewDispatcherImpl() {
    }

    public static MlViewDispatcherImpl getInstance() {
        return MlViewDispatcherImplHolder.INSTANCE;
    }

    @Override
    public void display(mlcObject object, ViewType type) {
        if (object != null) {
            for (MlObjectViewer v : viewers) {
                if (v.isSupported(type)) {
                    v.display(object, type);
                }
            }
        }
    }

    @Override
    public void display(final List<mlcObject> objects, final ViewType type) {
        if (objects != null) {
            for (final MlObjectViewer v : viewers) {
                if (v.isSupported(type)) {
                    v.display(objects, type);
                }
            }
        }
    }

    @Override
    public void registerViewer(MlObjectViewer viewer) {
        if (!viewers.contains(viewer)) {
            viewers.add(viewer);
        }
    }

    @Override
    public void unregisterViewer(MlObjectViewer viewer) {
        viewers.remove(viewer);
    }

    @Override
    public void back() {
        for (final MlObjectViewer v : viewers) {
            v.back();
        }
    }

    private static class MlViewDispatcherImplHolder {

        private static final MlViewDispatcherImpl INSTANCE = new MlViewDispatcherImpl();
    }
}
