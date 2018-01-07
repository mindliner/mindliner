/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.positioner;

import java.util.Comparator;
import com.mindliner.clientobjects.MlMapNode;

/**
 *
 * @author M.Messerli Created on 23.09.2012, 22:39:41
 */
public class NodePositionXComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        MlMapNode n1 = (MlMapNode) o1;
        MlMapNode n2 = (MlMapNode) o2;
        if (n1.getPosition().getX() < n2.getPosition().getX()) {
            return -1;
        }
        if (n1.getPosition().getX() == n2.getPosition().getX()) {
            return 0;
        }
        return 1;
    }
}
