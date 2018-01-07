/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * This class analyses a string and determins any Mindliner tags
 *
 * @author Marius Messerli
 */
public class TagParser {

    private final List<String> mindlinerTagOccurances = new ArrayList<>();
    private final List<String> plainWords = new ArrayList<>();

    public TagParser(String input) {
        StringTokenizer st = new StringTokenizer(input);
        for (; st.hasMoreTokens();) {
            String word = st.nextToken();
            boolean tagFound = false;
            for (int i = 0; !tagFound && i < MindlinerStripTag.getFullTags().size(); i++) {
                String tag = MindlinerStripTag.getFullTags().get(i);
                if (word.length() > 1 && word.substring(0, 2).equals(tag)) {
                    mindlinerTagOccurances.add(word);
                    tagFound = true;
                }
            }
            if (!tagFound) {
                plainWords.add(word);
            }
        }
    }

    /**
     * Returns the content for all the
     *
     * @return
     */
    public List<String> getMindlinerTagOccurances() {
        return mindlinerTagOccurances;
    }

    public List<String> getPlainWords() {
        return plainWords;
    }

}
