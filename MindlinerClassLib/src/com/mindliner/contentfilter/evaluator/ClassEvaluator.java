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
 * @author marius
 */
public class ClassEvaluator implements ObjectEvaluator, Serializable {

    public ClassEvaluator(Class c) {
        qualificationClass = c;
    }
    
    private Class qualificationClass = null;

    @Override
    public boolean passesEvaluation(mlsObject o) {
        if (qualificationClass == null) return true;
        if (qualificationClass.equals(o.getClass())) return true;
        return false;
    }

    @Override
    public boolean isMultipleInstancesAllowed() {
        return false;
    }
    
    private static final long serialVersionUID = 19640205L;

}
