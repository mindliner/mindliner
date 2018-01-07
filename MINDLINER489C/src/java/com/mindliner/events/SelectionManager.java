/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.mindliner.events;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.view.connectors.NodeConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the main class to notify the different parts of the application
 * whenever the selected object(s) have changed.
 *
 * @author Marius Messerli
 */
public class SelectionManager {

    private static List<NodeConnection> connSelection = new ArrayList<>();
    private static List<mlcObject> selection = new ArrayList<>();
    private static List<SelectionObserver> observers = new ArrayList<>();

    /**
     * Updates the currently selected object(s) and notifies observers.
     *
     * @param selection The list of objects that are currently selected. If null
     * is specified the current selection is cleared.
     */
    public static void setSelection(List<mlcObject> selection) {
        if (selection == null) {
            SelectionManager.selection = new ArrayList<>();
        } else {
            if (!selection.isEmpty()) {
                connSelection = new ArrayList<>();
            }
            SelectionManager.selection = selection;
        }
        notifyNodeChange();
    }

    public static void setConnectionSelection(List<NodeConnection> selection) {
        if (selection == null) {
            SelectionManager.connSelection = new ArrayList<>();
        } else {
            SelectionManager.connSelection = selection;
        }
        notifyConnectionChange();
    }

    public static void setConnectionSelection(NodeConnection selection) {
        List<NodeConnection> list = new ArrayList<>();
        list.add(selection);
        setConnectionSelection(list);
    }

    /**
     * This is a convenience function to set the selection to a single object
     *
     * @param newSelection
     */
    public static void setSelection(mlcObject newSelection) {
        List<mlcObject> list = new ArrayList<>();
        list.add(newSelection);
        setSelection(list);
    }

    public static void addToSelection(mlcObject newSelection) {
        getSelection().add(newSelection);
    }

    public static void registerObserver(SelectionObserver o) {
        if (!observers.contains(o)) {
            observers.add(o);
        }
    }

    public static void unregisterObserver(SelectionObserver o) {
        observers.remove(o);
    }

    private static void notifyNodeChange() {
        observers.stream().forEach((o) -> {
            o.selectionChanged(selection);
        });
    }

    private static void notifyConnectionChange() {
        observers.stream().forEach((o) -> {
            o.connectionSelectionChanged(connSelection);
        });
    }

    public static void clearSelection() {
        if (!selection.isEmpty()) {
            selection = new ArrayList<>();
            observers.stream().forEach((o) -> {
                o.clearSelections();
            });
        }
    }

    public static void clearConnectionSelection() {
        if (!connSelection.isEmpty()) {
            connSelection = new ArrayList<>();
            observers.stream().forEach((o) -> {
                o.clearConnectionSelections();
            });
        }
    }

    /**
     * Returns the first object of the selection list, i.e. the "current
     * selection".
     *
     * @return The object which was the most recent selection.
     */
    public static mlcObject getLastSelection() {
        if (selection == null || selection.isEmpty()) {
            return null;
        }
        return selection.get(0);
    }

    public static List<mlcObject> getSelection() {
        return selection;
    }

    public static List<NodeConnection> getConnectionSelection() {
        return connSelection;
    }

    public static boolean isConnectionSelected(NodeConnection conn) {
        return connSelection.contains(conn);
    }

    public static void addToConnectionSelection(NodeConnection conn) {
        if (!connSelection.contains(conn)) {
            connSelection.add(conn);
            notifyConnectionChange();
        }
    }

    public static void removeFromConnSelection(NodeConnection conn) {
        if (connSelection.remove(conn)) {
            notifyConnectionChange();
        }
    }

}
