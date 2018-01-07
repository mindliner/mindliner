/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.clientobjects;

import java.io.Serializable;

/**
 *
 * @author marius
 */
public class mlcNickName implements Serializable {

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public mlcObject getObject() {
        return object;
    }

    public mlcUser getUser() {
        return user;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setObject(mlcObject object) {
        this.object = object;
    }

    public void setUser(mlcUser user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return name;
    }
    private int id;
    private String name = "";
    private mlcUser user = null;
    private mlcObject object = null;
    private static final long serialVersionUID = 19640205L;
}
