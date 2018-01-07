/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.analysis;

import com.mindliner.entities.RatingParamSet;
import com.mindliner.entities.mlsClient;
import com.mindliner.exceptions.NonExistingObjectException;
import javax.persistence.EntityManager;

/**
 *
 * @author Marius Messerli
 */
public interface RatingEngine {

    /**
     * Recalculates the ratings for all database objects and updates if changed.
     *
     * @param c
     * @param em
     * @param inheritance The facor to which the rating from the relatives is
     * adopted into own rating
     * @param expirationDiscount The factor with which the rating of expired
     * objects is divided
     * @param batchSize The number of objects that are to be processed in a
     * single chunk (the larger the chunk size the more resources are required
     * on the server but the faster it goes)
     * @param generation The current rating generation. Only objects who's
     * rating generation is smaller get processed ensuring that objects are
     * maximally separated by one generation (necessary because we cant process
     * all objects in a single run)
     * @return The number of objects that were updated
     * @todo Replace the two parameters by the parameter set id
     */
    public int rateAll(mlsClient c,
            EntityManager em,
            double inheritance,
            double expirationDiscount,
            int batchSize,
            int generation);

    /**
     * Updates rating for all objects of the specified client.
     *
     * @param em
     * @param clientId
     * @param parameterSet
     * @return
     * @throws com.mindliner.exceptions.NonExistingObjectException
     */
    public int rateAll(EntityManager em, int clientId, RatingParamSet parameterSet) throws NonExistingObjectException;
    

}
