/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.comparatorsS;

import com.mindliner.entities.mlsUser;
import java.util.Comparator;
import java.util.Date;

/**
 *
 * Compares two login dates giving the more recent date more weight.
 *
 * @author Marius Messerli
 */
public class UserLastSeenComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        mlsUser u1 = (mlsUser) o1;
        mlsUser u2 = (mlsUser) o2;
        Date d1 = u1.getLastSeen();
        Date d2 = u2.getLastSeen();
        if (d1 == null) {
            if (d2 == null) {
                return 0;
            } else {
                return 1;
            }
        } else {
            if (d2 == null) {
                return -1;
            } else {
                return d1.compareTo(d2);
            }
        }
    }
}
