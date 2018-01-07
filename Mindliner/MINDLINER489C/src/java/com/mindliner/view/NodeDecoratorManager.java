/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view;

import com.mindliner.clientobjects.MlMapNode;

/**
 *
 * @author Marius Messerli
 */
public class NodeDecoratorManager {

    public static MlMapNode getOutermostDecorator(MlMapNode node) {
        if (node.getDecorator() != null) {
            return getOutermostDecorator(node.getDecorator());
        } else {
            return node;
        }
    }

}
