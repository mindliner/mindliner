/*
 * TextEvaluator.java
 * 
 * Created on 19.06.2007, 23:36:26
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.mindliner.contentfilter.evaluator;

import com.mindliner.contentfilter.ObjectEvaluator;
import com.mindliner.entities.mlsObject;
import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * This class selects objects based on specified text elements being present in
 * the headline or the description.
 *
 * @author Marius Messerli
 */
public class TextEvaluator implements ObjectEvaluator, Serializable {

    private String textConstraint = "";
    private boolean caseSensitive = false;
    private int relationLevel = 0;
    private static final long serialVersionUID = 19640205L;

    public TextEvaluator(String textConstraint, int relationLevel) {
        this.textConstraint = textConstraint;
        StringTokenizer st = new StringTokenizer(textConstraint);
        this.relationLevel = st.countTokens() < 2 ? 0 : relationLevel;
    }

    /**
     * Performs a breadth-first search for the specified search term in relatives
     *
     * @param current The object who's relatives are to be checked
     * @param searchLevels The number of relative levels to check. 1 means to
     * check just the relatives of the object itself, 2 means to check their
     * relatives, too ,etc.
     * @return True if one of the relatives complies, false otherwise.
     */
    private boolean isWordInRelatives(mlsObject current, String text, int searchLevels) {
        if (searchLevels < 1) return false;
        // first checkout all objects on this level
        for (mlsObject cld : current.getRelatives()) {
            // does the "child" match?
            if (hasWord(cld, text)) {
                System.out.println("found word " + text + " in object " + cld);
                return true;
            } 
        }
        // now check next level for each of the siblings
        for (mlsObject cld : current.getRelatives()) {
            if (isWordInRelatives(cld, text, searchLevels-1)) return true;
        }            
        return false;
    }

    /**
     * Determins if the specified element text passes the constraints
     *
     * @param o The object to be analyzed for compliance
     * @return True if the object complies with the filter's constraints, false
     * otherwise
     */
    @Override
    public boolean passesEvaluation(mlsObject o) {

        if (o == null) {
            throw new IllegalArgumentException("Object cannot be null");
        }
        if (textConstraint.isEmpty()) {
            return true;
        }
        StringTokenizer searchWords = new StringTokenizer(textConstraint);
        while (searchWords.hasMoreElements()) {
            String word = searchWords.nextToken();
            if (hasWord(o, word)) {
                continue;
            }
            if (!isWordInRelatives(o, word, relationLevel)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasWord(mlsObject o, String word) {
        String headline;
        String description;
        if (caseSensitive) {
            headline = o.getHeadline();
            description = o.getDescription();
        } else {
            headline = o.getHeadline().toLowerCase();
            description = o.getDescription().toLowerCase();
            word = word.toLowerCase();
        }
        return (headline.contains(word) || description.contains(word));
    }

    public void setTextConstraint(String s) {
        textConstraint = s;
    }

    public void setCaseSensitivity(boolean s) {
        caseSensitive = s;
    }

    @Override
    public boolean isMultipleInstancesAllowed() {
        return true;
    }

}
