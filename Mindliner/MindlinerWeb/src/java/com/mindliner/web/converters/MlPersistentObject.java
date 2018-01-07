/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.web.converters;

import java.io.Serializable;
import java.util.StringTokenizer;
import javax.faces.convert.Converter;

/**
 * This is the base class for all converters that convert an entity with an ID.
 *
 * @author Marius Messerli
 */
public abstract class MlPersistentObject implements Converter, Serializable {

    /**
     * This function will assume the ID is at the end of the input string
     *
     * @param value The formatted object with the ID at the end preceeded by a
     * white space
     * @return
     */
    protected int getId(String value) {
        StringTokenizer st = new StringTokenizer(value);
        String sid = st.nextToken();
        while (st.hasMoreTokens()) {
            sid = st.nextToken();
        }
        return Integer.parseInt(sid);
    }
    
    protected String appendID(String value, int id){
        return value + " " + id;
    }
}
