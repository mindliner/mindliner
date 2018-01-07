/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.exceptions;

import java.io.Serializable;

/**
 * Exception to describe that someone has updated the object while I was
 * currently editing it.
 * @author messerli
 */
public class mlModifiedException extends Exception implements Serializable {

    public mlModifiedException(){
        super();
    }
    
    public mlModifiedException(String s){
        super(s);
    }
    
    private static final long serialVersionUID = 19640205L;
}
