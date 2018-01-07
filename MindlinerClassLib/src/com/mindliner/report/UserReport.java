/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.report;

import com.mindliner.cal.ReportingPeriod.Period;

/**
 * This is the base class of all stat reports that provide information by user.
 *
 * @author Marius Messerli
 */
public class UserReport extends Report {

    public UserReport(Period period) {
        super(period);
    }
    private Integer userId;
    private String userName;
    private String firstName;
    private String lastName;
    private String email;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
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
}
