/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.exporter;

import com.mindliner.exceptions.ExportException;
import java.io.File;
import java.util.List;
import com.mindliner.clientobjects.MlMapNode;

/**
 *
 * @author M.Messerli
 */
public interface MlExporter {
    
    public void export(List<MlMapNode> rootNodes, File file) throws ExportException;
    
}
