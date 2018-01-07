/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.contentfilter.evaluator;

import com.mindliner.contentfilter.ObjectEvaluator;
import com.mindliner.entities.mlsObject;
import java.io.Serializable;

/**
 *
 * @author messerli
 */
public class PrivacyEvaluator implements ObjectEvaluator, Serializable{

    public PrivacyEvaluator(boolean show){
        showPrivate = show;
    }
    
    @Override
    public boolean passesEvaluation(mlsObject o) {
        mlsObject mbo  = (mlsObject) o;
        if (showPrivate == false && mbo.getPrivateAccess() == true) return false;
        return true;
    }
    
    private boolean showPrivate = false;

    @Override
    public boolean isMultipleInstancesAllowed() {
        return false;
    }
    
    private static final long serialVersionUID = 19640205L;

}
