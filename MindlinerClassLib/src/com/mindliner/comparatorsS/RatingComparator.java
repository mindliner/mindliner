/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.comparatorsS;

import com.mindliner.entities.mlsObject;
import java.util.Comparator;

/**
 *
 * @author M.Messerli
 */
public class RatingComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        mlsObject m1 = (mlsObject) o1;
        mlsObject m2 = (mlsObject) o2;
        if (m1.getRating() < m2.getRating()) {
            return -1;
        } else if (m1.getRating() == m2.getRating()) {
            return 0;
        } else {
            return 1;
        }
    }
}
