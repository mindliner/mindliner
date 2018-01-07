/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.web.validators;

import com.mindliner.managers.UserManagerLocal;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 * This class validates if a username is still available and suggests, via the
 * exception text a new name that is still available.
 *
 * @author Marius Messerli
 */
@ManagedBean
@RequestScoped
@FacesValidator("usernameValidator")
public class UsernameValidator implements Validator {

    @EJB
    UserManagerLocal userManager;

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        String username = (String) value;
        String suggestedName = userManager.isUsernameAvailable(username);
        if (!suggestedName.equals(username)) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "username not available", "Username already taken");
            throw new ValidatorException(message);
        }
    }
}
