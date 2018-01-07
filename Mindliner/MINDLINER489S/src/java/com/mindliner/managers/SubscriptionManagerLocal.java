/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.categories.MlsEventType.EventType;
import com.mindliner.entities.MlsNews;
import com.mindliner.entities.MlsSubscription;
import com.mindliner.entities.mlsObject;
import com.mindliner.entities.mlsUser;
import com.mindliner.exceptions.ForeignOwnerException;
import com.mindliner.exceptions.NonExistingObjectException;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author Marius Messerli
 */
@Local
public interface SubscriptionManagerLocal {

    /**
     * Creates a new subscription.
     * 
     * @param eventType The event to which to subscribe to or null to subscribe to all events
     * @param object The object to which to subscribe to or null to subscribe to all objects
     * @param actor The actor to whom to subscribe to or null to subscribe to all actors (an actor is another user)
     * @param reverse If true the meaning chanages so that the caller does not want to be informed about these events
     */
    void subscribe(EventType eventType, mlsObject object, mlsUser actor, boolean reverse);
        
    /**
     * Clears all subscriptions for the specified actor
     * @param actor 
     */
    void deleteAllSubscriptions(mlsUser actor);
    
    /**
     * Clears all subscriptions for the specified event
     * @param event 
     */
    void deleteAllSubscriptions(EventType event);
    
    /**
     * Clears all subscriptions for the specified object
     * @param object 
     */
    void deleteAllSubscriptions(mlsObject object);
    
    /**
     * Clears all of the caller's subscriptions
     */
    void deleteAllSubscriptions();
    
    /**
     * Deletes the specified subscription
     * @param id
     * @throws com.mindliner.exceptions.ForeignOwnerException
     */
    void deleteSubscription(int id) throws ForeignOwnerException;
    
    /**
     * Indicates whether the caller has a subscription for the specified object, type, and parameter
     * @param o The object for which the subscription is inquired
     * @param eventType The type (specify Any for any event type)
     * @param actor The actor (specify null for any actor)
     * @return 
     */
    boolean isSubscribed(mlsObject o, EventType eventType, mlsUser actor);
    
    /**
     * Obtain the caller's subscriptions
     * @return The caller's subscriptions
     */
    public List<MlsSubscription> getAllSubscriptions();

    MlsSubscription getSubscription(int id) throws NonExistingObjectException, ForeignOwnerException;

    /**
     * This method matches the log against the user subscriptions and delivers
     * the messages accordingly.
     */
    void deliverSubscriptions();

    /**
     * Returns the news for the calling user.
     * @return The active (non-archived) news for the calling user
     */
    public List<MlsNews> getNews();
    
    /**
     * Sets the archive flag for the specified news record
     * @param news The news record which should be archived
     */
    public void archiveNewsArticle(MlsNews news);
}
