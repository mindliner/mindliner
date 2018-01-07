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
public class MltLastLoginComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        if (o1 == null) {
            return -1;
        }
        if (o2 == null) {
            return 1;
        }

        mltUser u1 = (mltUser) o1;
        mltUser u2 = (mltUser) o2;

        if (u1.getLastLogin() == null) {
            return -1;
        }
        if (u2.getLastLogin() == null) {
            return 1;
        }

        return u1.getLastLogin().compareTo(u2.getLastLogin());
    }
}
