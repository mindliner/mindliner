/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.gui.tablemodels;

import com.mindliner.clientobjects.*;

/**
 * This class is needed only to identify this content in a table cell which
 * would not be the case by filling a string into the cell.
 *
 * @author M.Messerli Created on 05.10.2012, 08:21:09
 */
public class HumanReadableClass {

    private Class clazz;

    public HumanReadableClass(Class clazz) {
        this.clazz = clazz;
    }

    @Override
    public String toString() {
        return getReadableClassName(clazz);
    }

    private String getReadableClassName(Class c) {
        if (c == mlcNews.class) {
            return "Action Item";
        } else if (c == mlcContact.class) {
            return "Contact";
        } else if (c == mlcKnowlet.class) {
            return "Knowlet";
        } else if (c == mlcObject.class) {
            return "Object";
        } else if (c == mlcObjectCollection.class) {
            return "Collection";
        } else if (c == mlcTask.class) {
            return "Task";
        } else if (c == MlcImage.class) {
            return "Image";
        }
        return c.getSimpleName();
    }
}
