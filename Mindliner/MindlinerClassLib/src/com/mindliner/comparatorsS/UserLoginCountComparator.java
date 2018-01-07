/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.comparatorsS;

import com.mindliner.entities.mlsUser;
import java.util.Comparator;


/**
 *
 * @author messerli
 */
public class UserLoginCountComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        mlsUser u1 = (mlsUser) o1;
        mlsUser u2 = (mlsUser) o2;
        if (u1.getLoginCount() > u2.getLoginCount()) return -1;
        else if (u1.getLoginCount() == u2.getLoginCount()) return 0;
        return 1;
    }

}
