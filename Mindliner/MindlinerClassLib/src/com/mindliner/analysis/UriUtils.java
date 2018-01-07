/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.analysis;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * This class analyzes text and returns URIs if any.
 *
 * @author Marius Messerli
 */
public class UriUtils {

    private static String getFirstWord(String text) {
        StringTokenizer st = new StringTokenizer(text);
        if (st.hasMoreTokens()) {
            return st.nextToken();
        } else {
            return null;
        }
    }

    /**
     * This function evaluates the input string and returns the URI if present and properly formed.
     *
     * @param input
     * @return If a properly formed URI was found it it returned. Otherwise the return value is null.
     */
    public static URI getAbsoluteUri(String input) {
        try {
            String word = getFirstWord(input);
            if (word == null) {
                return null;
            }
            URI uri = new URI(word);
            if (uri.isAbsolute()) {
//                reportDetails(uri);
                return uri;
            } else {
                return null;
            }
        } catch (URISyntaxException ex) {
            return null;
        }
    }

    public static void openUri(URI uri) {
        try {
            if (uri.getScheme().equals("file")) {
                File f = new File(uri);
                if (!f.exists()) {
                    f = new File(uri.getPath());
                }
                if (f.exists()) {
                    Desktop.getDesktop().open(new File(uri));
                } else {
                    JOptionPane.showMessageDialog(null, "Could not open specified file.", "Launcher", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                Desktop.getDesktop().browse(uri);
            }
        } catch (IOException ex) {
            Logger.getLogger(UriUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
