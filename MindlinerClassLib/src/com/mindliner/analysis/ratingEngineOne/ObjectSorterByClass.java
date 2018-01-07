/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.analysis.ratingEngineOne;

import com.mindliner.entities.*;
import java.util.Comparator;

/**
 * This class sorts ML objects for rating in such a way that the objects that
 * are likely used as folders or collections rank higher than the typical
 * stand-alone or annotation items.
 *
 * @author Marius Messerli
 */
public class ObjectSorterByClass implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        if (!(o1 instanceof mlsObject && o2 instanceof mlsObject)) {
            throw new IllegalArgumentException("ObjectSorter called with non-mindliner object as argument");
        }
        if (getClassRating(o1) == getClassRating(o2)) {
            return 0;
        }
        if (getClassRating(o1) < getClassRating(o2)) {
            return -1;
        }
        return 1;
    }

    private int getClassRating(Object o) {

        if (o instanceof MlsNews) {
            return 0;
        } else if (o instanceof MlsImage) {
            return 1;
        } else if (o instanceof mlsKnowlet) {
            return 2;
        } else if (o instanceof mlsTask) {
            return 3;
        } else if (o instanceof mlsContact) {
            return 4;
        } else if (o instanceof mlsObjectCollection) {
            return 5;
        } else if (o instanceof MlsContainer) {
            return 6;
        } else if (o instanceof MlsContainerMap) {
            return 7;
        } else {
            throw new IllegalStateException("Don't know rating for class " + o.getClass().getName());
        }
    }
}
