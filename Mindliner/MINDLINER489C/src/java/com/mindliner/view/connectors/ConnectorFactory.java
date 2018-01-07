/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.view.connectors;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.view.connectors.NodeConnection.ConnectorType;
import static com.mindliner.view.connectors.NodeConnection.ConnectorType.CurvedTextClearRating;

/**
 *
 * @author M.Messerli Created on 05.09.2012, 19:10:11
 */
public class ConnectorFactory {

    /**
     * Returns a new connector of the specified type.
     *
     * @param type The connector type.
     * @return
     */
    public static mlNodeConnector getConnector(ConnectorType type) {
        switch (type) {

            case CurvedTextClearRating:
                return new CurvedClearTextRating(CacheEngineStatic.getRatingMinAndMax());
                
            case ZeroConnector:
                return new ZeroConnector();

            default:
                return (new CurvedConnectorClearText());
        }
    }
}
