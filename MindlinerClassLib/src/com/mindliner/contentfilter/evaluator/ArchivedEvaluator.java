/*
 * ExpirationEvaluator.java
 * 
 * Created on 19.06.2007, 22:29:01
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.mindliner.contentfilter.evaluator;

import com.mindliner.contentfilter.ObjectEvaluator;
import com.mindliner.entities.mlsObject;
import java.io.Serializable;

/**
 *
 * @author Marius Messerli
 */
public class ArchivedEvaluator implements ObjectEvaluator, Serializable {

    private static final long serialVersionUID = 19640205L;
    private boolean showArchived = false;

    public ArchivedEvaluator(boolean showARchived) {
        this.showArchived = showARchived;
    }

    @Override
    public boolean passesEvaluation(mlsObject o) {
        return showArchived == true || o.isArchived()== false;
    }

    @Override
    public boolean isMultipleInstancesAllowed() {
        return false;
    }

}
