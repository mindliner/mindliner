/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.exporter.html;

import com.mindliner.clientobjects.mlcTask;
import com.mindliner.clientobjects.MlMapNode;

/**
 *
 * @author M.Messerli Created on 26.08.2012, 10:58:43
 */
public class TaskFormatter extends NodeFormatter {

    @Override
    public String formatObjectSpecifics(MlMapNode node) {
        mlcTask t = (mlcTask) node.getObject();
        StringBuilder sb = new StringBuilder();
        String dueDateString;
        if (t.getDueDate() == null) {
            dueDateString = "none";
        } else {
            dueDateString = simpleDateFormatter.format(t.getDueDate());
        }
        sb.append("due=").append(dueDateString).append(" prio=").append(t.getPriority().getName());
        return sb.toString();
    }
}
