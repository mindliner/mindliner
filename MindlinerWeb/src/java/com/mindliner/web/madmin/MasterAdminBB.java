/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.web.madmin;

import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.common.MlPasswordEncoder;
import com.mindliner.comparatorsS.ClientNameComparator;
import com.mindliner.comparatorsS.UserLastSeenComparator;
import com.mindliner.entities.SoftwareFeature;
import com.mindliner.entities.mlsClient;
import com.mindliner.entities.mlsLog;
import com.mindliner.entities.mlsUser;
import com.mindliner.exceptions.UserCreationException;
import com.mindliner.managers.ConsistencyManagerRemote;
import com.mindliner.managers.FeatureManagerLocal;
import com.mindliner.managers.IslandManagerRemote;
import com.mindliner.managers.LinkManagerLocal;
import com.mindliner.managers.LogManagerLocal;
import com.mindliner.managers.ReportManagerLocal;
import com.mindliner.managers.TestManagerRemote;
import com.mindliner.managers.UserCacheBean;
import com.mindliner.managers.UserManagerLocal;
import com.mindliner.web.backbeans.UserFeature;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * This is the main backing bean for the Mindliner master admin application.
 *
 * @author Marius Messerli
 */
@ManagedBean
@SessionScoped
public class MasterAdminBB implements Serializable {

    private mlsClient currentClient = null;
    /*the selected user*/
    private mlsUser currentUser = null;
    private List<mlsUser> usersSortedByLoginDesc = null;
    private boolean showLog = false;

    @NotEmpty
    private String login = "";
    @NotEmpty
    private String firstName = "";
    @NotEmpty
    private String lastName = "";
    @NotEmpty
    private String password = "";
    private mlsConfidentiality maxConfidentiality;

    @EJB
    UserManagerLocal userManager;

    @EJB
    UserCacheBean userCache;

    @EJB
    LogManagerLocal logManager;

    @EJB
    IslandManagerRemote islandManager;

    @EJB
    private ReportManagerLocal reportManagerLocal;

    @EJB
    private FeatureManagerLocal featureManager;

    @EJB
    private LinkManagerLocal linkManager;

    /**
     * Creates a new instance of MasterAdminBB
     */
    public MasterAdminBB() {
    }

    public mlsClient getCurrentClient() {
        return currentClient;
    }

    public String showUserDetails() {
        return "adminHome?faces-redirect=true";
    }

    public void setCurrentClient(mlsClient currentClient) {

        System.out.println("setting current client to " + currentClient);
        this.currentClient = currentClient;
        maxConfidentiality = currentClient.getConfidentialities().get(0);
        if (!currentClient.getUsers().isEmpty()) {
            currentUser = currentClient.getUsers().get(0);
        }
    }

    public List<mlsClient> getClients() {
        List<mlsClient> clients = userManager.getClients();
        Collections.sort(clients, new ClientNameComparator());
        return clients;
    }

    public mlsUser getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(mlsUser currentUser) {
        this.currentUser = currentUser;
    }

    public String getLoggedInUsers() {
        StringBuilder s = new StringBuilder();
        for (Iterator it = userCache.getLoggedInUsers().iterator(); it.hasNext();) {
            mlsUser u = (mlsUser) it.next();
            s.append(u.getUserName());
            if (it.hasNext()) {
                s.append(", ");
            }
        }
        return s.toString();
    }

    public List<mlsLog> getCurrentUserLog() {
        if (currentUser == null) {
            return null;
        }
        if (!showLog) {
            return new ArrayList<>();
        }
        if (isMasterAdmin()) {
            return logManager.getLog(currentUser.getId(), 20);
        } else {
            throw new IllegalStateException("A user should not be able to get here");
        }
    }

    public boolean isShowLog() {
        return showLog;
    }

    public void setShowLog(boolean showLog) {
        this.showLog = showLog;
    }

    public void deleteCurrentClient() {
        if (currentClient != null) {
            userManager.deleteClient(currentClient);
            if (!userManager.getClients().isEmpty()) {
                setCurrentClient(userManager.getClients().get(0));
            }
        }
    }

    public void initializeIslands() {
        if (currentClient != null) {
            islandManager.initializeIslands(currentClient.getId());
        }
    }

    public void deleteIslands() {
        if (currentClient != null) {
            islandManager.deleteIslands(currentClient.getId());
        }
    }

    /**
     * Returns a list of all features known to the current software version
     * indicating whether or not it is authorized for the current user;
     *
     * @return
     */
    public List<UserFeature> getCurrentUserFeatures() {
        List<UserFeature> ufl = new ArrayList<>();
        Collection<SoftwareFeature> allFeatures = featureManager.getRequiredSoftwareFeatures();
        for (SoftwareFeature f : allFeatures) {
            UserFeature uf = new UserFeature(f, currentUser, currentUser.getSoftwareFeatures().contains(f), userManager);
            ufl.add(uf);
        }
        return ufl;
    }

    public boolean isMasterAdmin() {
        return userManager.isInRole("MasterAdmin");
    }

    public void inactivateCurrentUser() {
        if (currentUser != null) {
            userManager.setActive(currentUser.getId(), false);
            currentUser.setActive(false);
        }
    }

    public void activateCurrentUser() {
        if (currentUser != null) {
            userManager.setActive(currentUser.getId(), true);
            currentUser.setActive(true);
        }
    }

    public String createUser() {
        try {
            try {
                userManager.createUser(login, firstName, lastName, "", currentClient, maxConfidentiality, MlPasswordEncoder.encodePassword(password.toCharArray()));
                if (currentClient != null) {
                    // re-load current client to also contain the new user
                    currentClient = userManager.getOneClient(currentClient.getId());
                }
                return "userDetails.xhtml";
            } catch (NoSuchAlgorithmException ex) {
                FacesMessage msg = new FacesMessage("User Creation Error", ex.getMessage());
                msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                throw new ValidatorException(msg);
            }
        } catch (UserCreationException ex) {
            FacesMessage msg = new FacesMessage("User Creation Error", ex.getMessage());
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(msg);
        }
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public mlsConfidentiality getMaxConfidentiality() {
        return maxConfidentiality;
    }

    public void setMaxConfidentiality(mlsConfidentiality maxConfidentiality) {
        this.maxConfidentiality = maxConfidentiality;
    }

    public List<mlsUser> getUnconfirmedUsers() {
        List<mlsUser> unconfirmedUsers = userManager.getUnconfirmedUsers();
        currentUser = unconfirmedUsers.size() > 0 ? unconfirmedUsers.get(0) : null;
        return unconfirmedUsers;
    }

    public String confirmUser() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (currentUser == null) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "No user selected", "No user selected");
            fc.addMessage("userForm:confirmButton", message);
            fc.renderResponse();
            return "unconfirmendUsers?faces-redirect=true";
        }
        boolean success = userManager.confirmUser(currentUser.getUserName());
        return "unconfirmendUsers?faces-redirect=true";
    }

    public String deleteUnconfirmedUser() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (currentUser == null) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "No user selected", "No user selected");
            fc.addMessage("userForm:confirmButton", message);
            fc.renderResponse();
        } else {
            userManager.deleteUnconfirmedUser(currentUser);
        }
        return "unconfirmendUsers?faces-redirect=true";
    }

    public List<mlsUser> getUsersSortedByLoginDesc() {
        if (usersSortedByLoginDesc == null) {
            usersSortedByLoginDesc = userManager.getAllUsers();
            Collections.sort(usersSortedByLoginDesc, (new UserLastSeenComparator()).reversed());
            System.out.println("created sorted user list, most recent login: " + usersSortedByLoginDesc.get(0).getFirstName());
        }
        return usersSortedByLoginDesc;
    }

    public String showAllUsers() {
        currentClient = null;
        if (currentUser == null) {
            currentUser = getUsersSortedByLoginDesc().get(0);
        }
        return "userDetails?faces-redirect=true";
    }

    public int getObjectCount(mlsUser u) {
        return reportManagerLocal.getObjectCount(u.getId());
    }

    public void initializeObjectRelativeCount() {
        linkManager.reconcileObjectsRelativeCountField();
    }

}
