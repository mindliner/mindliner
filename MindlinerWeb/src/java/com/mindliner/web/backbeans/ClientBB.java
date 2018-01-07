package com.mindliner.web.backbeans;

import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.comparatorsS.ConfidentialityComparator;
import com.mindliner.entities.MlAuthorization;
import com.mindliner.entities.mlsClient;
import com.mindliner.entities.mlsUser;
import com.mindliner.exceptions.MlAuthorizationException;
import com.mindliner.managers.ObjectManagerLocal;
import com.mindliner.managers.ReportManagerLocal;
import com.mindliner.managers.SecurityManagerRemote;
import com.mindliner.managers.UserManagerLocal;
import com.mindliner.managers.UserManagerRemote;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * The bean that hold information for a new client to be created by the master
 * admin web interface.
 *
 * @author Marius Messerli
 */
@ManagedBean
@ViewScoped
public class ClientBB implements Serializable {

    // @todo merge this into the local interface, lazy solution for now
    @EJB
    private UserManagerRemote userManagerRemote;

    @EJB
    private UserManagerLocal userManagerLocal;

    @EJB
    private ObjectManagerLocal objectManager;

    @EJB
    private SecurityManagerRemote securityManager;
    
    @EJB
    private ReportManagerLocal reportManager;

    @Resource(lookup = "MindlinerMail")
    private Session mailSession;

    @NotEmpty
    private String datapoolName = "";
    private String userId = "";
    private String enrollmentToken;

    //current client
    private mlsClient dataPool = null;
    private boolean editMode = false;
    private boolean editNameMode = false;
    private mlsConfidentiality selectedMaxConfidentiality;

    private List<mlsConfidentiality> dataPoolConfidentialities = new ArrayList<>();
    
    @PostConstruct
    public void init() {
        setDataPool(userManagerLocal.getCurrentUser().getClients().get(0));
    }

    public String getDatapoolName() {
        return datapoolName;
    }

    public void setDatapoolName(String clientName) {
        this.datapoolName = clientName;
    }

    public mlsClient getDataPool() {
        return dataPool;
    }

    public void setDataPool(mlsClient dataPool) {
        editNameMode = false;
        editMode = false;
        this.dataPool = dataPool;
        if (dataPool != null) {
            dataPoolConfidentialities = securityManager.getConfidentialities(dataPool.getId());
        }
    }

    public mlsConfidentiality getSelectedMaxConfidentiality() {
        return selectedMaxConfidentiality;
    }

    public void setSelectedMaxConfidentiality(mlsConfidentiality conf) {
        this.selectedMaxConfidentiality = conf;
    }

    public boolean getEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public boolean getEditNameMode() {
        return editNameMode;
    }

    public void setEditNameMode(boolean editNameMode) {
        this.editNameMode = editNameMode;
    }

    public List<mlsConfidentiality> getDataPoolConfidentialities() {
        Collections.sort(dataPoolConfidentialities, new ConfidentialityComparator());
        return dataPoolConfidentialities;
    }

    public void setDataPoolConfidentialities(List<mlsConfidentiality> clientConfidentialities) {
        this.dataPoolConfidentialities = clientConfidentialities;
    }

    public void deleteConfidentiality(mlsConfidentiality conf) {
        //Cannot remove conf directly, since the client is not set
        for (int i = 0; i < dataPoolConfidentialities.size(); i++) {
            if (dataPoolConfidentialities.get(i).getName().equals(conf.getName())
                    && dataPoolConfidentialities.get(i).getClevel() == conf.getClevel()) {
                dataPoolConfidentialities.remove(i);
                return;
            }
        }
    }

    private int getMaxClevel(List<mlsConfidentiality> list) {
        int max = 0;
        for (mlsConfidentiality c : list) {
            max = Math.max(max, c.getClevel());
        }
        return max;
    }

    public void addConfidentiality() {
        if (dataPool != null) {
            mlsConfidentiality newConf = new mlsConfidentiality(getMaxClevel(dataPoolConfidentialities) + 10, "new confidentiality");
            newConf.setClient(dataPool);
            dataPoolConfidentialities.add(newConf);
            editMode = true;
        }
    }
    
    public void cancelConfidentiality() {
        // reset the confidentialities
        dataPoolConfidentialities = dataPool.getConfidentialities();
        editMode = false;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEnrollmentToken() {
        return enrollmentToken;
    }

    public void setEnrollmentToken(String enrollmentToken) {
        this.enrollmentToken = enrollmentToken;
    }
    
    public int getObjectCount() {
        if(dataPool != null) {
            return reportManager.getObjectCount(dataPool);
        }
        return 0;
    }

    private String createEnrollmentRequest(mlsUser user, String email) throws MalformedURLException {        
        // for data pools with only one confi we don't show the pull-down and selectedMaxConfidentiality may not have a meaningful value
        if (dataPool.getConfidentialities().size() == 1) {
            selectedMaxConfidentiality = dataPool.getConfidentialities().get(0);
        }
        String token = objectManager.createEnrollmentAuthorization(user, dataPool, selectedMaxConfidentiality, email);
        //Build link
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        URL link = new URL(request.getScheme(), request.getServerName(), request.getServerPort(), request.getContextPath());
        return link.toString() + "/faces/invitations.xhtml?token=" + token;
    }

    public void saveConfidentialities() {
        if (dataPool != null) {
            securityManager.updateConfidentialities(dataPoolConfidentialities);
        }
        editMode = false;
    }

    public void validateConfidentialities(ComponentSystemEvent event) {
        FacesContext fc = FacesContext.getCurrentInstance();
        String msgId = "poolForm:addButton";
        for (mlsConfidentiality conf : dataPoolConfidentialities) {
            if (conf.getName().isEmpty()) {
                FacesMessage msg = new FacesMessage("Access level name may not be empty." + conf.getClevel() + " " + dataPoolConfidentialities.size());
                msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fc.addMessage(msgId, msg);
                fc.renderResponse();
                return;
            }
        }
    }

    public void resetEditModes() {
        editMode = false;
        editNameMode = false;
    }

    public void saveName() {
        if (dataPool != null && !dataPool.getName().isEmpty()) {
            userManagerRemote.setClientName(dataPool.getId(), dataPool.getName());
        }
        editNameMode = false;
    }

    public void validateClientName(FacesContext context, UIComponent component, Object value) {
        if (value.toString().trim().isEmpty()) {
            String clientNameId = component.getClientId();
            FacesMessage msg = new FacesMessage("Datapool name may not be empty.");
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            context.addMessage(clientNameId, msg);
            context.renderResponse();
        }
    }

    public void acceptInvitation(String token) throws IOException {
        try {
            datapoolName = objectManager.enrollUser(token);
        } catch (MlAuthorizationException ex) {
            FacesMessage msg = new FacesMessage(ex.getMessage());
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            FacesContext.getCurrentInstance().addMessage(null, msg);
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            FacesContext.getCurrentInstance().getExternalContext().redirect("faces/enrollment.xhtml");
        }
    }

    public void inviteUser() throws MalformedURLException, IOException {
        if (dataPool == null) {
            com.mindliner.web.util.Messages.generateErrorMessageFromBundle("NoDatapoolSelected");
        }
        if (userId.matches("^\\S+@\\S+\\.\\S+$")) {
            // userID is an email address
            mlsUser user = userManagerLocal.findUserByEmail(userId);
            if (user != null) {
                // email registered
                if (isRequestCreationAllowed(user, true)) {
                    // all checks passed
                    //send data pool invitation mail only
                    String link = createEnrollmentRequest(user, userId);
                    sendInvitationEmail(userId, link, false);
                    com.mindliner.web.util.Messages.generateInfoMessage("An invitation has been sent to the email address " + getQuotedString(userId));
                }
            } else {
                // email not registered
                // send special email that includes instructions to set up a Mindliner account
                //MZHTODO check no request for this email yet?
                String link = createEnrollmentRequest(user, userId);
                sendInvitationEmail(userId, link, true);
                com.mindliner.web.util.Messages.generateInfoMessage("An invitation has been sent to the email address " + getQuotedString(userId));
            }
        } else {
            // userID is a username
            mlsUser user = userManagerLocal.findUser(userId);
            if (user != null) {
                // user registered
                if (isRequestCreationAllowed(user, false)) {
                    //all checks passed
                    String link = createEnrollmentRequest(user, user.getEmail());
                    sendInvitationEmail(user.getEmail(), link, false);
                    com.mindliner.web.util.Messages.generateInfoMessage("An invitation has been sent to user " + getQuotedString(userId));
                }
            } else {
                // user not found
                // display error message, no invitation sent
                com.mindliner.web.util.Messages.generateErrorMessage("User " + getQuotedString(userId) + " does not exist - no invitation sent.");
            }
        }
    }


    /**
     * Performs following checks: 1. Does an enrollment request already exist
     * for the user and datapool? 2. Is the user already enrolled in the
     * datapool=
     *
     * @param user the respective user
     * @param isEmail specifies whether the user was found via email return true
     * when request can be created
     */
    private boolean isRequestCreationAllowed(mlsUser user, boolean isEmail) {
        if (!objectManager.findPendingRequests(dataPool, user, MlAuthorization.AuthorizationType.DataPoolEnrollmentRequest).isEmpty()) {
            com.mindliner.web.util.Messages.generateErrorMessage("User " + (isEmail ? "with email " : "") + getQuotedString(userId) + " already has a pending request for data pool " + getQuotedString(dataPool.getName()));
            return false;
        } else if (dataPool.getUsers().contains(user)) {
            com.mindliner.web.util.Messages.generateErrorMessage("User " + (isEmail ? "with email " : "") + getQuotedString(userId) + " is already enrolled in data pool " + getQuotedString(dataPool.getName()));
            return false;
        }
        return true;
    }

    /**
     * Help function to enclose a string with quotes
     *
     * @param string String to be quoted
     * @return quoted String
     */
    private String getQuotedString(String string) {
        return "\"" + string + "\"";
    }

    /**
     * Sent invitation mail to new data pool member
     *
     * @param mailTo the recipient of the invitation
     * @param link the link containing the invitation token
     * @param includeMindlinerInvite flag to choose the right content template
     */
    private void sendInvitationEmail(String mailTo, String link, boolean includeMindlinerInvite) throws FileNotFoundException, IOException {

        //read template
        InputStream istream = includeMindlinerInvite ? FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/templates/email/dataPoolAndMindlinerInvitation.html")
                : FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/templates/email/dataPoolInvitation.html");

        String msgContent;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(istream))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            msgContent = builder.toString();
        }
        msgContent = msgContent.replace("%USER%", dataPool.getOwner().getFirstName() + " " + dataPool.getOwner().getLastName());
        msgContent = msgContent.replace("%LINK%", link);
        msgContent = msgContent.replace("%DATAPOOL%", dataPool.getName());

        MimeMessage message = new MimeMessage(mailSession);
        try {
            message.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(mailTo));
            message.setFrom(new InternetAddress(mailSession.getProperty("mail.from")));
            message.setSubject("Invitation to Mindliner Data Pool " + getQuotedString(dataPool.getName()));
            message.setContent(msgContent, "text/html");
            Transport.send(message);
        } catch (MessagingException ex) {
            Logger.getLogger(ClientBB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean isOwnedByCurrentUser() {
        if(dataPool != null) {
            return dataPool.getOwner().equals(userManagerLocal.getCurrentUser());
        }
        return false;
    }
    
    public void createPool() {
        setDataPool(userManagerRemote.createClient(datapoolName, userManagerLocal.getCurrentUser()));
        datapoolName="";
    }

}
