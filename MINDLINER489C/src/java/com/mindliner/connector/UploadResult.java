/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.connector;

/**
 * Container class containing the result of a file upload to a cloud storage
 * @author Dominic Plangger
 */
public class UploadResult {
    
    private String downloadUrl = null;
    private String openUrl = null;

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getOpenUrl() {
        return openUrl;
    }

    public void setOpenUrl(String openUrl) {
        this.openUrl = openUrl;
    }

}
