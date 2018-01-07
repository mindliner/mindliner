/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.entities.mlsClient;
import com.mindliner.entities.mlsObject;
import com.mindliner.entities.mlsUser;
import com.mindliner.enums.ObjectCollectionType;
import com.mindliner.enums.ObjectReviewStatus;
import com.mindliner.exceptions.ForeignOwnerException;
import com.mindliner.exceptions.IsOwnerException;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.NonExistingObjectException;
import com.mindliner.exceptions.SubCollectionExtractionException;
import com.mindliner.objects.transfer.MltObject;
import com.mindliner.objects.transfer.ObjectSignature;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.Remote;

/**
 *
 * @author messerli
 */
@Remote
public interface ObjectManagerRemote {

    /**
     * Returns the object with the specified key.
     *
     * @param key The object's Id.
     * @return The object or null if the object does not exist or belongs to a
     * client different from the caller.
     */
    public mlsObject find(int key);

    /**
     * Updates the confidentiality setting for the specified object.
     *
     * @param key The ID of the object that needs to be updated
     * @param confidentialityId The ID of the new confidentiality setting.
     * @return The object's version after the update or -1 if no update was
     * made.
     */
    public int setConfidentiality(int key, int confidentialityId);

    /**
     * Assigns the object to a new data pool. Note: This may expose the object
     * to new users in the new data pool and remove it from some of the users of
     * the current.
     *
     * @param key The object id
     * @param dataPoolId The new data pool id
     * @param confidentialityId The new confidentiality id - when changing a
     * data pool I must also assign a new confidentiality belonging to the new
     * pool
     * @return
     */
    public int setDataPool(int key, int dataPoolId, int confidentialityId);

    public int setHeadline(int key, String headline);

    /**
     * Sets/updates the description of an object
     * @param key The object's ID
     * @param description The new description
     * @return 
     */
    public int setDescription(int key, String description);

    /**
     * This call is intended for importers only. In normal use the modification
     * date is set automatically each time the object is modified.
     *
     * @param key The object's id
     * @param newDate The new modification date
     * @return The object's new version number.
     */
    public int setModificationDate(int key, Date newDate);

    public int setPrivacyFlag(int key, boolean b) throws ForeignOwnerException;

    public int setOwner(int objectKey, int newOwnerId) throws MlAuthorizationException;

    public int setPriority(int key, int priorityId);

    public int setStatus(int key, ObjectReviewStatus status);

    public int setCompletionState(int key, boolean completed);

    public int setArchived(int key, boolean state);

    public int setDueDate(int key, Date d);

    public int setEffortEstimation(int key, int minutes);

    public int setCollectionType(int key, ObjectCollectionType type);

    /**
     * Defines that the relatives of the specified object have a specific order.
     *
     * @param state True if the relatives are to have a specific order, false
     * otherwise (i.e. they are ordered by some attributes)
     * @return The version of the new object
     */
    public int setRelativesOrdered(int key, boolean state);

    public void remove(int key) throws ForeignOwnerException, IsOwnerException;

    public void removeObjects(List<Integer> keys);

    /**
     * Function compile a list of tranfer objects for the specified signatures
     * and forms part of the mindliner caching system that minimizes network
     * traffic.
     *
     * @param keyList The ids of the objects.
     * @return The transfer objects.
     */
    public List<MltObject> getTransferObjects(List<Integer> keyList);

    /**
     * Returns the transfer object for the specified key
     *
     * @param key The id of the object to be retreived.
     * @return The transfer object or null if the object belongs to a client
     * other than one of the caller's clients.
     */
    public MltObject getTransferObject(int key) throws NonExistingObjectException;

    /**
     * This function is identical to getTransferObject except that it does not
     * reject object not belonging to the caller's client and it does not
     * perform any confidentiality checks. However, master admin role is
     * required for this call.
     *
     * @param key The object's id.
     * @return The transfer object for the specified id.
     */
    public MltObject masterAdminGetTransferObject(int key);

    /**
     * Returns the current version of the specified object. This is used to
     * verify the cache actuality.
     *
     * @param key The id of the object.
     * @return The version number.
     */
    public int getVersion(int key);

    /**
     * This function returns the versions of the current objects with the
     * specified IDs.
     *
     * @param objectIds The IDs of the objects for which the versions are
     * required.
     * @return A map with the object id as the key and the object version as the
     * value. If one of the ids specified as the argument does not exist a
     * version of -1 is returned as the value.
     */
    public Map<Integer, Integer> getVersions(Integer[] objectIds);

    /**
     * This method matches the specified signatures against the main database
     * and returns those signatures that specify objects for which a newer
     * version exists.
     */
    public List<Integer> getOutdatedObjectsIds(List<ObjectSignature> inlist);

    /**
     * The bulkSetSomething() functions below set the attribute of a List of
     * objects to one particular value. The return a map with the new object
     * versions so that the caller can keep its cache updated. The return map
     * has the object id as the key and the object version as the value.
     *
     * @param objectIds
     * @param confdentialityId
     * @return
     */
    public Map<Integer, Integer> bulkSetConfidentiality(List<Integer> objectIds, int confdentialityId);

    public Map<Integer, Integer> bulkSetDataPool(List<Integer> objectIds, int dataPoolId, int confidentialityId);

    public Map<Integer, Integer> bulkSetPriority(List<Integer> objectIds, int priorityId);

    public Map<Integer, Integer> bulkSetStatus(List<Integer> objectIds, ObjectReviewStatus status);

    public Map<Integer, Integer> bulkSetOwner(List<Integer> objectIds, int newOwnerId) throws MlAuthorizationException;

    public Map<Integer, Integer> bulkSetPrivacy(List<Integer> objectIds, boolean privacy);

    public Map<Integer, Integer> bulkSetCompletion(List<Integer> objectIds, boolean completed);

    public Map<Integer, Integer> bulkSetArchived(List<Integer> objectIds, boolean state);

    public Map<Integer, Integer> bulkSetDueDate(List<Integer> ids, java.util.Date dueDate);

    public List<mlsUser> getUsersByFirstAndLAstnameSubstrings(String firstname, String lastname, int clientId);

    public int updateContactDetails(int contactId,
            String first, String middle, String last,
            String email, String phoneNumber, String mobileNumber, String description);

    public int updateContactProfilePicture(int contactId, int profilePictureId);

    public MltObject changeObjectType(int id, Class<? extends mlsObject> newType) throws Exception;

    /**
     * Fetches all icon relatives of the given objects
     *
     * @param keys
     * @return A map with the object id's as keys and their icon relatives as
     * values
     */
    public Map<Integer, List<Integer>> getObjectIcons(Set<Integer> keys);

    public mlsClient findDataPool(int dataPoolId);

    public void addSolrFile(byte[] bytes, int attachedObjId);

    /**
     * Takes the specified collection and splits the children up into subgroups.
     * @param collectionID The collection to be split up.
     * @param maxChildCount The maximum number of children per collection
     * @throws SubCollectionExtractionException 
     */
    public void buildSubCollections(int collectionID, int maxChildCount) throws SubCollectionExtractionException;

}
