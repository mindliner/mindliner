/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.analysis.CurrentWorkTask;
import com.mindliner.entities.MlAuthenticationGroups;
import com.mindliner.entities.MlUserPreferences;
import com.mindliner.entities.mlsClient;
import com.mindliner.entities.mlsUser;
import com.mindliner.exceptions.NonExistingObjectException;
import com.mindliner.exceptions.UserCreationException;
import com.mindliner.objects.transfer.MltObjectDefaultConfidentialities;
import com.mindliner.objects.transfer.mltClient;
import com.mindliner.objects.transfer.mltUser;
import java.util.List;
import javax.ejb.Remote;

/**
 *
 * @author Marius Messerli
 */
@Remote
public interface UserManagerRemote {

    public static final int USER_NOT_FOUND = -1;
    public static final int HEARTBEAT_INTERVALL = 10 * 1000; // ms

    /**
     *
     * @param clientName The name of the new client
     * @param adminUser The admin and first user of the new client
     * @return
     */
    public mlsClient createClient(
            String clientName,
            mlsUser adminUser);

    public int getCurrentUserId();

    /**
     * Get a user by login
     *
     * @param login The login of the user to be retrieved
     * @return The user or null if no such user exists.
     */
    public mltUser findUserRemote(String login);

    /**
     * Returns the user with the specified id.
     *
     * @param userId The ID of the requested user.
     * @return The user
     * @throws NonExistingObjectException If no user with the specified ID
     * exists on the server.
     */
    public mltUser getUser(int userId) throws NonExistingObjectException;

    /**
     * Returns the signatures of active users
     *
     * @return
     */
    public List<Integer> getSignaturesOfActiveUsers();

    public List<mltUser> getUsersForClient(int clientId);

    public List<mltClient> getAllClients();

    public mltClient getClient(int clientId);

    public void setActive(int key, boolean active);

    /**
     * Specifies the maximum confidentiality for the specified user.
     *
     * @param userId The id of the user who's confidentiality access is to be
     * changed.
     * @param confId The id of the new confidentiality for the specified user.
     * @param clientId The client for which the max confi is set for this user
     */
    public void setMaxConfidentiality(int userId, int confId, int clientId);

    /**
     * Upadtes the caller's contact details
     */
    public void setContactDetails(String firstName, String lastName, String email);

    /**
     * Determins if the currently authenticated user is in the specified role.
     *
     * @param role The role against which the current user is to be probed.
     * @return True if the current user is in the specified role, false
     * otherwise.
     */
    public boolean isInRole(String role);

    /**
     * Shows the server that this client is still alive.
     */
    public void heartBeat();

    public List<mltUser> getAllLoggedInUsers();

    public mltUser login();

    public mlsUser getCurrentUserRemote() throws javax.persistence.NoResultException;

    /**
     * Adds authorization for the specified software feature to the specified
     * user
     *
     * @param userId The id fo the user for whom the feature is to be added
     * @param featureId The id of the feature to be authorized
     * @throws NonExistingObjectException If either the user or the feature does
     * not exist
     */
    void addFeatureAuthorization(int userId, int featureId) throws NonExistingObjectException;

    void removeFeatureAuthorization(int userId, int featureId) throws NonExistingObjectException;

    /**
     * Completely and irreversibly deletes the specified client and all its
     * users, objects, categories, ratings, etc.
     *
     * @param clientId The id of the client to be deleted from the database
     */
    public void deleteClient(int clientId);

    List<MlAuthenticationGroups> getAllAuthenticationGroups();

    /**
     * This call initializes or updates the user's password and roles. This call
     * is only available to members of the role masteradmin.
     *
     * @param userId
     * @param encodedPassword An SHA-256 base64 encrypted and encoded password.
     * @param roleIds
     */
    public void updateUserAuth(int userId, String encodedPassword, List<Integer> roleIds);

    /**
     * This call updates the caller's password.
     *
     * @param encodedPassword An SHA-256 base64 encrypted and encoded password
     */
    public void updatePassword(String encodedPassword);
    
    /**
     * This call updates the password for the specified user. This call
     * requires the caller to be in the MasterAdmin roll.
     * 
     * @param userId The user's id
     * @param encodedPassword The SHA-256 base64 encrypted and encoded password
     */
    public void updatePassword(int userId, String encodedPassword);

    public MlUserPreferences getDefaults(int userId);

    /**
     * Sets or replaces the object creation defaults for the specified user
     *
     * @param userId The user for which the defaults are to be stored
     * @param clientId The default client
     *
     * @param privateflag The default privacy flag
     * @param priorityId The default priority (tasks only)
     * @param confidentialities A list of default confidentialities
     */
    public void setObjectDefaults(int userId, int clientId, boolean privateflag, int priorityId, List<MltObjectDefaultConfidentialities> confidentialities);
    
    /**
     * Returns the object defaults for the specified user
     * @param userId
     * @return The defaults with all relations instantiated
     */
    public MlUserPreferences getUserPreferences(int userId);

    /**
     * Sets the name of the specified client.
     * @param clientId 
     * @param clientName 
     */
    public void setClientName(int clientId, String clientName);
    
    /**
     * Returns all users that share a datapool with the given user
     * @param userId Id of the given user
     * @return List of users
     */
    public List<mlsUser> getUsersWithSharedDatapool(int userId);
    
    /**
     * Specify the object the caller is currently working on
     * @param taskId The id of the current work task or null to clear the current work task
     */
    public void setCurrentWorkObject(Integer taskId);
    
    /**
     * Returns tasks that users sharing data pools with the caller are working on
     * @return A list of work tasks
     */
    public List<CurrentWorkTask> getCurrentWorkTasks();
}
