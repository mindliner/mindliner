/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.web.converters;

import com.mindliner.entities.mlsUser;
import com.mindliner.managers.UserManagerLocal;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 * Converts mlsUser to/from string.
 *
 * @author Marius Messerli
 */
@ManagedBean
@RequestScoped
public class MlUserConverter extends MlPersistentObject{

    @EJB
    UserManagerLocal userManager;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        mlsUser user = userManager.findUser(getId(value));
        if(user == null)
            throw new IllegalStateException("No user found with username " + value);
        return user;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        mlsUser u = (mlsUser) value;
        return appendID(u.getUserName(), u.getId());
    }

}
