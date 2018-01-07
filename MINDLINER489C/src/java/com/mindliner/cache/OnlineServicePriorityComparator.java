/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.cache;

import com.mindliner.serveraccess.OnlineService;
import java.util.Comparator;

/**
 * This class compares two online services using the service's connection
 * priority.
 *
 * @author Marius Messerli
 */
public class OnlineServicePriorityComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        OnlineService s1 = (OnlineService) o1;
        OnlineService s2 = (OnlineService) o2;
        if (s1.getConnectionPriority() < s2.getConnectionPriority()) {
            return -1;
        }
        if (s1.getConnectionPriority() == s2.getConnectionPriority()) {
            return 0;
        }
        return 1;
    }
}
