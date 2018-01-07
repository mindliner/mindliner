/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.comparatorsS;

import com.mindliner.categories.mlsConfidentiality;
import java.util.Comparator;

/**
 *
 * @author Marius Messerli
 */
public class ConfidentialityComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        mlsConfidentiality c1 = (mlsConfidentiality) o1;
        mlsConfidentiality c2 = (mlsConfidentiality) o2;
        if (c1.getClevel() < c2.getClevel()) {
            return -1;
        }
        if (c1.getClevel() == c2.getClevel()) {
            return 0;
        }
        return 1;
    }
}
