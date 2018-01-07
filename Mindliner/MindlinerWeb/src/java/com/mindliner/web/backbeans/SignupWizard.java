package com.mindliner.web.backbeans;
 
import com.mindliner.common.MlPasswordEncoder;
import com.mindliner.entities.SoftwareFeature;
import com.mindliner.entities.mlsUser;
import com.mindliner.exceptions.NonExistingObjectException;
import com.mindliner.exceptions.UserCreationException;
import com.mindliner.managers.FeatureManagerLocal;
import com.mindliner.managers.UserManagerLocal;
import com.mindliner.managers.UserManagerRemote;
import java.io.IOException;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.hibernate.validator.constraints.NotEmpty;
 

/**
 *
 * @author Ming
 * 
 * Handles the signup wizard view (signup.xhtml).
 * Step 1: Create a user account
 * Step 2: Create a datapool
 * Step 3: Redirection/possible tutorial page
 * 
 */
@ManagedBean
@ViewScoped
public class SignupWizard implements Serializable {
    
    @EJB
    private UserManagerLocal userManagerLocal;
    
    @EJB
    private UserManagerRemote userManagerRemote;
    
    @EJB
    private FeatureManagerLocal featureManager;
    
    // User information
    @NotEmpty
    private String firstName = "";
    @NotEmpty
    private String lastName = "";
    @NotEmpty
    private String username = "";
    @NotEmpty
    private String email = "";
    @NotEmpty
    private String password = "";
    
    // Datapool information
    @NotEmpty
    private String datapoolName = "";
    
    private int step;
    
    @PostConstruct
    public void initialize() {
        step = 1;
    }
    
    @Resource(lookup = "MindlinerMail")
    private Session mailSession;

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getFirstName() {
        return firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getLastName() {
        return lastName;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
    
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDatapoolName() {
        return datapoolName;
    }

    public void setDatapoolName(String datapoolName) {
        this.datapoolName = datapoolName;
    }
    
    private void sendNotificationEmail() {
        MimeMessage message = new MimeMessage(mailSession);
        try {
            message.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(mailSession.getProperty("mail.to")));
            message.setFrom(new InternetAddress(mailSession.getProperty("mail.from")));
            message.setSubject("New user "+"["+firstName+" "+lastName+"]"+ " has signed up for Mindliner");
            message.setText("Username: "+username+"\n"+
                            "Email: "+email+"\n");
            Transport.send(message);
        } catch (MessagingException ex) {
            Logger.getLogger(SignupWizard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }
    
    // Automatically log the newly created user in
    private void login() throws ServletException, NoSuchAlgorithmException {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
        request.login(username, password);
    }
    
    public void createUser() throws UserCreationException, NoSuchAlgorithmException, MessagingException, ServletException, NonExistingObjectException { 
        sendNotificationEmail();
        userManagerLocal.createUser(username, firstName, lastName, email, null, null, MlPasswordEncoder.encodePassword(password.toCharArray()));
        login();
        password = ""; //mzh: can't hurt?
        
        // add default software features
        mlsUser user = userManagerLocal.getCurrentUser();
        Collection<SoftwareFeature> allFeatures = featureManager.getRequiredSoftwareFeatures();
        for(SoftwareFeature feature : allFeatures) {
            switch(feature.getName()) {
                case "CONFIDENTIALITY_LEVELS":
                case "OFFLINE_MODE":
                // 2-oct-2015: excluding WSM because we are now starting to sell it
                // case "WORKSPHEREMAP":
                case "SUBSCRIPTION":
                case "FILE_INDEXING":
                    userManagerLocal.addFeatureAuthorization(user.getId(), feature.getId());
                    break;
                default:
                    break;
            }
        }
        this.step = 2;
    }
    
    public void createPool() {
        // handles duplicate submits (e.g. with back button magic)
        if(this.step > 1 && !userManagerLocal.getCurrentUser().getClients().isEmpty()){
            return;
        }
        userManagerRemote.createClient(datapoolName, userManagerLocal.getCurrentUser());
        this.step = 3;
    }
    
    // Set Style of the wizard steps
    public String getStepStyle(int step) {
        if(this.step == step) return "current";
        if(this.step > step) return "done";
        return "";
    }
    
    // Handles back button behaviour for Chrome and page refresh for all browsers
    public void check() throws IOException {
        if((!FacesContext.getCurrentInstance().isPostback() || step == 1) && 
            FacesContext.getCurrentInstance().getExternalContext().getRemoteUser() != null) {
            FacesContext.getCurrentInstance().getExternalContext().redirect("dashboard.xhtml");  
        }
    }

}
