/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.clientobjects;

import com.mindliner.enums.ObjectCollectionType;
import java.io.Serializable;

/**
 *
 * @author Marius Messerli
 */
public class mlcObjectCollection extends mlcObject implements Serializable {

    private static final long serialVersionUID = 19640205L;
    private ObjectCollectionType type = ObjectCollectionType.GENERIC;

    public ObjectCollectionType getType() {
        return type;
    }

    public void setType(ObjectCollectionType type) {
        this.type = type;
    }
    
    
}
