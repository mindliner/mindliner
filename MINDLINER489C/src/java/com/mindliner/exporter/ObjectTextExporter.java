/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.exporter;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.exporter.html.FomatterFactory;
import com.mindliner.exporter.html.NodeFormatter;
import com.mindliner.gui.tablemodels.HumanReadableClass;
import com.mindliner.clientobjects.MlMapNode;

/**
 * This class exports a map node and all its decendants as formatted HTML text
 * to the clipboard.
 *
 * @author M.Messerli Created on 12.08.2012, 08:51:06
 */
public class ObjectTextExporter {

    /**
     * Returns the full text and description of the mindliner object associated
     * with the specified node in a HTML formatted manner.
     *
     * @param node The node for which the formatted string is requested.
     * @return A HTML string that represents the text elements of the specified
     * node.
     */
    public static String formatNodeAsHTML(MlMapNode node) {
        return formatNodeAsHTML(node, true, true, true, true);
    }

    public static String formatNodeAsHTML(MlMapNode node,
            boolean includeId,
            boolean includeOwner,
            boolean includeModificationDate,
            boolean includeDescription) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        NodeFormatter formatter = FomatterFactory.getFormatter(node.getObject());
        sb.append(formatter.format(node, includeId, includeOwner, includeModificationDate, includeDescription));
        sb.append("</body></html>");
        return sb.toString();

    }

    public static String formatObject(mlcObject object) {
        StringBuilder sb = new StringBuilder();
        sb.append(object.getHeadline()).append("\n");
        if (!object.getDescription().isEmpty()) {
            sb.append(object.getDescription()).append("\n");
        }
        sb.append(new HumanReadableClass(object.getClass())).append("(").append(Integer.toString(object.getId())).append(")");
        return sb.toString();
    }
}
