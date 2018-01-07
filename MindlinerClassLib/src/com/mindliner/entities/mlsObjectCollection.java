/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.entities;

import com.mindliner.enums.ObjectCollectionType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 *
 * @author Marius Messerli
 */
@Entity
@Table(name = "objectcollections")
@DiscriminatorValue(value = "OCOL")
@NamedQueries({
    @NamedQuery(name = "mlsObjectCollection.getObjectCollectionsByType", query = "SELECT o FROM mlsObjectCollection o WHERE o.type = :type"),
    @NamedQuery(name = "mlsObjectCollection.getAll", query = "SELECT o FROM mlsObjectCollection o")
})
public class mlsObjectCollection extends mlsObject {

    private static final long serialVersionUID = 19640205L;
    private ObjectCollectionType type = ObjectCollectionType.GENERIC;

    @Column(name = "TYPE")
    @Enumerated(EnumType.STRING)
    public ObjectCollectionType getType() {
        return type;
    }

    public void setType(ObjectCollectionType type) {
        this.type = type;
    }

}
