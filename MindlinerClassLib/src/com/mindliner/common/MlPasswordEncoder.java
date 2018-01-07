/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import sun.misc.BASE64Encoder;

/**
 * Encodes the output of JPasswordField into a String.
 *
 * @author Marius Messerli
 */
public class MlPasswordEncoder {

    /**
     * Encodes the specified plain password using SHA-256 hashing and
     * BASE64 encoding.
     * 
     * @param plainPassword The password, typically the output of JPasswordField.getPassword()
     * @return A string with the encoded Password
     * @throws NoSuchAlgorithmException If the selected SHA-256 algorithm could not be loaded.
     */
    public static String encodePassword(char[] plainPassword) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        // @todo need to find a way to convert the char array of getPassword to the byte array digest needs
        md.update((new String(plainPassword)).getBytes());
        byte[] hash = md.digest();
        BASE64Encoder b64e = new BASE64Encoder();
        return b64e.encode(hash);
    }
}
