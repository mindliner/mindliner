/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.objects.transfer;

import com.mindliner.entities.mlsObjectCollection;
import com.mindliner.enums.ObjectCollectionType;

/**
 *
 * @author Marius Meserli
 */
public class mltObjectCollection extends MltObject {

    private ObjectCollectionType type = ObjectCollectionType.GENERIC;

    public mltObjectCollection() {
    }

    public mltObjectCollection(mlsObjectCollection oc) {
        super(oc);
        type = oc.getType();
    }
    
    public ObjectCollectionType getType() {
        return type;
    }

    public void setType(ObjectCollectionType type) {
        this.type = type;
    }

}
