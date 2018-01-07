/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.comparatorsS;

import com.mindliner.entities.MlsLink;
import java.util.Comparator;

/**
 * Compares the relative position of the links
 *
 * @author Marius Messerli
 */
public class LinkPositionComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        MlsLink l1 = (MlsLink) o1;
        MlsLink l2 = (MlsLink) o2;
        if (l1.getRelativeListPosition() < l2.getRelativeListPosition()) {
            return -1;
        }
        if (l1.getRelativeListPosition() == l2.getRelativeListPosition()) {
            return 0;
        }
        return 1;
    }

}
