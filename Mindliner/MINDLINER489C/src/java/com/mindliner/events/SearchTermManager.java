/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.events;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author M.Messerli Created on 24.07.2012, 15:06:30
 */
public class SearchTermManager {

    public static String searchTerm = "";

    public static void setSearchTerm(String term) {
        searchTerm = term;
    }

    public static String getSearchTerm() {
        return searchTerm;
    }

    public static List<String> getSearchWords() {
        List<String> searchWords = new ArrayList<>();
        if (!searchTerm.isEmpty()) {
            StringTokenizer st = new StringTokenizer(searchTerm);
            for (; st.hasMoreTokens();) {
                searchWords.add(st.nextToken());
            }
        }
        return searchWords;
    }
}
