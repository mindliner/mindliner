/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.entities;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 *
 * @author Marius Messerli
 */
@Entity
@Table(name = "contacts")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorValue(value = "CONT")
@NamedQueries({
    @NamedQuery(
            name = "mlsContact.findTopRated",
            query = "SELECT c FROM mlsContact c WHERE c.client.id = :clientId  AND c.confidentiality.clevel <= :confidentialityLevel ORDER BY c.rating desc")
})
public class mlsContact extends mlsObject implements Comparable, Serializable {

    private String firstName = "";
    private String middleName = "";
    private String lastName = "";
    private String email = "";
    private String phoneNumber = "";
    private String mobileNumber = "";
    private MlsImage profilePicture;
    private static final long serialVersionUID = 19640205L;

    @Column(name = "FIRSTNAME")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String fname) {
        firstName = fname;
    }

    @Column(name = "LASTNAME")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lname) {
        lastName = lname;
    }

    public void setMiddleName(String n) {
        middleName = n;
    }

    @Column(name = "MIDDLE")
    public String getMiddleName() {
        return middleName;
    }

    @Column(name = "PHONE_NUMBER")
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Column(name = "MOBILE_NUMBER")
    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public void setEmail(String w) {
        email = w;
    }

    @Column(name = "EMAIL")
    public String getEmail() {
        return email;
    }

    @OneToOne
    @JoinColumn(name = "PICTURE_ID", referencedColumnName = "ID")
    public MlsImage getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(MlsImage profilePicture) {
        this.profilePicture = profilePicture;
    }

    @Transient
    public String getName() {
        // update headline for user information only, the fields are not used in the software
        StringBuilder sb = new StringBuilder();
        sb.append(firstName);
        if (middleName != null && !middleName.isEmpty()) {
            sb.append(" ").append(middleName);
        }
        if (lastName != null && !lastName.isEmpty()) {
            sb.append(" ").append(lastName);
        }
        return sb.toString();
    }

    /**
     * Contactenames the first character of the firstname, middlename, and
     * lastname. If either of these fields are zero lengh a hypan (-) is
     * inserted instead.
     * @return 
     */
    @Transient
    public String getInitials() {
        StringBuilder sb = new StringBuilder();
        if (getFirstName().length() > 0) {
            sb.append(getFirstName().substring(0, 1));
        }
        if (getMiddleName() != null && getMiddleName().length() > 0) {
            sb.append(getMiddleName().substring(0, 1));
        }
        if (getLastName().length() > 0) {
            sb.append(getLastName().substring(0, 1));
        }
        if (sb.length() == 0) {
            sb.append("NA");
        }
        return sb.toString();
    }

    /**
     * Returns name + company + city + department
     */
    @Transient
    public String getConcatenatedText() {
        return getName();
    }

    @Override
    public String toString() {
        return getFirstName() + " " + getLastName();
    }

    @Override
    public int compareTo(Object o) {
        mlsContact c = (mlsContact) o;
        return this.getFirstName().compareTo(c.getFirstName());
    }

    @Override
    public String getHeadline() {
        return getName();
    }
    
    
}
