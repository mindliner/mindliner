/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.exporter.html;

import com.mindliner.clientobjects.mlcObject;
import java.text.SimpleDateFormat;
import com.mindliner.clientobjects.MlMapNode;

/**
 *
 * @author M.Messerli Created on 26.08.2012, 10:43:24
 */
public class NodeFormatter {

    protected static SimpleDateFormat simpleDateFormatter = new SimpleDateFormat();

    /**
     * This function recursively formats the specified and all expanded children
     * using the html tags <br>, <ul>, <li>, and their closing counterparts
     * only, so it can easily be concatenated
     * @param node The node to be formatted
     * @param includeId Whether or not to include the id
     * @param includeOwner
     * @param includeModificationDate
     * @param includeDescription
     * @return 
     */
    public String format(MlMapNode node, boolean includeId, boolean includeOwner, 
            boolean includeModificationDate, boolean includeDescription) {
        mlcObject object = node.getObject();
        StringBuilder sb = new StringBuilder();
        sb.append(object.getHeadline()).append("<BR>");
        if (object.isPrivateAccess()) {
            sb.append("PRIVATE ");
        }
        if (includeId) {
            sb.append("id=").append(object.getId()).append(" ");
        }
        if (includeOwner) {
            sb.append("owner=").append(object.getOwner().getLoginName()).append(" ");
        }
        if (includeModificationDate) {
            sb.append("mod=").append(simpleDateFormatter.format(object.getModificationDate()));
        }

        if (includeDescription){
            sb.append("<BR>").append(object.getDescription());
        }
        if (!node.getChildren().isEmpty() && node.isExpanded()) {
            sb.append("<ul>");
            for (MlMapNode child : node.getChildren()) {
                NodeFormatter formatter = FomatterFactory.getFormatter(child.getObject());
                sb.append("<li>").append(formatter.format(child, includeId, includeOwner, includeModificationDate, includeDescription)).append("</li>");
            }
            sb.append("</ul>");
        }
        return sb.toString();
    }

    /**
     * This function formats those attributes that are specific to the node's
     * mindliner object.
     *
     * @param node
     * @return
     */
    public String formatObjectSpecifics(MlMapNode node) {
        return "";
    }
}
