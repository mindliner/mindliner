/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.containermap;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.connector.CloudConnector;
import com.mindliner.connector.FileUploadThread;
import com.mindliner.connector.UploadResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that uploads files dropped onto a container map (aka Worksphere Map)
 * @author Marius Messerli
 */
public class CmFileUploadThread extends FileUploadThread {
    
    public CmFileUploadThread(List<File> files, mlcObject linkTarget, CloudConnector connector) throws FileNotFoundException {
        super(files, linkTarget, connector);
    }

    @Override
    protected void afterUpload(List<UploadResult> result) {
        if (result == null) {
            result = new ArrayList<>();
            for (File f : droppedFiles) {
                UploadResult ur = new UploadResult();
                ur.setOpenUrl(FILE_PATH_PREFIX + f.toURI());
                result.add(ur);
            }
        }
        // create a knowlet for each file
//        createMindlinerObjects(result);
    }
    
    
    
}
