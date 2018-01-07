/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.web.converters;

import com.mindliner.entities.mlsClient;
import com.mindliner.managers.UserManagerLocal;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 * This class converts a mlsClient to and from a string
 * @author Marius Messerli
 */
@ManagedBean
@RequestScoped
public class MlDataPoolConverter extends MlPersistentObject {

    @EJB
    UserManagerLocal userManager;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        mlsClient c = userManager.getOneClient(getId(value));
        if (c != null) {
            return c;
        }
        throw new IllegalStateException("Could not find data pool with name " + value);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        mlsClient dataPool = (mlsClient) value;
        return appendID(dataPool.getName(), dataPool.getId());
    }

}
