/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.clientobjects;

import java.util.Comparator;

/**
 *
 * @author marius
 */
public class mlcRatingComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        mlcObject b1 = (mlcObject) o1;
        mlcObject b2 = (mlcObject) o2;
        if (b2.getRating() < b1.getRating()) return 1;
        else if (b2.getRating() > b1.getRating()) return -1;
        else return 0;
    }
    
}
