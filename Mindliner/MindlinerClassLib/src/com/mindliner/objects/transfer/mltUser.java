/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.objects.transfer;

import com.mindliner.categories.mlsConfidentiality;
import com.mindliner.entities.SoftwareFeature;
import com.mindliner.entities.mlsClient;
import com.mindliner.entities.mlsUser;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This is the transfer object for the current user.
 *
 * @author Marius Messerli
 */
public class mltUser implements Serializable {
    
    private int id = -1;
    private List<Integer> maxConfidentialityIds = new ArrayList<>();
    private String loginName = "";
    private String firstName = "";
    private String lastName = "";
    private String email = "";
    private Date lastLogin = null;
    private Date lastLogout = null;
    private Date lastSeen = null;
    private List<Integer> clientIds;
    private boolean active = false;
    private int loginCount = 0;
    private int version = -1;
    private List<Integer> softwareFeatureIds;
    private static final long serialVersionUID = 19640205L;
    
    public mltUser(mlsUser u) {
        id = u.getId();
        for (mlsConfidentiality c : u.getMaxConfidentialities()) {
            maxConfidentialityIds.add(c.getId());
        }
        loginName = u.getUserName();
        firstName = u.getFirstName();
        lastName = u.getLastName();
        email = u.getEmail();
        lastLogin = u.getLastLogin();
        lastLogout = u.getLastLogout();
        lastSeen = u.getLastSeen();
        clientIds = buildClientIdList(u.getClients());
        active = u.getActive();
        loginCount = u.getLoginCount();
        version = u.getVersion();
        softwareFeatureIds = new ArrayList<>();
        for (SoftwareFeature sf : u.getSoftwareFeatures()) {
            if (SoftwareFeature.isStillExisting(sf.getName())) {
                softwareFeatureIds.add(sf.getId());
            }
        }
    }
    
    private List<Integer> buildClientIdList(List<mlsClient> clients) {
        List<Integer> cids = new ArrayList<>();
        for (mlsClient c : clients) {
            cids.add(c.getId());
        }
        return cids;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public List<Integer> getClientIds() {
        return clientIds;
    }
    
    public List<Integer> getMaxConfidentialityIds() {
        return maxConfidentialityIds;
    }
    
    public int getId() {
        return id;
    }
    
    public Date getLastLogin() {
        return lastLogin;
    }
    
    public Date getLastLogout() {
        return lastLogout;
    }
    
    public Date getLastSeen() {
        return lastSeen;
    }
    
    public int getLoginCount() {
        return loginCount;
    }
    
    public String getLoginName() {
        return loginName;
    }
    
    public int getVersion() {
        return version;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    @Override
    public String toString() {
        return loginName;
    }
    
    public List<Integer> getSoftwareFeatureIds() {
        return softwareFeatureIds;
    }
}
