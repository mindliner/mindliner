/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.web.validators;

import com.mindliner.common.MlPasswordEncoder;
import com.mindliner.entities.mlsUser;
import com.mindliner.managers.UserManagerLocal;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 *
 * @author Ming
 */
@ManagedBean
@RequestScoped
@FacesValidator("passwordValidator")
public class PasswordValidator implements Validator {
    
    @EJB
    UserManagerLocal userManager;
    
    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        
        String password = value.toString();
        if(password == null || password.isEmpty()) {
            FacesMessage message = new FacesMessage(com.mindliner.web.util.Messages.getStringFromBundle("PasswordEmpty"));
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            context.addMessage(component.getClientId(), message);
            context.renderResponse();
        } else if(password.length() < 6) {
            FacesMessage message = new FacesMessage(com.mindliner.web.util.Messages.getStringFromBundle("PasswordTooShort"));
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            context.addMessage(component.getClientId(), message);
            context.renderResponse();
        }
        
        // Check password confirmation
        UIInput uiInputConfirmPassword = (UIInput) component.getAttributes().get("confirmPassword");
        String confirmPassword = uiInputConfirmPassword.getSubmittedValue().toString();
        String confirmPasswordId = uiInputConfirmPassword.getClientId();
        if(confirmPassword == null || confirmPassword.isEmpty()) {
            context.addMessage(confirmPasswordId, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Password confirmation", 
                    com.mindliner.web.util.Messages.getStringFromBundle("PasswordNotConfirmed")));
            context.renderResponse();
        } else if(!password.equals(confirmPassword)) {
            context.addMessage(confirmPasswordId, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Pasword mismatch.", 
                    com.mindliner.web.util.Messages.getStringFromBundle("PasswordNotMatched")));
            context.renderResponse();
        }
        
        // Check whether old password matches with the one in the DB
        UIInput uiInputOldPassword = (UIInput) component.getAttributes().get("oldPassword");
        if(uiInputOldPassword != null) {
            try {
                // Call getValue(), because this component appears before the password component
                String oldPassword = uiInputOldPassword.getValue().toString();
                String oldPasswordId = uiInputOldPassword.getClientId();

                mlsUser user = userManager.getCurrentUser();
                if(!user.getPassword().equals(MlPasswordEncoder.encodePassword(oldPassword.toCharArray()))) {
                    context.addMessage(oldPasswordId, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Wrong password.",
                            com.mindliner.web.util.Messages.getStringFromBundle("WrongPasswordEntered")));
                    context.renderResponse();
                }
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(PasswordValidator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
