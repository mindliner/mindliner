/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.main;

import java.io.IOException;
import javax.security.auth.callback.*;
import javax.swing.JOptionPane;

/**
 *
 * @author Marius Messerli
 */
public class MindlinerLoginCallbackHandler implements CallbackHandler {

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for( int i = 0; i < callbacks.length; i++ ) {
            if( callbacks[i] instanceof TextOutputCallback ) {

                // display the message according to the specified type
                TextOutputCallback toc = (TextOutputCallback)callbacks[i];
                switch( toc.getMessageType() ) {
                        case TextOutputCallback.INFORMATION:
                            JOptionPane.showMessageDialog(null, toc.getMessage(), "Login", JOptionPane.INFORMATION_MESSAGE);
                            break;
                        case TextOutputCallback.ERROR:
                            JOptionPane.showMessageDialog(null, toc.getMessage(), "Login", JOptionPane.ERROR_MESSAGE);
                            break;
                        case TextOutputCallback.WARNING:
                            JOptionPane.showMessageDialog(null, toc.getMessage(), "Login", JOptionPane.WARNING_MESSAGE);
                            break;
                        default:
                            throw new IOException( "Unsupported message type: " + toc.getMessageType() );
                }
            } 
            
            else if( callbacks[i] instanceof NameCallback ) {
                NameCallback nc = (NameCallback)callbacks[i];
                nc.setName(LoginGUI.getLogin());
            } 
            
            else if( callbacks[i] instanceof PasswordCallback ) {
                PasswordCallback pc = (PasswordCallback)callbacks[i];
                pc.setPassword(LoginGUI.getPassword());
            }
            else {
                throw new UnsupportedCallbackException
                        ( callbacks[i], "Unrecognized Callback" );
            }
        }
    }

}
