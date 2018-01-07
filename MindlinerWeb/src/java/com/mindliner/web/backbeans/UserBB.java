/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.web.backbeans;

import com.mindliner.common.MlPasswordEncoder;
import com.mindliner.comparatorsS.ClientNameComparator;
import com.mindliner.entities.SoftwareFeature;
import com.mindliner.entities.mlsClient;
import com.mindliner.entities.mlsUser;
import com.mindliner.managers.FeatureManagerLocal;
import com.mindliner.managers.UserManagerLocal;
import com.mindliner.managers.UserManagerRemote;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.annotation.PostConstruct;

/**
 * @author Ming
 */
@ManagedBean
@ViewScoped
public class UserBB {

    @EJB
    private UserManagerRemote userManagerRemote;

    @EJB
    private UserManagerLocal userManagerLocal;

    @EJB
    private FeatureManagerLocal featureManager;

    // The current user.
    private mlsUser user;
    private boolean editMode;
    
     @PostConstruct
     public void init() {
         editMode = false;
         user = userManagerLocal.getCurrentUser();
     }
     
    public mlsUser getUser() {
        return user;
    }

    public void setUser(mlsUser user) {
        this.user = user;
    }

    public boolean getEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }
    
    public List<UserFeature> getUserFeatures() {
        List<UserFeature> ufl = new ArrayList<>();
        Collection<SoftwareFeature> allFeatures = featureManager.getRequiredSoftwareFeatures();
        for (SoftwareFeature f : allFeatures) {
            UserFeature uf = new UserFeature(f, user, user.getSoftwareFeatures().contains(f), userManagerLocal);
            ufl.add(uf);
        }
        return ufl;
    }

    public void savePassword(String newPassword) {
        try {
            String encodedPass = MlPasswordEncoder.encodePassword(newPassword.toCharArray());
            userManagerRemote.updatePassword(encodedPass);
            com.mindliner.web.util.Messages.generateInfoMessageFromBundle("SuccessfullyChangedPassword");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(UserBB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void saveAccountDetails(String firstname, String lastname, String email){
        userManagerRemote.setContactDetails(firstname, lastname, email);
        setEditMode(false);
    }
    

    /**
     * This call is used form MasterAdmin page and only works if the caller in
     * in MasterAdmin role.
     *
     * @param user The user for who the password is to be updated
     * @param newPassword The new password
     * @return
     */
    public String savePasswordForUser(mlsUser user, String newPassword) {
        try {
            String encodedPass = MlPasswordEncoder.encodePassword(newPassword.toCharArray());
            userManagerLocal.updatePassword(user.getId(), encodedPass);

        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(UserBB.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "masteradmin/userDetails?faces-redirect=true";
    }

    public List<mlsClient> getOwnedClients() {
        List<mlsClient> clients = userManagerLocal.getOwnedClients();
        Collections.sort(clients, new ClientNameComparator());
        return clients;
    }
}
