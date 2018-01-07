/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.analysis.ratingEngineOne;

import com.mindliner.analysis.MlsObjectRatingAssessor;
import com.mindliner.analysis.RatingEngine;
import com.mindliner.entities.*;
import com.mindliner.exceptions.NonExistingObjectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * @TODO finish class design - load the named params from the specified
 * parameter set id (to be defined)
 * @author Marius Messerli
 */
public class RatingEngineDec08 implements RatingEngine {

    @Override
    public int rateAll(EntityManager em, int clientId, RatingParamSet parameterSet) throws NonExistingObjectException {
        return 0;
    }


    public enum ParamNames {

        INHERITANCE_RATE,
        EXPIRATION_DISCOUNT
    }

    @Override
    public int rateAll(
            mlsClient client, EntityManager em,
            double inheritance,
            double expirationDiscount,
            int batchSize, int generation) {
        Logger myLogger = Logger.getAnonymousLogger();

        Query nq = em.createNamedQuery("mlsObject.getObjectForRatingGeneration");
        nq.setParameter("clientId", client.getId());
        nq.setParameter("ratingGeneration", generation);

        List<mlsObject> objectList = nq.getResultList();
        Collections.sort(objectList, new ObjectSorterByClass());
        List<mlsObject> batch = new ArrayList<>();
        for (int i = 0; i < batchSize && i < objectList.size(); i++) {
            batch.add(objectList.get(i));
        }
        HashMap<Class, MlsObjectRatingAssessor> assessors = buildAssessorMap(inheritance, expirationDiscount);
        for (mlsObject o : batch) {
            MlsObjectRatingAssessor ra = assessors.get(o.getClass());
            if (ra != null) {
                ra.updateRate(o);
            }
        }

        /**
         * now make the changes permanent for those object that were updated we
         * don't want to touch all other objects or else we would unneccessarily
         * invalidate the user's locally cached objects
         */
        int objectsUpdated = 0;
        for (mlsObject o : batch) {
            if (o.getRating() != o.getRatingDetail().getRating()) {
                if (o.getRatingDetail().getRating() < 0) {
                    o.setRating(0);
                    myLogger.log(Level.WARNING, "rating was returned < 0 for object {0}; patched to 0", o.getHeadline());
                } else {
                    o.setRating(o.getRatingDetail().getRating());
                }
                objectsUpdated++;
            }
            o.getRatingDetail().setGeneration(o.getRatingDetail().getGeneration() + 1);
        }
        return objectsUpdated;
    }

    /**
     * @todo Add code for the new objects MlsImage, MlsSpreadsheet, MlsCell, etc
     */
    private HashMap<Class, MlsObjectRatingAssessor> buildAssessorMap(double inheritance, double expirationDiscount) {
        HashMap<Class, MlsObjectRatingAssessor> assessorMap = new HashMap<>();
        AssessorFactory af = new AssessorFactoryDEC08();
        assessorMap.put(mlsTask.class, af.getAssessorForClass(mlsTask.class, inheritance, expirationDiscount));
        assessorMap.put(mlsObjectCollection.class, af.getAssessorForClass(mlsObjectCollection.class, inheritance, expirationDiscount));
        assessorMap.put(mlsKnowlet.class, af.getAssessorForClass(mlsKnowlet.class, inheritance, expirationDiscount));
        assessorMap.put(mlsContact.class, af.getAssessorForClass(mlsContact.class, inheritance, expirationDiscount));
        assessorMap.put(MlsNews.class, af.getAssessorForClass(MlsNews.class, inheritance, expirationDiscount));
        return assessorMap;
    }

}
