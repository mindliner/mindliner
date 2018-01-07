/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.comparatorsS;

import com.mindliner.entities.mlsUser;
import java.util.Comparator;

/**
 * This class compares two users based on their ID.
 * @author Marius Messerli
 */
public class UserIdComparator implements Comparator{

    @Override
    public int compare(Object o1, Object o2) {
        if (!(o1 instanceof mlsUser && o2 instanceof mlsUser)){
            throw new IllegalArgumentException("Expecting both arguments to be an insatnce of mlsUser");
        }
        mlsUser u1 = (mlsUser) o1;
        mlsUser u2 = (mlsUser) o2;
        if (u1.getId() < u2.getId()) return -1;
        if (u2.getId() == u2.getId()) return 0;
        return 1;
    }
    
}
