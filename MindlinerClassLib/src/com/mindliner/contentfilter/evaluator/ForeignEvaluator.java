/*
 * ForeignEvaluator.java
 * 
 * Created on 19.06.2007, 23:01:09
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.mindliner.contentfilter.evaluator;

import com.mindliner.contentfilter.ObjectEvaluator;
import com.mindliner.entities.mlsContact;
import com.mindliner.entities.mlsObject;
import com.mindliner.entities.mlsUser;
import java.io.Serializable;
import java.util.List;

/**
 * Class handles the exclusion of foreign objects depending on the filter
 * selection.
 *
 * @author Marius Messerli
 */
public class ForeignEvaluator implements ObjectEvaluator, Serializable {

    private List<Integer> ownerIds;
    private static final long serialVersionUID = 19640205L;

    public ForeignEvaluator(List<Integer> ownerIds) {
        this.ownerIds = ownerIds;
    }

    @Override
    public boolean passesEvaluation(mlsObject o) {
        if (ownerIds == null || ownerIds.isEmpty()) {
            return true;
        }

        mlsUser c = o.getOwner();
        if (c == null) {
            return false;
        }
        if (ownerIds.contains(c.getId())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isMultipleInstancesAllowed() {
        return false;
    }
}
