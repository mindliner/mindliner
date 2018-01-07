/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.entities.MlUserPreferences;
import com.mindliner.entities.SoftwareFeature;
import com.mindliner.entities.mlsClient;
import com.mindliner.entities.mlsUser;
import com.mindliner.exceptions.NonExistingObjectException;
import com.mindliner.exceptions.UserCreationException;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author Dominic Plangger
 */
@Local
public interface UserManagerLocal {

    public mlsUser getCurrentUser();

    /**
     * Lets the application know that a particular user in currently online.
     */
    public void heartBeat();

    public mlsUser findUser(String login);
    
    public mlsUser findUser(int userId);
    
    public mlsUser findUserByEmail(String email);

    List<mlsClient> getClients();

    List<mlsClient> getOwnedClients();

    /**
     * Returns a single client. The name was chose to avoid a conflict with the
     * same call in the remote interface that returns mltClient.
     *
     * @param clientId The id of the client requested
     * @return The client entity or null if none was found with that id
     */
    mlsClient getOneClient(int clientId);

    public void setActive(int userId, boolean state);

    public void deleteClient(mlsClient c);

    void addFeatureAuthorization(int userId, int featureId) throws NonExistingObjectException;

    void removeFeatureAuthorization(int userId, int featureId) throws NonExistingObjectException;

    /**
     * Evaluates if the specified username is still available
     *
     * @param username The prefered user name
     * @return If the username is still available it is returned, otherwise a
     * close and available suggestion is returned
     */
    public String isUsernameAvailable(String username);

    /**
     * Creates a new Mindliner user account
     *
     * @param username
     * @param firstName
     * @param lastName
     * @param email
     * @param initialDataPool The first data pool for the new user. Specify null
     * to just create the user account. Ignored if
     * initialDataPoolMaxConfidentiality is null
     * @param initialDataPoolMaxConfidentiality The maximum confidentiality
     * clearance this user gets in the initial data pool; ignored if
     * initialDataPool is null
     * @param encryptedPassword The password
     * @return
     * @throws UserCreationException
     */
    public mlsUser createUser(String username, String firstName, String lastName, String email, mlsClient initialDataPool, mlsConfidentiality initialDataPoolMaxConfidentiality, String encryptedPassword) throws UserCreationException;

    /**
     * Determines if the currently authenticated user is in the specified role.
     *
     * @param role The role against which the current user is to be probed.
     * @return True if the current user is in the specified role, false
     * otherwise.
     */
    public boolean isInRole(String role);

    /**
     * This call updates existing user defaults or creates a new record if no previous record exists
     * @param userId The user
     * @param dataPoolId The data pool
     * @param privateflag
     * @param priorityId
     * @param confidentialityId The confidentiality for the specified data pool; confi defaults for other data pools which won't be affected if such exist
     */
    public void updateObjectDefaults(int userId, int dataPoolId, boolean privateflag, int priorityId, int confidentialityId);
    
    /**
     * Returns the object defaults for the specified user
     *
     * @param userId
     * @return The defaults with all relations instantiated
     */
    public MlUserPreferences getUserPreferences(int userId);
    
     /**
     *
     * Creates a newly signed up user, hence inactive.
     * Must be manually set active.
     * 
     * @param login The login name chosen by the user.
     * @param firstName The user's first name.
     * @param lastName The user's last name.
     * @param email The user's email.
     * @param encryptedPassword The password chosen by the user.
     * @return
     * @throws UserCreationException 
     */
    public mlsUser createUnconfirmedUser(String login, String firstName, String lastName, String email, String encryptedPassword) throws UserCreationException;
    
    /**
     * Deletes an unconfirmed user
     * @param user user to be deleted
     */
    public void deleteUnconfirmedUser(mlsUser user);
    
    /**
     * This call updates the password for the specified user. This call
     * requires the caller to be in the MasterAdmin roll.
     * 
     * @param userId The user's id
     * @param encodedPassword The SHA-256 base64 encrypted and encoded password
     */
    public void updatePassword(int userId, String encodedPassword);

    /**
     * 
     * @return all users in the unconfirmed group
     */
    public List<mlsUser> getUnconfirmedUsers();
    
    /**
     * 
     * Precondition: A user who belongs to the unconfirmed group only
     * Postcondition: A user who belongs to the user group only
     * 
     * @param login the user's username for identification
     * @return true if the confirmation was successful
     */
    public boolean confirmUser(String login);
    
    /**
     * Determins whether the calling user is authorized to use the specified software feature
     * @param feature The feature which is being inquired
     * @return True if the calling user is authorized to use the specified feature, false otherwise
     */
    public boolean isAuthorized(SoftwareFeature.CurrentFeatures feature);

    /**
     * Updates the time of the last news digest was delivered to the current time
     * @param userId The user for which the last delivery time needs to be set
     */
    void updateLastNewsDeliveryDigest(int userId);

    /**
     * Feature only for master admins
     * @return All users in the system
     */
    public List<mlsUser> getAllUsers();
}
