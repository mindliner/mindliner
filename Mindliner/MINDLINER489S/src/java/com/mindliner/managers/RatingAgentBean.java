/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.managers;

import com.mindliner.analysis.RatingEngineFactory;
import com.mindliner.analysis.RatingEngine;
import com.mindliner.analysis.MlsRatingTimerControl;
import com.mindliner.entities.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Timer;
import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author Marius Messerli
 */
@Stateless
@DeclareRoles(value = {"MasterAdmin", "Admin", "User"})
@RolesAllowed(value = "MasterAdmin")
public class RatingAgentBean implements RatingAgentRemote {

    @PersistenceContext
    private EntityManager em;
    @Resource
    TimerService timerService;
    @EJB
    LogManagerRemote logManager;
    @EJB
    UserManagerLocal userManager;
    @EJB
    ObjectFactoryLocal objectFactory;
    @EJB
    ObjectManagerLocal objectManager;

    /**
     * Updates the ratings for all database objects. This function runs against
     * all items for all users and all clients. To avoid update of all objects
     * only those objects are updated who's rating really changes. Because
     * multiple iterations may be required the rates of all objects are stored
     * in a map and only applied if after the final iteration the rate differes
     * from the previous object rate.
     *
     * @param parameters Defines several rating parameters
     */
    @Override
    @RolesAllowed(value = "MasterAdmin")
    public void updateRatings(MlsRatingTimerControl parameters) {
        RatingEngine re = RatingEngineFactory.getRatingEngine(parameters.getEngineType());
        mlsClient c = em.find(mlsClient.class, parameters.getClientId());
        int currentWorkingGeneration = getRatingGeneration(c);
        int updateCount = 1;
        while (updateCount > 0 && getRatingGeneration(c) <= currentWorkingGeneration) {
            updateCount = re.rateAll(c, em, parameters.getInheritanceRate(), parameters.getExpirationDiscount(), parameters.getBatchSize(), currentWorkingGeneration);
            Logger.getLogger(getClass().getName()).log(Level.INFO,
                    "Rating completed for batch: clientId={0}, generation < {1}: {2} objects updated in batch of size {3}",
                    new Object[]{parameters.getClientId(), currentWorkingGeneration, updateCount, parameters.getBatchSize()});
        }
    }

    @Override
    public void createTimer(MlsRatingTimerControl timer) {
        timerService.createTimer(timer.getStart(), timer.getIntervall(), timer);
    }

    /**
     * @todo implement dependancy on batchSize
     */
    @Timeout
    private void runRating(Timer t) {
        MlsRatingTimerControl rt = (MlsRatingTimerControl) t.getInfo();
        updateRatings(rt);
    }

    /**
     * Returns registered timers for this bean.
     *
     * @todo Could not create EJB when the timerService was injected : error
     * message was that Timer Service was not available
     *
     * @return
     */
    @Override
    public List<MlsRatingTimerControl> getTimerControls() {
        List<MlsRatingTimerControl> timerControls = new ArrayList<>();
        Collection timers = timerService.getTimers();
        for (Object o : timers) {
            Timer t = (Timer) o;
            MlsRatingTimerControl rtc = (MlsRatingTimerControl) t.getInfo();
            rtc.setTimer(t.getHandle());
            timerControls.add(rtc);
        }
        return timerControls;
    }

    @Override
    public void cancelTimer(TimerHandle th) {
        Timer t = th.getTimer();
        t.cancel();
    }

    @RolesAllowed(value = {"MasterAdmin", "Admin"})
    @Override
    public void initializeRatingDetails(int clientId) {
        mlsClient client = em.find(mlsClient.class, clientId);
        if (client != null) {
            Query q = em.createQuery("SELECT o FROM mlsObject o WHERE o.client.id = " + client.getId());
            List<mlsObject> olist = q.getResultList();
            for (mlsObject o : olist) {
                if (o.getRatingDetail() != null) {
                    em.remove(o.getRatingDetail());
                }
                mlsRatingDetail rd = new mlsRatingDetail();
                o.setRatingDetail(rd);
                o.setRating(0D);
                em.merge(o);
            }
        }
    }

    private int getRatingGeneration(mlsClient c) {
        Query q = em.createQuery("SELECT MAX(o.ratingDetail.generation) FROM mlsObject o WHERE o.client.id = " + c.getId());
        Integer maxgen = (Integer) q.getSingleResult();
        if (maxgen != null) {
            q = em.createQuery(
                    "SELECT o FROM mlsObject o WHERE o.client.id = " + c.getId()
                    + " AND o.ratingDetail.generation < " + maxgen);
            if (q.getResultList().size() > 0) {
                return maxgen;
            } else {
                return maxgen + 1;
            }
        }
        return 1;
    }

    /**
     * This function attempts to find an object with a non-zero rating. Objects
     * with zero ratings probably were not rated yet.
     *
     * @param start The object to start with
     * @param maxIterations The maximum layers of expansion the search will be
     * performed in.
     * @param searchPathBehind The search path up to this point so that we can
     * avoid walking backwards or in loops
     * @return The result which is either an object with non-zero rating or the
     * start object again
     */
    private mlsObject escapeLocalMaximum(mlsObject start, int currentIteration, int maxIterations, double localMax, List<mlsObject> searchPathBehind) {
        // ensure the start is in the search path otherwise it will loop back when we try to escape from the children
        if (!searchPathBehind.contains(start)) {
            searchPathBehind.add(start);
        }
        if (currentIteration >= maxIterations) {
            return start;
        }
        mlsObject newPeak = null;
        // first check direct relatives of start
        for (mlsObject r : start.getRelatives()) {
            if (!searchPathBehind.contains(r)) {
                if (r.getRating() > localMax) {
                    newPeak = r;
                    localMax = r.getRating();
                }
            }
        }
        if (newPeak != null) {
            return newPeak;
        } else {
            // now expand the search from each of start's relatives
            for (mlsObject r : start.getRelatives()) {
                if (!searchPathBehind.contains(r)) {
                    mlsObject escape = escapeLocalMaximum(r, ++currentIteration, maxIterations, localMax, searchPathBehind);
                    if (!escape.equals(r)) {
                        newPeak = escape;
                    }
                    searchPathBehind.add(r);
                }
            }
        }
        if (newPeak != null) {
            return newPeak;
        } else {
            return start;
        }
    }

    /**
     * Searches for a related object with higher rating avoiding the search path
     * behind
     *
     * @param start The starting object
     * @param pathBehind The search path so far and to be avoided
     * @return A new peak or start if none found
     */
    private mlsObject getNearestNeighborPeak(mlsObject start, List<mlsObject> pathBehind) {
        mlsObject peak = start;
        double currentRating = start.getRating();
        for (mlsObject r : start.getRelatives()) {
            if (!pathBehind.contains(r)) {
                if (r.getRating() > currentRating) {
                    currentRating = r.getRating();
                    peak = r;
                    pathBehind.add(r);
                }
            }
        }
        return peak;
    }

    @RolesAllowed(value = {"MasterAdmin", "Admin", "User"})
    @Override
    public int getFamilyRatingPeak(int objectId, int maxEscapeLevels) {
        mlsObject start = em.find(mlsObject.class, objectId);
        List<mlsObject> successPath = new ArrayList<>();
        successPath.add(start);
        List<mlsObject> searchPath = new ArrayList<>();
        mlsObject currentPeak = start;
        boolean progress = true;
        while (progress) {
            mlsObject neighborPeak = getNearestNeighborPeak(currentPeak, searchPath);
            if (neighborPeak.equals(currentPeak)) {
                // the last argument of the following call is intentional, its not the succesPath
                mlsObject escape = escapeLocalMaximum(currentPeak, 0, maxEscapeLevels, currentPeak.getRating(), searchPath);
                if (escape.equals(currentPeak)) {
                    // try one more time starting from the initial start point as the algorithm may have been lead down the wrong path
                    escape = escapeLocalMaximum(start, 0, maxEscapeLevels, currentPeak.getRating(), searchPath);
                    if (!escape.equals(start)) {
                        currentPeak = escape;
                        successPath.add(escape);
                    } else {
                        progress = false;
                    }
                } else {
                    if (escape.getRating() <= currentPeak.getRating()) {
                        throw new IllegalStateException("The escape function return an object with equal or smaller rating of " + escape.getRating() + " and current rating = " + currentPeak.getRating());
                    }
                    currentPeak = escape;
                    successPath.add(escape);
                }
            } else {
                currentPeak = neighborPeak;
                successPath.add(neighborPeak);
            }
        }
        return currentPeak.getId();
    }

    @Override
    public void deleteRatingSet(final int setId) {
        RatingParamSet set = em.find(RatingParamSet.class, setId);
        if (set != null) {
            em.remove(set);
        }
    }

    @Override
    public int createNewSet(RatingParamSet set) {
        em.persist(set);
        em.flush();
        return set.getId();
    }

    @Override
    public void setActiveState(final int setId, final boolean state) {
        RatingParamSet set = em.find(RatingParamSet.class, setId);
        if (set != null) {
            set.setActive(state);
        }
    }

    @Override
    public RatingParamSet updateSet(RatingParamSet set) {
        em.merge(set);
        return set;
    }

}
