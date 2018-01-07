/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.clientobjects;

import com.mindliner.entities.SoftwareFeature;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The client version of a contact.
 *
 * @author Marius Messerli
 */
public class mlcUser implements Serializable {

    private int id = -1;
    private List<Integer> maxConfidentialityIds = new ArrayList<>();
    private String loginName = "";
    private String firstName = "";
    private String lastName = "";
    private String email = "";
    private Date lastLogin = null;
    private Date lastLogout = null;
    private Date lastSeen = null;
    private List<Integer> clientIds = new ArrayList<>();
    private boolean active = false;
    private int loginCount = 0;
    private List<SoftwareFeature> softwareFeatures;
    private int version = -1;
    private static final long serialVersionUID = 11L;

    public mlcUser() {
    }

    public boolean isActive() {
        return active;
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

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public void setLastLogout(Date lastLogout) {
        this.lastLogout = lastLogout;
    }

    public void setLastSeen(Date lastSeen) {
        this.lastSeen = lastSeen;
    }

    public void setLoginCount(int loginCount) {
        this.loginCount = loginCount;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public List<SoftwareFeature> getSoftwareFeatures() {
        return softwareFeatures;
    }

    public void setSoftwareFeatures(List<SoftwareFeature> softwareFeatures) {
        this.softwareFeatures = softwareFeatures;
    }

    public void setVersion(int version) {
        this.version = version;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Integer> getMaxConfidentialityIds() {
        return maxConfidentialityIds;
    }

    public void setMaxConfidentialityIds(List<Integer> maxConfidentialityIds) {
        this.maxConfidentialityIds = maxConfidentialityIds;
    }

    public List<Integer> getClientIds() {
        return clientIds;
    }

    public void setClientIds(List<Integer> clientIds) {
        this.clientIds = clientIds;
    }

    /**
     * Determins if the current user is authorized for the specified feautre.
     * Note that this function is mainly used to control user interface aspects.
     * Deep authorization for any server operation is evaluated on the server.
     *
     * @param feature The feautre for which authorization is inquired.
     * @return True if the user authroization is granted, false otherwise.
     *
     */
    public boolean isAuthorizedForFeature(SoftwareFeature.CurrentFeatures feature) {
        for (SoftwareFeature f : getSoftwareFeatures()) {
            if (f.getName().equals(feature.name())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final mlcUser other = (mlcUser) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return hash;
    }

    @Override
    public String toString() {
        if (firstName == null || lastName == null) {
            return loginName;
        }
        if (firstName.isEmpty() && lastName.isEmpty()) {
            return loginName;
        } else {
            return firstName.concat(" ").concat(lastName);
        }
    }
}
