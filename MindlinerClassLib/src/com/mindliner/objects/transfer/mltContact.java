/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.objects.transfer;

import com.mindliner.entities.mlsContact;

/**
 * This class is the data transfer object for Contacts. It does not contain any
 * of the arrays that contain the related object and it contains only the
 * owner_id instead of the owner contact object.
 *
 * @author Marius Messerli
 */
public class mltContact extends MltObject {

    private String firstName = "";
    private String middleName = "";
    private String lastName = "";
    private String phoneNumber = "";
    private String mobileNumber = "";
    private String email = "";
    private int profilePictureId = -1;

    public mltContact(mlsContact c) {
        super(c);
        firstName = c.getFirstName();
        middleName = c.getMiddleName();
        lastName = c.getLastName();
        email = c.getEmail();
        mobileNumber = c.getMobileNumber();
        phoneNumber = c.getPhoneNumber();
        if (c.getProfilePicture() != null) {
            profilePictureId = c.getProfilePicture().getId();
        }
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public int getProfilePictureId() {
        return profilePictureId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

}
