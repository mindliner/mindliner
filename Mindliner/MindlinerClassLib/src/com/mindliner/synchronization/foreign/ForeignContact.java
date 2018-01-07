/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.synchronization.foreign;

import javax.swing.ImageIcon;

/**
 * This is an abstract representation of a Contact entitiy in a foreign data
 * source with which Mindliner is synchronizing its contacts.
 *
 * @author Marius Messerli
 */
public abstract class ForeignContact extends ForeignObject {

    public abstract void setFirstname(String firstname);

    public abstract String getFirstname();

    public abstract void setMiddlename(String middlename);

    public abstract String getMiddlename();

    public abstract void setLastname(String lastname);

    public abstract String getLastname();

    public abstract void setEmailAddress(String email);

    public abstract String getEmailAddress();

    public abstract String getWorkPhone();

    public abstract void setWorkPhone(String workPhone);

    public abstract String getMobile();

    public abstract void setMobile(String mobileNumber);

    public abstract ImageIcon getProfilePicture();
}
