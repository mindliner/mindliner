/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.connector;

import com.mindliner.clientobjects.MlcImage;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.commands.CommandRecorder;
import com.mindliner.commands.ImageUpdateCommand;
import com.mindliner.commands.MindlinerCommand;
import com.mindliner.commands.TextUpdateCommand;
import com.mindliner.entities.MlsImage;
import java.util.List;

/**
 * Updates the description of the provided mindliner objects with the google drive links 
 * as soon as the upload has finished. 
 * In contrast to FileUploadThread, it does not create new mindliner objects but rather updates the existing ones.
 * @author Dominic Plangger
 */
public class StreamUploadThread extends AbstractUploadThread {
    
     List<mlcObject> objects;

    public StreamUploadThread(CloudConnector connector, List<UploadSource> source, List<mlcObject> objects) {
        super(connector);
        setSource(source);
        this.objects = objects;
    }

    @Override
    protected void afterUpload(List<UploadResult> result) {
        if (result == null) {
            return;
        }
        updateMindlinerObjects(result);
    }

    private void updateMindlinerObjects(List<UploadResult> result) {
        // update the description of the mindliner objects with the new cloud links
        CommandRecorder cr = CommandRecorder.getInstance();
        for(int i = 0; i < objects.size(); i++) {
            UploadResult ur = result.get(i);
            StringBuilder builder = new StringBuilder();
            if (ur.getDownloadUrl() != null) {
                builder.append(ur.getDownloadUrl()).append("\n");
            }
            if (ur.getOpenUrl() != null) {
                builder.append(ur.getOpenUrl());
            }
            mlcObject obj = objects.get(i);
            MindlinerCommand cmd = new TextUpdateCommand(obj, obj.getHeadline(), builder.toString());
            cr.scheduleCommand(cmd);
            String url = ur.getDownloadUrl().replace(CloudConnector.DOWNLOAD_LINK, "");
            if (cmd.getObject() instanceof MlcImage) {
                cmd = new ImageUpdateCommand(cmd.getObject(), null, cmd.getObject().getHeadline(), MlsImage.ImageType.URL, url);
                cr.scheduleCommand(cmd);
            }
        }
    }

}
