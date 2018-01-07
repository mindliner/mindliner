/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.clipboard;

import com.mindliner.importer.MlTextTransfer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Used to parse the content of the clipboard. Using line breaks and line length, it creates
 * a list of text units which are either interpreted as headline or as description
 * @author Dominic Plangger
 */
public class ClipboardParser {
    
    public static final int MAX_HEADLINE_LENGTH = 200;
    
    public List<TextUnit> parseClipboard() {
        MlTextTransfer mtt = new MlTextTransfer();
        String content = mtt.getClipboardContents();
        StringReader reader = new StringReader(content);
        BufferedReader br = new BufferedReader(reader);
        List<TextUnit> result = new ArrayList<>();
        try {
            // use line breaks as delimiter for text units
            String line = br.readLine();
            while (line != null) {
                TextUnit u = createTextUnit(line);
                if (u != null) {
                    result.add(u);
                }
                line = br.readLine();
            }
        } catch (IOException ex) {
            Logger.getLogger(ClipboardParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    /**
     * Converts the clipboard string into a URL object. 
     * 
     * @return the URL object or null if clipboard content is nod a valid URL
     */
    public URL parseClipboardAsURL() {
        MlTextTransfer mtt = new MlTextTransfer();
        String content = mtt.getClipboardContents();
        try {
            URL url = new URL(content);
            return url;
        } catch (MalformedURLException ex) {
            Logger.getLogger(ClipboardParser.class.getName()).log(Level.FINER, "Clipboard content is not a valid URL", ex);
            return null;
        }
    }

    private TextUnit createTextUnit(String line) {
        if (line == null || line.isEmpty()) {
            return null;
        }
        // remove unicode code points from private use area (they can't be displayed)
        line = line.replaceAll("[\ue000-\uf8ff]", "");
        if (line.isEmpty()) {
            return null;
        }
        // remove special punctuations for listings (i.e. bullets)
        int c = line.codePointAt(0);
        if ((c >= 8208 && c <= 8215)
                || (c >= 8226 && c <= 8231)
                || (c >= 8248 && c <= 8303)) {
            line = line.substring(1);
        } 
        if (line.isEmpty()) {
            return null;
        }
        // remove ascii punctuations for listings (i.e. ->,=>,*,-,...)
        else {
            String s = line.substring(0, 1);
            s = s.replaceAll("[-=><*]", "");
            if (s.isEmpty()) {
                line = line.substring(1);
                if (!line.isEmpty() && line.charAt(0) == '>') {
                    line = line.substring(1);
                }
            }
        }
        // remove leading/trailing whitespaces
        line = line.trim();
        
        if (line.isEmpty()) {
            return null;
        }
        
        // remove trailing colon
        if (line.codePointAt(line.length()-1) == 58) {
            line = line.substring(0, line.length()-1);
        }
        
        if (line.length() <= MAX_HEADLINE_LENGTH) {
            return new TextUnit(line, true);
        }
        else {
            return new TextUnit(line, false);
        }
    }

}
