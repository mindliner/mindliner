/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.web.validators;

import com.mindliner.entities.mlsUser;
import com.mindliner.managers.UserManagerLocal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
@FacesValidator("emailValidator")
public class EmailValidator implements Validator {

    private final static String regex = "^\\S+@\\S+\\.\\S+$";
    
    @EJB
    UserManagerLocal userManager;

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        String email = (String) value;
        
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(email);
        if(!email.matches(regex)) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "e-mail not valid", "Please provide a valid email address");
            throw new ValidatorException(message);
        }
        
        mlsUser user = userManager.findUserByEmail(email);
        if (user != null) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "e-mail not available", "Email already taken");
            throw new ValidatorException(message);
        }
    }
}
