/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.connector;

import com.mindliner.serveraccess.StatusReporter;
import java.util.List;

/**
 * Connector that does nothing. Used in case when user does not want to upload the files
 * 
 * @author Dominic Plangger
 */
public class NoActionConnector extends CloudConnector{

    @Override
    public List<UploadResult> uploadFiles(List<UploadSource> fileStreams) throws ConnectorException {
        return null;
    }

    @Override
    public boolean authenticate() {
        return true;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void clear() {
    }
    
}
