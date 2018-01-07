/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.analysis;

import com.mindliner.analysis.MlClassHandler.MindlinerObjectType;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

// we can search for id or text; the id search syntax is 'id integer_number'
/**
 * This class determins what needs to be searched.
 *
 * @author Marius Messerli
 */
public class SearchStringAnalyzer {

    public static enum SearchType {

        SearchById,
        SearchInClassOnly,
        PlainTextSearch
    }
    private String plainSearchString;
    private int id;
    protected MindlinerObjectType targetType;

    public String getPlainSearchString() {
        return plainSearchString;
    }

    public int getId() {
        return id;
    }

    private boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    /**
     *
     * @return The class, derived from mlsObject
     */
    public MindlinerObjectType getTargetType() {
        return targetType;
    }

    /**
     * Analyzes the specified search string and returns the search type.
     * Depending on the type additional calls to getters are necessary to obtain
     * the results of the analysis
     *
     * @return
     * @see getId
     * @see getPlainSearchString
     * @see getTargetClass
     *
     * @param searchString The search string. To search by id the first two
     * characters must be 'id' followed by a space. To search by class the first
     * character must be {c,t,k} followed by a whilte space
     * @throws NumberFormatException If the first characters are 'id' followed
     * by a white space followed by anything but an integer number this
     * exception is thrown.
     */
    public SearchType analyze(String searchString) {
        StringTokenizer idCheckerTokenizer = new StringTokenizer(searchString);
        plainSearchString = searchString;
        if ((idCheckerTokenizer.countTokens() == 1 && isNumeric(searchString)) || (idCheckerTokenizer.countTokens() == 2 && idCheckerTokenizer.nextToken().toLowerCase().equals("id"))) {
            try {
                id = Integer.parseInt(idCheckerTokenizer.nextToken());
                return SearchType.SearchById;
            } catch (NumberFormatException ex) {
                Logger.getAnonymousLogger().log(Level.WARNING, "The specified id was not a number: {0}", ex.getMessage());
                // do nothing so it continues below and assign type PlainTextSearch
            }
        }
        if (searchString.length() == 1 || (searchString.length() > 3 && searchString.substring(1, 2).equals(" "))) {
            targetType = MlClassHandler.getTypeByCharacterSymbol(searchString.substring(0, 1));
            plainSearchString = searchString.length() == 1 ? "" : searchString.substring(2);
            return SearchType.SearchInClassOnly;
        } else {
            targetType = MindlinerObjectType.Any;
            plainSearchString = searchString;
            return SearchType.PlainTextSearch;
        }
    }
}
