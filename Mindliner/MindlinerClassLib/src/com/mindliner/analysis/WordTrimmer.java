/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.analysis;

/**
 *
 * @author Marius Messerli
 */
public class WordTrimmer {

    public static String stripIrrelavantCharacters(String s) {
        char[] charArray = s.toCharArray();
        int len = charArray.length;
        int start = -1;
        for (int i = 0; start < 0 && i < len; i++) {
            char c = charArray[i];
            if (('0' <= c && c <= '9') || ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z')) {
                start = i;
            }
        }
        if (start == -1) {
            return "";
        }
        int end = -1;
        for (int i = start; end < 0 && i < len; i++) {
            char c = charArray[i];
            if ('0' <= c && c <= '9') {
                continue;
            }
            if ('a' <= c && c <= 'z') {
                continue;
            }
            if ('A' <= c && c <= 'Z') {
                continue;
            }
            if (c == '-') {
                continue;
            }
            end = i;
        }
        if (end == -1) {
            return s.substring(start);
        } else {
            return s.substring(start, end);
        }
    }
}
