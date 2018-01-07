/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.synchronization.foreign;

import java.util.Date;

/**
 * An abstract representation of a remote synch item.
 * @author Marius Messerli
 */
public abstract class ForeignObject {
        
    public abstract String getId();
    
    public abstract String getHeadline();
    
    public abstract void setHeadline(String headline);
    
    public abstract String getDescription();
    
    public abstract void setDescription(String description);
    
    public abstract Date getModificationDate();
    
    public abstract Date getCreationDate();
    
    public abstract boolean isPrivate();
    
    public abstract void setPrivacyFlag(boolean privacy);
    
    public abstract void setOwnerName(String name);
    
    public abstract String getOwnerName();
    
    // implemented for MS Outlook
    public abstract void save();
    
    // implemented for MS Outlook
    public abstract void delete();
    
    public abstract void setCategory(String category);
    
    public abstract String getCategory();
    
    /**
     * For tasks this means that it is completed. For Knowlets and Collections
     * this means their lifetime has expired.
     * @return 
     */
    public abstract boolean isCompleted();
}
