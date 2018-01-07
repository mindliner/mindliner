/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import java.util.List;
import javax.ejb.Remote;

/**
 *
 * @author Marius Messerli
 */
@Remote
public interface SubscriptionManagerRemote {
    
        /**
     * Returns the news for the calling user.
     * @return The ids of active (non-archived) news for the calling user
     */
    public List<Integer> getNewsIds();
    
    /**
     * Sets the archive flag for the specified news record
     * @param newsId The id of the news record which should be archived; if id does not exist the call returns without side effects
     */
    public void archiveNewsArticle(int newsId);
    
}
