/*
 * WorkActionEvaluator.java
 * 
 * Created on 05.07.2007, 13:50:57
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.contentfilter.evaluator;

import com.mindliner.categories.MlsNewsType;
import com.mindliner.contentfilter.ObjectEvaluator;
import com.mindliner.entities.MlsNews;
import com.mindliner.entities.mlsObject;
import java.io.Serializable;

/**
 *
 * @author messerli
 */
public class NewsTypeEvaluator implements ObjectEvaluator, Serializable {

    public NewsTypeEvaluator(MlsNewsType action) {
        type = action;
    }

    @Override
    public boolean passesEvaluation(mlsObject o) {
        if (o == null) {
            throw new IllegalStateException("Cannot evaluate null object.");
        }
        if (o instanceof MlsNews) {
            MlsNews n = (MlsNews) o;
            return n.getNewsType().equals(type);
        }
        return true;
    }
    MlsNewsType type = null;

    @Override
    public boolean isMultipleInstancesAllowed() {
        return false;
    }
    
    private static final long serialVersionUID = 19640205L;
}
