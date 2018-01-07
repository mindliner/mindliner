/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.mindliner.importer.maps;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.exceptions.ImportException;
import java.io.File;

/**
 *
 * @author Marius Messerli
 */
public interface MlMapImporter {

    /**
     * Import the map's content and create creates the Mindliner elements that represent the maps elements. The return object is intended to be submitted to the Mindliner map creation subsystem
     * Mindliner2DViewer::display.
     *
     * @param file
     * @return The new root object or null if the server could not be reached
     * @throws com.mindliner.exceptions.ImportException
     */
    mlcObject importMap(File file) throws ImportException;
}
