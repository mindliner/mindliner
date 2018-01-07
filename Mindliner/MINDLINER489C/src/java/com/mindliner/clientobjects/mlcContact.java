/*
 * Contact.java
 *
 * Created on 10. Dezember 2005, 22:13
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package com.mindliner.clientobjects;

import com.mindliner.entities.ObjectAttributes;
import java.io.Serializable;
import java.util.List;

/**
 * The class describes a person (contact).
 *
 * @author Marius Messerli
 */
public class mlcContact extends mlcObject implements Comparable, Serializable {

    private String firstName = "";
    private String middleName = "";
    private String lastName = "";
    private String phoneNumber = "";
    private String mobileNumber = "";
    private String email;
    private MlcImage profilePicture;
    private static final long serialVersionUID = 19640205L;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String fname) {
        firstName = fname;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lname) {
        lastName = lname;
    }

    public void setMiddleName(String n) {
        middleName = n;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setEmail(String w) {
        email = w;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return getFirstName() + " " + getLastName();
    }

    /**
     * Contactenames the first character of the firstname, middlename, and
     * lastname. If either of these fields are zero lengh a hypan (-) is
     * inserted instead.
     */
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
    @Override
    public String getConcatenatedText() {
        return getName();
    }

    @Override
    public String toString() {
        return getFirstName() + " " + getLastName();
    }

    @Override
    public int compareTo(Object o) {
        mlcContact c = (mlcContact) o;
        return this.getFirstName().compareTo(c.getFirstName());
    }

    public MlcImage getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(MlcImage profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        mlcContact clone = (mlcContact) super.clone();
        clone.setFirstName(firstName);
        clone.setMiddleName(middleName);
        clone.setLastName(lastName);
        clone.setEmail(email);
        clone.setPhoneNumber(phoneNumber);
        clone.setMobileNumber(mobileNumber);
        clone.setProfilePicture(profilePicture);
        return clone;
    }

    @Override
    public List<ObjectAttributes> getChanges(mlcObject previousState) {
        List<ObjectAttributes> changes = super.getChanges(previousState);
        mlcContact previousContact = (mlcContact) previousState;
        if (!firstName.equals(previousContact.firstName)) {
            changes.add(ObjectAttributes.Firstname);
        }
        if (!middleName.equals(previousContact.middleName)) {
            changes.add(ObjectAttributes.Middlename);
        }
        if (!lastName.equals(previousContact.lastName)) {
            changes.add(ObjectAttributes.Lastname);
        }
        if (!email.equals(previousContact.email)) {
            changes.add(ObjectAttributes.Email);
        }
        if (!phoneNumber.equals(previousContact.phoneNumber)) {
            changes.add(ObjectAttributes.Workphone);
        }
        if (!mobileNumber.equals(previousContact.mobileNumber)) {
            changes.add(ObjectAttributes.Mobilephone);
        }
        return changes;
    }

}
