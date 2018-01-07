/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.comparatorsS;

import com.mindliner.entities.mlsObject;
import java.util.Comparator;

/**
 *
 * Compares two
 * @author marius
 */
public class mlsRatingComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        mlsObject b1 = (mlsObject) o1;
        mlsObject b2 = (mlsObject) o2;
        if (b1.getRating() < b2.getRating()) return -1;
        else if (b1.getRating() > b2.getRating()) return 1;
        else return 0;
    }

}
