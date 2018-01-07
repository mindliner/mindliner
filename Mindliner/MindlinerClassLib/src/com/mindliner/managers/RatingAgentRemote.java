/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.analysis.MlsRatingTimerControl;
import com.mindliner.entities.RatingParamSet;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.TimerHandle;

/**
 *
 * @author Marius Messerli
 */
@Remote
public interface RatingAgentRemote {

    /**
     * Updates the ratings of all mindliner objects. Since the rating of one
     * object depends on other object's rating we need more than one pass.
     * Although Mindliner is not strictly hierarchical there is a top down
     * structure. Projects are at the top, Milestones in the middle, and tasks
     * at the bottom of the project side of things. Therefore this function
     * takes a parameter describing how parents influence their children.
     *
     * @param parameters defines all the parameters for the rating run
     */
    public void updateRatings(MlsRatingTimerControl parameters);

    /**
     * Sets all the rating details to zero, meaning that all the objects are
     * deemed unrated.
     *
     * @param clientId The client for which the object rating are to be
     * initialized
     */
    public void initializeRatingDetails(int clientId);


    public void createTimer(MlsRatingTimerControl timer);

    public List<MlsRatingTimerControl> getTimerControls();

    public void cancelTimer(TimerHandle th);

    /**
     * This function returns the object with the peak rating in the extended
     * family network. The peak is found by recursively follow relatives of the
     * specified object until none of the relatives has a higher rating than the
     * parent.
     *
     * To escape potential local maximas the search is extended beyond the maxim
     * found by no more than the specified number of levels.
     *
     * @param objectId The id of the object for which the root is
     * @param maxEscapeLevels The number of look-ahead neighborhood levels the
     * function uses to escape a potential local maximum. Specify 0 to stop with
     * the first local maximum.
     * @return The id of the object with the highest rating along the path of
     * constant rating accent.
     */
    @RolesAllowed(value = {"User", "Admin", "MasterAdmin"})
    public int getFamilyRatingPeak(int objectId, int maxEscapeLevels);

    /**
     * Deletes the specified rating set with all its parameters
     *
     * @param setId
     */
    void deleteRatingSet(final int setId);

    /**
     * Perists the specified rating set, provided all essential parameters are
     * specified.
     *
     * @param set The set to be persisted
     * @return The id of the new set
     */
    int createNewSet(RatingParamSet set);

    /**
     * Updates the active flag for the specified parameter set. For each client
     * there should be exactly one active set. If there is none the rating
     * cannot run. If there are multiple the first one will be taken.
     *
     * @param setId The id of the set to be updated.
     * @param state True if this set is to be the active one, false otherwise
     */
    void setActiveState(final int setId, final boolean state);

    RatingParamSet updateSet(RatingParamSet set);

}
