/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.web.converters;

import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.managers.SecurityManagerRemote;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 * This class converts confidentialities to/from strings.
 *
 * @author Marius Messerli
 */
@ManagedBean
@RequestScoped
public class MlConfidentialityConverter extends MlPersistentObject {

    @EJB
    private SecurityManagerRemote securityManager;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        mlsConfidentiality conf = securityManager.getConfidentiality(getId(value));
        if (conf == null) {
            throw new IllegalStateException("Could not convert selector string to confidentiality");
        }
        return conf;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        mlsConfidentiality c = (mlsConfidentiality) value;
        return c.getName() + " " + c.getId();
    }
}
