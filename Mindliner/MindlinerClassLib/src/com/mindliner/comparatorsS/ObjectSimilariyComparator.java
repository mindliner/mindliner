/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.comparatorsS;

import com.mindliner.analysis.ObjectSimilariy;
import java.util.Comparator;

/**
 *
 * @author Marius Messerli
 */
public class ObjectSimilariyComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        ObjectSimilariy s1 = (ObjectSimilariy) o1;
        ObjectSimilariy s2 = (ObjectSimilariy) o2;
        if (s1.getSimilarity() < s2.getSimilarity()) {
            return -1;
        }
        if (s1.getSimilarity() > s2.getSimilarity()) {
            return 1;
        }
        return 0;
    }
}
