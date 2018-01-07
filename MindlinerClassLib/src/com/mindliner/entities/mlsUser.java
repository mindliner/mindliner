/*
 * mlUser.java
 * 
 * Created on 28.09.2007, 14:56:44
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.entities;

import com.mindliner.categories.mlsConfidentiality;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

/**
 * This class represents the user who is interacting with Mindlner.
 *
 * @author Marius Messerli
 */
@Entity
@NamedQueries({
    @NamedQuery(name = "mlsUser.findAll", query = "SELECT u FROM mlsUser u ORDER BY u.userName"),
    @NamedQuery(name = "mlsUser.getUserByUserName", query = "SELECT u FROM mlsUser u WHERE u.userName = :userName"),
    @NamedQuery(name = "mlsUser.getUserByEmail", query = "SELECT u FROM mlsUser u WHERE u.email = :email"),
    @NamedQuery(name = "mlsUser.getActiveUserByUserName", query = "SELECT u FROM mlsUser u WHERE u.userName = :userName AND u.active=true"),
    @NamedQuery(name = "mlsUser.getActiveUsers", query = "SELECT u FROM mlsUser u WHERE u.active=true"),
    @NamedQuery(name = "mlsUser.getUserByFirstAndLastnameSubstring",
            query = "SELECT u FROM mlsUser u, IN(u.clients) client WHERE client.id = :clientId AND u.firstName LIKE :firstNameFragment AND u.lastName LIKE :lastNameFragment"),
    @NamedQuery(name = "mlsUser.getUserByFirstnameSubstring", query = "SELECT u FROM mlsUser u, IN(u.clients) client WHERE client.id = :clientId AND u.firstName LIKE :firstNameFragment"),
    @NamedQuery(name = "mlsUser.getUsersWithSharedDataPools", query = "SELECT DISTINCT u FROM mlsUser u, IN(u.clients) client, IN(client.users) sharedUser WHERE sharedUser.id= :userId AND u.active = 1")
})
@Table(name = "users")
public class mlsUser implements Serializable {

    private static final long serialVersionUID = 100L;
    private int id = -1;
    private List<mlsConfidentiality> maxConfidentialities = new ArrayList<>();
    private String userName = "";
    private Date lastLogin = null;
    private Date lastLogout = null;
    private Date lastSeen = null;
    // list of clients for which this user is authorized and can switch between
    private List<mlsClient> clients = new ArrayList<>();
    private boolean active = true;
    private int loginCount = 0;
    private int version = -1;
    private List<SoftwareFeature> softwareFeatures = new ArrayList<>();
    private String password;
    private Collection<MlAuthenticationGroups> authGroups = new ArrayList<>();
    private String firstName;
    private String lastName;
    private String email;
    private Collection<mlsWorkUnit> workUnits;
    private Collection<MlsSubscription> subscriptions;

    public mlsUser() {
    }

    public void setId(int id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    public int getId() {
        return id;
    }

    @Version
    @Column(name = "LOCK_VERSION")
    public int getVersion() {
        return version;
    }

    @ManyToMany(mappedBy = "users", fetch = FetchType.LAZY)
    public List<SoftwareFeature> getSoftwareFeatures() {
        return softwareFeatures;
    }

    public void setSoftwareFeatures(List<SoftwareFeature> softwareFeatures) {
        this.softwareFeatures = softwareFeatures;
    }

    @OneToMany(mappedBy = "user")
    public Collection<MlsSubscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Collection<MlsSubscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    @OneToMany(mappedBy = "user")
    public Collection<mlsWorkUnit> getWorkUnits() {
        return workUnits;
    }

    public void setWorkUnits(Collection<mlsWorkUnit> workUnits) {
        this.workUnits = workUnits;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinTable(name = "users_clients",
            joinColumns = {
                @JoinColumn(name = "USER_ID", referencedColumnName = "ID")},
            inverseJoinColumns = {
                @JoinColumn(name = "MAX_CONFIDENTIALITY_ID", referencedColumnName = "ID")})
    public List<mlsConfidentiality> getMaxConfidentialities() {
        return maxConfidentialities;
    }

    /**
     * A convenience function to return the highest confidentiality this user
     * has with the specified client
     *
     * @param client The client for which the max confi is needed
     * @return The confidentiality or null if
     */
    public mlsConfidentiality getMaxConfidentiality(mlsClient client) {
        if (client == null) {
            return null;
        }
        for (mlsConfidentiality c : maxConfidentialities) {
            if (c.getClient().equals(client)) {
                return c;
            }
        }
        System.err.println("Could not determine maximum confidentiality for user " + getUserName() + " and data pool " + client.getName());
        return null;
    }

    public void setMaxConfidentialities(List<mlsConfidentiality> maxConfidentialities) {
        this.maxConfidentialities = maxConfidentialities;
    }

    /**
     * This call is required by ORM but should not be called to add a client to
     * the current user. Instead create a new UserClientLink object and persist
     * that one, then call EntityRefresher.updateCachedEntity on the mlsUser
     * object
     *
     * @return
     */
    @Deprecated
    @ManyToMany(mappedBy = "users", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    public List<mlsClient> getClients() {
        return clients;
    }

    public void setClients(List<mlsClient> clients) {
        this.clients = clients;
    }

    @Column(name = "USERNAME")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setLastLogin(Date d) {
        lastLogin = d;
    }

    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = "LAST_LOGIN")
    public Date getLastLogin() {
        return lastLogin;
    }

    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = "LAST_LOGOUT")
    public Date getLastLogout() {
        return lastLogout;
    }

    public void setLastLogout(Date d) {
        lastLogout = d;
    }

    public void setLastSeen(Date d) {
        lastSeen = d;
    }

    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = "LAST_SEEN")
    public Date getLastSeen() {
        return lastSeen;
    }

    @Column(name = "ACTIVE")
    public boolean getActive() {
        return active;
    }

    public void setActive(boolean a) {
        active = a;
    }

    public void setLoginCount(int c) {
        loginCount = c;
    }

    @Column(name = "LOGIN_COUNT")
    public int getLoginCount() {
        return loginCount;
    }

    @Column(name = "PASSWORD")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Column(name = "FIRST_NAME")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Column(name = "LAST_NAME")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Column(name = "EMAIL")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinTable(name = "users_groups",
            joinColumns = {
                @JoinColumn(name = "USER_ID", referencedColumnName = "ID")},
            inverseJoinColumns = {
                @JoinColumn(name = "GROUP_ID", referencedColumnName = "ID")})
    public Collection<MlAuthenticationGroups> getAuthGroups() {
        return authGroups;
    }

    public void setAuthGroups(Collection<MlAuthenticationGroups> authGroups) {
        this.authGroups = authGroups;
    }

    @Override
    public String toString() {
        return userName;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof mlsUser)) {
            return false;
        }
        mlsUser that = (mlsUser) o;
        return (this.getId() == that.getId());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + this.id;
        hash = 11 * hash + (this.userName != null ? this.userName.hashCode() : 0);
        return hash;
    }

}
