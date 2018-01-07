/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.mindliner.cache;

import com.mindliner.clientobjects.MlcImage;
import com.mindliner.clientobjects.MlcLink;
import com.mindliner.clientobjects.mlcClient;
import com.mindliner.clientobjects.mlcNews;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcObjectCollection;
import com.mindliner.clientobjects.mlcTask;
import com.mindliner.contentfilter.mlFilterTO;
import com.mindliner.exceptions.ForeignOwnerException;
import com.mindliner.exceptions.IsOwnerException;
import com.mindliner.exceptions.SubCollectionExtractionException;
import com.mindliner.image.LazyImage;
import com.mindliner.objects.transfer.MltLog;
import com.mindliner.serveraccess.StatusReporter;
import java.awt.Image;
import java.util.List;

/**
 *
 * @author M.Messerli
 */
public interface ObjectCache {

    /**
     * This function retrieves a single object from the cache. If no object with
     * the specified key exists in the cache then a server lookup is performed.
     *
     * @param key The id of the object
     * @return The object or null if none was found.
     * @throws com.mindliner.cache.MlCacheException
     */
    mlcObject getObject(int key) throws MlCacheException;

    /**
     * Fetches an object from the server and re-populates the cache with the
     * updated version of it. You need a sub-sequent call to getObject to
     * retreive it.
     *
     * @param key
     * @throws com.mindliner.cache.MlCacheException
     */
    void fetchObject(int key) throws MlCacheException;

    /**
     * Returns objects that are linked to the specified object.
     *
     * @param o
     * @return A list of zero or more objects that are linked to the specified
     * object and accessible by the caller.
     * @throws com.mindliner.cache.MlCacheException
     *
     */
    List<mlcObject> getLinkedObjects(mlcObject o) throws MlCacheException;

    /**
     * Loads the immediate relatives for the specified holders into cache. This
     * call is more performance than calling getLinkedObjects(mlcObject o)
     * multiple times. The links are then retrieved from cache with
     * getLinkedObjects(mlcObject o).
     *
     * @param holders The objects for which linked objects are requested.
     * @throws com.mindliner.cache.MlCacheException
     */
    void loadLinkedObjects(List<mlcObject> holders) throws MlCacheException;


    /**
     * Updates the cache link sets for the specified object.
     *
     * @param key The id of the mindliner object for which the link is to be
     * @param force if true, forces a server update. Otherwise it wont ask
     * server if we already have links cached for object <code>key<code>
     * @throws MlCacheException
     */
    public void updateLinks(int key, boolean force) throws MlCacheException;

    /**
     * Loads the linkes for multile specified keys into cach in a single server
     * call.
     *
     * @param force
     * @see updateLinks(int key)
     * @param keys
     * @throws MlCacheException
     */
    public void updateLinks(List<Integer> keys, boolean force) throws MlCacheException;

    public void removeRelative(mlcObject o, mlcObject relative, boolean isOneWay);

    /**
     * This function adds the specified relatives to the map.
     *
     * @param o The object who's relatives are being updated
     * @param relatives A list of new relatives of o
     * @param isOneWay
     */
    public void addRelative(mlcObject o, mlcObject relatives, boolean isOneWay);

    /**
     * Links two objects and adds the link to relationship maps.
     *
     * @param o1
     * @param o2
     */
    public void linkObjects(mlcObject o1, mlcObject o2);

    public int getLinkCount();

    /**
     * This function returns a list of objects specified by their IDs. Objects
     * are returned from cache if possible and only fetched from the server when
     * missing.
     *
     * @param ids
     * @return A list of expanded objectcs.
     * @throws com.mindliner.cache.MlCacheException
     */
    public List<mlcObject> getObjects(List<Integer> ids) throws MlCacheException;

    /**
     * Contacts the server to search for objects matching the specified class
     * and containing the specified search string.
     *
     * @param searchString The search string who's words must be present in
     * either the headline or description in any order
     * @param fto
     * @return A list of objects satisfying the criteria and modulated by the
     * settings of the search filter
     * @throws MlCacheException
     */
    public List<mlcObject> getPrimarySearchHitsP(String searchString, mlFilterTO fto) throws MlCacheException;

    /**
     * Set a status reporter for proress reports.
     *
     * @param sr The status reporter to report progress.
     */
    void setStatusReporter(StatusReporter sr);

    /**
     * Injects a reference to the category cache manager.
     *
     * @param categoryCache A reference to the category cache subsystem.
     */
    void setCategoryCache(CategoryCache categoryCache);

    /**
     * This call is only used by the creation command to store new objects.
     * Needs to be refactored...
     *
     * @deprecated
     * @param o
     */
    void addToCache(mlcObject o);

    public int getObjectCount();

    void removeObject(mlcObject object) throws ForeignOwnerException, IsOwnerException;

    void removeObjects(List<mlcObject> object) throws ForeignOwnerException, IsOwnerException;

    void removeObjectFromCache(mlcObject object);

    public mlcClient getClient(int clientId);

    /**
     * Returns all icons belonging to the caller's client
     *
     * @param dataPool The data pool for which the clients are needed
     * @return
     */
    public List<MlcImage> getIcons(mlcClient dataPool);

    /**
     * Returns the specified icons
     *
     * @param iconIds
     * @return
     */
    public List<MlcImage> getIcons(List<Integer> iconIds);

    /**
     * Returns the link object for an owner and a relative. Returns null if
     * there is no relation between them.
     *
     * @param ownerId
     * @param relativeId
     * @return
     */
    public MlcLink getLink(int ownerId, int relativeId);

    /**
     * Returns the links of an object id
     *
     * @param ownerId
     * @return
     */
    public List<MlcLink> getLinks(int ownerId);

    /**
     * Updates all links having oldId as relativeId with the id from newObj
     *
     * @param oldId
     * @param newObj
     */
    public void replaceObject(int oldId, mlcObject newObj);

    /**
     * Returns immediately a LazyImage object which will eventually contain the
     * downloaded image from <code>imageUrl</code>
     *
     * @param imageUrl
     * @return
     */
    public LazyImage getImageAsync(MlcImage imageUrl);

    /**
     * Causes a currentness check the next time the image is accessed
     *
     * @param imageId
     */
    public void invalidateImage(int imageId);

    /**
     * Adds an image to the image cache
     *
     * @param imageId
     * @param img
     */
    public void putImage(int imageId, Image img);

    /**
     * Blocks until image is read from cache (might need to read from disk)
     *
     * @param imageId
     * @return The cached image or null if image not cached
     */
    public Image getImageSync(int imageId);

    /**
     * Returns the minimum and the maximum rating for all objects in the cache
     *
     * @return
     */
    public double[] getRatingMinMax();

    /**
     * Returns all cached tasks belonging to the caller
     * @return 
     */
    public List<mlcTask> getMyTasks();
    
    /**
     * Produces a list of current news for the caller
     * @return 
     */
    public List<mlcNews> getNews();
    
    public List<MltLog> getLog(List<Integer> keys);
    
    /**
     * Removes all objects and all links from the cache
     */
    public void clearObjectAndLinkCache();
    
    /**
     * Return a list of islands (aka sets) of linked objects
     * @param minimumIslandSize
     * @param maximumResultcount
     * @return 
     */
    public List<mlcObject> getIslandPeaks(int minimumIslandSize, int maximumResultcount);
    
    /**
     * Takes a node with too many children and splits its children out into sub-collections
     * @param rootCollection The original collection that needs to be split up
     * @throws com.mindliner.exceptions.SubCollectionExtractionException Thrown if the sub-collections cannot be created
     */
    public void createSubCollections(mlcObjectCollection rootCollection, int maxChildCount) throws SubCollectionExtractionException;
        
}
