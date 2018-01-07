/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.entities;

import java.io.Serializable;
import javax.persistence.*;

/**
 *
 * @author Marius Messerli
 */
@Entity
@Table(name = "knowlets")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorValue(value = "KNOW")
@NamedQuery(name = "mlsKnowlet.getAll", query = "SELECT k FROM mlsKnowlet k")
public class mlsKnowlet extends mlsObject implements Serializable {

    private static final long serialVersionUID = 19640205L;

}
