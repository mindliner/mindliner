/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.exceptions;

/**
 * Exception is thrown if something goes wrong with Mindliners authorization
 * process.
 *
 * @author Marius Messerli
 */
public class MlAuthorizationException extends Exception {

    public MlAuthorizationException() {
    }

    public MlAuthorizationException(String message) {
        super(message);
    }

}
