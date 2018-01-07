/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.analysis;

import com.mindliner.entities.mlsObject;

/**
 * @author Marius Messerli
 */
public class ObjectSimilariy {

    private mlsObject object;
    private Double similarity;

    public ObjectSimilariy(mlsObject object, Double similarity) {
        this.object = object;
        this.similarity = similarity;
    }

    public mlsObject getObject() {
        return object;
    }

    public Double getSimilarity() {
        return similarity;
    }
}
