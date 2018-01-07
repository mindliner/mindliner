/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.contentfilter.evaluator;

import com.mindliner.contentfilter.ObjectEvaluator;
import com.mindliner.entities.mlsObject;
import com.mindliner.entities.mlsClient;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

/**
 * Rejects elements that do not belong to the specified client.
 *
 * @author Marius Messerli
 */
public class ClientEvaluator implements ObjectEvaluator, Serializable {

    Collection<mlsClient> clients = new HashSet<>();
    private static final long serialVersionUID = 19640205L;

    public ClientEvaluator(Collection<mlsClient> clients) {
        this.clients = clients;
    }

    @Override
    public boolean passesEvaluation(mlsObject o) {
        return clients.contains(o.getClient());
    }

    @Override
    public boolean isMultipleInstancesAllowed() {
        return false;
    }

}
