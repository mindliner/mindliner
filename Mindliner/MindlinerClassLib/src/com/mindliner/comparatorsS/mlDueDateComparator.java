/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.comparatorsS;

import com.mindliner.contentfilter.Timed;
import java.util.Comparator;

/**
 *
 * @author messerli
 */
public class mlDueDateComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        if (o1 instanceof Timed && o2 instanceof Timed){
            Timed t1 = (Timed) o1;
            Timed t2 = (Timed) o2;
            return t1.getDueDate().compareTo(t2.getDueDate());
        }
        return 0;
    }

}
