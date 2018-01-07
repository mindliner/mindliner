/*
 * ObjectEvaluator.java
 * 
 * Created on 19.06.2007, 21:36:23
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.mindliner.contentfilter;

import com.mindliner.entities.mlsObject;

/**
 * This interface is presented by objects that can serve as evaluators in
 * object filters.
 * 
 * @author messerli
 */
public interface ObjectEvaluator {
    
    public boolean passesEvaluation(mlsObject o);

    /**
     * Specifies whether multiple instances of this evaluator class is allowed in a filter
     * @return
     */
    public boolean isMultipleInstancesAllowed();

}
