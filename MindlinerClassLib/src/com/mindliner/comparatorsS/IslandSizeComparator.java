/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.comparatorsS;

import com.mindliner.entities.Island;
import java.util.Comparator;

/**
 * Compares islands based on their size, i.e. number of objetcs.
 *
 * @author Marius Messerli
 */
public class IslandSizeComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        if (!(o1 instanceof Island && o2 instanceof Island)) {
            throw new IllegalArgumentException("Comparator requires arguments of class Island");
        }
        Island i1 = (Island) o1;
        Island i2 = (Island) o2;
        if (i1.getObjects().size() < i2.getObjects().size()) {
            return -1;
        }
        if (i1.getObjects().size() == i2.getObjects().size()) {
            return 0;
        }
        return 1;
    }

}
