/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.comparatorsS;

import com.mindliner.objects.transfer.mltUser;
import java.util.Comparator;

/**
 *
 * @author marius
 */
public class MtlLoginCountComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        mltUser u1 = (mltUser) o1;
        mltUser u2 = (mltUser) o2;
        if (u1.getLoginCount() > u2.getLoginCount()) return 1;
        else if (u1.getLoginCount() == u2.getLoginCount()) return 0;
        return -1;
    }

}
