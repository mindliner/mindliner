/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.comparatorsS;

import com.mindliner.objects.transfer.MltLink;
import java.util.Comparator;

/**
 * Compares MltLinks
 * @author marius
 * @see ListPositionComparator for MlsLinks
 */
public class LinkPositionComparatorTransfer implements Comparator{

    @Override
    public int compare(Object o1, Object o2) {
        MltLink l1 = (MltLink) o1;
        MltLink l2 = (MltLink) o2;
        if (l1.getRelativeListPosition() < l2.getRelativeListPosition()) {
            return -1;
        }
        if (l1.getRelativeListPosition() == l2.getRelativeListPosition()) {
            return 0;
        }
        return 1;
    }
    
}
