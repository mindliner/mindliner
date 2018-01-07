/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.navigator;

/**
 * This class takes keyboard navigation input (left, right, up, down arrows) and
 * moves the current selected node accordingly.
 *
 * @author Marius Messerli Created on 25.09.2012, 15:42:26
 */
public interface NodeNavigator {

    public void moveEast();

    public void moveWest();

    public void moveNorth();

    public void moveSouth();

    public void moveParent();

    public enum ChildPosition {

        First, Last
    }

    public void moveChild(ChildPosition position);
}
