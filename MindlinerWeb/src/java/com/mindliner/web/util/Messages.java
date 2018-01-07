/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.web.util;

import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import javax.faces.application.FacesMessage;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

/**
 *
 * @author Ming
 */
@Named(value = "utility")
@SessionScoped
public class Messages {
    
   private static ResourceBundle getBundle() {
        return java.util.ResourceBundle.getBundle("com/mindliner/web/resources/WebText");
    }
   
    public static String getStringFromBundle(String key) {
        ResourceBundle bundle = getBundle();
        return bundle.getString(key);
    }
    
    public static String getReadableDate(Date date) {
        Date now = new Date();
        long units = TimeUnit.MILLISECONDS.toDays(now.getTime() - date.getTime());
        if(units != 0) {
            return units + " days ago";
        }
        units = TimeUnit.MILLISECONDS.toHours(now.getTime() - date.getTime());
        if(units != 0) {
            return units + " hours ago";
        }
        units = TimeUnit.MILLISECONDS.toMinutes(now.getTime() - date.getTime());
        return units + " minutes ago";
    }
    
     /**
     * Generates a feedback message to be displayed in the web. Respective xhtml:
     * <h:outputText class="rounded msgError" value="#{flash.keep.errorMessage.summary}" />
     *
     * @param msg error message to be displayed
     */
    public static void generateErrorMessage(String msg) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, ""));
    }
    
        /**
     * Generates an error feedback message based on a localized message from the resource bundle. 
     * 
     * @param key key of the message from the resource bundle
     */
    public static void generateErrorMessageFromBundle(String key) {
        String msg = getStringFromBundle(key);
        generateErrorMessage(msg);
    }
    
    /**
     * Generates a feedback message to be displayed in the web. Respective xhtml:
     * <h:outputText class="rounded msgInfo" value="#{flash.keep.infoMessage.summary}" />
     *
     * @param msg info message to be displayed
     */
    public static void generateInfoMessage(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, msg, ""));
    }
    
    /**
     * Generates an info feedback message based on a localized message from the resource bundle. 
     * 
     * @param key key of the message from the resource bundle
     */
    public static void generateInfoMessageFromBundle(String key) {
        String msg = getStringFromBundle(key);
        generateInfoMessage(msg);
    }
}
