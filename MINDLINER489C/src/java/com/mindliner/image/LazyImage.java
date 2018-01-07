/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.image;

import com.mindliner.analysis.MlClassHandler;
import com.mindliner.img.icons.MlIconManager;
import java.awt.Image;

/**
 * Simple class that returns a default image until the real image is available.
 * 
 * @author Dominic Plangger
 */
public class LazyImage {
    
    private Image defaultImage;
    private Image image = null;
    private boolean hasError = false;
    private CompletionHandler handler = null;

    public LazyImage(Image defaultImage) {
        this.defaultImage = defaultImage;
    }
    
    public LazyImage() {
        this.defaultImage = MlIconManager.getImageForType(MlClassHandler.MindlinerObjectType.Image);
    }

    public synchronized void setImage(Image image) {
        this.image = image;
        if (handler != null) {
            handler.completed();
        }
    }

    public synchronized Image getImage() {
        if (hasError) {
            return MlIconManager.getErrorImage();
        }
        
        if (image == null) {
            return defaultImage;
        }
        
        return image;
    }

    public synchronized boolean isHasError() {
        return hasError;
    }

    public synchronized void setHasError(boolean hasError) {
        this.hasError = hasError;
        if (hasError && handler != null) {
            handler.completed();
        }
    }
    
    public void registerCompletionHandler(CompletionHandler handler) {
        this.handler = handler;
    }
    
    public interface CompletionHandler {
        
        public void completed();
        
    }
    
}
