/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.objects.transfer;

import com.mindliner.entities.*;

/**
 *
 * Factory creates a transfer object for the specified mindliner object.
 *
 * @author Marius Messerli
 */
public class mlTransferObjectFactory {

    /**
     * Creates a transfer object for the specified server object.
     *
     * @param mo The mindliner server object.
     * @return The transfe
     */
    public static MltObject getTransferObject(mlsObject mo) {
        if (mo instanceof mlsTask) {
            return new mltTask((mlsTask) mo);
        } else if (mo instanceof MlsNews) {
            return new MltNews((MlsNews) mo);
        } else if (mo instanceof mlsContact) {
            return new mltContact((mlsContact) mo);
        } else if (mo instanceof mlsKnowlet) {
            return new mltKnowlet((mlsKnowlet) mo);
        } else if (mo instanceof mlsObjectCollection) {
            return new mltObjectCollection((mlsObjectCollection) mo);
        } else if (mo instanceof MlsImage) {
            return new MltImage((MlsImage) mo);
        } else if (mo instanceof MlsContainer) {
            return new MltContainer((MlsContainer) mo);
        } else if (mo instanceof MlsContainerMap) {
            return new MltContainerMap((MlsContainerMap) mo);
        }
        throw new IllegalArgumentException("Don't know how to create transfer object for class " + mo.getClass().getName());
    }
}
