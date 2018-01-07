/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.common;

import com.mindliner.entities.mlsClient;
import com.mindliner.entities.mlsUser;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A helper class to compile clients or users into a comma-separated ids string
 *
 * @author Marius Messerli
 */
public class ClientIdsStringBuilder {

    public static String getCommaSeparatedString(Collection<Integer> ids) {
        StringBuilder sb = new StringBuilder();
        Iterator it = ids.iterator();
        while (it.hasNext()) {
            Integer i = (Integer) it.next();
            sb.append(i);
            if (it.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    /**
     * Builds a string consisting of the ids of the specified clients.
     *
     * @param clients The clients for which the IDs are to be compiled in a
     * string
     * @return A comma-separated list of ids
     */
    public static String buildClientIdsString(List<mlsClient> clients) {
        List<Integer> ids = new ArrayList<>();
        for (mlsClient c : clients) {
            ids.add(c.getId());
        }
        return getCommaSeparatedString(ids);
    }

    /**
     * Builds a string consisting of the ids of those users who share at least
     * one client with the specified user
     *
     * @param user The user for which the colleague's ids are needed
     * @return A comma-separated list of ids
     */
    public static String buildColleaguesIdsString(mlsUser user) {
        Set<Integer> ids = new HashSet<>();
        for (mlsClient c : user.getClients()){
            for (mlsUser u : c.getUsers()){
                ids.add(u.getId());
            }
        }
        ids.remove(user.getId());
        return getCommaSeparatedString(ids);
    }

}
