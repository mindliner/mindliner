/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view;

import com.mindliner.clientobjects.MlMapNode;
import java.util.Comparator;

/**
 *
 * @author marius
 */
public class NodeChildCountComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        if (o1 == null) {
            if (o2 == null) {
                return 0;
            } else {
                return 1;
            }
        } else if (o2 == null) {
            return -1;
        } else {
            MlMapNode n1 = (MlMapNode) o1;
            MlMapNode n2 = (MlMapNode) o2;
            if (n1.getChildren().size() < n2.getChildren().size()) {
                return -1;
            }
            if (n1.getChildren().size() == n2.getChildren().size()) {
                return 0;
            }
            return 1;
        }
    }

}
