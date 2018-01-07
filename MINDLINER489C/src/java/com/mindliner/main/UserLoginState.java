/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.main;

import com.mindliner.clientobjects.mlcUser;

/**
 *
 * @author Marius Messerli
 */
public class UserLoginState {

    mlcUser user = null;
    boolean loggedIn = false;

    public UserLoginState(mlcUser u, boolean li) {
        user = u;
        loggedIn = li;
    }

    public void setLoggedIn(boolean l) {
        loggedIn = l;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public mlcUser getUser() {
        return user;
    }

    @Override
    public String toString() {
        return user.toString();
    }
}