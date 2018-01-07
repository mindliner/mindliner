/*
 * ConfidentialityEvaluator.java
 * 
 * Created on 19.06.2007, 21:58:36
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.mindliner.contentfilter.evaluator;

import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.contentfilter.ObjectEvaluator;
import com.mindliner.entities.mlsObject;
import com.mindliner.entities.mlsUser;
import java.io.Serializable;
import java.util.Map;

/**
 * This class evaluates objects based on their confidentiality settings.
 *
 * @author Marius Messerli
 */
public class ConfidentialityEvaluator implements ObjectEvaluator, Serializable {

    private Map<Integer, mlsConfidentiality> highestAllowedConfidentiality;
    private mlsUser currentUser = null;

    /**
     *
     * @param highestAllowedConfidentiality A map with clients ids as keys and
     * confidentialities as values to ensure that no objects with higher confi
     * are found
     * @param u The user for which the objects are to be filtered
     */
    public ConfidentialityEvaluator(Map<Integer, mlsConfidentiality> highestAllowedConfidentiality, mlsUser u) {
        this.highestAllowedConfidentiality = highestAllowedConfidentiality;
        currentUser = u;
    }

    @Override
    public boolean passesEvaluation(mlsObject o) {

        if (o == null) {
            return false;
        }

        // if we have a cap for the object's client then check, otherwise let pass
        mlsConfidentiality maxConf = highestAllowedConfidentiality == null ? null : highestAllowedConfidentiality.get(o.getClient().getId());
        if (maxConf == null) {
            maxConf = currentUser.getMaxConfidentiality(o.getClient());
        }
        if (maxConf == null) {
            throw new IllegalStateException("ConfidentialityEvaluator: Cannot determine confidentiality ceiling.");
        }
        return maxConf.compareTo(o.getConfidentiality()) >= 0;
    }

    @Override
    public boolean isMultipleInstancesAllowed() {
        return false;
    }
    private static final long serialVersionUID = 19640205L;
}
