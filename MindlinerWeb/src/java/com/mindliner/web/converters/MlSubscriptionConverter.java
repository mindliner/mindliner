/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.web.converters;

import com.mindliner.entities.MlsSubscription;
import com.mindliner.exceptions.ForeignOwnerException;
import com.mindliner.exceptions.NonExistingObjectException;
import com.mindliner.managers.SubscriptionManagerLocal;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 *
 * @author Marius Messerli
 */
@ManagedBean
@RequestScoped
public class MlSubscriptionConverter extends MlPersistentObject {

    @EJB
    SubscriptionManagerLocal sm;

    /**
     * Creates a new instance of MlSubscriptionConverter
     */
    public MlSubscriptionConverter() {
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        try {
            MlsSubscription sub = sm.getSubscription(getId(value));
            if (sub != null) {
                return sub;
            }
            throw new IllegalStateException("Could not find subscription with id" + getId(value));
        } catch (NonExistingObjectException | ForeignOwnerException ex) {
            Logger.getLogger(MlSubscriptionConverter.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        MlsSubscription s = (MlsSubscription) value;
        return appendID(s.toString(), s.getId());
    }

}
