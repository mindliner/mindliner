/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.entities.MlAuthorization;
import com.mindliner.entities.mlsClient;
import com.mindliner.entities.mlsObject;
import com.mindliner.entities.mlsUser;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.exceptions.ForeignOwnerException;
import com.mindliner.exceptions.IsOwnerException;
import java.util.List;
import java.util.Map;
import javax.ejb.Local;
import javax.persistence.OptimisticLockException;

/**
 * This is the local interface for functions to manaage objects.
 *
 * @author Marius Messerli
 */
@Local
public interface ObjectManagerLocal {

    public mlsObject findLocal(int key);

    public int getNumberOfAccessibleRelatives(int objectId);

    public void remove(mlsObject object) throws ForeignOwnerException, IsOwnerException;

    public void removeObjects(List<Integer> keys);

    public int setConfidentiality(int objectKey, int confidentialityKey);

    public int setPrivacyFlag(int objectKey, boolean privacyFlag) throws ForeignOwnerException;

    public mlsObject merge(mlsObject o) throws OptimisticLockException;

    /**
     * Removes the specified object without logging or messaging. For normal use
     * call remove(object) instead.
     *
     * @param managedObject The object to be removed, must be managed by the
     * EntityManager
     * @return
     */
    public List<Integer> removeWithoutMessage(mlsObject managedObject);

    public int setHeadline(int key, String headline);

    public int setDescription(int key, String description);

    public mlsObject changeObjectType(mlsObject o, Class<? extends mlsObject> newType) throws Exception;

    /**
     * Enrolls a user in a data pool via an authorization token
     *
     * @param authToken An alpha-numerical authorization token that the invitee
     * has received via email
     * @throws com.mindliner.exceptions.MlAuthorizationException
     * @return Name of the datapool where the user is now enrolled
     *
     */
    public String enrollUser(String authToken) throws MlAuthorizationException;

    /**
     * Creates a data pool invitation
     *
     * @param user the invited user
     * @param datapool the datapool for which the enrollment is requested
     * @param maxConfidentiality maximum confidentiality chosen by the pool
     * admin
     * @param email email of the invitee (can be an unregistered user, too)
     * @return A token with which any data pool admin can enroll this user into
     * their pool
     */
    public String createEnrollmentAuthorization(mlsUser user, mlsClient datapool, mlsConfidentiality maxConfidentiality, String email);

    /**
     * Finds pending (uncompleted) authorization requests for a specified data
     * pool, user and type
     *
     * @param dataPool uncompleted requests for this dataPool
     * @param user uncompleted requests for this user
     * @param type uncompleted requests for this type
     * @return List of pending requests
     */
    public List<MlAuthorization> findPendingRequests(mlsClient dataPool, mlsUser user, MlAuthorization.AuthorizationType type);

    public int setArchived(int key, boolean state);

    public Map<Integer, Integer> bulkSetArchived(List<Integer> objectIds, boolean state);
    
    /**
     * Checks whether the current user is authorized to see or alter an object.
     *
     * @param object to be checked.
     * @return returns false, when the current user is not authorized to
     * see/alter the object
     */
    public boolean isAuthorizedForCurrentUser(mlsObject object);

}
