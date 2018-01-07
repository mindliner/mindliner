/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.gui.tablemanager;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.gui.tablemanager.MlObjectTable;
import java.util.List;

/**
 * This interface is implemented by classes that are capable of displaying
 * mindliner objects.
 *
 * @author marius
 */
public interface ObjectViewer {

    public void display(mlcObject o);

    public void display (List<mlcObject> objects);

    public void display (mlcObject sourceObject, MlObjectTable sourceTable);
}
