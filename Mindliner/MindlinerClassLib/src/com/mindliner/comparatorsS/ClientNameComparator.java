/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.comparatorsS;

import com.mindliner.entities.mlsClient;
import java.util.Comparator;

/**
 * Sorts clients according to their name.
 *
 * @author Marius Messerli
 */
public class ClientNameComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        if (o1 instanceof mlsClient && o2 instanceof mlsClient) {
            mlsClient c1 = (mlsClient) o1;
            mlsClient c2 = (mlsClient) o2;
            return c1.getName().compareTo(c2.getName());
        } else {
            throw new IllegalArgumentException("Both arguments must be of class mlsClient");
        }
    }

}
